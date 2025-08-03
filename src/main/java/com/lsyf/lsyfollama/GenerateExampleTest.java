package com.lsyf.lsyfollama;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.exceptions.ToolInvocationException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResponseModel;
import io.github.ollama4j.models.generate.OllamaTokenHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerateExampleTest {
    public static void main(String[] args) throws OllamaBaseException, IOException, InterruptedException, ToolInvocationException {
//        OllamaAPI api = new OllamaAPI("http://www.linlsyf.cn/");
//        String response = api.generate("llama3", "解释量子力学");
//        System.out.println(response);

        OllamaAPI ollama =new OllamaAPI("http://www.linlsyf.cn:11434");
        String prompt = "如何记单词";

        String  model="qwen2.5-coder:0.5b";
        OllamaChatRequest request=new OllamaChatRequest();
//        request.set
        request.setStream(true);
        request.setModel(model);
        List<OllamaChatMessage> messages = new ArrayList<>();
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, "为什么天空是蓝色的？"));
        request.setMessages(messages); // 必须包含消息列表
        ollama.chatStreaming(request, new OllamaTokenHandler() {
            @Override
            public void accept(OllamaChatResponseModel ollamaChatResponseModel) {
                System.out.println(ollamaChatResponseModel.getMessage().getContent());
            }
        });

    }
}
