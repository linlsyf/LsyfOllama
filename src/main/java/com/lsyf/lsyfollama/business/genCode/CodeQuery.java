package com.lsyf.lsyfollama.business.genCode;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.ToolWindowService;
import com.lsyf.lsyfollama.ui.view.ChatRootView;

import static com.lsyf.lsyfollama.ChatConstant.ChatToolWindow_ID;

public class CodeQuery {


  public static void query(AnActionEvent e, AnAction action) {
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (editor == null) return; // 确保编辑器存在
    Project project = e.getProject();
    if (project == null) return; // 确保项目存在
    Document document = editor.getDocument();

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
    String prompt = "解释以下" + ChatConstant.DEV_LAN + "代码：" + selectedText + "\n ";

    ToolWindowService service = project.getService(ToolWindowService.class);
    ChatRootView chatTool = service.getCustomPanel();
    if (null == chatTool) {

      ToolWindowManager manager = ToolWindowManager.getInstance(project);
      ToolWindow chatWindow = manager.getToolWindow(ChatToolWindow_ID);

      if (chatWindow != null) {

        chatWindow.show(); // 显示窗口
        chatWindow.activate(null, true); // 聚焦并展开
        chatTool = service.getCustomPanel();
        chatTool.sendMessage(prompt);
      } else {
        Messages.showInfoMessage(ChatConstant.OPEN_RIGHT_PANEL, ChatConstant.OPEN_RIGHT_PANEL);
        return;
      }
    } else {

      chatTool.sendMessage(prompt);
    }

  }
}
