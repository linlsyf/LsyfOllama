package com.lsyf.lsyfollama.evenbus;

import com.intellij.util.messages.Topic;

public interface MyGlobalNotifier {
  // 应用级 topic：用 @Topic.AppLevel 标注，displayName 随便起
  @Topic.AppLevel
  Topic<MyGlobalNotifier> TOPIC =
      Topic.create("my.plugin.global.events", MyGlobalNotifier.class);

  void onDatasChanged(BusMessage busMessage);

}