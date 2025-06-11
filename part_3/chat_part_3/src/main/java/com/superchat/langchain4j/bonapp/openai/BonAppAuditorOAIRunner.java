package com.superchat.langchain4j.bonapp.openai;

import com.superchat.langchain4j.bonapp.IAgentAuditorA;
import com.superchat.langchain4j.bonapp.utils.ImageUtil;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
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
@Profile("bonapp-oai-runner-1")
public class BonAppAuditorOAIRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat-model.api-key}")
    private String openAiApiKey;

    @Override
    public void run(String... args) throws Exception {

        // 1. Inicializa el modelo GPT-4o (con visión)
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o") // ← Cambiado a modelo multimodal
                .temperature(0.2)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        // 2. Crea el servicio AI con la interfaz
        IAgentAuditorA auditor = AiServices.create(IAgentAuditorA.class, model);

        // 3. Carga y redimensiona imágenes
        byte[] evidenciaBytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-current.jpeg"));
        byte[] estandarBytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-standard.jpeg"));

        byte[] evidenciaRedimensionada = ImageUtil.resizeImage2(evidenciaBytes, 400);
        byte[] estandarRedimensionada = ImageUtil.resizeImage2(estandarBytes, 400);

        /*
        String itemAuditado = "Mesa de terraza exterior";

        String promptText = String.format(
                """
                Compara visualmente las dos imágenes. Reporta las diferencias observables. No inventes. Solo JSON.
                """,
                itemAuditado
        );

        // 3. Ensambla el UserMessage con todos los contenidos
        UserMessage userMessage = UserMessage.from(
                TextContent.from(promptText),
                ImageContent.from(imagenEstandar),
                ImageContent.from(imagenEvidencia)
        );
         */

        Image imagenEvidencia = Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(evidenciaRedimensionada))
                .mimeType("image/jpeg")
                .build();

        Image imagenEstandar = Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(estandarRedimensionada))
                .mimeType("image/jpeg")
                .build();

        // 4. Crea mensaje del usuario solo con imágenes
        UserMessage userMessage = UserMessage.from(
                TextContent.from("Primera imagen: estándar. Segunda imagen: evidencia actual."),
                ImageContent.from(imagenEstandar),
                ImageContent.from(imagenEvidencia)
        );

        // 5. Ejecuta la auditoría visual
        String resultadoJson = auditor.auditar(userMessage);

        System.out.println("Resultado de la Auditoría:");
        System.out.println(resultadoJson);
    }
}
