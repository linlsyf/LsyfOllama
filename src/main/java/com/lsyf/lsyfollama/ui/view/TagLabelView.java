package com.lsyf.lsyfollama.ui.view;

import com.lsyf.lsyfollama.utils.DiffPreviewUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TagLabelView {

  public static JScrollPane createFilsList(JTextField inputField) {
    JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    tagPanel.setBackground(new Color(248, 249, 250));
    tagPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

// 预定义常用标签
    String[] commonTags = {
        "accept code", "repair code", "口语练习", "听力技巧",
        "阅读理解", "写作模板", "词汇积累", "发音纠正",
        "商务英语", "旅游英语", "考试技巧", "每日一句"
    };

// 添加标签到面板
    for (String tag : commonTags) {
      JLabel tagLabel = TagLabelView.createTagLabel(tag, inputField);
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
    return tagScrollPane;
  }

  public static JLabel createTagLabel(String text, JTextField inputField) {
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

        if (text.equals("accept code")){
          DiffPreviewUtil.show("test");


//          MyViewNotifier publisher = ProjectInitData.getInstance().getProject().getMessageBus()
//              .syncPublisher(MyViewNotifier.MY_VIEW_TOPIC);
////              .syncPublisher(MyViewNotifier.MY_VIEW_TOPIC);
//          publisher.onViewMessage("Hello from View!", ProjectInitData.getInstance().getProject());

//          ProjectInitData.getInstance().getProject().getMessageBus()
//              .syncPublisher(MyViewNotifier.MY_VIEW_TOPIC)
//              .onViewMessage("test", ProjectInitData.getInstance().getProject());
        }
      }
    });

    return label;
  }




}
