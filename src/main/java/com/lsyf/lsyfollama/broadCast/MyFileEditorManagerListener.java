package com.lsyf.lsyfollama.broadCast;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.lsyf.lsyfollama.evenbus.FileContentChangeEvent;
import com.lsyf.lsyfollama.evenbus.FileContentChangeListener;
import org.jetbrains.annotations.NotNull;

public class MyFileEditorManagerListener implements FileEditorManagerListener {

  private DocumentListener currentDocumentListener;
  private Document currentDocument;

  @Override
  public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    // 移除旧文件的监听，防止内存泄漏
    unregisterPreviousListener();

    FileEditor newEditor = event.getNewEditor();
    if (newEditor instanceof TextEditor) {
      currentDocument = ((TextEditor) newEditor).getEditor().getDocument();

      // 获取选中文件的 VirtualFile（可用于判断文件类型等）

      // 注册内容变化监听
      currentDocumentListener = new DocumentListener() {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
          String newContent = currentDocument.getText();
          System.out.println("文件内容变为: " + newContent);

          VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(currentDocument);
          if (virtualFile != null) {
            System.out.println("选中文件: " + virtualFile.getName());
            System.out.println("选中文件: " + virtualFile.getPath());
          }
          // 发布消息到 MessageBus
          ApplicationManager.getApplication()
              .getMessageBus()
              .syncPublisher(FileContentChangeListener.TOPIC)
              .onContentChanged(new FileContentChangeEvent(
                  virtualFile != null ? virtualFile.getName() : "unknown",
                  newContent
              ));

        }
      };
      currentDocument.addDocumentListener(currentDocumentListener);
    }
  }

  private void unregisterPreviousListener() {
    if (currentDocument != null && currentDocumentListener != null) {
      currentDocument.removeDocumentListener(currentDocumentListener);
      currentDocument = null;
      currentDocumentListener = null;
    }
  }
}