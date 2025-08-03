package com.lsyf.lsyfollama.ui;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.lsyf.lsyfollama.OllamaClient;

public class ContextMenuHelloAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        OllamaClient.consumerContextMenu(e , this);
    }
}