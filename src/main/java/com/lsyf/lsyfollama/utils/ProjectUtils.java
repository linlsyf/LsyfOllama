package com.lsyf.lsyfollama.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ProjectUtils {


  public   static   VirtualFile getProjectDir(Project project) {
    if (project == null || project.isDisposed()) {
      return null;
    }
    VirtualFile projectDir = project.getWorkspaceFile();
    if (projectDir != null) {
      projectDir = projectDir.getParent(); // workspace.xml 的父目录
    }
    return projectDir;
  }
}
