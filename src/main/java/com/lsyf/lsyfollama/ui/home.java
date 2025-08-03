package com.lsyf.lsyfollama.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class home extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        //显示对话框并展示对应的信息
        Messages.showInfoMessage("这是新的开始！", "Hello");
    }
}
