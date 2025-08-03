package com.lsyf.lsyfollama;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.lsyf.lsyfollama.ui.ChatToolWindow;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.generate.OllamaTokenHandler;

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
//        String model = "qwen2.5-coder:0.5b";
        request.setStream(true);
        request.setModel(model);

        io.github.ollama4j.OllamaAPI ollama = new io.github.ollama4j.OllamaAPI(apiUrl);

        ollama.chatStreaming(request, tokenHandler);

    }
    public static void consumerContextMenu(AnActionEvent e, AnAction action) {
//        Messages.showInfoMessage("这是新的开始！", action.getTemplateText());
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return; // 确保编辑器存在

        Project project = e.getProject();
        if (project == null) return; // 确保项目存在
        Document document = editor.getDocument();
        String fullText = document.getText(); // 读取整个文件内容

// 读取选中文本（若有）
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        String prompt = "检测以下 Java 代码的错误：\n```java\n" + selectedText + "\n```";

// 获取当前项目实例
// 通过 ToolWindowManager 获取工具窗口
//        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(MY_SIDE_BAR);
//        JComponent jComponent= toolWindow.getComponent();
//        Component chatComponent =  jComponent.getComponent(0);
//        ChatToolWindow chatTool = (ChatToolWindow) chatComponent.getParent().getComponent(0);
//        ChatToolWindow chatTool =project.getComponent(ChatToolWindow.class);
        ToolWindowService service = project.getService(ToolWindowService.class);
        ChatToolWindow chatTool =service.getCustomPanel();
          chatTool.sendMessage(prompt);

    }
    public static void stopMessage() {
    }
}
