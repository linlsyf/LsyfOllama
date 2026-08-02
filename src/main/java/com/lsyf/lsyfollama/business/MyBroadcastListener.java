package com.lsyf.lsyfollama.business;

import com.intellij.openapi.project.Project;

public interface MyBroadcastListener {
  // 广播方法，参数就是携带的数据
  void onBroadcast(String message, Project project);
}
