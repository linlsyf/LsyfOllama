package com.lsyf.lsyfollama.ui;

import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.OllamaClient;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResponseModel;
import io.github.ollama4j.models.generate.OllamaTokenHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ChatToolWindow {
    private JPanel chatPanel;          // 主面板
    private JTextArea messageArea;    // 消息显示区域
    private JTextField inputField;     // 消息输入框
    private JButton sendButton;        // 发送按钮
    private JButton repairButton;        // stop按钮

    public ChatToolWindow() {
        // 初始化组件
        chatPanel = new JPanel(new BorderLayout());
//        chatPanel.add(new JButton("测试按钮"));
        messageArea = new JTextArea() {
            @Override
            public void paintComponent(Graphics g) {
                // 启用双缓冲
                super.paintComponent(g);
            }
        };
        inputField = new JTextField(20);
        inputField.setText("如何记单词");
        sendButton = new JButton("发送");
        repairButton = new JButton("stop");

        // 消息区域设置
        messageArea.setEditable(false); // 禁止编辑
        messageArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(messageArea); // 添加滚动条

        // 底部输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
//        inputPanel.add(repairButton, BorderLayout.WEST);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // 组装主面板
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

//        // 事件监听
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String prompt = inputField.getText().trim();

                sendMessage(prompt);


            }
        });
        // 事件监听
//        repairButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String selectedText = messageArea.getSelectedText();
//
////                OllamaClient.repair(e);
//            }
//        });
        inputField.addActionListener(new ActionListener() { // 支持回车发送
            @Override
            public void actionPerformed(ActionEvent e) {
                String prompt = inputField.getText().trim();

                sendMessage(prompt);
            }
        });
    }

    // 发送消息逻辑
    public void sendMessage(String prompt) {
        messageArea.append("我: " + prompt + "\n"); // 添加消息到显示区
        inputField.setText("");                      // 清空输入框
        inputField.requestFocus();                   // 焦点回到输入框

         if (!ChatConstant.apiUrl.startsWith("http")){
             writeMsg("please  set ip and  model");
         }

        try {

            Thread appThread = new Thread() {
                public void run() {
                    OllamaChatRequest request = new OllamaChatRequest();
                    List<OllamaChatMessage> messages = new ArrayList<>();
//                    messages.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM, "你是一个Java专家，只回答技术问题"));
                    messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));
//                    messages.add(new OllamaChatMessage(OllamaChatMessageRole.ASSISTANT, "直接输出代码"));

                    request.setMessages(messages); // 必须包含消息列表
                    try {
                        OllamaClient.chatStreaming(request, new OllamaTokenHandler() {
                            @Override
                            public void accept(OllamaChatResponseModel ollamaChatResponseModel) {
                                writeMsg(ollamaChatResponseModel.getMessage().getContent());
                                System.out.println(ollamaChatResponseModel.getMessage().getContent());
                            }

                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            };
            appThread.start();


        } catch (Exception e) {
            writeMsg(e.getMessage() + "\n"); // 添加消息到显示区
//            throw new RuntimeException(e);
        }


    }


    private void writeMsg(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message));
    }

    // 返回主面板（供IDEA插件集成）
    public JPanel getContent() {
        return chatPanel;
    }

}