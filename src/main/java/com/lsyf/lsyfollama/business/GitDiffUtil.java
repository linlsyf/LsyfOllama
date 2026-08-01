package com.lsyf.lsyfollama.business;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VfsUtilCore;
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


        StringBuilder buffer = new StringBuilder();

        // 获取 IDEA ChangeList 中的变更，然后获取每个文件的 diff
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        LocalChangeList defaultList = changeListManager.getDefaultChangeList();

        for (Change change : defaultList.getChanges()) {
            //            ContentRevision before = change.getBeforeRevision();
//            String changeType = change.getType().toString(); // MODIFICATION, ADD, DELETE
            ContentRevision afterRevision = change.getAfterRevision();
            if (afterRevision != null) {
                VirtualFile file = afterRevision.getFile().getVirtualFile();
                if (file != null) {
                    String relativePath = VfsUtilCore.getRelativePath(file, project.getBaseDir());
                    String diff = getFileDiff(project, relativePath, "HEAD");
                    buffer.append("Diff for ").append(relativePath).append(":\n").append(diff);
                }
            }
        }
        return buffer.toString();
    }

//
//    private static String getFilePath(Change change) {
//        ContentRevision rev = change.getAfterRevision() != null
//                ? change.getAfterRevision()
//                : change.getBeforeRevision();
//        return rev != null ? rev.getFile().getPath() : "unknown";
//    }
//
//    private static String safeGetContent(ContentRevision rev) {
//        try {
//            String content = rev.getContent();
//            return content != null ? content : "";
//        } catch (Exception e) {
//            return ""; // 二进制文件等会抛异常，直接忽略
//        }
//    }

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

    /**
     * 获取当前工作区与 HEAD 的差异
     */
    public String getDiffWithHead(Project project) {
        return getDiff(project, "HEAD", null);
    }

    /**
     * 获取两个提交之间的差异
     */
    public String getDiffBetweenCommits(Project project, String fromCommit, String toCommit) {
        return getDiff(project, fromCommit + ".." + toCommit, null);
    }

    /**
     * 获取指定文件的差异
     */
    public static String getFileDiff(Project project, String filePath, String reference) {
        return getDiff(project, reference, filePath);
    }

    private static String getDiff(Project project, String reference, String filePath) {
        VirtualFile root = project.getBaseDir();
        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.DIFF);

        if (reference != null) {
            handler.addParameters(reference);
        }

        if (filePath != null) {
            handler.addParameters("--", filePath);
        }

        GitCommandResult result = Git.getInstance().runCommand(handler);

        if (!result.success()) {
            throw new RuntimeException("Git diff failed: " + result.getErrorOutput());
        }

        return String.join("\n", result.getOutput());
    }

}