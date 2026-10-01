package com.lsyf.lsyfollama.ui.view;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.awt.RelativePoint;
import com.lsyf.lsyfollama.ui.view.select.ChatPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class AskAboutSelectionAction extends AnAction {

  @Override
  public void update(@NotNull AnActionEvent e) {
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    // 没选中就隐藏菜单项，避免误导
    e.getPresentation().setEnabledAndVisible(
        editor != null && editor.getSelectionModel().hasSelection());
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    Project project = e.getProject();
    if (editor == null || project == null) return;
    showChat(project, editor, e.getDataContext());
  }

  public static void showChat(@NotNull Project project,
                              @NotNull Editor editor,
                              @Nullable DataContext dataContext) {
    String code = editor.getSelectionModel().getSelectedText();
    if (code == null || code.isBlank()) return;

    ChatPanel panel = new ChatPanel(project, editor, code);
    JBPopup popup = JBPopupFactory.getInstance()
        .createComponentPopupBuilder(panel, panel.getPreferredFocusComponent())
        .setTitle("AI Chat")
        .setResizable(true)
        .setMovable(true)
        .setRequestFocus(true)
        .setCancelKeyEnabled(true)        // Esc 关闭
        .setCancelOnClickOutside(false)   // 要输入，别一点外面就消失
        .setMinSize(new Dimension(480, 320))
        .setCancelButton(new IconButton("关闭 (Esc)",           // ← 原生 X，点击自动 cancel
            AllIcons.Actions.Close,
            AllIcons.Actions.CloseHovered))

//        .setDimensionServiceKey("AiChatPopup") // 记住上次窗口大小
        .createPopup();

    Disposer.register(popup, panel);          // 关闭时顺带释放资源

    Point anchor = anchorPoint(editor);
    if (anchor != null) {
      // 贴在选区末尾下方（RelativePoint 会自动换算屏幕坐标，别自己加 getLocationOnScreen）
      popup.show(new RelativePoint(editor.getContentComponent(), anchor));
    } else if (dataContext != null) {
      popup.showInBestPositionFor(dataContext); // 越界就让 IDE 自己挑位置
    } else {
      popup.showCenteredInCurrentWindow(project);
    }
  }

  /** 选区末尾的锚点；超出可视区域返回 null */
  private static @Nullable Point anchorPoint(@NotNull Editor editor) {
    Rectangle area = editor.getScrollingModel().getVisibleArea();
    int end = editor.getSelectionModel().getSelectionEnd();
    Point p = editor.visualPositionToXY(editor.offsetToVisualPosition(end));
    if (p == null || !area.contains(p)) return null;
    int y = p.y + editor.getLineHeight();
    if (y > area.y + area.height - 48) y = p.y - 8; // 太靠底部就往上翻
    return new Point(Math.max(p.x, 8), y);
  }
}