package com.lsyf.lsyfollama.evenbus;

import com.intellij.util.messages.Topic;

// 定义订阅者接口
public interface FileContentChangeListener {
  Topic<FileContentChangeListener> TOPIC = Topic.create(
      "fileContentChanged", FileContentChangeListener.class
  );

  void onContentChanged(FileContentChangeEvent event);
}