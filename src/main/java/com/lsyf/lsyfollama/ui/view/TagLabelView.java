package com.lsyf.lsyfollama.ui.view;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import com.lsyf.lsyfollama.constant.ProjectInitData;
import com.lsyf.lsyfollama.evenbus.BusMessage;
import com.lsyf.lsyfollama.evenbus.FileSelectChangeListener;
import com.lsyf.lsyfollama.utils.DiffPreviewUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Data
@EqualsAndHashCode(callSuper = false)  // ← 明确忽略父类字段
public class TagLabelView extends JPanel {
  JScrollPane jScrollPane;

  JScrollPane tagScrollPane;
  JTextField inputField;
  String[] commonTags = {
      "accept code", "repair code", "口语练习", "听力技巧",
      "阅读理解", "写作模板", "词汇积累", "发音纠正",
      "商务英语", "旅游英语", "考试技巧", "每日一句"
  };

  public JScrollPane createFilsList(JTextField inputField) {
    this.inputField = inputField;
    tagScrollPane = initScrooller();
    initListener();

    return tagScrollPane;
  }
  public JScrollPane initScrooller() {
    // 创建内容面板
    JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    tagPanel.setBackground(new Color(248, 249, 250));
    tagPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

    // 添加标签
    for (String tag : commonTags) {
      JLabel tagLabel = TagLabelView.createTagLabel(tag, inputField);
      tagPanel.add(tagLabel);
    }

    if (tagScrollPane == null) {
      // ★ 第一次：创建
      tagScrollPane = new JScrollPane(
          tagPanel,
          JScrollPane.VERTICAL_SCROLLBAR_NEVER,
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
      );
      tagScrollPane.setPreferredSize(new Dimension(0, 45));
      tagScrollPane.setBorder(BorderFactory.createEmptyBorder());
      tagScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
      tagScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
    } else {
      // ★ 后续更新：只换内容，不碰 JScrollPane 本身
      tagScrollPane.setViewportView(tagPanel);
    }
    return  tagScrollPane;
  }

  private void initListener() {
    // 订阅消息
    MessageBusConnection connection = ProjectInitData.getInstance().getProject().getMessageBus().connect();

    connection.subscribe(FileSelectChangeListener.TOPIC, new FileSelectChangeListener() {
      @Override
      public void onMessage(BusMessage busMessage ) {
        VirtualFile virtualFile=busMessage.getVirtualFile();
        Document currentDocument = FileDocumentManager.getInstance().getDocument(virtualFile);
        String str = currentDocument.getText();
//        String str = currentDocument.getText();
        String fileName = virtualFile.getName();

//        monitorPanel.updateContent(event.getFilePath(), event.getContent());
//     String str=event.getFilePath()+(event.getContent());
        System.out.println("select change2 ========" + str + "====================");
        // 预定义常用标签

        ApplicationManager.getApplication().invokeLater(() -> {
          commonTags = new String[]{virtualFile.getName(),busMessage.getLine()+"行"};
//          commonTags = new String[]{str};
          initScrooller();              // 重新创建/填充内部组件
          tagScrollPane.revalidate();   // 通知布局管理器重新布局
          tagScrollPane.repaint();      // 触发重绘
        });
      }
    });
    // 项目关闭时自动断开连接
    Disposer.register(ProjectInitData.getInstance().getProject(), connection);

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

        if (text.equals("accept code")) {
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
