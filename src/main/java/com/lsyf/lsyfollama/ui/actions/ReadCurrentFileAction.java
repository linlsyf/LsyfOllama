package com.lsyf.lsyfollama.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class ReadCurrentFileAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (project == null || editor == null) {
            Messages.showErrorDialog(project, "No active editor found", "Error");
            return;
        }

        // 方法1：从Editor获取Document
        Document document = editor.getDocument();
        String text = document.getText();

        // 方法2：从VirtualFile获取内容
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (virtualFile != null) {
            try {
                String fileContent = new String(virtualFile.contentsToByteArray(), virtualFile.getCharset());
                // 使用内容...
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // 显示内容（示例）
        Messages.showMessageDialog(
                project,
                "File content length: " + text.length() + "\nFirst 100 chars: " +
                        (text.length() > 100 ? text.substring(0, 100) + "..." : text),
                "Current File Content",
                Messages.getInformationIcon()
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只在有打开的编辑器时启用此Action
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }
}