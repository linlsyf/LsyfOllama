package com.lsyf.lsyfollama.test;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.exceptions.ToolInvocationException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResponseModel;
import io.github.ollama4j.models.generate.OllamaTokenHandler;
import io.github.ollama4j.models.response.OllamaResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateExampleTest {
            static String model = "qwen2.5-coder:0.5b";

    public static void main(String[] args) throws OllamaBaseException, IOException, InterruptedException, ToolInvocationException {



        extracted();

    }

    private static void extracted() throws OllamaBaseException, IOException, InterruptedException, ToolInvocationException {
        OllamaAPI ollama =new OllamaAPI("http://www.linlsyf.cn:11434");
        String promptSyStem =
        """
            生成需修复的" + int i=10/0; + " java 代码（带语法错误）
            """;
        String prompt =
        """
           输出代码不要解释
            """;

        OllamaChatRequest request=new OllamaChatRequest();
        request.setStream(true);
        request.setModel(model);
        List<OllamaChatMessage> messages = new ArrayList<>();


        messages.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM, promptSyStem));
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));
        request.setMessages(messages); // 必须包含消息列表
        ollama.chatStreaming(request, new OllamaTokenHandler() {
            @Override
            public void accept(OllamaChatResponseModel ollamaChatResponseModel) {
                System.out.println(ollamaChatResponseModel.getMessage().getContent());
            }
        });
//        extracted();


    }
    private static void gen() throws OllamaBaseException, IOException, InterruptedException, ToolInvocationException {
                String model = "qwen2.5-coder:0.5b";
        OllamaAPI ollama = new OllamaAPI("http://www.linlsyf.cn:11434");

        Map<String, Object> optionsMap = new HashMap<>();
        optionsMap.put("temperature", 0.7);  // 温度参数
        optionsMap.put("max_tokens", 100); // 最大 token 数
        OllamaResult result = ollama.generate(model, """
            修复 int i=10/0;
            ，要求：
            1. 错误类型：%s
            2. 包含至少3行代码
            3. 不要提供修复方案，仅输出代码
            示例错误：缺少分号、变量未定义、逻辑错误等
            """, optionsMap);
        System.out.println(result.getResponse());


    }
}
