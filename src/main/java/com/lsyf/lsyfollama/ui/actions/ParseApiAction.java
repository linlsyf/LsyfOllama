package com.lsyf.lsyfollama.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.lsyf.lsyfollama.business.ContextMenuLogic;

public class ParseApiAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ContextMenuLogic.consumerContextMenu(e,this);
    }

}
