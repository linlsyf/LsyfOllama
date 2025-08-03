package com.lsyf.lsyfollama;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.lsyf.lsyfollama.ui.ChatToolWindow;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.generate.OllamaTokenHandler;

import java.util.ArrayList;
import java.util.List;

public class OllamaClient {
    static String apiUrl = PropertiesComponent.getInstance().getValue(
            ChatConstant.MY_PLUGIN_SETTING,
            "未設置" // 默认值
    );
    static String modelSetting = PropertiesComponent.getInstance().getValue(
            ChatConstant.MY_MODEL_SETTING,
            "未設置" // 默认值
    );
    public static void chatStreaming(OllamaChatRequest request, OllamaTokenHandler tokenHandler) throws Exception {
        String model = modelSetting;
        request.setStream(true);
        request.setModel(model);

        io.github.ollama4j.OllamaAPI ollama = new io.github.ollama4j.OllamaAPI(apiUrl);

        ollama.chatStreaming(request, tokenHandler);

    }
    public static void consumerContextMenu(AnActionEvent e, AnAction action) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return; // 确保编辑器存在
        Project project = e.getProject();
        if (project == null) return; // 确保项目存在
//        Document document = editor.getDocument();
//        String fullText = document.getText(); // 读取整个文件内容

// 读取选中文本（若有）
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        String prompt = "修复以下Java代码的错误：\n```java\n" + selectedText + "\n```";


        ToolWindowService service = project.getService(ToolWindowService.class);
        ChatToolWindow chatTool =service.getCustomPanel();
          chatTool.sendMessage(prompt);

    }
    public static void repair(AnActionEvent e, AnAction action) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return; // 确保编辑器存在
        Project project = e.getProject();
        if (project == null) return; // 确保项目存在
//        Document document = editor.getDocument();
//        String fullText = document.getText(); // 读取整个文件内容

// 读取选中文本（若有）
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        String prompt = "修复 Java 代码的错误：\n```java\n" + selectedText + "\n```";

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();
        final String newText = processText(selectedText); // 自定义替换逻辑

        // 执行替换（线程安全）
        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().replaceString(start, end, newText);
            selectionModel.removeSelection(); // 取消选中状态
        });
//        ToolWindowService service = project.getService(ToolWindowService.class);
//        ChatToolWindow chatTool =service.getCustomPanel();
//          chatTool.sendMessage(prompt);

    }

    private static String processText(String selectedText) {
        OllamaAPI ollama =new OllamaAPI("http://www.linlsyf.cn:11434");
        String result = "";

        OllamaChatRequest request=new OllamaChatRequest();
        request.setStream(true);
        request.setModel(modelSetting);
        List<OllamaChatMessage> messages = new ArrayList<>();
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, selectedText));
        request.setMessages(messages); // 必须包含消息列表
        try {
            OllamaChatResult ollamaChatResult=ollama.chat(request);
            result=ollamaChatResult.getResponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

       return result;
    }

}
