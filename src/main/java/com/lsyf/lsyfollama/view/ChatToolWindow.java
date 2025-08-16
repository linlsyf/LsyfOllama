package com.lsyf.lsyfollama.ui;

import com.intellij.openapi.roots.ui.componentsList.layout.Orientation;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.Contant;
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
    private JButton stopButton;        // stop按钮
    private JButton cleanButton;        // stop按钮
JPanel inputPanel;

     String lastRequestTxt="";
    private Thread appThread;

    public ChatToolWindow() {
        // 初始化组件
        chatPanel = new JPanel(new BorderLayout());
        messageArea = new JTextArea() {
            @Override
            public void paintComponent(Graphics g) {
                // 启用双缓冲
                super.paintComponent(g);
            }
        };
//        messageArea.setColumns(10);
        inputField = new JTextField(20);
        inputField.setText("如何记单词");
        sendButton = new JButton("send");
        stopButton = new JButton("stop");
        cleanButton = new JButton("clean");
        // 底部输入面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(cleanButton, BorderLayout.EAST);

        // 消息区域设置
        messageArea.setEditable(false); // 禁止编辑
        messageArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageArea.setLineWrap(true);      // 启用自动换行
        messageArea.setWrapStyleWord(true); // 按单词边界换行（避免截断单词）
        JScrollPane scrollPane =new JScrollPane(
                messageArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        // 底部输入面板
         inputPanel = new JPanel(new BorderLayout(10, 10));

        inputPanel.add(inputField, BorderLayout.CENTER);

        inputPanel.add(sendButton, BorderLayout.EAST);

        // 组装主面板
        chatPanel.add(topPanel, BorderLayout.NORTH);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

//        // 事件监听
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               String buttonText =sendButton.getText();
                if (buttonText.equals(Contant.SEND)) {
                    String prompt = inputField.getText().trim();
                    sendButton.setText(Contant.STOP);
                    sendMessage(prompt);
                }else{
                    sendButton.setText(Contant.SEND);
                    appThread.interrupt();
                }

            }
        });

        cleanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(() -> messageArea.setText("")); //

            }
        });

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

             appThread = new Thread() {
                public void run() {
                    OllamaChatRequest request = new OllamaChatRequest();
                    List<OllamaChatMessage> messages = new ArrayList<>();

                    messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, lastRequestTxt));
                    messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));
                    request.setMessages(messages); // 必须包含消息列表
                    lastRequestTxt=prompt;


//                    OllamaChatOptions options = OllamaChatOptions.create()
//                            .withTemperature(0.4)
//                            .withMaxTokens(500); // 可选：限制生成长度

                    try {
                        OllamaClient.chatStreaming(request, new OllamaTokenHandler() {
                            @Override
                            public void accept(OllamaChatResponseModel ollamaChatResponseModel) {

                                if (ollamaChatResponseModel.isDone()){
                                    sendButton.setText(Contant.SEND);
                                }else{

                                    writeMsg(ollamaChatResponseModel.getMessage().getContent());
                                    System.out.println(ollamaChatResponseModel.getMessage().getContent());
                                }
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