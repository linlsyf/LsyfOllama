package com.lsyf.lsyfollama.ui.view.select;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.lsyf.lsyfollama.constant.ProjectInitData;
import com.lsyf.lsyfollama.ui.view.ChatRootView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ChatPanel extends JPanel implements com.intellij.openapi.Disposable {

  private final Project project;
  private final Editor editor;
  private final String selectedCode;

  private final JPanel messageBox = new JPanel();
  private final JBTextArea input = new JBTextArea(3, 40);
  private volatile boolean disposed;

  public ChatPanel(@NotNull Project project, @NotNull Editor editor, @NotNull String selectedCode) {
    this.project = project;
    this.editor = editor;
    this.selectedCode = selectedCode;

    setLayout(new BorderLayout(0, JBUI.scale(8)));
    setPreferredSize(new Dimension(480, 340));
    setBorder(JBUI.Borders.empty(10));

    // --- 上下文提示条 ---
    JPanel ctx = new JPanel(new BorderLayout(6, 0));
    ctx.setBorder(JBUI.Borders.emptyBottom(6));
    ctx.add(new JLabel(AllIcons.Actions.IntentionBulb), BorderLayout.WEST);
    JLabel ctxLabel = new JLabel("已选中 " + selectedCode.lines().count() + " 行代码作为上下文");
    ctxLabel.setFont(JBUI.Fonts.smallFont());
    ctxLabel.setForeground(JBUI.CurrentTheme.Label.disabledForeground());
    ctx.add(ctxLabel, BorderLayout.CENTER);
    add(ctx, BorderLayout.NORTH);

    // --- 消息列表 ---
    messageBox.setLayout(new BoxLayout(messageBox, BoxLayout.Y_AXIS));
//    messageBox.setBackground(JBUI.CurrentTheme.Editor.background());
    JBScrollPane scroll = new JBScrollPane(messageBox);
    scroll.setBorder(JBUI.Borders.empty());
    add(scroll, BorderLayout.CENTER);

    // --- 输入区 ---
    input.setLineWrap(true);
    input.setWrapStyleWord(true);
    input.setBorder(JBUI.Borders.empty(6));
    input.putClientProperty("JTextArea.placeholder", "Enter 发送 / Shift+Enter 换行");
    // Enter 发送（IME 候选冲突见下方「坑位」）
    input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
    input.getActionMap().put("send", new AbstractAction() {
      @Override public void actionPerformed(java.awt.event.ActionEvent e) { send(); }
    });

    JButton sendBtn = new JButton("发送");
    sendBtn.addActionListener(e -> send());

    JPanel bottom = new JPanel(new BorderLayout(6, 6));
    bottom.add(new JBScrollPane(input), BorderLayout.CENTER);
    bottom.add(sendBtn, BorderLayout.EAST);
    add(bottom, BorderLayout.SOUTH);

    addMessage("AI", "你好，我已读取你选中的代码，想问什么？");
  }

  public JComponent getPreferredFocusComponent() { return input; }

  private void addMessage(@NotNull String who, @NotNull String text) {
    JPanel bubble = new JPanel(new BorderLayout(0, 4));
    bubble.setOpaque(false);
    bubble.setBorder(JBUI.Borders.empty(6, 8));

    JLabel name = new JLabel(who);
    name.setFont(JBUI.Fonts.smallFont());
    name.setForeground(JBUI.CurrentTheme.Label.disabledForeground());

    JTextArea body = new JTextArea(text);
    body.setEditable(false);
    body.setOpaque(false);
    body.setLineWrap(true);
    body.setWrapStyleWord(true);
    body.setBorder(JBUI.Borders.empty());

    bubble.add(name, BorderLayout.NORTH);
    bubble.add(body, BorderLayout.CENTER);
    messageBox.add(bubble);
    messageBox.revalidate();
    messageBox.repaint();
    SwingUtilities.invokeLater(() ->
        ((JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, messageBox))
            .getVerticalScrollBar().setValue(Integer.MAX_VALUE));
  }

  private void send() {
    String question = input.getText().trim();
    if (question.isEmpty()) return;
    input.setText("");
    addMessage("我", question);

    JTextArea[] holder = new JTextArea[1];
    addMessage("AI", "思考中…");
    // 拿到刚插入的 body 组件用于就地替换
    java.awt.Component last = messageBox.getComponent(messageBox.getComponentCount() - 1);
    if (last instanceof java.awt.Container) {
      java.awt.Component body = ((java.awt.Container) last).getComponent(1);
      if (body instanceof JTextArea) holder[0] = (JTextArea) body;
    }

    String prompt = question+selectedCode;


    ChatRootView chatTool = ProjectInitData.getInstance().getChatTool();
    chatTool.sendMessage(prompt);
//    ApplicationManager.getApplication().executeOnPooledThread(() -> {   // 网络请求必须离开 EDT
//      String answer = AiClient.chat(selectedCode, question);
//      ApplicationManager.getApplication().invokeLater(() -> {        // 回 EDT 更新 UI
//        if (disposed) return;
//        if (holder[0] != null) holder[0].setText(answer);
//        messageBox.revalidate();
//        messageBox.repaint();
//      });
//    });
  }

  @Override public void dispose() { disposed = true; }
}
