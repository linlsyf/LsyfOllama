package com.lsyf.lsyfollama.evenbus;

import com.intellij.openapi.vfs.VirtualFile;
import lombok.Data;

@Data
public class BusMessage {
  VirtualFile virtualFile;
  int offset;
  int line;
}
