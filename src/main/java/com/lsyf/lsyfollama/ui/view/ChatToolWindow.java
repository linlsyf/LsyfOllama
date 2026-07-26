package com.lsyf.lsyfollama.ui.view;

import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.constant.Contant;
import com.lsyf.lsyfollama.constant.OllamaClientUtils;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResponseModel;
import io.github.ollama4j.models.chat.OllamaChatTokenHandler;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        chatPanel.add(scrollPane, BorderLayout.CENTER);

// 创建南部容器，包含标签面板和输入面板
        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.add(createFilsList(), BorderLayout.NORTH);  // 标签在上方
        southContainer.add(inputPanel, BorderLayout.CENTER);    // 输入框在下方
        chatPanel.add(inputPanel, BorderLayout.SOUTH);


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

//                   		const punctuation = [
//                    '.',
//                            '。',  '!', '?', ';', ':', '"', "'",
//                            '(', ')', '[', ']', '{', '}', '<', '>', '/', '\\',
//                            '、', '！', '？', '；', '：', '「', '」', '『', '』', '《', '》'
//		];
//
//		/ollama/stream
                    try {
                        OllamaClientUtils.chatStreaming(request, new OllamaChatTokenHandler() {
                            @Override
                            public void accept(OllamaChatResponseModel ollamaChatResponseModel) {

                                if (ollamaChatResponseModel.isDone()){
                                    sendButton.setText(Contant.SEND);
                                }else{

                                String thinking=    ollamaChatResponseModel.getMessage().getThinking();
                                String response=    ollamaChatResponseModel.getMessage().getResponse();
                                String  writeText;
                                 if(StringUtils.isNoneBlank(thinking)){
                                     writeText=thinking;
                                 }else{
                                     writeText=response;
                                 }
                                   writeMsg(writeText);

                                    System.out.println(writeText);
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

   private  JScrollPane  createFilsList(){
       JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
       tagPanel.setBackground(new Color(248, 249, 250));
       tagPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

// 预定义常用标签
       String[] commonTags = {
               "如何记单词", "英语语法", "口语练习", "听力技巧",
               "阅读理解", "写作模板", "词汇积累", "发音纠正",
               "商务英语", "旅游英语", "考试技巧", "每日一句"
       };

// 添加标签到面板
       for (String tag : commonTags) {
           JLabel tagLabel = createTagLabel(tag);
           tagPanel.add(tagLabel);
       }

// 创建横向滚动面板
       JScrollPane tagScrollPane = new JScrollPane(
               tagPanel,
               JScrollPane.VERTICAL_SCROLLBAR_NEVER,
               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
       );
       tagScrollPane.setPreferredSize(new Dimension(0, 45));
       tagScrollPane.setBorder(BorderFactory.createEmptyBorder());
       tagScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
       tagScrollPane.getHorizontalScrollBar().setUnitIncrement(20); // 平滑滚动
       return   tagScrollPane;
   }


    private JLabel createTagLabel(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setForeground(new Color(73, 80, 87));
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        // 鼠标悬停效果
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setBackground(new Color(233, 236, 239));
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setBackground(Color.WHITE);
                label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                inputField.setText(text);
                inputField.requestFocus(); // 焦点移到输入框
            }
        });

        return label;
    }
    private void writeMsg(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message));
    }

    // 返回主面板（供IDEA插件集成）
    public JPanel getContent() {
        return chatPanel;
    }

}