package com.lsyf.lsyfollama.ui.view;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SelectionChatStartup implements StartupActivity {
  @Override public void runActivity(@NotNull Project project) {
    EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
      @Override public void editorCreated(@NotNull EditorFactoryEvent event) {
        Editor editor = event.getEditor();
        if (editor.getProject() != project || editor.getDocument().isInBulkUpdate()) return;
        new AutoPopup(project, editor).install();
      }
    }, project);
  }
}

class AutoPopup {
  private final Project project;
  private final Editor editor;
  private final com.intellij.openapi.Disposable life = Disposer.newDisposable();
  private final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, life);
  private JBPopup hint;

  AutoPopup(Project project, Editor editor) { this.project = project; this.editor = editor; }

  void install() {
    Disposer.register(life, () -> { if (hint != null && !hint.isDisposed()) hint.cancel(); });
    editor.addEditorMouseListener(new EditorMouseListener() {
      @Override public void mouseReleased(@NotNull EditorMouseEvent e) { schedule(); }
    }, life);

    // 编辑器被关掉（切文件/关标签页）时释放监听器与 Alarm
    EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
      @Override public void editorReleased(@NotNull EditorFactoryEvent event) {
        if (event.getEditor() == editor) Disposer.dispose(life);
      }
    }, life);
  }

  private void schedule() {
    alarm.cancelAllRequests();
    alarm.addRequest(() -> {                 // 250ms 防抖，避免拖选过程乱弹
      SelectionModel sm = editor.getSelectionModel();
      if (editor.isDisposed() || !sm.hasSelection()) return;
      String text = sm.getSelectedText();
      if (text == null || text.length() < 3) return;   // 太短不扰民
      showHint();
    }, 250);
  }

  private void showHint() {
    if (hint != null && !hint.isDisposed()) hint.cancel();
    JButton btn = new JButton(" 问 AI ");
    btn.addActionListener(e -> {
      hint.cancel();
      AskAboutSelectionAction.showChat(project, editor, null);
    });
    hint = JBPopupFactory.getInstance().createComponentPopupBuilder(btn, null)
        .setRequestFocus(false)          // 不抢焦点，写代码不被打断
        .setCancelOnClickOutside(true)
        .setCancelKeyEnabled(true)
        .createPopup();
    java.awt.Point anchor = /* 复用上面的 anchorPoint(editor) */ null;
    if (anchor != null) hint.show(new RelativePoint(editor.getContentComponent(), anchor));
  }
}