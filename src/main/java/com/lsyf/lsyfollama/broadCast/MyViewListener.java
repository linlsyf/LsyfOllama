package com.lsyf.lsyfollama.broadCast;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.lsyf.lsyfollama.business.MyViewNotifier;
import com.lsyf.lsyfollama.utils.DiffPreviewUtil;

public class MyViewListener implements MyViewNotifier {

  public MyViewListener() {
    System.out.println("✅ MyViewListener instantiated");
  }

  @Override
  public void onViewMessage(String msg, Project project) {
    System.out.println("✅ onViewMessage triggered: " + msg);
    ApplicationManager.getApplication().invokeLater(() -> {
      System.out.println("View 广播收到: " + msg);
      DiffPreviewUtil.show(msg);
    });
  }
}