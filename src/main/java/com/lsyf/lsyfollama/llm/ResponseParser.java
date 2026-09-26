package com.lsyf.lsyfollama.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
public class ResponseParser {

  private static final ObjectMapper mapper = new ObjectMapper();

  // ========== OpenAI 兼容格式（/v1/chat/completions） ==========

  public static String extractContent(String json) {
    try {
      JsonNode root = mapper.readTree(json);
      return root.path("choices")
          .path(0)
          .path("message")
          .path("content")
          .asText("");
    } catch (Exception e) {
      return json;
    }
  }

  public static String extractStreamToken(String json) {
    try {
      JsonNode root = mapper.readTree(json);
      return root.path("choices")
          .path(0)
          .path("delta")
          .path("content")
          .asText("");
    } catch (Exception e) {
      return "";
    }
  }

  // ========== Ollama 原生格式（/api/chat） ==========

  /**
   * 非流式：提取完整回复
   * Ollama 返回: {"message":{"content":"..."}, "done":true}
   */
  public static String extractOllamaContent(String json) {
    try {
      JsonNode root = mapper.readTree(json);
      return root.path("message")
          .path("content")
          .asText("");
    } catch (Exception e) {
      return json;
    }
  }

  /**
   * 流式 chunk：提取增量 token
   * Ollama 返回: {"message":{"content":"public"}, "done":false}
   */
  public static String extractOllamaStreamToken(String json) {
    try {
      JsonNode root = mapper.readTree(json);
      return root.path("message")
          .path("content")
          .asText("");
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * 判断是否流结束
   */
  public static boolean isOllamaDone(String json) {
    try {
      JsonNode root = mapper.readTree(json);
      return root.path("done").asBoolean(false);
    } catch (Exception e) {
      return false;
    }
  }
}