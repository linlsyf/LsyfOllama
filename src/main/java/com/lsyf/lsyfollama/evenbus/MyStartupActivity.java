package com.lsyf.lsyfollama.evenbus;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyStartupActivity implements ProjectActivity {



  private DocumentListener currentDocumentListener;
  private Document currentDocument;


  @Nullable
  @Override
  public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
    install();
    // 在项目启动后注册监听器
    project.getMessageBus().connect()
        .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
            new FileEditorManagerListener() {

              @Override
              public void fileOpened(@NotNull com.intellij.openapi.fileEditor.FileEditorManager source,
                                     @NotNull com.intellij.openapi.vfs.VirtualFile file) {
                System.out.println("fileOpened: " + file.getName());
//                Messages.showInfoMessage("fileOpened", "Debug");

//                registerDocumentListener(source.getProject(), file);
              }

              @Override
              public void fileClosed(@NotNull com.intellij.openapi.fileEditor.FileEditorManager source,
                                     @NotNull com.intellij.openapi.vfs.VirtualFile file) {
                System.out.println("=== [Bus] 文件关闭: " + file.getName() + " ===");
              }
              @Override
              public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                System.out.println("selectionChanged");
//                Messages.showInfoMessage("selectionChanged", "Debug");

//                if (event.getNewFile() != null) {
//
//                  registerDocumentListener(event.getManager().getProject(), event.getNewFile() );
//                }
              }

            });

    System.out.println("=== MessageBus 监听器已注册到项目: " + project.getName() + " ===");
    return Unit.INSTANCE;
  }
//
//  private void registerDocumentListener(Project project, VirtualFile file) {
//    // 移除旧文件的监听，防止内存泄漏
//    unregisterPreviousListener();
//
//    // 在这里给 Document 注册监听，才能感知内容变化
//    currentDocument= FileDocumentManager.getInstance().getDocument(file);
//
//    // 注册内容变化监听
//    currentDocumentListener = new DocumentListener() {
//      @Override
//      public void documentChanged(@NotNull DocumentEvent event) {
//        String newContent = currentDocument.getText();
//        System.out.println("文件内容变为: " + newContent);
//
//        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(currentDocument);
//        if (virtualFile != null) {
//          System.out.println("选中文件: " + virtualFile.getName());
//          System.out.println("选中文件: " + virtualFile.getPath());
//        }
//    Messages.showInfoMessage("selection ", newContent);
//
//    // 发布消息到 MessageBus
//        ApplicationManager.getApplication()
//            .getMessageBus()
//            .syncPublisher(FileSelectChangeListener.TOPIC)
//            .onMessage(virtualFile);
//
//      }
//    };
//    currentDocument.addDocumentListener(currentDocumentListener);
//  }




  private void unregisterPreviousListener() {
    if (currentDocument != null && currentDocumentListener != null) {
      currentDocument.removeDocumentListener(currentDocumentListener);
      currentDocument = null;
      currentDocumentListener = null;
    }
  }
  public static void install() {
//    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentListener() {
//      @Override
//      public void documentChanged(@NotNull DocumentEvent event) {
//        VirtualFile file = FileDocumentManager.getInstance().getFile(event.getDocument());
//        if (file == null) return;
//
//        // 只关心 java 和 kt 文件
//        if (!"java".equals(file.getExtension()) && !"kt".equals(file.getExtension())) return;
//        Document currentDocument= FileDocumentManager.getInstance().getDocument(file);
//
//        // 这里写你的逻辑
//        System.out.println("File changed: " + file.getPath());
//        String newContent = currentDocument.getText();
//        System.out.println("文件内容变为: " + newContent);
//
//        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(currentDocument);
//        if (virtualFile != null) {
//          System.out.println("选中文件: " + virtualFile.getName());
//          System.out.println("选中文件: " + virtualFile.getPath());
//        }
//        Messages.showInfoMessage("selection ", newContent);
//
//        // 发布消息到 MessageBus
//        ApplicationManager.getApplication()
//            .getMessageBus()
//            .syncPublisher(FileContentChangeListener.TOPIC)
//            .onContentChanged(new FileContentChangeEvent(
//                virtualFile != null ? virtualFile.getName() : "unknown",
//                newContent
//            ));
//
//      }
//    });


        EditorFactory.getInstance().getEventMulticaster().addCaretListener(new CaretListener() {

          @Override
          public void caretPositionChanged(@NotNull CaretEvent event) {
            // 光标移动时触发（打字、方向键、鼠标点击都会触发）
            if (event.getEditor() == null) return;

            VirtualFile virtualFile = FileDocumentManager.getInstance()
                .getFile(event.getEditor().getDocument());
            if (virtualFile == null) return;

            // 当前光标位置
            int offset = event.getEditor().getCaretModel().getOffset();
            int line = event.getEditor().getCaretModel().getLogicalPosition().line;
            int column = event.getEditor().getCaretModel().getLogicalPosition().column;

            System.out.println("Cursor moved -> " + virtualFile.getName() + ":" + line + ":" + column);

            Document currentDocument= FileDocumentManager.getInstance().getDocument(virtualFile);

            // 这里写你的逻辑
//            System.out.println("File changed: " + file.getPath());
            String newContent = currentDocument.getText();
//            System.out.println("文件内容变为: " + newContent);

//            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(currentDocument);
            if (virtualFile != null) {
              System.out.println("选中文件: " + virtualFile.getName());
              System.out.println("选中文件: " + virtualFile.getPath());
            }
//            Messages.showInfoMessage("selection ", line+"");

            BusMessage busMessage=new BusMessage();
            busMessage.setOffset(offset);
            busMessage.setLine(line);
            busMessage.setVirtualFile(virtualFile);
            // 发布消息到 MessageBus
            ApplicationManager.getApplication()
                .getMessageBus()
                .syncPublisher(FileSelectChangeListener.TOPIC)
                .onMessage(busMessage);



          }

          @Override
          public void caretAdded(@NotNull CaretEvent event) {
            // 多光标：新增了一个光标
          }

          @Override
          public void caretRemoved(@NotNull CaretEvent event) {
            // 多光标：移除了一个光标
          }
        });


  }


}