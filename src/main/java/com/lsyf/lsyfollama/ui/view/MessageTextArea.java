package com.lsyf.lsyfollama.ui.view;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupMenuListenerAdapter;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.TextTransferable;
import com.intellij.util.ui.UIUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = false)  // ← 明确忽略父类字段
public class MessageTextArea extends JTextArea {
    private static final Color BG_PANEL = JBColor.namedColor("Panel.background", Color.WHITE);
    private static final Color BG_USER_BUBBLE = JBColor.namedColor("ActionButton.hoverBackground", new Color(220, 245, 255));
    private static final Color BG_AI_BUBBLE = JBColor.namedColor("EditorPane.background", new Color(240, 240, 240));
    private static final Color BG_ACCEPTED = JBColor.namedColor("TestCase.passedBackground", new Color(225, 255, 225));
    private static final Color TEXT_COLOR = JBColor.namedColor("Label.foreground", Color.BLACK);
    private static final Color BORDER_COLOR = JBColor.namedColor("Border.color", new Color(220, 220, 220));

    public MessageTextArea() {
        init();
    }

    public void init(){
        this.setFont(JBUI.Fonts.label().deriveFont(JBUI.scaleFontSize(14f)));
        this.setLineWrap(true);
        this.setWrapStyleWord(true);
        this.setEditable(false);
        this.setBackground(BG_AI_BUBBLE);
        this.setForeground(TEXT_COLOR);
        this.setBorder(JBUI.Borders.empty(8, 12));
        this.setOpaque(true);

        int preferredWidth = JBUI.scale(400);
        this.setSize(new Dimension(preferredWidth, Short.MAX_VALUE));
        this.setPreferredSize(new Dimension(preferredWidth,
                this.getPreferredSize().height));



        this.setEditable(false);   // 回复区只读，Ctrl+C 仍可用
        this.setLineWrap(true);
        this.setWrapStyleWord(true);
        this.setOpaque(true);
        this.setBackground(UIUtil.getTextFieldBackground());
        // 深色/浅色主题切换时背景能跟着变
        this.putClientProperty("JTextArea.infoBackground", Boolean.TRUE);

        installCopyActions(this);
    }



        private static void installCopyActions(JTextArea area) {
            JPopupMenu popup = new JPopupMenu();

            JMenuItem copySelection = new JMenuItem("Copy Selection");
            copySelection.addActionListener(e -> copy(area.getSelectedText()));

            JMenuItem copyAll = new JMenuItem("Copy All");
            copyAll.addActionListener(e -> copy(area.getText()));

            popup.add(copySelection);
            popup.add(copyAll);

            area.setComponentPopupMenu(popup);   // 比手写 MouseListener 更简洁

            // 只在有选中时才启用 "Copy Selection"
            popup.addPopupMenuListener(new PopupMenuListenerAdapter() {
                @Override
                public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                    String sel = area.getSelectedText();
                    copySelection.setEnabled(sel != null && !sel.isBlank());
                }
            });
        }

        /**
         * Copies text to the IDE clipboard.
         * Uses CopyPasteManager so the text also lands in the IDE's paste history.
         */
        private static void copy(String text) {
            if (text == null || text.isBlank()) return;
            CopyPasteManager.getInstance().setContents(new TextTransferable(text));
        }

    
}
