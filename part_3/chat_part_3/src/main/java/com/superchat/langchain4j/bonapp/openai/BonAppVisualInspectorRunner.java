package com.superchat.langchain4j.bonapp.openai;

import com.superchat.langchain4j.bonapp.IAgentVisualInspector;
import com.superchat.langchain4j.bonapp.utils.ImageUtil;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;

@Component
@Profile("bonapp-visual")
public class BonAppVisualInspectorRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat-model.api-key}")
    private String openAiApiKey;

    @Override
    public void run(String... args) throws Exception {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o")
                .temperature(0.2)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        IAgentVisualInspector inspector = AiServices.create(IAgentVisualInspector.class, model);

        byte[] estandarBytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-standard.jpeg"));
        byte[] evidenciaBytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-current.jpeg"));

        byte[] estandarResized = ImageUtil.resizeImage2(estandarBytes, 512);
        byte[] evidenciaResized = ImageUtil.resizeImage2(evidenciaBytes, 512);

        Image imagenEstandar = Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(estandarResized))
                .mimeType("image/jpeg")
                .build();

        Image imagenEvidencia = Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(evidenciaResized))
                .mimeType("image/jpeg")
                .build();

        String jsonEstandar = inspector.describir(UserMessage.from(ImageContent.from(imagenEstandar)));
        String jsonEvidencia = inspector.describir(UserMessage.from(ImageContent.from(imagenEvidencia)));

        System.out.println("🟢 Descripción estándar:");
        System.out.println(jsonEstandar);

        System.out.println("🟠 Descripción evidencia:");
        System.out.println(jsonEvidencia);

        // Aquí puedes agregar la lógica para comparar ambos JSON y generar una lista de diferencias
        // y situaciones de riesgo detectadas por código
    }
}
