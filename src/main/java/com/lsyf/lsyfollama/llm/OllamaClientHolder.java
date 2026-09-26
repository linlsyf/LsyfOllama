package com.lsyf.lsyfollama.llm;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class OllamaClientHolder {

  private static OkHttpClient client;

  public static synchronized OkHttpClient getClient(OllamaConfig config) {
    if (client == null) {
      OkHttpClient.Builder builder = new OkHttpClient.Builder()
          .connectTimeout(60, TimeUnit.SECONDS)
          .readTimeout(120, TimeUnit.SECONDS)
          .writeTimeout(60, TimeUnit.SECONDS);

      // API Key（如果 Ollama 需要）
      if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
        builder.addInterceptor(chain -> {
          Request request = chain.request().newBuilder()
              .addHeader("Authorization", "Bearer " + config.getApiKey())
              .build();
          return chain.proceed(request);
        });
      }

      // 自定义 CA（HTTPS）
      if (config.getCaCertPath() != null && !config.getCaCertPath().isEmpty()) {
        try {
          CertificateFactory cf = CertificateFactory.getInstance("X.509");
          X509Certificate caCert = (X509Certificate) cf.generateCertificate(
              new FileInputStream(config.getCaCertPath())
          );

          KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
          keyStore.load(null);
          keyStore.setCertificateEntry("ca", caCert);

          TrustManagerFactory tmf = TrustManagerFactory.getInstance(
              TrustManagerFactory.getDefaultAlgorithm()
          );
          tmf.init(keyStore);

          SSLContext sslContext = SSLContext.getInstance("TLS");
          sslContext.init(null, tmf.getTrustManagers(), null);

          builder.sslSocketFactory(
              sslContext.getSocketFactory(),
              (X509TrustManager) tmf.getTrustManagers()[0]
          );
          builder.hostnameVerifier((hostname, session) -> true);

        } catch (Exception e) {
          throw new RuntimeException("CA 证书加载失败", e);
        }
      }

      client = builder.build();
    }
    return client;
  }
}