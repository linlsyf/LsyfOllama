package com.lsyf.lsyfollama.ui.Activity;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class FileContentListener implements FileDocumentManagerListener {

    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file != null) {
            System.out.println("Saving file: " + file.getPath());
            // 可以在保存前处理内容
        }
    }

    @Override
    public void fileWithNoDocumentChanged(@NotNull VirtualFile file) {
        System.out.println("File changed (no document): " + file.getPath());
    }
}

