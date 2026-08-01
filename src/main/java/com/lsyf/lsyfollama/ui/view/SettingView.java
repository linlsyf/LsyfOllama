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

     public   void  init(){
         mainPanel= new JPanel(new BorderLayout());

         inputField = new JTextField();

         modelField = new JTextField();
         commitReceiptFied = new JTextField();
         commitRejectFied = new JTextField();
         mainPanel.add(inputField, BorderLayout.CENTER);
         mainPanel.add(modelField, BorderLayout.SOUTH);
         mainPanel.add(commitReceiptFied, BorderLayout.SOUTH);
         mainPanel.add(commitRejectFied, BorderLayout.SOUTH);

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
