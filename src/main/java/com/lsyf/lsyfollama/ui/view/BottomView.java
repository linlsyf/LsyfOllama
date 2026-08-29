package com.lsyf.lsyfollama.ui.view;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import com.lsyf.lsyfollama.constant.Contant;
import com.lsyf.lsyfollama.constant.EvenBusContants;
import com.lsyf.lsyfollama.evenbus.BusMessage;
import com.lsyf.lsyfollama.evenbus.LsyfGlobalNotifier;
import lombok.Data;

import javax.swing.*;
import java.awt.*;

import static com.lsyf.lsyfollama.constant.EvenBusContants.TYPE_BUSINESS;

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

    initListener();

    return southContainer;
  }

  public void initListener() {

    sendButton.setPreferredSize(JBUI.size(80, 32));
    sendButton.setFocusPainted(false);
    sendButton.setBackground(JBColor.namedColor("Button.background"));
    sendButton.setForeground(JBColor.namedColor("Button.foreground"));
    sendButton.setBorder(JBUI.Borders.empty(4, 12));

    MessageBus bus = ApplicationManager.getApplication().getMessageBus();



    sendButton.addActionListener(e -> {
      String buttonText =sendButton.getText();
      if (buttonText.equals(Contant.SEND)) {
        String prompt = inputField.getText().trim();
        if (!prompt.isEmpty()) {
          sendButton.setText(Contant.STOP);
//          sendMessage(prompt);

          LsyfGlobalNotifier publisher = bus.syncPublisher(LsyfGlobalNotifier.TOPIC);
          BusMessage busMessage=new BusMessage();
          busMessage.setKey(EvenBusContants.SEND_MESSAGE);
          busMessage.setValue(prompt);
          busMessage.setMessageType(TYPE_BUSINESS);
          publisher.onDatasChanged(busMessage);
        }
      } else {
        sendButton.setText(Contant.SEND);
//        stopGeneration();
        LsyfGlobalNotifier publisher = bus.syncPublisher(LsyfGlobalNotifier.TOPIC);
        BusMessage busMessage=new BusMessage();
        busMessage.setKey(EvenBusContants.STOP_MESSAGE);
        busMessage.setMessageType(TYPE_BUSINESS);

        publisher.onDatasChanged(busMessage);
      }


    });
  }

}
