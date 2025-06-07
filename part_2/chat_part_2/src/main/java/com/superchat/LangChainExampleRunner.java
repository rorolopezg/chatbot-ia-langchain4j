package com.superchat;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Scanner;

@Component
@Profile("langchain-example-1") // Run only if this profile is active
public class LangChainExampleRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(LangChainExampleRunner.class);

    //Injects a variable with the value of the property "com.superchat.openai.api-key" from application.properties
    @Value("${com.superchat.openai.api-key}")
    private String openAiApiKey;

    @Override
    public void run(String... args) throws Exception {
        logger.info("LangChain4j Example Runner started...");
        IChatAgentA chatAgentA = createAgenteChatRecomendador();

        System.out.println("Type your question here:");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String line = scanner.nextLine();
            if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                break;
            }
            String respuesta = chatAgentA.chat(line);
            System.out.printf("Agent response: %s%n", respuesta);
        }
        logger.info("LangChain4j Example Runner finished.");
    }

    private IChatAgentA createAgenteChatRecomendador() {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o") // Or "gpt-4-1106-preview", "gpt-3.5-turbo"
                .temperature(0.2)
                .timeout(Duration.ofSeconds(60))
                .logRequests(false)
                .logResponses(false)
                .build();

        IChatAgentA chatService = AiServices.builder(IChatAgentA.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(50))
                .build();

        return chatService;
    }

}