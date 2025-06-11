package com.superchat.langchain4j.bonapp.gemini;

import com.superchat.langchain4j.bonapp.IAgentAuditorA;
import com.superchat.langchain4j.bonapp.utils.ImageUtil;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
// Importa el modelo de Vertex AI Gemini
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Component
@Profile("bonapp-2") // Asegúrate de que este perfil esté activo
public class BonAppAuditorGeminiRunner implements CommandLineRunner {

    // Ya no necesitas la API Key de OpenAI aquí

    @Override
    public void run(String... args) throws Exception {

        // --- CAMBIO PRINCIPAL: INSTANCIACIÓN DEL MODELO ---
        // Se reemplaza OpenAiChatModel por VertexAiGeminiChatModel
        VertexAiGeminiChatModel model = VertexAiGeminiChatModel.builder()
                .project("my-ia-project-462513") // Reemplaza con el ID de tu proyecto de Google Cloud
                .location("us-central1")         // Una región que soporte el modelo, ej. us-central1
                .modelName("gemini-1.0-pro-vision") // Modelo rápido y eficiente, o usa "gemini-1.5-pro-001" para máxima calidad
                .temperature(0.2f)
                //.timeout(Duration.ofSeconds(90))
                .logRequests(true)
                .logResponses(true)
                .build();

        // El resto del código es EXACTAMENTE EL MISMO

        // 2. Crear la instancia del servicio de IA (sin cambios)
        IAgentAuditorA auditor = AiServices.create(IAgentAuditorA.class, model);

        // --- Preparación de las Imágenes (sin cambios) ---

        System.out.println("Cargando y redimensionando imágenes...");

        byte[] evidenciaBytesOriginales = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-current.jpeg"));
        byte[] estandarBytesOriginales = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-standard.jpeg"));

        int targetWidth = 768;
        byte[] evidenciaBytesRedimensionados = ImageUtil.resizeImage(evidenciaBytesOriginales, targetWidth);
        byte[] estandarBytesRedimensionados = ImageUtil.resizeImage(estandarBytesOriginales, targetWidth);

        System.out.println("Imágenes redimensionadas. Construyendo la solicitud...");

        Image imagenEvidencia = Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(evidenciaBytesRedimensionados))
                .mimeType("image/jpeg")
                .build();

        Image imagenEstandar = Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(estandarBytesRedimensionados))
                .mimeType("image/jpeg")
                .build();

        // --- Construcción del Mensaje (sin cambios, pero usando el prompt mejorado) ---
        String itemAuditado = "Mesa de terraza exterior";
        String promptText = String.format(
                """
                Inicia el análisis para el siguiente ítem: '%s'.
                Basa tu análisis ÚNICA Y EXCLUSIVAMENTE en las diferencias visuales entre las dos imágenes.
                La primera imagen es el 'estándar de calidad' y la segunda es la 'evidencia actual'.
                Describe únicamente lo que ves y no hagas suposiciones.
                Genera el informe en el formato JSON solicitado.
                """,
                itemAuditado
        );

        UserMessage userMessage = UserMessage.from(
                TextContent.from(promptText),
                ImageContent.from(imagenEstandar),
                ImageContent.from(imagenEvidencia)
        );

        // --- Ejecución y Resultado (sin cambios) ---
        System.out.println("Enviando solicitud a Google Vertex AI (Gemini)...");
        String resultadoJson = auditor.auditar(userMessage);

        System.out.println("\n--- INICIO DEL RESULTADO DE LA AUDITORÍA (GEMINI) ---");
        System.out.println(resultadoJson);
        System.out.println("--- FIN DEL RESULTADO DE LA AUDITORÍA (GEMINI) ---");
    }
}