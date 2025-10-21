package com.superchat;

import com.superchat.services.ProductIngestion;
import com.superchat.repositories.ProductRepository;
import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import com.superchat.model.ClientProfile;
import com.superchat.model.Product;
import com.superchat.model.ProductRecommendationResult;
import com.superchat.services.AudienceSearcherService;
import com.superchat.services.IABuilderService;
import com.superchat.utils.AgentContextBuilder;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@Profile("langchain-example-1") // Run only if this profile is active
@Slf4j
public class LangChainExampleRunner implements CommandLineRunner {
    private final Boolean firstRun = false; // Flag to control data ingestion
    private final IABuilderService iaBuilderService;
    private final AudienceSearcherService audienceSearcher;
    private final ProductIngestion productIngestion;

    public LangChainExampleRunner(IABuilderService iaBuilderService,
                                  AudienceSearcherService audienceSearcher,
                                  ProductIngestion productIngestion) {
        this.iaBuilderService = iaBuilderService;
        this.audienceSearcher = audienceSearcher;
        this.productIngestion = productIngestion;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("LangChain4j Example Runner started...");
        String jsonDataForProfileExtractionAgent = null;
        List<String> candidateIds = null;
        String contextForAgent = "";

        // 1) Build the models and the store:
        final ProductRecommendationResult productRecommendationResult = iaBuilderService.createAgenteChatRecomendador();

        IChatAgentA chatAgentA = productRecommendationResult.getChatAgentA();
        EmbeddingModel embeddingModel = productRecommendationResult.getEmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = productRecommendationResult.getEmbeddingStore();
        IProfileExtractionAgent profileExtractionAgent = productRecommendationResult.getProfileExtractionAgent();

        // 2) Get insurance products:
        List<Product> products = ProductRepository.findAllProducts();

        // 3) If first run, ingest products into the embedding store:
        if (firstRun)
            // Ingest only on first run
            productIngestion.ingestAll(products, embeddingStore, embeddingModel);

        // 4) Create ClientProfile object to hold extracted data:
        ClientProfile clientProfile = new ClientProfile();

        // 5) Start interaction loop:
        System.out.println("Type your question here:");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String line = scanner.nextLine();
            if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line) || "bye".equalsIgnoreCase(line)) {
                log.info("LangChain4j Example Runner finished.");
                break;
            }

            if (line == null || line.trim().isEmpty()) {
                continue; // Skip empty lines
            }

            // 6) When a message arrives from the user, try yo extract profile info using the profile extraction agent:
            jsonDataForProfileExtractionAgent = profileExtractionAgent.extractData(line);
            clientProfile.applyJson(jsonDataForProfileExtractionAgent);

            log.info("Extracted client profile: {}", clientProfile.toString());
            log.info("Friendly description: {}", clientProfile.friendlyProfileDescription());

            // 7) Use AudienceSearcher to find candidate products based on the extracted profile:
            candidateIds = audienceSearcher.findCandidateProductIds(
                    clientProfile,
                    embeddingModel,
                    embeddingStore,
                    5, // maxResults
                    0.75 // minScore
            );

            // 8) Build context for main agent.
            //    The context will include only the candidate products... Not ALL products!:
            //    This improves performance and reduces costs.
            contextForAgent = AgentContextBuilder.buildContextForAgent(products, candidateIds);

            // 9) Call main chat agent with context:
            String respuesta = chatAgentA.chat(line, contextForAgent);
            System.out.printf("Agent response: %s%n", respuesta);
        }
    }
}