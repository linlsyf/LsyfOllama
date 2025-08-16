package com.lsyf.lsyfollama.constant;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.ToolWindowService;
import com.lsyf.lsyfollama.business.ContextMenuLogic;
import com.lsyf.lsyfollama.ui.ChatToolWindow;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.generate.OllamaTokenHandler;
import io.github.ollama4j.utils.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OllamaClient {
    public static OllamaAPI ollama;

    public static void chatStreaming(OllamaChatRequest request, OllamaTokenHandler tokenHandler) throws Exception {
        String model = ChatConstant.modelSetting;
        request.setStream(true);
        request.setModel(model);

         ollama = new OllamaAPI(ChatConstant.apiUrl);
        ollama.chatStreaming(request, tokenHandler);
    }



    public  static String processText(String selectedText) {
        OllamaAPI ollama =new OllamaAPI(ChatConstant.apiUrl);
        String result = "";
        OllamaChatRequest request=new OllamaChatRequest();
        request.setStream(true);
        request.setModel(ChatConstant.modelSetting);
        List<OllamaChatMessage> messages = new ArrayList<>();
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM, "你是一个"+ ChatConstant.DEV_LAN+"专家，仅输出代码，不要任何解释、注释或额外文本。\n" +
                "输出格式要求：纯代码，无换行符(\\n)或描述"));
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, selectedText));
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.ASSISTANT, "不需要推理过程和描述"));

        request.setMessages(messages); // 必须包含消息列表
        try {
            Map<String, Object> optionsMap = new HashMap<>();
//            optionsMap.put("temperature", 0.5);  // 温度参数
//            optionsMap.put("stream",false);  // 温度参数
            Options options=new Options(optionsMap);
//            String model, String prompt, boolean raw, Options options
//            OllamaResult ollamaChatResult=ollama.generate(request.getModel(),selectedText,true,options);
            OllamaChatResult ollamaChatResult=ollama.chat(request);
            result=ollamaChatResult.getResponseModel().getMessage().getContent();
//            result=new String(ollamaChatResult.getResponse().getBytes());
        } catch (Exception e) {
            Messages.showInfoMessage("repair fail ", e.getMessage());

            throw new RuntimeException(e);
        }

       return result;
    }

}
