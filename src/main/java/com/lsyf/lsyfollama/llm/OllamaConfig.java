package com.lsyf.lsyfollama.llm;

import lombok.Data;

@Data
public class OllamaConfig {
  private  String baseUrl;
  private  String modelSetting;
  private  String apiKey;
  private  String caCertPath;

  public OllamaConfig() {
  }


}