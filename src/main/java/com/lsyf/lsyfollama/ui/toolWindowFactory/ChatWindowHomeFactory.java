package com.lsyf.lsyfollama.ui.toolWindowFactory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.lsyf.lsyfollama.ToolWindowService;
import com.lsyf.lsyfollama.constant.ProjectInitData;
import com.lsyf.lsyfollama.ui.view.ChatRootView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ChatWindowHomeFactory implements ToolWindowFactory {
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    ProjectInitData.getInstance().setProject(project);
    ChatRootView chatTool = new ChatRootView(project);
    chatTool.setProject(project);

    // 注册到服务
    ToolWindowService service = project.getService(ToolWindowService.class);
    service.registerPanel(chatTool);

    JPanel contentRootPanl = chatTool.getChatPanel();
    toolWindow.getComponent().add(contentRootPanl);
    contentRootPanl.revalidate(); // 强制刷新布局
    toolWindow.show(); // 显示窗口



  }
}