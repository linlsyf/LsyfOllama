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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GitDiffUtil {

    private static String generateGitSummary(String diff) {
        StringBuilder summary = new StringBuilder();
        Set<String> addedFiles = new HashSet<>();
        Set<String> modifiedFiles = new HashSet<>();
        Set<String> deletedFiles = new HashSet<>();
        Set<String> renamedFiles = new HashSet<>();
 int i=0;
        // Track change statistics
        int linesAdded = 0;
        int linesRemoved = 0;

        String[] lines = diff.split("\n");
        String currentFile = null;

        for (String line : lines) {
            // Detect file changes
            if (line.startsWith("diff --git")) {
                String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    currentFile = parts[2].substring(2); // Remove "b/"
                }
            }
            // New file
            else if (line.startsWith("new file")) {
                if (currentFile != null) addedFiles.add(currentFile);
            }
            // Deleted file
            else if (line.startsWith("deleted file")) {
                if (currentFile != null) deletedFiles.add(currentFile);
            }
            // Renamed file
            else if (line.contains("rename from") || line.contains("rename to")) {
                if (currentFile != null) renamedFiles.add(currentFile);
            }
            // Modified file (fallback)
            else if (line.startsWith("+++") && currentFile != null &&
                    !addedFiles.contains(currentFile) &&
                    !deletedFiles.contains(currentFile) &&
                    !renamedFiles.contains(currentFile)) {
                modifiedFiles.add(currentFile);
            }
            // Count additions/deletions
            else if (line.startsWith("+") && !line.startsWith("+++")) {
                linesAdded++;
            }
            else if (line.startsWith("-") && !line.startsWith("---")) {
                linesRemoved++;
            }
        }

        // Build summary
        if (!addedFiles.isEmpty()) {
            summary.append("Add ").append(pluralize(addedFiles.size(), "file", "files"))
                    .append(formatFileList(addedFiles)).append("\n");
        }
        if (!modifiedFiles.isEmpty()) {
            summary.append("Update ").append(pluralize(modifiedFiles.size(), "file", "files"))
                    .append(formatFileList(modifiedFiles)).append("\n");
        }
        if (!deletedFiles.isEmpty()) {
            summary.append("Remove ").append(pluralize(deletedFiles.size(), "file", "files"))
                    .append(formatFileList(deletedFiles)).append("\n");
        }
        if (!renamedFiles.isEmpty()) {
            summary.append("Rename ").append(pluralize(renamedFiles.size(), "file", "files"))
                    .append(formatFileList(renamedFiles)).append("\n");
        }

        // Add statistics
        if (linesAdded > 0 || linesRemoved > 0) {
            summary.append("\n")
                    .append(linesAdded).append(" insertion")
                    .append(linesAdded != 1 ? "s" : "")
                    .append(", ")
                    .append(linesRemoved).append(" deletion")
                    .append(linesRemoved != 1 ? "s" : "");
        }

        // Fallback if no specific changes detected
        if (summary.length() == 0) {
            summary.append("Update codebase");
        }

        return summary.toString().trim();
    }

    private static String pluralize(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    private static String formatFileList(Set<String> files) {
        if (files.isEmpty()) return "";
        if (files.size() <= 3) {
            return ": " + String.join(", ", files);
        }
        return ": " + String.join(", ", files.stream().limit(3).toArray(String[]::new))
                + " and " + (files.size() - 3) + " more";
    }

    public static String getStagedDiff(
            @NotNull Project project,
//            @NotNull Change[] changes,
            @NotNull ProgressIndicator indicator
    ) {


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

        String gitmsg=buffer.toString();
      String result=  analyzeChanges(gitmsg);
//      String result=  generateGitSummary(gitmsg);

        return result;
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

    private static String analyzeChanges(String diff) {
        Set<String> changes = new LinkedHashSet<>();

        // Split into lines for analysis
        String[] lines = diff.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();

            // Skip diff metadata
            if (trimmed.startsWith("diff --git") ||
                    trimmed.startsWith("index ") ||
                    trimmed.startsWith("@@") ||
                    trimmed.startsWith("+++") ||
                    trimmed.startsWith("---") ||
                    trimmed.isEmpty()) {
                continue;
            }

            // Analyze actual code changes
            if (line.startsWith("+") || line.startsWith("-")) {
                String content = line.substring(1).toLowerCase();

                // Feature additions
                if (StringUtils.isNotBlank(content)&&!content.startsWith("import")){
                    changes.add("change "+content);
                }
            }
        }

        // Return summary
        if (changes.isEmpty()) {
            return "Update implementation";
        }

        // Combine changes naturally
        List<String> changeList = new ArrayList<>(changes);
        if (changeList.size() == 1) {
            return changeList.get(0);
        } else if (changeList.size() == 2) {
            return changeList.get(0) + " and " + changeList.get(1);
        } else {
            String last = changeList.remove(changeList.size() - 1);
            return String.join(", ", changeList) + ", and " + last;
        }
    }
}