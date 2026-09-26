package com.lsyf.lsyfollama.business.actions;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.lsyf.lsyfollama.business.ContextMenuLogic;

public class ContextMenuRepairAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ContextMenuLogic.consumerContextMenu(e,this);
    }
}