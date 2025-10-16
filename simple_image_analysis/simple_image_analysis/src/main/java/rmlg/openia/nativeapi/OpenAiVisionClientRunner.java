package rmlg.openia.nativeapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Component
@Profile("image-comparison")
public class OpenAiVisionClientRunner implements CommandLineRunner {
    @Value("${langchain4j.openai.chat-model.api-key}")
    private String openAiApiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Override
    public void run(String... args) throws Exception {
        byte[] imageStandardBytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-standard.jpg"));
        byte[] imageCurrentBytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-current-2.jpg"));

        String imageStandardBase64 = Base64.getEncoder().encodeToString(imageStandardBytes);
        String imageCurrentBase64 = Base64.getEncoder().encodeToString(imageCurrentBytes);

        String body = """
        {
          "model": "gpt-4o",
          "temperature": 0.2,
          "messages": [
            {
              "role": "system",
              "content": "Eres un sistema de inspección visual. Recibirás dos imágenes: la primera describe el entorno estándar (o situación deseada), la segunda es la evidencia de la situación actual. Compara la segunda imágen (situación actual) con la primera imágen (situación deseada) y devuelve un JSON con las diferencias visibles y posibles riesgos. No inventes elementos no presentes en las imágenes."
            },
            {
              "role": "user",
              "content": [
                {
                  "type": "text",
                  "text": "Primera imagen: estándar (situación deseada). Segunda imagen: evidencia de la situación actual. Describe sólo lo visible sobre las diferencias respecto al estándar. Salida en JSON."
                },
                {
                  "type": "image_url",
                  "image_url": {
                    "url": "data:image/jpeg;base64,%s"
                  }
                },
                {
                  "type": "image_url",
                  "image_url": {
                    "url": "data:image/jpeg;base64,%s"
                  }
                }
              ]
            }
          ]
        }
        """.formatted(imageStandardBase64, imageCurrentBase64);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL))
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("🔍 Respuesta:");
        System.out.println(response.body());
    }
}
