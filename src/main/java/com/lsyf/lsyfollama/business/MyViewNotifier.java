package com.lsyf.lsyfollama.business;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

public interface MyViewNotifier {
  // 用 @Topic.ProjectLevel 标注，表明这是 Project 级 Topic
  @Topic.ProjectLevel
  Topic<MyViewNotifier> MY_VIEW_TOPIC =
      Topic.create("my.view.topic", MyViewNotifier.class);

  // 广播方法，参数就是要传递的数据
  void onViewMessage(String msg, Project project);
}