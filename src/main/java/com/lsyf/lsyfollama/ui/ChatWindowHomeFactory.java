package com.lsyf.lsyfollama.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.lsyf.lsyfollama.ToolWindowService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ChatWindowHomeFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ChatToolWindow chatTool = new ChatToolWindow();

        // 注册到服务
        ToolWindowService service = project.getService(ToolWindowService.class);
        service.registerPanel(chatTool);

        JPanel panel = chatTool.getContent();
        toolWindow.getComponent().add(panel);
        panel.revalidate(); // 强制刷新布局
//        });
        toolWindow.show(); // 显示窗口

    }
}