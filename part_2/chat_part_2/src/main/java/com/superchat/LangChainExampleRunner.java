package com.superchat;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
@Profile("langchain-example-1") // Run only if this profile is active
public class LangChainExampleRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(LangChainExampleRunner.class);

    @Value("${langchain4j.openai.chat-model.api-key}") //Injects a variable with the value of the property "langchain4j.openai.chat-model.api-key" from application.properties
    private String openAiApiKey;


    public EmbeddingStore<TextSegment> createInMemoryEmbeddingStoreWithSampleData(EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        List<String> productList = new ArrayList<>();

        productList.add(
        """
        Individual Life Insurance: Insurance designed to provide financial protection to your loved ones in case of death.
        - Coverages:
            - Natural death: Provides a benefit for death due to natural causes.
            - Accidental death: Covers death by accidents, offering an additional benefit.
        """);

        productList.add(
        """
        Personal Accident Insurance: Insurance that offers protection in case of accidents resulting in injuries or death.
        - Coverages:
            - Accidental death: Provides a benefit for death due to accidents.
            - Permanent disability: Covers permanent disability resulting from an accident, offering financial benefits.
        """);

        productList.add(
        """
        Health Insurance: Insurance that covers medical expenses for illnesses or accidents.
        - Coverages:
            - Hospitalization: Covers costs of hospitalization due to illness or accident.
            - Surgical procedures: Covers expenses for surgeries required due to health issues.
            - Medical consultations: Provides coverage for medical consultations with specialists.
        """);

        for (String content : productList) {
            Document doc = Document.from(content);

            EmbeddingStoreIngestor.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build()
                    .ingest(doc);
        }

        logger.info("Product description loaded into embedding store.");
        return embeddingStore;
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

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName("text-embedding-3-small")
                .dimensions(1536) // Important for text-embedding-3-small
                .logRequests(false)
                .logResponses(false)
                .build();

        EmbeddingStore<TextSegment> embeddingStore = createInMemoryEmbeddingStoreWithSampleData(embeddingModel);

        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(7) // Number of fragments to retrieve.
                .build();

        IChatAgentA chatService = AiServices.builder(IChatAgentA.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(30))
                .build();

        return chatService;
    }


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

}