package com.lsyf.lsyfollama;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.lsyf.lsyfollama.ui.ChatToolWindow;

import javax.swing.*;

public class ToolWindowService {
    private final Project project;
    private ChatToolWindow customPanel;

    public ToolWindowService(Project project) {
        this.project = project;
    }

    public void registerPanel(ChatToolWindow panel) {
        this.customPanel = panel; // 注册组件
    }

    // 返回主面板（供IDEA插件集成）
    public JPanel getContent() {
        return customPanel.getContent();
    }

    public ChatToolWindow getCustomPanel() {
        return customPanel;
    }

    public void updateContent(String text) {
        ApplicationManager.getApplication().invokeLater(() -> {
//            if (customPanel != null) {
//                customPanel.updateText(text); // 线程安全更新
//            }
        });
    }
}