package com.lsyf.lsyfollama;

import com.intellij.ide.util.PropertiesComponent;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.generate.OllamaTokenHandler;

public class OllamaClient {
    static String apiUrl = PropertiesComponent.getInstance().getValue(
            ChatConstant.MY_PLUGIN_SETTING,
            "未設置" // 默认值
    );
    public static void chatStreaming(OllamaChatRequest request, OllamaTokenHandler tokenHandler) throws Exception {
        String model = "qwen2.5-coder:0.5b";
        request.setStream(true);
        request.setModel(model);

        io.github.ollama4j.OllamaAPI ollama = new io.github.ollama4j.OllamaAPI(apiUrl);

        ollama.chatStreaming(request, tokenHandler);

    }
    public static void stopMessage() {
    }
}
