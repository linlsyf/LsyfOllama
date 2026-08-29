package com.lsyf.lsyfollama.evenbus;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Data;

@Data
public class BusMessage {
  VirtualFile virtualFile;
  int offset;
  int line;
  Editor editor;
  Document currentDocument;

  String key;
  Object value;
  String messageType;
}
