package com.lsyf.lsyfollama.ui.view.message;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import javax.swing.*;
import java.awt.*;

public class AiChatPanel extends JPanel implements Disposable {

    private final MarkdownCardPanel markdown ;

    public AiChatPanel(Project project) {
       markdown = new MarkdownCardPanel(project);
        setLayout(new BorderLayout());
        add(markdown, BorderLayout.CENTER);
        Disposer.register(this, markdown);
    }

    /** AI 返回完整回复后调用 */
    public void onResponse(String text) {
        ApplicationManager.getApplication().invokeLater(() -> markdown.render(text));
    }

    @Override public void dispose() { /* markdown 随 this 一起释放 */ }

  public String getText() {
      return "";
  }

  public void append(String text) {
    ApplicationManager.getApplication().invokeLater(() ->
        markdown.append(text)
    );

  }

  public void beginStream(){
    markdown.beginStream();
  }

  public void endStream(){
    markdown.endStream();
  }




}