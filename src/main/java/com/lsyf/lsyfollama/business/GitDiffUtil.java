package com.lsyf.lsyfollama.business;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class GitDiffUtil {

    public static String getStagedDiff(
            @NotNull Project project,
            @NotNull Change[] changes,
            @NotNull ProgressIndicator indicator
    ) {
//        if (!GitUtil.isUnderGit(project)) return "";

        List<VirtualFile> files = Arrays.stream(changes)
                .map(Change::getVirtualFile)
                .filter(Objects::nonNull)
                .toList();

        if (files.isEmpty()) return "";

        GitRepository repo = GitRepositoryManager.getInstance(project)
                .getRepositoryForFileQuick(files.get(0));
        if (repo == null) return "";

        GitLineHandler handler = new GitLineHandler(
                project,
                repo.getRoot(),
                GitCommand.DIFF
        );

        handler.addParameters("--cached", "--no-color", "-U10");

        for (int i = 0; i < files.size(); i++) {
            indicator.setFraction((double) i / files.size());
            handler.addParameters(files.get(i).getPath());
        }

        GitCommandResult result = Git.getInstance().runCommand(handler);
        return result.success()
                ? result.getOutputAsJoinedString()
                : "";
    }




    public static String buildPromptFromChanges(Change[] changes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the code changes:\n\n");

        int fileIndex = 1;
        for (Change change : changes) {
            String filePath = getFilePath(change);
            sb.append("=== File ").append(fileIndex++).append(": ")
                    .append(filePath).append(" (").append(change.getType()).append(") ===\n");

            ContentRevision before = change.getBeforeRevision();
            ContentRevision after = change.getAfterRevision();

            String beforeContent = before != null ? safeGetContent(before) : "";
            String afterContent = after != null ? safeGetContent(after) : "";

            if (!beforeContent.isEmpty()) {
                sb.append("--- BEFORE ---\n").append(beforeContent).append("\n");
            }
            if (!afterContent.isEmpty()) {
                sb.append("+++ AFTER +++\n").append(afterContent).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String getFilePath(Change change) {
        ContentRevision rev = change.getAfterRevision() != null
                ? change.getAfterRevision()
                : change.getBeforeRevision();
        return rev != null ? rev.getFile().getPath() : "unknown";
    }

    private static String safeGetContent(ContentRevision rev) {
        try {
            String content = rev.getContent();
            return content != null ? content : "";
        } catch (Exception e) {
            return ""; // 二进制文件等会抛异常，直接忽略
        }
    }
    public static Change[] getCommitChanges(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return new Change[0];

        // ✅ 1. SELECTED_CHANGES（新 UI 最稳）
        Change[] changes = e.getData(VcsDataKeys.SELECTED_CHANGES);
        if (changes != null && changes.length > 0) {
            return changes;
        }

        // ✅ 2. CHANGES（兜底）
        changes = e.getData(VcsDataKeys.CHANGES);
        if (changes != null && changes.length > 0) {
            return changes;
        }

//        // ✅ 3. CheckinProjectPanel（模态提交框）
//        CheckinProjectPanel panel = e.getData(CheckinProjectPanel.PANEL);
//        if (panel != null) {
//            Collection<Change> selected = panel.getSelectedChanges();
//            if (selected != null && !selected.isEmpty()) {
//                return selected.toArray(new Change[0]);
//            }
//        }

        // ✅ 4. 全局 Default Changelist（最后兜底）
        ChangeListManager manager = ChangeListManager.getInstance(project);
        Collection<Change> defaultChanges = manager.getDefaultChangeList().getChanges();
        if (!defaultChanges.isEmpty()) {
            return defaultChanges.toArray(new Change[0]);
        }

        return new Change[0];
    }



}