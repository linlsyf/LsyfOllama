package com.lsyf.lsyfollama.business;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;

public class MyBroadcaster {
  public static void broadcast(Project project, String message) {
    // 从项目中获取 MessageBus
    MessageBus bus = project.getMessageBus();

    // 获取 Publisher 并发送
    MyBroadcastListener publisher = bus.syncPublisher(
        MyTopics.MY_BROADCAST_TOPIC
    );
    publisher.onBroadcast(message, project);

    // 如果需要异步广播，用 asyncPublisher
    // MyBroadcastListener asyncPublisher = bus.asyncPublisher(
    //     MyTopics.MY_BROADCAST_TOPIC
    // );
  }
}