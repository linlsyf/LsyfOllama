package com.lsyf.lsyfollama.ui;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.lsyf.lsyfollama.business.ContextMenuLogic;
import com.lsyf.lsyfollama.constant.OllamaClient;

public class ContextMenuSearchAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ContextMenuLogic.consumerContextMenu(e,this);
    }
}