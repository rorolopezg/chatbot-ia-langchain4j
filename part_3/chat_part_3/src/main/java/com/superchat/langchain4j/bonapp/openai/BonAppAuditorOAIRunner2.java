package com.superchat.langchain4j.bonapp.openai;

import com.superchat.langchain4j.bonapp.IAgentAuditorA2;
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
@Profile("bonapp-oai-runner-2")
public class BonAppAuditorOAIRunner2 implements CommandLineRunner {

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

        IAgentAuditorA2 auditor = AiServices.create(IAgentAuditorA2.class, model);

        // Carga imágenes originales
        byte[] estandarBytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-standard.jpeg"));
        byte[] evidenciaBytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-current.jpeg"));

        // Combina ambas imágenes en una sola (horizontalmente)
        byte[] imagenCombinada = ImageUtil.combineImagesHorizontally(estandarBytes, evidenciaBytes, 400);

        // Crea imagen LangChain
        Image imagen = Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(imagenCombinada))
                .mimeType("image/jpeg")
                .build();

        // Crea el UserMessage con una sola imagen
        UserMessage userMessage = UserMessage.from(
                TextContent.from("La imagen contiene dos mitades: IZQUIERDA = estándar, DERECHA = evidencia. Describe diferencias visuales entre las dos MITADES, sin inventar objetos ni personas."),
                ImageContent.from(imagen)
        );

        // Ejecuta auditoría
        String resultadoJson = auditor.auditar(userMessage);

        System.out.println("Resultado de la Auditoría:");
        System.out.println(resultadoJson);
    }
}
