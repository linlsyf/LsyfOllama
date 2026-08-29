package com.lsyf.lsyfollama.ui.Activity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 项目级启动活动 - 每个项目打开时执行一次
 */
public class MyPluginStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        // 在项目初始化完成后执行
        ApplicationManager.getApplication()
                .getMessageBus()
                .connect(project)
                .subscribe(FileDocumentManagerListener.TOPIC, new FileContentListener());
        ApplicationManager.getApplication()
                .getMessageBus()
                .connect(project)
                .subscribe(FileDocumentManagerListener.TOPIC, new FileContentListener());

        System.out.println("Plugin started for project: " + project.getName());
    }
}