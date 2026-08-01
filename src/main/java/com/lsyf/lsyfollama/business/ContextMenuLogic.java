package com.lsyf.lsyfollama.business;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.vcs.commit.CommitMessageUi;
import com.intellij.vcs.commit.CommitWorkflowUi;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.ToolWindowService;
import com.lsyf.lsyfollama.constant.Contant;
import com.lsyf.lsyfollama.constant.OllamaClientUtils;
import com.lsyf.lsyfollama.ui.view.ChatToolWindow;
import org.jetbrains.annotations.NotNull;

import static com.lsyf.lsyfollama.ChatConstant.ChatToolWindow_ID;


public class ContextMenuLogic {

    public static void consumerContextMenu(AnActionEvent e, AnAction action) {
        String actionId = ActionManager.getInstance().getId(action);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null) {
            FileType fileType = psiFile.getFileType();
            String languageId = fileType.getName(); // 如 "JAVA", "XML"
            if (languageId.contains(ChatConstant.JAVA_FILE) || languageId.contains(ChatConstant.PYTHON_File)) {
                ChatConstant.DEV_LAN = languageId;
            }
        }

        if (Contant.genCode.equals(actionId)) {
            String actionDesc = "生成" + ChatConstant.DEV_LAN + " 代码：\n";

            genCode(e, action, actionDesc);
        } else if (Contant.linlsyfQuery.equals(actionId)) {
            query(e, action);
        } else if (Contant.apiTest.equals(actionId)) {
//            String actionDesc="修复"+ ChatConstant.DEV_LAN+" 代码的错误：\n" ;
//            genCode(e,action,actionDesc);
            Messages.showInfoMessage(ChatConstant.OPEN_RIGHT_PANEL, ChatConstant.DEV_ING);

        }
    }

    private static void query(AnActionEvent e, AnAction action) {
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
        ChatToolWindow chatTool = service.getCustomPanel();
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

    public static void setCommit(AnActionEvent e) {

        Project project = e.getProject();


        String selectedText = "test";

        Change[] changes = GitDiffUtil.getCommitChanges(e);
//        Change[] changes = e.getData(VcsDataKeys.CHANGES);


//        // 兜底：直接取 COMMIT_WORKFLOW_UI（社区实测可用 [6](@ref)）
        CommitWorkflowUi workflowUi = e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI);
        if (workflowUi == null) {
            Messages.showInfoMessage("messageUi  is  null", "Debug");
            return;
        }

//
//// 优先使用 CommitMessageUi（新 API）
        CommitMessageUi messageUi = workflowUi.getCommitMessageUi();


        ProgressManager.getInstance().run(
                new Task.Backgroundable(project, "Generating Commit Message", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        indicator.setIndeterminate(false);

                        // 1. 拿 diff
                        String diff = GitDiffUtil.getStagedDiff(project, changes, indicator);
                        if (diff.isBlank()) {
//                            notify("No staged changes found");
                            return;
                        }

                        // 2. 调用 AI
//                        String aiMessage = callAI(diff, indicator);

                        // 3. 写回提交框（切回 UI 线程）
                        ApplicationManager.getApplication().invokeLater(() -> {
//                            commitPanel.setCommitMessage(aiMessage);


//                            String oldMsg = messageUi.getText();
//                            String newMsg = oldMsg.isEmpty() ? selectedText : oldMsg + "\n" + selectedText;
                            messageUi.setText(diff);
                        });
                    }
                }
        );


    }

    private static CommitWorkflowUi resolveCommitWorkflowUi(AnActionEvent e) {
        CommitWorkflowUi ui = e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI);
        if (ui != null) return ui;

//         2. Handler（公开接口，非 Abstract）
//        Object handler = e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
//        if (handler instanceof CommitWorkflowHandler) {
//            ui = ((CommitWorkflowHandler) handler).getAmendCommitHandler();
//            if (ui != null) return ui;
//        }

        // 3. Editor 反推（2025.2 Non-Modal 唯一可靠路径）
//        Editor editor = e.getData(CommonDataKeys.EDITOR);
//        if (editor != null && isCommitMessageEditor(editor)) {
//            ui = editor.getUserData(VcsDataKeys.COMMIT_WORKFLOW_UI_KEY);
//            if (ui != null) return ui;
//
//            // 尝试从 Project 找（兜底）
//            Project project = e.getProject();
//            if (project != null) {
//                return project.getUserData(COMMIT_WORKFLOW_UI_KEY);
//            }
//        }

        return null;
    }

    private static boolean isCommitMessageEditor(@NotNull Editor editor) {
        VirtualFile file = editor.getVirtualFile();
        return file != null && "commit-message.txt".equals(file.getName());
    }

    private static void genCode(AnActionEvent e, AnAction action, String actionDesc) {
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
        // 执行替换（线程安全）
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.insertString(lineEndOffset, "\n" + newText); // 插入下一行[6](@ref)

        });

    }
}
