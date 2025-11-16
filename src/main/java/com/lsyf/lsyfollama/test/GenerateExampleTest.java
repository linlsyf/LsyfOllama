package com.lsyf.lsyfollama.test;

import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.constant.OllamaClientUtils;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.generate.OllamaGenerateStreamObserver;
import io.github.ollama4j.models.generate.OllamaGenerateTokenHandler;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateExampleTest {



    private static void extracted() throws Exception {
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
        OllamaClientUtils.ollama.chat(request, new OllamaChatTokenHandler() {
            @Override
            public void accept(OllamaChatResponseModel ollamaChatResponseModel) {
//                  ollamaChatResponseModel.
                System.out.println(ollamaChatResponseModel.getMessage().getResponse());
            }
        });
//        extracted();


    }
    public static void main(String[] args) throws Exception {

        gen();

//        extracted();

    }
    private static void gen() throws Exception {


        Map<String, Object> optionsMap = new HashMap<>();
        optionsMap.put("temperature", 0.1);  // 温度参数
        optionsMap.put("stream",false);  // 温度参数
//        optionsMap.put("max_tokens", 100); // 最大 token 数
        String promptSyStem =
                """
                  修复 int i=10/0   输出代码不要解释
                    """;
        String prompt =
                """
                 
                    """;

        OllamaGenerateRequest request=new OllamaGenerateRequest();
        request.setStream(true);
        request.setModel(ChatConstant.MODEL);
        List<OllamaChatMessage> messages = new ArrayList<>();

        messages.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM, promptSyStem));
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));
        request.setPrompt(promptSyStem);
//                .setMessages(messages); // 必须包含消息列表

        OllamaGenerateStreamObserver streamObserver=new OllamaGenerateStreamObserver(new OllamaGenerateTokenHandler() {
            @Override
            public void accept(String message) {

            }
        }, new OllamaGenerateTokenHandler() {
            @Override
            public void accept(String message) {

            }
        });

        OllamaResult result =  OllamaClientUtils.ollama.generate(request,streamObserver);
        System.out.println(result.getResponse());


    }
}
