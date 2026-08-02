package com.lsyf.lsyfollama.business.genCode;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.vcs.commit.CommitMessageUi;
import com.intellij.vcs.commit.CommitWorkflowUi;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.business.GitDiffUtil;
import com.lsyf.lsyfollama.constant.OllamaClientUtils;
import com.lsyf.lsyfollama.vo.DiffMsg;
import org.jetbrains.annotations.NotNull;

public class GitMsgCommit {




  public static void setCommit(AnActionEvent e) {

    Project project = e.getProject();

//        Change[] changes = GitDiffUtil.getCommitChanges(e);
//        Change[] changes = e.getData(VcsDataKeys.CHANGES);

//        // 兜底：直接取 COMMIT_WORKFLOW_UI（社区实测可用 [6](@ref)）
    CommitWorkflowUi workflowUi = e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI);
    if (workflowUi == null) {
      Messages.showInfoMessage("messageUi  is  null", "Debug");
      return;
    }

//
//// 优先使用 CommitMessageUi（新 API）
    CommitMessageUi messageUi = workflowUi.getCommitMessageUi();

    ProgressManager.getInstance().run(
        new Task.Backgroundable(project, "Generating Commit Message", true) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(false);

            // 1. 拿 diff
            DiffMsg diff = GitDiffUtil.getStagedDiff(project);
            if (diff.getResult().isBlank()) {
              return;
            }

            if (ChatConstant.isAiModeSave) {
              // 2. 调用 AI

              String prompt = diff.getGitmsg() + "  根据git信息 总结修改内容。\n" +
                  "输出格式要求：纯代码，无换行符(\\n)或描述 输入为英文内容 \n" +
                  "git信息如下: \n";

              prompt = prompt + prompt;
              final String newText = OllamaClientUtils.processText(prompt); // 自定义替换逻辑

              // 执行替换（线程安全）
              ApplicationManager.getApplication().invokeLater(() -> {

                messageUi.setText(newText);
              });

            } else {
              // 3. 写回提交框（切回 UI 线程）
              ApplicationManager.getApplication().invokeLater(() -> {
//                            commitPanel.setCommitMessage(aiMessage);

//                            String oldMsg = messageUi.getText();
//                            String newMsg = oldMsg.isEmpty() ? selectedText : oldMsg + "\n" + selectedText;
                messageUi.setText(diff.getResult());
              });
            }

          }
        }
    );

  }
}
