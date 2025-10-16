package com.superchat.langchain4j.bonapp.openai.services;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
public class ResumenPdfService {
    @Value("${langchain4j.openai.chat-model.api-key}")
    private String openAiApiKey;

    private final OpenAiChatModel chatModel;

    public ResumenPdfService() {
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4")
                .temperature(0.2)
                .build();
    }

    public String resumirPdf() {
        StringBuilder resumenFinal = new StringBuilder();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-file.pdf")) {
            DocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(inputStream);

            String promptFinal = "Resume el siguiente fragmento del documento de forma clara y estructurada:\n\n"
                    + document.text() + "\n\nResumen:";

            ChatResponse respuesta = chatModel.chat(UserMessage.from(promptFinal));

            resumenFinal.append(respuesta.aiMessage().text());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return resumenFinal.toString();
    }
}
