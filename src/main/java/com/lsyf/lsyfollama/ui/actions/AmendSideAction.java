package com.lsyf.lsyfollama.ui.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.lsyf.lsyfollama.business.ContextMenuLogic;

public class AmendSideAction extends AnAction {
    @Override
    public void update( AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
        // 只有 Git 项目才启用
//        Project project = e.getProject();
//        boolean visible = project != null
//                && GitUtil.getRepositories(project).stream()
//                .anyMatch(r -> r.getCurrentRevision() != null);
//        e.getPresentation().setEnabledAndVisible(visible);
    }


    @Override
    public void actionPerformed( AnActionEvent e) {
        Project project = e.getProject();
//        if (project == null) return;
        ContextMenuLogic.setCommit(e);
        // 这里写你的按钮逻辑，比如触发 amend 流程
        // 例：调用 Git 的 amend
//        GitRepository repo = GitUtil.getRepositories(project).iterator().next();
        // ... 调起 amend 逻辑
    }

    @Override
    public  ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}