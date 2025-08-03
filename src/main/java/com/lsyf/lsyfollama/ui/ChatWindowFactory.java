package com.lsyf.lsyfollama.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ChatWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
//        SwingUtilities.invokeLater(() -> { // 确保在 EDT 线程
            ChatToolWindow chatTool = new ChatToolWindow();
            JPanel panel = chatTool.getContent();
            toolWindow.getComponent().add(panel);
            panel.revalidate(); // 强制刷新布局
//        });
        toolWindow.show(); // 显示窗口


    }
}