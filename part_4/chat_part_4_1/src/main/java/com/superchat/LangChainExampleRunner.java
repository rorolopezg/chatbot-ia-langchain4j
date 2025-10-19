package com.superchat;

import com.superchat.ingestion.ProductIngestion;
import com.superchat.repositories.ProductRepository;
import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import com.superchat.model.ClientProfile;
import com.superchat.model.Product;
import com.superchat.model.ProductRecommendationResult;
import com.superchat.semanticsearches.AudienceSearcher;
import com.superchat.tools.InsuranceQuoteTool;
import com.superchat.utils.AgentContextBuilder;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
@Profile("langchain-example-1") // Run only if this profile is active
@Slf4j
public class LangChainExampleRunner implements CommandLineRunner {
    public static final String PRODUCT_ID = "Product ID";
    public static final String PRODUCT_NAME = "Product Name";
    public static final String PRODUCT_DESCRIPTION = "Product Description";
    public static final String COVERAGES = "Coverages";
    public static final String TARGET_AUDIENCE = "Target Audience";

    @Value("${langchain4j.openai.chat-model.api-key}") //Injects a variable with the value of the property "langchain4j.openai.chat-model.api-key" from application.properties
    private String openAiApiKey;

    private Boolean firstRun = true; // Flag to control data ingestion

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

    private ProductRecommendationResult createAgenteChatRecomendador() {
        ProductRecommendationResult result = new ProductRecommendationResult();
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o") // Or "gpt-4-1106-preview", "gpt-3.5-turbo"
                .temperature(0.1)
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

        IChatAgentA mainChatAgent = AiServices.builder(IChatAgentA.class)
                .chatModel(chatModel)
                //.contentRetriever(contentRetriever) --> From now on we won't use this because we'll perform the semantic search manually
                .chatMemory(MessageWindowChatMemory.withMaxMessages(30))
                .tools(new InsuranceQuoteTool())
                .build();

        IProfileExtractionAgent profileExtractionAgent = AiServices.builder(IProfileExtractionAgent.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(5))
                .build();

        result.setChatAgentA(mainChatAgent);
        result.setEmbeddingModel(embeddingModel);
        result.setEmbeddingStore(embeddingStore);
        result.setProfileExtractionAgent(profileExtractionAgent);

        return result;
    }


    @Override
    public void run(String... args) throws Exception {
        log.info("LangChain4j Example Runner started...");
        String jsonDataForProfileExtractionAgent = null;
        List<String> candidateIds = null;
        String contextForAgent = "";

        // 1) Build the models and the store:
        final ProductRecommendationResult productRecommendationResult = createAgenteChatRecomendador();

        IChatAgentA chatAgentA = productRecommendationResult.getChatAgentA();
        EmbeddingModel embeddingModel = productRecommendationResult.getEmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = productRecommendationResult.getEmbeddingStore();
        IProfileExtractionAgent profileExtractionAgent = productRecommendationResult.getProfileExtractionAgent();

        // 2) Get insurance products:
        List<Product> products = ProductRepository.findAllProducts();
        if (firstRun)
            // Ingest only on first run
            ProductIngestion.ingestAll(products, embeddingStore, embeddingModel);

        // Create ClientProfile to hold extracted data:
        ClientProfile clientProfile = new ClientProfile();


        System.out.println("Type your question here:");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String line = scanner.nextLine();
            if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                log.info("LangChain4j Example Runner finished.");
                break;
            }

            jsonDataForProfileExtractionAgent = profileExtractionAgent.extractData(line);
            jsonDataForProfileExtractionAgent = jsonDataForProfileExtractionAgent.replaceAll("```json", "").replaceAll("```", "");
            clientProfile.applyJson(jsonDataForProfileExtractionAgent);
            log.info("Extracted client profile: {}", clientProfile);

            // 3) Cuando llega un mensaje del usuario, extraes perfil (como ya lo haces) y luego:
            candidateIds = AudienceSearcher.findCandidateProductIds(
                    clientProfile,
                    embeddingModel,
                    embeddingStore,
                    4,       // maxResults candidatos
                    0.75     // minScore más exigente
            );

            // 4) Construyes el contexto sólo con productos candidatos:
            contextForAgent = AgentContextBuilder.buildContextForAgent(products, candidateIds);


            // 5) Llamas a tu agente principal:
            String respuesta = chatAgentA.chat(line, contextForAgent);
            System.out.printf("Agent response: %s%n", respuesta);
        }
    }
}