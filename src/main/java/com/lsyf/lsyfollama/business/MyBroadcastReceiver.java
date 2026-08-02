package com.lsyf.lsyfollama.business;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.messages.MessageBusConnection;

public class MyBroadcastReceiver implements MyBroadcastListener {

  private MessageBusConnection connection;

  public void connect(Project project) {
    connection = project.getMessageBus().connect();

    // 注册监听
    connection.subscribe(
        MyTopics.MY_BROADCAST_TOPIC,
        this
    );
  }

  @Override
  public void onBroadcast(String message, Project project) {
    // 处理接收到的广播
    System.out.println("收到广播: " + message);
    // TODO: 更新 UI、触发动作等
    Messages.showInfoMessage(message, "Debug");



  }

  // 重要：组件销毁时必须断开连接，否则内存泄漏
  public void disconnect() {
    if (connection != null) {
      connection.disconnect();
    }
  }



}