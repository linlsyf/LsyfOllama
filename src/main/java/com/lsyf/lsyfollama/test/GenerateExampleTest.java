package com.lsyf.lsyfollama.test;

import com.lsyf.lsyfollama.ChatConstant;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResponseModel;
import io.github.ollama4j.models.generate.OllamaTokenHandler;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateExampleTest {

    public static void main(String[] args) throws Exception {

//        gen();

        extracted();

    }

    private static void extracted() throws Exception {
        OllamaAPI ollama =new OllamaAPI(ChatConstant.API_TEST);
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
        request.setModel(ChatConstant.MODEL);
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
    private static void gen() throws Exception {
        OllamaAPI ollama = new OllamaAPI(ChatConstant.API_TEST);

        Map<String, Object> optionsMap = new HashMap<>();
//        optionsMap.put("temperature", 0.7);  // 温度参数
//        optionsMap.put("stream",false);  // 温度参数
//        optionsMap.put("max_tokens", 100); // 最大 token 数
        Options options=new Options(optionsMap);
        OllamaResult result = ollama.generate(ChatConstant.MODEL, "修复 int i=10/0 不需要解释直接给代码", optionsMap);
        System.out.println(result.getResponse());


    }
}
