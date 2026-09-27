package com.lsyf.lsyfollama.ui.view.message;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class AiChatPanel extends JPanel implements Disposable {

  private final MarkdownCardPanel markdown;

  public AiChatPanel(Project project) {
    markdown = new MarkdownCardPanel(project);
    setLayout(new BorderLayout());
    setOpaque(true);
    setBackground(UIUtil.getPanelBackground());
    add(markdown, BorderLayout.CENTER);
    Disposer.register(this, markdown);
  }

  /** AI 返回完整回复后调用（非流式路径） */
  public void onResponse(String text) {
    ApplicationManager.getApplication().invokeLater(() -> markdown.render(text));
  }

  /**
   * 流式输出：三个方法都走同一个 EDT 队列，保证 begin → append×N → end 的顺序。
   * 之前 append 走 invokeLater 而 begin/end 直接同步调用，导致 end 先于 append 执行，
   * 读到的是空缓冲。
   */
  public void beginStream() {
    ApplicationManager.getApplication().invokeLater(markdown::beginStream);
  }

  public void append(String text) {
    ApplicationManager.getApplication().invokeLater(() -> markdown.append(text));
  }

  public void endStream() {
    ApplicationManager.getApplication().invokeLater(markdown::endStream);
  }

  public String getText() {
    return "";
//    return markdown.getLastMarkdown();
  }

  @Override public void dispose() { /* markdown 随 this 一起释放 */ }
}
