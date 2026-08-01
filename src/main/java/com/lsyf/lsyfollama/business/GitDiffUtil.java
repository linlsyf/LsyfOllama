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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GitDiffUtil {

    private static String generateGitSummary(String diff) {
        StringBuilder summary = new StringBuilder();
        Set<String> addedFiles = new HashSet<>();
        Set<String> modifiedFiles = new HashSet<>();
        Set<String> deletedFiles = new HashSet<>();
        Set<String> renamedFiles = new HashSet<>();

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
                if (content.contains("new") || content.contains("add") ||
                        content.contains("implement") || content.contains("support") ||
                        content.contains("feature") || content.contains("enable")) {
                    changes.add("Add new functionality");
                }

                // Bug fixes
                if (content.contains("fix") || content.contains("bug") ||
                        content.contains("issue") || content.contains("error") ||
                        content.contains("crash") || content.contains("null") ||
                        content.contains("exception") || content.contains("patch")) {
                    changes.add("Fix bugs");
                }

                // Performance improvements
                if (content.contains("optimize") || content.contains("performance") ||
                        content.contains("speed") || content.contains("fast") ||
                        content.contains("slow") || content.contains("cache") ||
                        content.contains("memory") || content.contains("leak")) {
                    changes.add("Improve performance");
                }

                // Refactoring
                if (content.contains("refactor") || content.contains("clean") ||
                        content.contains("reorganize") || content.contains("restructure") ||
                        content.contains("simplify") || content.contains("extract") ||
                        content.contains("rename") || content.contains("move")) {
                    changes.add("Refactor code");
                }

                // Documentation
                if (content.contains("doc") || content.contains("comment") ||
                        content.contains("readme") || content.contains("javadoc") ||
                        content.contains("license") || content.contains("changelog")) {
                    changes.add("Update documentation");
                }

                // Testing
                if (content.contains("test") || content.contains("assert") ||
                        content.contains("mock") || content.contains("stub") ||
                        content.contains("spec") || content.contains("jest") ||
                        content.contains("junit") || content.contains("pytest")) {
                    changes.add("Update tests");
                }

                // Configuration/Dependencies
                if (content.contains("config") || content.contains("dependency") ||
                        content.contains("version") || content.contains("upgrade") ||
                        content.contains("downgrade") || content.contains("package") ||
                        content.contains("gradle") || content.contains("maven") ||
                        content.contains("npm") || content.contains("yarn") ||
                        content.contains("docker") || content.contains("k8s")) {
                    changes.add("Update configuration");
                }

                // Security
                if (content.contains("security") || content.contains("vulnerability") ||
                        content.contains("auth") || content.contains("password") ||
                        content.contains("token") || content.contains("encrypt") ||
                        content.contains("decrypt") || content.contains("hash") ||
                        content.contains("ssl") || content.contains("tls")) {
                    changes.add("Enhance security");
                }

                // UI/UX changes
                if (content.contains("ui") || content.contains("ux") ||
                        content.contains("style") || content.contains("css") ||
                        content.contains("layout") || content.contains("button") ||
                        content.contains("color") || content.contains("theme") ||
                        content.contains("responsive") || content.contains("mobile")) {
                    changes.add("Update user interface");
                }

                // Logging/Monitoring
                if (content.contains("log") || content.contains("monitor") ||
                        content.contains("metric") || content.contains("trace") ||
                        content.contains("debug") || content.contains("info") ||
                        content.contains("warn") || content.contains("error")) {
                    changes.add("Enhance logging");
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