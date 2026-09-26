package com.lsyf.lsyfollama.constant;

import com.intellij.openapi.project.Project;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.llm.OllamaConfig;
import lombok.Data;

@Data
public class ProjectInitData {
  Project project;
  static ProjectInitData instance;
  static OllamaConfig cfgInstance;

   String documentContent;
  public  static ProjectInitData  getInstance(){
    if (instance==null){
      instance=new ProjectInitData();
    }
    return instance;
  }

  public static OllamaConfig getCfgInstance() {

    if (cfgInstance==null){
      cfgInstance=new OllamaConfig();
      cfgInstance.setBaseUrl(ChatConstant.apiUrl);
      cfgInstance.setModelSetting(ChatConstant.modelSetting);

    }
    return cfgInstance;
  }
}
