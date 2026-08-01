package com.lsyf.lsyfollama.ui.view;

import lombok.Data;

import javax.swing.*;
import java.awt.*;

@Data
public class SettingView {

  private JPanel mainPanel;

  private JTextField inputField;
  private JTextField commitReceiptFied;
  private JTextField commitRejectFied;
  private JTextField modelField;
  // 切换状态的方法
//    private boolean isAiMode = true; // 初始为编辑模式
  JButton toggleButton;

   public void init() {
    initView();

    // 添加按钮点击事件
  }

  public void initView() {
      mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

      // 创建切换按钮
      toggleButton = new JButton();
//      getToggleText();
      toggleButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 居中对齐
      toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // 宽度撑满

      // 初始化文本框
      inputField = new JTextField();
      modelField = new JTextField();
      commitReceiptFied = new JTextField();
      commitRejectFied = new JTextField();

      // 设置文本框最大高度，防止被拉伸
      Dimension textFieldSize = new Dimension(Integer.MAX_VALUE, 30);
      inputField.setMaximumSize(textFieldSize);
      modelField.setMaximumSize(textFieldSize);
      commitReceiptFied.setMaximumSize(textFieldSize);
      commitRejectFied.setMaximumSize(textFieldSize);

      // 添加组件到面板，按上下顺序排列
      mainPanel.add(toggleButton);
      mainPanel.add(Box.createVerticalStrut(10)); // 添加10像素的垂直间距
      mainPanel.add(inputField);
      mainPanel.add(Box.createVerticalStrut(5));
      mainPanel.add(modelField);
      mainPanel.add(Box.createVerticalStrut(5));
      mainPanel.add(commitReceiptFied);
      mainPanel.add(Box.createVerticalStrut(5));
      mainPanel.add(commitRejectFied);

  }

//
//  public void getToggleText() {
//    String state = (ChatConstant.isAiModeDefault ? "智能模式" : "本地模式");
//    toggleButton.setText(state);
//
//  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public JTextField getInputField() {
    return inputField;
  }

  public JTextField getCommitReceiptFied() {
    return commitReceiptFied;
  }

  public JTextField getCommitRejectFied() {
    return commitRejectFied;
  }

  public JTextField getModelField() {
    return modelField;
  }

  public JButton getToggleButton() {
    return toggleButton;
  }
}
