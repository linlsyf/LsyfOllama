package com.lsyf.lsyfollama.evenbus;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
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



  public static void install() {

    EditorFactory.getInstance().getEventMulticaster().addCaretListener(new CaretListener() {

      @Override
      public void caretPositionChanged(@NotNull CaretEvent event) {
        // 光标移动时触发（打字、方向键、鼠标点击都会触发）
        Editor editor = event.getEditor();

        if (event.getEditor() == null) return;
        // ✅ 需要 ReadAction 来访问 FileDocumentManager
        VirtualFile virtualFile = ReadAction.compute(() ->
            FileDocumentManager.getInstance().getFile(editor.getDocument())
        );
        if (virtualFile == null) return;

        // 当前光标位置
        int offset = event.getEditor().getCaretModel().getOffset();
        int line = event.getEditor().getCaretModel().getLogicalPosition().line;
        int column = event.getEditor().getCaretModel().getLogicalPosition().column;

        System.out.println("Cursor moved -> " + virtualFile.getName() + ":" + line + ":" + column);

        var selectionModel = event.getEditor().getSelectionModel();

        String selectedText = selectionModel.getSelectedText();

        if (selectionModel.hasSelection()) {
          int start = selectionModel.getSelectionStart();
          int end = selectionModel.getSelectionEnd();
          int startLine = editor.getDocument().getLineNumber(start);
          int endLine = editor.getDocument().getLineNumber(end);
          System.out.println("选中: 第" + (startLine+1) + "行 到 第" + (endLine+1) + "行");
        } else {
          // 无选区 → 单纯光标移动
           line = editor.getCaretModel().getLogicalPosition().line;
          System.out.println("光标移到第" + (line+1) + "行");
        }
        if (virtualFile != null) {
          System.out.println("选中文件: " + virtualFile.getName());
          System.out.println("选中文件: " + virtualFile.getPath());
        }

        BusMessage busMessage = new BusMessage();
        busMessage.setOffset(offset);
        busMessage.setLine(line);
        busMessage.setVirtualFile(virtualFile);
        busMessage.setEditor(event.getEditor());

        Document currentDocument = FileDocumentManager.getInstance().getDocument(virtualFile);
        busMessage.setCurrentDocument(currentDocument);

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