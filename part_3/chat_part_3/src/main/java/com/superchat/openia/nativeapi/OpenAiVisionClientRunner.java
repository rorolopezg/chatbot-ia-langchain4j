package com.superchat.openia.nativeapi;

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
@Profile("bonapp-native-api-1")
public class OpenAiVisionClientRunner implements CommandLineRunner {
    @Value("${langchain4j.openai.chat-model.api-key}")
    private String openAiApiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Override
    public void run(String... args) throws Exception {
        byte[] image1Bytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-standard.jpeg"));
        byte[] image2Bytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-standard-2.jpg"));
        byte[] image3Bytes = Files.readAllBytes(Paths.get("/Users/rodrigolopezgatica/Downloads/table-current.jpeg"));

        String image1Base64 = Base64.getEncoder().encodeToString(image1Bytes);
        String image2Base64 = Base64.getEncoder().encodeToString(image2Bytes);
        String image3Base64 = Base64.getEncoder().encodeToString(image3Bytes);

        String body = """
        {
          "model": "gpt-4o",
          "temperature": 0.2,
          "messages": [
            {
              "role": "system",
              "content": "Eres un sistema de inspección visual. Recibirás tres imágenes: las dos primeras describen el entorno estándar (o situación deseada), la tercera es la evidencia de la situación actual. Compara la tercera imágen (situación actual) con respecto a las primeras dos imágenes (situación deseada) y devuelve un JSON con las diferencias visibles y posibles riesgos. No inventes elementos no presentes en las imágenes."
            },
            {
              "role": "user",
              "content": [
                {
                  "type": "text",
                  "text": "Primeras dos imágenes: estándar ()situación deseada). Tercera imagen: evidencia de la situación actual. Describe sólo lo visible sobre las diferencias respecto al estándar. Salida en JSON."
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
        """.formatted(image1Base64, image2Base64, image3Base64);

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
