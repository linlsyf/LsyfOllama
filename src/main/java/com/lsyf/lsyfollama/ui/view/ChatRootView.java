package com.lsyf.lsyfollama.ui.view;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.cofig.SettingsConfig;
import com.lsyf.lsyfollama.constant.Contant;
import com.lsyf.lsyfollama.constant.EvenBusContants;
import com.lsyf.lsyfollama.constant.OllamaClientUtils;
import com.lsyf.lsyfollama.evenbus.BusMessage;
import com.lsyf.lsyfollama.evenbus.MyGlobalNotifier;
import com.lsyf.lsyfollama.utils.DiffPreviewUtil;
import io.github.ollama4j.models.chat.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChatRootView {

  // ===== 使用IntelliJ标准颜色 =====
  private static final Color BG_PANEL = JBColor.namedColor("Panel.background", Color.WHITE);
  private static final Color BG_USER_BUBBLE = JBColor.namedColor("ActionButton.hoverBackground", new Color(220, 245, 255));
  private static final Color BG_AI_BUBBLE = JBColor.namedColor("EditorPane.background", new Color(240, 240, 240));
  private static final Color BG_ACCEPTED = JBColor.namedColor("TestCase.passedBackground", new Color(225, 255, 225));
  private static final Color TEXT_COLOR = JBColor.namedColor("Label.foreground", Color.BLACK);
  private static final Color BORDER_COLOR = JBColor.namedColor("Border.color", new Color(220, 220, 220));

  // ===== UI组件 =====
  private JPanel chatPanel;                 // 主面板
  private JPanel chatContainer;             // 聊天消息容器
  private JScrollPane scrollPane;           // 滚动面板
  private JButton cleanButton;              // 清空按钮
  private JButton settingsButton;           // 新增：设置按钮
  private BottomView bottomView;            // 底部输入视图

  // ===== 状态变量 =====
  private volatile String lastRequestTxt = "";
  private volatile Thread appThread;
  private volatile boolean stopFlag = false;

  // ===== 新增：AI回复缓存 =====
  private volatile String lastCompletedAIResponse = "";  // 缓存最后一条已完成的AI回复
  private volatile String currentGeneratingContent = ""; // 当前正在生成的内容

  // ===== AI消息相关引用 =====
  private JTextArea currentAIResponseArea;  // 当前AI回复的文本区域
  private JPanel currentAIPanel;           // 当前AI消息面板
  private String currentPrompt;            // 当前提问（用于重新生成）

  private Project project;

  public ChatRootView(Project project) {
    this.project = project;
    initView();
//    initListener();

  }

  public void initView() {
    initMainPanel();
    initTopPanel();
    initChatArea();
    initBottomPanel();
    initListeners();

  }
//
//  private void initListener() {
//    // 订阅消息
//    MessageBusConnection connection = project.getMessageBus().connect();
//
//    connection.subscribe(FileContentChangeListener.TOPIC, new FileContentChangeListener() {
//      @Override
//      public void onContentChanged(FileContentChangeEvent event) {
////        monitorPanel.updateContent(event.getFilePath(), event.getContent());
//      }
//    });
//    // 项目关闭时自动断开连接
//    Disposer.register(project, connection);
//
//  }

  private void updateUIWithContent(String content) {
    // 更新你的 Swing 组件
//    myTextArea.setText(content);
//    myLabel.setText("字符数: " + content.length());
  }

  /**
   * 初始化主面板 - 使用JBUI确保正确缩放
   */
  private void initMainPanel() {
    chatPanel = new JPanel(new BorderLayout(0, 0));
    chatPanel.setBackground(BG_PANEL);
    chatPanel.setBorder(JBUI.Borders.empty(8));

    // 关键：强制最小尺寸，使用JBUI缩放
    chatPanel.setMinimumSize(JBUI.size(480, 350));
    chatPanel.setPreferredSize(JBUI.size(550, 650));
  }

  /**
   * 初始化顶部面板 - 新增设置按钮
   */
  private void initTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBackground(BG_PANEL);
    topPanel.setBorder(JBUI.Borders.empty(8, 12, 8, 12));

    // ===== 左侧：标题 + 版本号 =====
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    leftPanel.setBackground(BG_PANEL);
    leftPanel.setOpaque(true);

    // 原有的标题
    JLabel titleLabel = new JLabel("AI 聊天助手");
    titleLabel.setFont(JBUI.Fonts.label().deriveFont(Font.BOLD, JBUI.scaleFontSize(14f)));
    titleLabel.setForeground(TEXT_COLOR);
    leftPanel.add(titleLabel);

    // ✅ 新增：版本号标签
    String version = getPluginVersion();
    JLabel versionLabel = new JLabel(" v" + version);
    versionLabel.setFont(JBUI.Fonts.smallFont());
    versionLabel.setForeground(JBColor.GRAY);
    versionLabel.setBorder(JBUI.Borders.emptyLeft(4));
    leftPanel.add(versionLabel);

    topPanel.add(leftPanel, BorderLayout.WEST);

    // 右侧按钮容器
    JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(8), 0));
    rightButtonsPanel.setBackground(BG_PANEL);
    rightButtonsPanel.setOpaque(true);

    // 设置按钮
    settingsButton = new JButton("⚙️ 设置");
    settingsButton.setFont(JBUI.Fonts.smallFont());
    settingsButton.setFocusPainted(false);
    settingsButton.setBorder(JBUI.Borders.empty(4, 12));
    settingsButton.setBackground(JBColor.namedColor("Button.background"));
    settingsButton.setForeground(JBColor.namedColor("Button.foreground"));
    settingsButton.setToolTipText("打开 Ollama 设置");

    // 清空按钮
    cleanButton = new JButton("清空对话");
    cleanButton.setFont(JBUI.Fonts.smallFont());
    cleanButton.setFocusPainted(false);
    cleanButton.setBorder(JBUI.Borders.empty(4, 12));
    cleanButton.setBackground(JBColor.namedColor("Button.background"));
    cleanButton.setForeground(JBColor.namedColor("Button.foreground"));

    rightButtonsPanel.add(settingsButton);
    rightButtonsPanel.add(cleanButton);

    topPanel.add(rightButtonsPanel, BorderLayout.EAST);

    chatPanel.add(topPanel, BorderLayout.NORTH);
  }

  /**
   * 初始化聊天区域
   */
  private void initChatArea() {
    // 聊天容器 - 使用GridBagLayout替代BoxLayout，解决宽度问题
    chatContainer = new JPanel(new GridBagLayout());
    chatContainer.setBackground(BG_PANEL);
    chatContainer.setBorder(JBUI.Borders.empty(8));

    // 滚动面板配置
    scrollPane = new JScrollPane(chatContainer);
    scrollPane.setBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getViewport().setBackground(BG_PANEL);
    scrollPane.getViewport().setMinimumSize(JBUI.size(460, 200));
    scrollPane.setViewportBorder(null);

    chatPanel.add(scrollPane, BorderLayout.CENTER);
  }

  /**
   * 初始化底部面板
   */
  private void initBottomPanel() {
    bottomView = new BottomView();

    JPanel bottomPanel = bottomView.getInPutView();
    bottomPanel.setBorder(JBUI.Borders.empty(8, 12, 12, 12));
    bottomPanel.setBackground(BG_PANEL);

    JTextField inputField = bottomView.getInputField();
    inputField.setPreferredSize(JBUI.size(360, 32));
    inputField.setMaximumSize(JBUI.size(Integer.MAX_VALUE, 32));
    inputField.setBorder(JBUI.Borders.compound(
        JBUI.Borders.customLine(BORDER_COLOR, 1),
        JBUI.Borders.empty(4, 8)
    ));
    inputField.setBackground(JBColor.namedColor("TextField.background"));
    inputField.setForeground(JBColor.namedColor("TextField.foreground"));
    inputField.setCaretColor(JBColor.namedColor("TextField.caretForeground"));


    chatPanel.add(bottomPanel, BorderLayout.SOUTH);
  }

  /**
   * 初始化事件监听器 - 新增设置按钮监听
   */
  private void initListeners() {
//    // 发送按钮
//    bottomView.getSendButton().addActionListener(e -> {
//      String buttonText = bottomView.getSendButton().getText();
//      if (buttonText.equals(Contant.SEND)) {
//        String prompt = bottomView.getInputField().getText().trim();
//        if (!prompt.isEmpty()) {
//          bottomView.getSendButton().setText(Contant.STOP);
//          sendMessage(prompt);
//        }
//      } else {
//        bottomView.getSendButton().setText(Contant.SEND);
//        stopGeneration();
//      }
//    });

    // 清空按钮 - 同时清空缓存
    cleanButton.addActionListener(e -> {
      chatContainer.removeAll();
      lastRequestTxt = "";
      currentPrompt = null;
      lastCompletedAIResponse = "";  // 清空缓存
      currentGeneratingContent = ""; // 清空当前生成内容
      chatContainer.revalidate();
      chatContainer.repaint();
    });

    // 设置按钮 - 打开设置界面
    settingsButton.addActionListener(e -> openSettings());

    // 回车发送
    bottomView.getInputField().addActionListener(e -> {
      String prompt = bottomView.getInputField().getText().trim();
      if (!prompt.isEmpty()) {
        bottomView.getSendButton().setText(Contant.STOP);
        sendMessage(prompt);
      }
    });




    MessageBusConnection conn = ApplicationManager.getApplication()
        .getMessageBus().connect();

    conn.subscribe(MyGlobalNotifier.TOPIC, new MyGlobalNotifier() {
      @Override
      public void onDatasChanged(BusMessage busMessage) {
          if (EvenBusContants.SEND_MESSAGE.equals(busMessage.getKey())){
            String prompt=busMessage.getValue().toString();
            sendMessage(prompt);
          }
          else if (EvenBusContants.STOP_MESSAGE.equals(busMessage.getKey())){
            stopGeneration();
          }

      }

    });

// 不用时释放，或挂到某个 Disposable 上自动释放
// conn.disconnect();
  }

  /**
   * 打开 IDEA 设置界面
   */
  private void openSettings() {
    if (project == null) {
      Messages.showErrorDialog("无法打开设置：项目未初始化", "错误");
      return;
    }

    ApplicationManager.getApplication().invokeLater(() -> {
      try {
        // 方式一：直接打开 Ollama 设置页面（推荐）
        ShowSettingsUtil.getInstance().showSettingsDialog(
            project,
            SettingsConfig.class
        );

        // 方式二：打开通用设置，然后导航到 Ollama（备选）
        // ShowSettingsUtil.getInstance().showSettingsDialog(project, "Ollama");

        // 方式三：打开所有设置，用户手动找到 Ollama
        // ShowSettingsUtil.getInstance().showSettingsDialog(project);

      } catch (Exception e) {
        // 如果找不到 OllamaSettingsConfigurable，则打开通用设置
        ShowSettingsUtil.getInstance().showSettingsDialog(project);
      }
    });
  }

  /**
   * 检查 Ollama 配置是否有效
   */
  private boolean checkOllamaConfiguration() {
    if (!ChatConstant.apiUrl.startsWith("http")) {
      // 提示用户去设置
      int result = Messages.showYesNoDialog(
          project,
          "Ollama API 地址未配置或配置无效。\n是否现在打开设置？",
          "配置缺失",
          "打开设置",
          "取消",
          Messages.getWarningIcon()
      );

      if (result == Messages.YES) {
        openSettings();
      }
      return false;
    }
    return true;
  }

  /**
   * 创建气泡面板
   */
  private JPanel createBubblePanel(String text, boolean isUser) {
    JPanel bubblePanel = new JPanel(new GridBagLayout());
    bubblePanel.setBackground(BG_PANEL);
    bubblePanel.setOpaque(true);

    JTextArea textArea = new JTextArea(text);
    textArea.setFont(JBUI.Fonts.label().deriveFont(JBUI.scaleFontSize(14f)));
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);
    textArea.setBackground(isUser ? BG_USER_BUBBLE : BG_AI_BUBBLE);
    textArea.setForeground(TEXT_COLOR);
    textArea.setBorder(JBUI.Borders.empty(8, 12));
    textArea.setOpaque(true);

    int preferredWidth = JBUI.scale(400);
    textArea.setSize(new Dimension(preferredWidth, Short.MAX_VALUE));
    textArea.setPreferredSize(new Dimension(preferredWidth, textArea.getPreferredSize().height));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = isUser ? GridBagConstraints.EAST : GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 1.0;
    gbc.insets = JBUI.insets(4, 8, 4, 8);

    bubblePanel.add(textArea, gbc);
    return bubblePanel;
  }

  /**
   * 添加用户消息
   */
  private void addUserMessage(String text) {
    SwingUtilities.invokeLater(() -> {
      JPanel bubblePanel = createBubblePanel(text, true);

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = GridBagConstraints.RELATIVE;
      gbc.anchor = GridBagConstraints.EAST;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1.0;
      gbc.insets = JBUI.insets(4, 60, 4, 8);

      chatContainer.add(bubblePanel, gbc);
      chatContainer.revalidate();
      scrollToBottom();
    });
  }

  /**
   * 添加AI消息面板（包含按钮）
   */
  private void addAIMessagePanel(String prompt) {
    SwingUtilities.invokeLater(() -> {
      currentPrompt = prompt;
      currentGeneratingContent = ""; // 重置当前生成内容

      JPanel aiPanel = new JPanel(new GridBagLayout());
      aiPanel.setBackground(BG_PANEL);
      aiPanel.setOpaque(true);

      currentAIResponseArea = new JTextArea("");
      currentAIResponseArea.setFont(JBUI.Fonts.label().deriveFont(JBUI.scaleFontSize(14f)));
      currentAIResponseArea.setLineWrap(true);
      currentAIResponseArea.setWrapStyleWord(true);
      currentAIResponseArea.setEditable(false);
      currentAIResponseArea.setBackground(BG_AI_BUBBLE);
      currentAIResponseArea.setForeground(TEXT_COLOR);
      currentAIResponseArea.setBorder(JBUI.Borders.empty(8, 12));
      currentAIResponseArea.setOpaque(true);

      int preferredWidth = JBUI.scale(400);
      currentAIResponseArea.setSize(new Dimension(preferredWidth, Short.MAX_VALUE));
      currentAIResponseArea.setPreferredSize(new Dimension(preferredWidth,
          currentAIResponseArea.getPreferredSize().height));

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.anchor = GridBagConstraints.WEST;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1.0;
      gbc.insets = JBUI.insets(4, 8, 4, 60);

      aiPanel.add(currentAIResponseArea, gbc);

      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), JBUI.scale(4)));
      buttonPanel.setBackground(BG_PANEL);
      buttonPanel.setOpaque(true);
      buttonPanel.setBorder(JBUI.Borders.empty(0, JBUI.scale(8), 0, 0));

      JButton acceptBtn = new JButton("✅ 接受");
      JButton regenerateBtn = new JButton("🔄 重新生成");

      styleButton(acceptBtn);
      styleButton(regenerateBtn);

      acceptBtn.addActionListener(e -> {
        // 接受时更新缓存
        lastCompletedAIResponse = currentAIResponseArea.getText();
        DiffPreviewUtil.show(lastCompletedAIResponse);
      });

      regenerateBtn.addActionListener(e -> {
        chatContainer.remove(currentAIPanel);
        chatContainer.revalidate();
        chatContainer.repaint();
        if (currentPrompt != null && !currentPrompt.isEmpty()) {
          sendMessage(currentPrompt);
        }
      });

      buttonPanel.add(acceptBtn);
      buttonPanel.add(regenerateBtn);

      GridBagConstraints btnGbc = new GridBagConstraints();
      btnGbc.gridx = 0;
      btnGbc.gridy = 1;
      btnGbc.anchor = GridBagConstraints.WEST;
      btnGbc.fill = GridBagConstraints.HORIZONTAL;
      btnGbc.weightx = 1.0;
      btnGbc.insets = JBUI.insets(0, JBUI.scale(8), 0, 0);

      aiPanel.add(buttonPanel, btnGbc);

      currentAIPanel = aiPanel;

      GridBagConstraints mainGbc = new GridBagConstraints();
      mainGbc.gridx = 0;
      mainGbc.gridy = GridBagConstraints.RELATIVE;
      mainGbc.anchor = GridBagConstraints.WEST;
      mainGbc.fill = GridBagConstraints.HORIZONTAL;
      mainGbc.weightx = 1.0;
      mainGbc.insets = JBUI.insets(8, 8, 8, 8);

      chatContainer.add(aiPanel, mainGbc);
      chatContainer.revalidate();
      scrollToBottom();
    });
  }

  /**
   * 按钮样式统一
   */
  private void styleButton(JButton button) {
    button.setFont(JBUI.Fonts.smallFont());
    button.setFocusPainted(false);
    button.setBorder(JBUI.Borders.empty(4, 10));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setBackground(JBColor.namedColor("Button.background"));
    button.setForeground(JBColor.namedColor("Button.foreground"));
  }

  /**
   * 追加AI回复内容
   */
  private void appendAIResponse(String text) {
    SwingUtilities.invokeLater(() -> {
      if (currentAIResponseArea != null) {
        currentAIResponseArea.append(text);
        currentGeneratingContent += text; // 更新当前生成内容

        Dimension preferredSize = currentAIResponseArea.getPreferredSize();
        int width = currentAIResponseArea.getWidth();
        if (width <= 0) {
          width = JBUI.scale(400);
        }

        FontMetrics fm = currentAIResponseArea.getFontMetrics(currentAIResponseArea.getFont());
        int lineHeight = fm.getHeight();
        int lines = currentAIResponseArea.getLineCount();
        int newHeight = Math.max(lineHeight * lines + JBUI.scale(20), JBUI.scale(40));

        currentAIResponseArea.setPreferredSize(new Dimension(width, newHeight));
        currentAIResponseArea.setSize(width, newHeight);

        currentAIResponseArea.revalidate();
        currentAIPanel.revalidate();
        chatContainer.revalidate();

        scrollToBottom();
      }
    });
  }

  /**
   * 滚动到底部
   */
  private void scrollToBottom() {
    SwingUtilities.invokeLater(() -> {
      JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
      if (verticalBar != null) {
        verticalBar.setValue(verticalBar.getMaximum());
      }
    });
  }

  /**
   * 停止生成
   */
  private void stopGeneration() {
    stopFlag = true;
    if (appThread != null && appThread.isAlive()) {
      appThread.interrupt();
    }
    // 停止时更新缓存
    if (currentAIResponseArea != null) {
      lastCompletedAIResponse = currentAIResponseArea.getText();
    }
  }

  /**
   * 发送消息 - 添加配置检查
   */
  public void sendMessage(String prompt) {
    addUserMessage(prompt);
    addAIMessagePanel(prompt);

    bottomView.getInputField().setText("");
    bottomView.getInputField().requestFocus();

    if (!ChatConstant.apiUrl.startsWith("http")) {
      appendAIResponse("⚠️ 请先在设置中配置 Ollama API 地址和模型\n");
      bottomView.getSendButton().setText(Contant.SEND);
      return;
    }

    stopFlag = false;
    currentGeneratingContent = ""; // 重置当前生成内容

    appThread = new Thread(() -> {
      try {
        OllamaChatRequest request = new OllamaChatRequest();
        List<OllamaChatMessage> messages = new ArrayList<>();

        if (StringUtils.isNotBlank(lastRequestTxt)) {
          messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, lastRequestTxt));
        }
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));

        request.setMessages(messages);
        lastRequestTxt = prompt;

        OllamaClientUtils.chatStreaming(request, new OllamaChatTokenHandler() {
          @Override
          public void accept(OllamaChatResponseModel response) {
            if (stopFlag) {
              return;
            }

            if (response.isDone()) {
              SwingUtilities.invokeLater(() -> {
                bottomView.getSendButton().setText(Contant.SEND);
                // 完成时更新缓存
                if (currentAIResponseArea != null) {
                  lastCompletedAIResponse = currentAIResponseArea.getText();
                }
              });
            } else {
              String thinking = response.getMessage().getThinking();
              String content = response.getMessage().getResponse();

              String textToAppend;
              if (StringUtils.isNotBlank(thinking)) {
                textToAppend = thinking;
              } else {
                textToAppend = content;
              }

              if (StringUtils.isNotBlank(textToAppend)) {
                appendAIResponse(textToAppend);
              }
            }
          }
        });

      } catch (Exception e) {
        if (!stopFlag) {
          appendAIResponse("\n❌ 错误: " + e.getMessage() + "\n");
        }
        SwingUtilities.invokeLater(() -> {
          bottomView.getSendButton().setText(Contant.SEND);
          // 出错时也更新缓存
          if (currentAIResponseArea != null) {
            lastCompletedAIResponse = currentAIResponseArea.getText();
          }
        });
      }
    });

    appThread.setDaemon(true);
    appThread.start();
  }

  // ==================== 新增：获取AI回复内容的方法 ====================

  /**
   * 获取最后一条已完成的AI回复内容
   *
   * @return 最后一条已完成的AI回复内容，如果没有则返回空字符串
   */
  public String getLastCompletedAIResponse() {
    return lastCompletedAIResponse;
  }

  /**
   * 获取聊天历史中最后一条AI消息内容（包括正在生成但未完成的）
   *
   * @return 最后一条AI消息内容，如果没有则返回空字符串
   */
  public String getLastAIHistoryMessage() {
    // 从后往前遍历聊天容器
    for (int i = chatContainer.getComponentCount() - 1; i >= 0; i--) {
      Component comp = chatContainer.getComponent(i);
      if (comp instanceof JPanel) {
        JPanel panel = (JPanel) comp;
        String aiText = findAIResponseInPanel(panel);
        if (aiText != null && !aiText.isEmpty()) {
          return aiText;
        }
      }
    }
    return "";
  }

  /**
   * 从面板中查找AI回复文本
   */
  private String findAIResponseInPanel(JPanel panel) {
    // 检查面板中是否包含AI消息的按钮
    if (containsAIButtons(panel)) {
      JTextArea textArea = findTextAreaInPanel(panel);
      if (textArea != null) {
        return textArea.getText();
      }
    }
    return null;
  }

  /**
   * 检查面板是否包含AI消息的按钮
   */
  private boolean containsAIButtons(JPanel panel) {
    for (Component comp : panel.getComponents()) {
      if (comp instanceof JPanel) {
        JPanel subPanel = (JPanel) comp;
        int acceptCount = 0;
        int regenerateCount = 0;
        for (Component subComp : subPanel.getComponents()) {
          if (subComp instanceof JButton) {
            JButton btn = (JButton) subComp;
            String text = btn.getText();
            if ("✅ 接受".equals(text)) {
              acceptCount++;
            } else if ("🔄 重新生成".equals(text)) {
              regenerateCount++;
            }
          }
        }
        if (acceptCount > 0 && regenerateCount > 0) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 在面板中递归查找第一个JTextArea
   */
  private JTextArea findTextAreaInPanel(JPanel panel) {
    for (Component comp : panel.getComponents()) {
      if (comp instanceof JTextArea) {
        return (JTextArea) comp;
      } else if (comp instanceof JPanel) {
        JTextArea textArea = findTextAreaInPanel((JPanel) comp);
        if (textArea != null) {
          return textArea;
        }
      }
    }
    return null;
  }

  /**
   * 获取最后一条消息（用户或AI）
   *
   * @return 最后一条消息内容，如果没有则返回空字符串
   */
  public String getLastMessage() {
    if (chatContainer.getComponentCount() == 0) {
      return "";
    }
    Component lastComp = chatContainer.getComponent(chatContainer.getComponentCount() - 1);
    if (lastComp instanceof JPanel) {
      JPanel panel = (JPanel) lastComp;
      JTextArea textArea = findTextAreaInPanel(panel);
      if (textArea != null) {
        return textArea.getText();
      }
    }
    return "";
  }

  /**
   * 获取当前正在生成的内容（流式输出时用）
   *
   * @return 当前已生成的AI内容，如果没有正在生成则返回空字符串
   */
  public String getCurrentGeneratingContent() {
    return currentGeneratingContent;
  }

  /**
   * 检查是否有正在生成的AI回复
   *
   * @return true如果有正在生成的回复
   */
  public boolean isGenerating() {
    return currentAIResponseArea != null &&
        Contant.STOP.equals(bottomView.getSendButton().getText());
  }

  /**
   * 获取当前激活的编辑器
   */
  private void getActiveEditor() {
    if (project == null) return;

    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (editor != null) {
      String selectedText = editor.getSelectionModel().getSelectedText();
      if (StringUtils.isNotBlank(selectedText)) {
        bottomView.getInputField().setText(selectedText);
        bottomView.getInputField().requestFocus();
      }
    }
  }

  /**
   * 获取主面板（供ToolWindow注册使用）
   */
  public JPanel getChatPanel() {
    return chatPanel;
  }

  /**
   * 设置项目
   */
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * 获取插件版本号
   */
  private String getPluginVersion() {
    try {
      PluginId pluginId = PluginId.getId("com.lsyf.LsyfOllama");
      IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(pluginId);
      if (plugin != null) {
        return plugin.getVersion();
      }
    } catch (Exception e) {
      // 忽略异常，返回默认值
    }
    return "version code"; // 默认版本号
  }
}