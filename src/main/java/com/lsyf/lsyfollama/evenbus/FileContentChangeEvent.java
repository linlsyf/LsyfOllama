package com.lsyf.lsyfollama.evenbus;

import lombok.Data;

// 自定义事件数据
@Data
public class FileContentChangeEvent {
  private final String filePath;
  private final String content;

  public FileContentChangeEvent(String filePath, String content) {
    this.filePath = filePath;
    this.content = content;
  }
  // getters...
}

