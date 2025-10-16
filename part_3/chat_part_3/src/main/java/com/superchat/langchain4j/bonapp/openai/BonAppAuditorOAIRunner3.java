package com.superchat.langchain4j.bonapp.openai;

import com.superchat.langchain4j.bonapp.IAgentAuditorA2;
import com.superchat.langchain4j.bonapp.openai.services.ResumenPdfService;
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
@Profile("bonapp-oai-runner-3")
public class BonAppAuditorOAIRunner3 implements CommandLineRunner {
    ResumenPdfService resumenPdfService;

    public BonAppAuditorOAIRunner3(ResumenPdfService resumenPdfService) {
        this.resumenPdfService = resumenPdfService;
    }

    @Override
    public void run(String... args) throws Exception {
        String result = resumenPdfService.resumirPdf();
        System.out.println("🔍 Resumen del PDF: " + result);
    }
}
