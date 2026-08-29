package com.lsyf.lsyfollama.evenbus;

import com.intellij.util.messages.Topic;

public interface LsyfGlobalNotifier {
  // 应用级 topic：用 @Topic.AppLevel 标注，displayName 随便起
  @Topic.AppLevel
  Topic<LsyfGlobalNotifier> TOPIC =
      Topic.create("linlsyf.plugin.global.events", LsyfGlobalNotifier.class);

  void onDatasChanged(BusMessage busMessage);

}