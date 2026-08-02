package com.lsyf.lsyfollama.constant;

import com.intellij.openapi.project.Project;
import lombok.Data;

@Data
public class ProjectInitData {
  Project project;
  static ProjectInitData instance;

  public  static ProjectInitData  getInstance(){
    if (instance==null){
      instance=new ProjectInitData();
    }
    return instance;
  }

}
