package com.lsyf.lsyfollama.broadCast;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.lsyf.lsyfollama.business.MyBroadcastListener;
import com.lsyf.lsyfollama.business.MyTopics;
import com.lsyf.lsyfollama.utils.DiffPreviewUtil;

public class MyBroadcastService implements MyBroadcastListener {
  private final MessageBusConnection connection;
  private final Project project;

  public MyBroadcastService(Project project) {
    this.project = project;
    this.connection = project.getMessageBus().connect();
    this.connection.subscribe(MyTopics.MY_BROADCAST_TOPIC, this);
  }

  @Override
  public void onBroadcast(String message, Project project) {
    // 处理逻辑
    ApplicationManager.getApplication().invokeLater(() -> {
      System.out.println("View 广播收到: " + message);
      DiffPreviewUtil.show(message);
    });



  }

  // 可选：提供一个静态获取实例的方法
  public static MyBroadcastService getInstance(Project project) {
    return project.getService(MyBroadcastService.class);
  }
}