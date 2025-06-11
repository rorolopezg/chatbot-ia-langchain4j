package com.superchat.openia.nativeapi;

import com.superchat.openia.nativeapi.client.OpenAiVisionClient;
import com.superchat.openia.nativeapi.dto.Content;
import com.superchat.openia.nativeapi.dto.Message;
import com.superchat.openia.nativeapi.dto.OpenAiVisionResponse;
import com.superchat.openia.nativeapi.dto.OpenAiVisionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@Component
@Profile("bonapp-native-api-2")
public class OpenAiVisionClientRunner2 implements CommandLineRunner {
    @Value("${langchain4j.openai.chat-model.api-key}")
    private String openAiApiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    OpenAiVisionClient openAiClient;

    public OpenAiVisionClientRunner2(OpenAiVisionClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    @Override
    public void run(String... args) throws Exception {
        byte[] image1Bytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-standard.jpeg"));
        byte[] image2Bytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-current.jpeg"));

        String image1Base64 = Base64.getEncoder().encodeToString(image1Bytes);
        String image2Base64 = Base64.getEncoder().encodeToString(image2Bytes);

        String jsonResult = analizarAmbasImagenes(image1Base64, image2Base64);

        System.out.println("🔍 Respuesta:");
        System.out.println(jsonResult);
    }


    public String analizarAmbasImagenes(String base64Estandar, String base64Evidencia) {
        OpenAiVisionRequest req = new OpenAiVisionRequest();
        req.setModel("gpt-4o");
        req.setTemperature(0.2);

        Message msg = new Message();
        msg.setRole("user");

        msg.setContent(List.of(
                Content.ofText("Primera imagen: estándar. Segunda imagen: evidencia. Describe las diferencias visibles en JSON."),
                Content.ofImageBase64(base64Estandar),
                Content.ofImageBase64(base64Evidencia)
        ));

        req.setMessages(List.of(
                new Message() {{
                    setRole("system");
                    setContent(List.of(Content.ofText("Eres un sistema de inspección visual. Devuelve un JSON estructurado con diferencias visibles y posibles riesgos.")));
                }},
                msg
        ));

        OpenAiVisionResponse response = openAiClient.analizarImagen("Bearer " + openAiApiKey, req);
        return response.getChoices()[0].getMessage().getContent();
    }
}
