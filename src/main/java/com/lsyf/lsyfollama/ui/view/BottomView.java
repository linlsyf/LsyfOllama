package com.lsyf.lsyfollama.ui.view;

import lombok.Data;

import javax.swing.*;
import java.awt.*;

@Data
public class BottomView {
  JPanel inputPanel;
  JTextField inputField;
  JButton sendButton;        // 发送按钮
  JScrollPane selectScroolPanel;

  public JPanel getInPutView() {

    inputField = new JTextField(20);
    inputField.setText("如何记单词");
//        sendButton.setText("send");
    sendButton = new JButton("send");

    // 底部输入面板
    inputPanel = new JPanel(new BorderLayout(10, 10));

    inputPanel.add(inputField, BorderLayout.CENTER);

    inputPanel.add(sendButton, BorderLayout.EAST);

    JPanel southContainer = new JPanel(new BorderLayout());
    TagLabelView tagLabelView = new TagLabelView();
    selectScroolPanel = tagLabelView.createFilsList(inputField);
    southContainer.add(selectScroolPanel, BorderLayout.NORTH);  // 标签在上方
    southContainer.add(inputPanel, BorderLayout.CENTER);    // 输入框在下方




    return southContainer;
  }

}
