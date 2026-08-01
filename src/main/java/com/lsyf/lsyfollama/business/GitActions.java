package com.lsyf.lsyfollama.business;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.util.ArrayList;
import java.util.List;

public class GitActions {

    public static List<VirtualFile> changesToVirtualFiles(Change[] changes) {
        List<VirtualFile> files = new ArrayList<>();
        for (Change change : changes) {
            VirtualFile vf = change.getVirtualFile();
            if (vf != null) {
                files.add(vf);
            }
        }
        return files;
    }
    public static String getGitDiff(Project project, VirtualFile file) {
        if (project == null || file == null) return "";

        GitRepositoryManager manager = GitRepositoryManager.getInstance(project);
        GitRepository repo = manager.getRepositoryForFileQuick(file);
        if (repo == null) return "";

        GitLineHandler handler = new GitLineHandler(
                project,
                repo.getRoot(),
                GitCommand.DIFF
        );

        handler.addParameters("--cached");     // 已 staged（即将提交）
        handler.addParameters("--no-color");
        handler.addParameters(file.getPath());

        // ✅ 正确执行方式
        GitCommandResult result = Git.getInstance().runCommand(handler);

        if (result.success()) {
            return result.getOutputAsJoinedString();
        } else {
            return "Git error: " + result.getErrorOutputAsJoinedString();
        }
    }


}