package com.lsyf.lsyfollama.ui.view;

import lombok.Data;

import javax.swing.*;

@Data
public class SettingView {

    private JPanel mainPanel;

    private JTextField inputField;
    private JTextField commitReceiptFied;
    private JTextField commitRejectFied;
    private JTextField modelField;

    public void init() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        inputField = new JTextField();
        modelField = new JTextField();
        commitReceiptFied = new JTextField();
        commitRejectFied = new JTextField();

        // 添加组件到面板，按上下顺序排列
        mainPanel.add(inputField);
        mainPanel.add(modelField);
        mainPanel.add(commitReceiptFied);
        mainPanel.add(commitRejectFied);
    }

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
}
