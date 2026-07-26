package com.lsyf.lsyfollama.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

public class ReadPsiFilesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        FileEditorManager manager = FileEditorManager.getInstance(project);
        VirtualFile[] openFiles = manager.getOpenFiles();

        StringBuilder result = new StringBuilder();

        for (VirtualFile vFile : openFiles) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(vFile);
            if (psiFile != null) {
                result.append("PSI File: ").append(psiFile.getName()).append("\n");
                result.append("Language: ").append(psiFile.getLanguage().getID()).append("\n");
                result.append("Text length: ").append(psiFile.getTextLength()).append("\n");

                // 获取PSI树结构
                psiFile.accept(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        // 遍历PSI元素
                        super.visitElement(element);
                    }
                });
            }
        }

        // 显示结果
        Messages.showMessageDialog(project, result.toString(), "PSI Files Info", Messages.getInformationIcon());
    }
}