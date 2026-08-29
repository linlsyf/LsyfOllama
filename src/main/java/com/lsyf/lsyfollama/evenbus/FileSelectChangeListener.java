package com.lsyf.lsyfollama.evenbus;

import com.intellij.util.messages.Topic;

// 定义订阅者接口
public interface FileSelectChangeListener {
  Topic<FileSelectChangeListener> TOPIC = Topic.create(
      "fileContentChanged", FileSelectChangeListener.class
  );

  void onMessage(BusMessage busMessage);
}