package com.lsyf.lsyfollama;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public class FileReaderUtil {

    /**
     * 获取当前活动的VirtualFile
     */
    @Nullable
    public static VirtualFile getCurrentFile(Project project) {
        if (project == null) return null;

        var editor = com.intellij.openapi.fileEditor.FileEditorManager
                .getInstance(project).getSelectedTextEditor();

        if (editor == null) return null;

        Document document = editor.getDocument();
        return FileDocumentManager.getInstance().getFile(document);
    }

    /**
     * 读取文件内容为字符串
     */
    @Nullable
    public static String readFileContent(VirtualFile file) {
        if (file == null || file.isDirectory()) return null;

        try {
            return new String(file.contentsToByteArray(), file.getCharset());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前编辑器选中文本
     */
    @Nullable
    public static String getSelectedText(Project project) {
        if (project == null) return null;

        var editor = com.intellij.openapi.fileEditor.FileEditorManager
                .getInstance(project).getSelectedTextEditor();

        if (editor == null) return null;

        var selectionModel = editor.getSelectionModel();
        return selectionModel.getSelectedText();
    }
}