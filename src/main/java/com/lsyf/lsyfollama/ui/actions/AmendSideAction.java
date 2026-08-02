package com.lsyf.lsyfollama.ui.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.lsyf.lsyfollama.business.genCode.GitMsgCommit;

public class AmendSideAction extends AnAction {
  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(true);
    // 只有 Git 项目才启用
//        Project project = e.getProject();
//        boolean visible = project != null
//                && GitUtil.getRepositories(project).stream()
//                .anyMatch(r -> r.getCurrentRevision() != null);
//        e.getPresentation().setEnabledAndVisible(visible);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
//    Project project = e.getProject();
//        if (project == null) return;
    GitMsgCommit.setCommit(e);
  }

  @Override
  public ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }
}