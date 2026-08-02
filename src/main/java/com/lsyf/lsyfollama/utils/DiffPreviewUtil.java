package com.lsyf.lsyfollama.utils;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class DiffPreviewUtil {

  private DiffPreviewUtil() {}

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
}