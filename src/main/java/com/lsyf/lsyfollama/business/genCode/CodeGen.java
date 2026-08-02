package com.lsyf.lsyfollama.business.genCode;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.lsyf.lsyfollama.constant.OllamaClientUtils;
import com.lsyf.lsyfollama.utils.DiffPreviewUtil;

public class CodeGen {

  public static void genCode(AnActionEvent e, AnAction action, String actionDesc) {
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (editor == null) return; // 确保编辑器存在
    Project project = e.getProject();
    if (project == null) return; // 确保项目存在
    Document document = editor.getDocument();
//        String fullText = document.getText(); // 读取整个文件内容

// 读取选中文本（若有）
    SelectionModel selectionModel = editor.getSelectionModel();
    String selectedText = selectionModel.getSelectedText();

    if (StringUtil.isEmpty(selectedText)) {
      CaretModel caretModel = editor.getCaretModel(); // 获取光标模型

      int caretOffset = caretModel.getOffset(); // 光标在文档中的偏移量

      int lineNumber = document.getLineNumber(caretOffset); // 当前行号（从0开始）
      int lineStartOffset = document.getLineStartOffset(lineNumber); // 行起始偏移量
      int lineEndOffset = document.getLineEndOffset(lineNumber); // 行结束偏移量
      selectedText = document.getText().substring(lineStartOffset, lineEndOffset); // 当前行文本
    }
//        String prompt = actionDesc+ ChatConstant.DEV_LAN+" 代码的错误：\n" + selectedText + "  仅输出代码，不要任何解释、注释或额外文本。\n" +
//                "输出格式要求：纯代码，无换行符(\\n)或描述 \n";
    String prompt = actionDesc + selectedText + "  仅输出代码，不要任何解释、注释或额外文本。\n" +
        "输出格式要求：纯代码，无换行符(\\n)或描述 \n";

    final String newText = OllamaClientUtils.processText(prompt); // 自定义替换逻辑
    int endOffset = selectionModel.getSelectionEnd();
    int lineEndOffset = document.getLineEndOffset(document.getLineNumber(endOffset));


    DiffPreviewUtil.showDiffPreview(
        project,
        "Code Generation Preview",
        "Original",
        "Generated",
        selectedText,
        newText,
        finalText -> {
          WriteCommandAction.runWriteCommandAction(project, () -> {
            int insertPos = document.getLineEndOffset(
                document.getLineNumber(Math.min(lineEndOffset, document.getTextLength())));
            document.insertString(insertPos, "\n" + finalText);
          });
        }
    );

  }
}
