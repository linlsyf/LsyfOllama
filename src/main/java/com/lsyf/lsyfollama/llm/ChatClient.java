package com.lsyf.lsyfollama.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.constant.ProjectInitData;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ChatClient {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static OkHttpClient HTTP_CLIENT;

  @FunctionalInterface
  public interface StreamCallback {
    void onToken(String token);

    default void onDone() {
    }

    default void onError(String msg) {
    }
  }

  /* ==================== 非流式 ==================== */

  public static String processText(String selectedText) {
    OllamaConfig cfg = ProjectInitData.getCfgInstance();
    try {
      String body = MAPPER.writeValueAsString(Map.of(
          "model", ChatConstant.modelSetting,
          "stream", false,
          "messages", List.of(
              Map.of("role", "system", "content",
                  "你是一个" + ChatConstant.DEV_LAN + "专家，仅输出代码，不要任何解释、注释或额外文本。\n输出格式要求：纯代码，无换行符(\\n)或描述"),
              Map.of("role", "user", "content", selectedText)
          )
      ));

      Request request = baseRequest(cfg, body).build();
      try (Response resp = client(cfg).newCall(request).execute()) {
        if (!resp.isSuccessful()) {
          return "request ai error: HTTP " + resp.code();
        }
        return extractContent(resp.body().string());
      }
    } catch (Exception e) {
      return "request ai error: " + e.getMessage();
    }
  }

  /* ==================== 流式 ==================== */

  public static void chatStreaming(String selectedText, StreamCallback cb) {
    OllamaConfig cfg = ProjectInitData.getCfgInstance();

    try {
      String body = MAPPER.writeValueAsString(Map.of(
          "model", cfg.getModelSetting(),
          "stream", true,
          "messages", List.of(
              Map.of("role", "system", "content",
                  "你是一个开发专家，直接回答问题,不要任何解释"),
//                  "你是一个" + ChatConstant.DEV_LAN + "专家，仅输出代码，不要任何解释"),
              Map.of("role", "user", "content", selectedText)
          )
      ));

      Request request = baseRequest(cfg, body)
          .addHeader("Accept", "application/json, text/event-stream")
          .build();

      try (Response resp = client(cfg).newCall(request).execute()) {
        if (!resp.isSuccessful()) {
          cb.onError("HTTP " + resp.code());
          return;
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(resp.body().byteStream(), StandardCharsets.UTF_8)
        );

        String line;
        while ((line = reader.readLine()) != null) {
          if (line.isBlank()) {
            continue;
          }
          // 提取增量 token
          String token = ResponseParser.extractOllamaStreamToken(line);
          if (!token.isEmpty()) {
//            fullResponse.append(token);
            cb.onToken(token.replace("", ""));   // 写入 IDEA Editor
          }

          // 检查结束
          if (ResponseParser.isOllamaDone(line)) {
            break;
          }
        }

        cb.onDone();
      }
    } catch (Exception e) {
      cb.onError(e.getMessage());
    }
  }

  /* ==================== HTTP 客户端 ==================== */

  private static OkHttpClient client(OllamaConfig cfg) throws Exception {
    if (HTTP_CLIENT != null) {
      return HTTP_CLIENT;
    }

    OkHttpClient.Builder b = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS);

    if (cfg.getCaCertPath() != null && !cfg.getCaCertPath().isEmpty()) {
      b.sslSocketFactory(sslSocketFactory(cfg.getCaCertPath()), trustAll());
      b.hostnameVerifier((h, s) -> true);
    }

    if (cfg.getApiKey() != null && !cfg.getApiKey().isEmpty()) {
      b.addInterceptor(chain ->
          chain.proceed(
              chain.request().newBuilder()
                  .addHeader("Authorization", "Bearer " + cfg.getApiKey())
                  .build()
          )
      );
    }

    HTTP_CLIENT = b.build();
    return HTTP_CLIENT;
  }

  private static Request.Builder baseRequest(OllamaConfig cfg, String body) {
    String url = cfg.getBaseUrl().replaceAll("/$", "") + "/api/chat";
    ;
// 结果必须是：http://127.0.0.1:11434/api/chat
    return new Request.Builder()
        .url(url)
        .post(RequestBody.create(body, MediaType.parse("application/json")));
  }

  /* ==================== SSL ==================== */

  private static SSLSocketFactory sslSocketFactory(String caPath) throws Exception {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate ca = (X509Certificate) cf.generateCertificate(new FileInputStream(caPath));

    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null);
    ks.setCertificateEntry("ca", ca);

    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);

    SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(null, tmf.getTrustManagers(), null);
    return ctx.getSocketFactory();
  }

  private static X509TrustManager trustAll() {
    return new X509TrustManager() {
      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }

      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType) {
        // trust all
      }

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType) {
        // trust all
      }
    };
  }

  /* ==================== JSON ==================== */

  private static String extractContent(String json) {
    try {
      JsonNode n = MAPPER.readTree(json);
      return n.path("choices").path(0).path("message").path("content").asText("");
    } catch (Exception e) {
      return json;
    }
  }

  private static String extractStreamToken(String json) {
    try {
      JsonNode n = MAPPER.readTree(json);
      return n.path("choices").path(0).path("delta").path("content").asText("");
    } catch (Exception e) {
      return "";
    }
  }
}