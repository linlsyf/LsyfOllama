package com.lsyf.lsyfollama.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReadAllOpenFilesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();

        List<String> fileInfoList = new ArrayList<>();

        for (VirtualFile file : openFiles) {
            try {
                String content = new String(file.contentsToByteArray(), file.getCharset());
                fileInfoList.add(String.format("File: %s\nSize: %d bytes\nPath: %s\n---",
                        file.getName(),
                        content.length(),
                        file.getPath()));
            } catch (Exception ex) {
                fileInfoList.add("Error reading file: " + file.getName());
            }
        }

        String message = String.join("\n", fileInfoList);
        Messages.showMessageDialog(
                project,
                message.isEmpty() ? "No files open" : message,
                "All Open Files",
                Messages.getInformationIcon()
        );
    }
}