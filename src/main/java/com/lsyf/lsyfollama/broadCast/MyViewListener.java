package com.lsyf.lsyfollama.broadCast;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.lsyf.lsyfollama.business.MyViewNotifier;
import com.lsyf.lsyfollama.utils.DiffPreviewUtil;

public class MyViewListener implements MyViewNotifier {
  private final Project myProject;
  private final MessageBusConnection myConnection;

  public MyViewListener(Project project) {
    this.myProject = project;
    this.myConnection = project.getMessageBus().connect();
    this.myConnection.subscribe(
        MyViewNotifier.MY_VIEW_TOPIC,
        this
    );
  }

  @Override
  public void onViewMessage(String msg, Project project) {
    // ⚠️ 如果是 UI 更新，必须在 EDT 执行
    ApplicationManager.getApplication().invokeLater(() -> {
      System.out.println("View 广播收到: " + msg);
      DiffPreviewUtil.show(msg);
    });

  }

  // Project 关闭时记得释放
  public void dispose() {
    myConnection.disconnect();
  }



}
