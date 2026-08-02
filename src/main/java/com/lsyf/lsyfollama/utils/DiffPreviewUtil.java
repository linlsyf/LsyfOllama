package com.lsyf.lsyfollama.utils;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.lsyf.lsyfollama.constant.ProjectInitData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class DiffPreviewUtil {

  private DiffPreviewUtil() {
  }

  public interface OnApplyCallback {
    void onApply(@NotNull String finalText);
  }

  public static void showDiffPreview(
      @NotNull Project project,
      @NotNull String title,
      @NotNull String leftTitle,
      @NotNull String rightTitle,
      @NotNull String original,
      @NotNull String generated,
      @NotNull OnApplyCallback callback
  ) {
    // ✅ 只需要管理 DiffRequestPanel 的生命周期
    Disposable disposable = Disposer.newDisposable("DiffPreview");

    try {
      DiffRequestPanel diffPanel = DiffManager.getInstance()
          .createRequestPanel(project, disposable, null);

      // ✅ 创建 Document（不需要释放）
      Document generatedDoc = EditorFactory.getInstance().createDocument(generated);

      DiffContentFactory contentFactory = DiffContentFactory.getInstance();
      SimpleDiffRequest request = new SimpleDiffRequest(
          title,
          contentFactory.create(project, original),
          contentFactory.create(project, generatedDoc),
          leftTitle,
          rightTitle
      );
      diffPanel.setRequest(request);

      DialogWrapper dialog = new DialogWrapper(project) {
        {
          setTitle(title);
          setOKButtonText("Apply");
          setCancelButtonText("Cancel");
          init();
        }

        @Override
        protected JComponent createCenterPanel() {
          return diffPanel.getComponent();
        }

        @Override
        protected void doOKAction() {
          super.doOKAction();

          String finalText = ApplicationManager.getApplication()
              .runReadAction((com.intellij.openapi.util.Computable<String>) generatedDoc::getText);
          callback.onApply(finalText);
        }

        @Override
        public void dispose() {
          super.dispose();
          // ✅ 只释放 DiffRequestPanel，不碰 Document
          Disposer.dispose(disposable);
        }
      };

      dialog.show();
    } catch (Exception e) {
      Disposer.dispose(disposable);
      throw e;
    }
  }

  public static void show(String newText) {
    Project project = ProjectInitData.getInstance().getProject();

    // 1. 获取当前编辑器
    Editor editor = FileEditorManager.getInstance(project)
        .getSelectedTextEditor();
    if (editor == null) {
      return;
    }

    // 2. 获取 Document（关键）
    Document document = editor.getDocument();

    // 3. 获取选中位置（用于插入）
    SelectionModel selectionModel = editor.getSelectionModel();
    String selectedText = selectionModel.getSelectedText();
    int lineEndOffset;
    int lineNumber=0;
    if (StringUtil.isNotEmpty(selectedText)) {
      CaretModel caretModel = editor.getCaretModel(); // 获取光标模型

      int caretOffset = caretModel.getOffset(); // 光标在文档中的偏移量

       lineNumber = document.getLineNumber(caretOffset); // 当前行号（从0开始）
      int lineStartOffset = document.getLineStartOffset(lineNumber); // 行起始偏移量
      lineEndOffset = document.getLineEndOffset(lineNumber); // 行结束偏移量
      selectedText = document.getText().substring(lineStartOffset, lineEndOffset); // 当前行文本
    } else {
      lineEndOffset = 0;
    }

    DiffPreviewUtil.showDiffPreview(
        project,
        "Code Generation Preview",
        "Original",
        "Generated",
        selectedText,
        newText,
        finalText -> {
          WriteCommandAction.runWriteCommandAction(project, () -> {

            int insertPos = document.getLineEndOffset(
                document.getLineNumber(Math.min(lineEndOffset, document.getTextLength())));
            document.insertString(insertPos, "\n" + finalText);

          });
        }
    );
  }
}