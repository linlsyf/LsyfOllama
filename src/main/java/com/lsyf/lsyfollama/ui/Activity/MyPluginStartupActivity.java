package com.lsyf.lsyfollama.ui.Activity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

// 注册监听器（在plugin.xml或代码中）
public class MyPluginStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        ApplicationManager.getApplication()
                .getMessageBus()
                .connect(project)
                .subscribe(FileDocumentManagerListener.TOPIC, new FileContentListener());
    }
}