package com.superchat;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
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
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
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

    private Boolean firstRun = false; // Flag to control data ingestion

    public EmbeddingStore<TextSegment> buildPersistentEmbeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .user("test_user")
                .password("12345")
                .database("medium_ia")
                .table("medium.product_embeddings")
                .dimension(1536) // Important for text-embedding-3-small
                .build();
    }


    public EmbeddingStore<TextSegment> populatePersistentEmbeddingStoreWithSampleData(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        List<String> productList = new ArrayList<>();

        // Sample product descriptions (these would typically be fetched from a database or an API)
        productList.add(
        """
        Product ID: 1
        Product Name: Individual Life Insurance
        Product Description: Insurance designed to provide financial protection to your loved ones in case of death.
        Coverages:
        - Natural death: Provides a benefit for death due to natural causes.
        - Accidental death: Covers death by accidents, offering an additional benefit.
        Target Audience: Adults aged 25-60, of any gender, who are primary income earners or have financial dependents (such as spouses, children, or elderly parents), seeking to ensure the financial security and well-being of their families in the event of unforeseen circumstances.
        """);

        productList.add(
        """
        Product ID: 2
        Product Name: Personal Accident Insurance
        Product Description: Insurance that offers protection in case of accidents resulting in injuries or death.
        Coverages:
        - Accidental death: Provides a benefit for death due to accidents.
        - Permanent disability: Covers permanent disability resulting from an accident, offering financial benefits.
        Target Audience: Adults of any age who are exposed to risks of accidents in their daily activities, such as workers, students, athletes, or people who frequently travel, and who wish to protect themselves and their families from the financial consequences of accidental injuries or death.
        """);

        productList.add(
        """
        Product ID: 3
        Product Name: Health Insurance
        Product Description: Insurance that covers medical expenses for illnesses or accidents.
        Coverages:
        - Hospitalization: Covers costs of hospitalization due to illness or accident.
        - Surgical procedures: Covers expenses for surgeries required due to health issues.
        - Medical consultations: Provides coverage for medical consultations with specialists.
        Target Audience: Individuals and families of all ages who are concerned about potential medical expenses due to illness or accidents, including those with pre-existing health conditions, self-employed professionals, parents seeking coverage for their children, elderly individuals, and anyone who wants to ensure access to quality healthcare and financial protection against unexpected medical costs.
        """);

        int productId = 1; // Start product ID from 1
        for (String content : productList) {
            Metadata metadata = new Metadata();
            metadata.put("index", productId);
            metadata.put("productId", productId);
            metadata.put("item_type", "Product");

            Document doc = Document.from(content, metadata);

            EmbeddingStoreIngestor.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build()
                    .ingest(doc);

            productId++;
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

        EmbeddingStore<TextSegment> embeddingStore = buildPersistentEmbeddingStore();

        if (firstRun)
            populatePersistentEmbeddingStoreWithSampleData(embeddingStore, embeddingModel);

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