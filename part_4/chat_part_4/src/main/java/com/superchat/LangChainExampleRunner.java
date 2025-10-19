package com.superchat;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
@Profile("langchain-example-1") // Run only if this profile is active
public class LangChainExampleRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(LangChainExampleRunner.class);
    public static final String PRODUCT_ID = "Product ID";
    public static final String PRODUCT_NAME = "Product Name";
    public static final String PRODUCT_DESCRIPTION = "Product Description";
    public static final String COVERAGES = "Coverages";
    public static final String TARGET_AUDIENCE = "Target Audience";

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


    public EmbeddingStore<TextSegment> populatePersistentEmbeddingStoreWithSampleData(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {

        List<Map<String, String>> productList = new ArrayList<>();

        // ---------------------- PROD_01 ----------------------
        Map<String, String> product = new LinkedHashMap<>();
        product.put(PRODUCT_ID, "PROD_01");
        product.put(PRODUCT_NAME, "Individual Life Insurance");
        product.put(PRODUCT_DESCRIPTION, "Insurance designed to provide financial protection to your loved ones in case of death.");
        product.put(COVERAGES, """
            - Natural death: Provides a benefit for death due to natural causes.
            - Accidental death: Covers death by accidents, offering an additional benefit.
            """);
        product.put(TARGET_AUDIENCE, """
            Adults aged 25-65, of any gender, who are primary income earners or have financial dependents
            (such as spouses, children, or elderly parents), seeking to ensure the financial security and
            well-being of their families in the event of unforeseen circumstances.
            """);
        productList.add(product);

        // ---------------------- PROD_02 ----------------------
        product = new LinkedHashMap<>();
        product.put(PRODUCT_ID, "PROD_02");
        product.put(PRODUCT_NAME, "Personal Accident Insurance");
        product.put(PRODUCT_DESCRIPTION, "Insurance that offers protection in case of accidents resulting in injuries or death.");
        product.put(COVERAGES, """
            - Accidental death: Provides a benefit for death due to accidents.
            - Permanent disability: Covers permanent disability resulting from an accident, offering financial benefits.
            """);
        product.put(TARGET_AUDIENCE, """
            Adults of any age who are exposed to risks of accidents in their daily activities, such as workers,
            students, athletes, or people who frequently travel, and who wish to protect themselves and their
            families from the financial consequences of accidental injuries or death.
            """);
        productList.add(product);

        // ---------------------- PROD_03 ----------------------
        product = new LinkedHashMap<>();
        product.put(PRODUCT_ID, "PROD_03");
        product.put(PRODUCT_NAME, "Health Insurance");
        product.put(PRODUCT_DESCRIPTION, "Insurance that covers medical expenses for illnesses or accidents.");
        product.put(COVERAGES, """
            - Hospitalization: Covers costs of hospitalization due to illness or accident.
            - Surgical procedures: Covers expenses for surgeries required due to health issues.
            - Medical consultations: Provides coverage for medical consultations with specialists.
            """);
        product.put(TARGET_AUDIENCE, """
            Individuals and families of all ages who are concerned about potential medical expenses due to illness
            or accidents, including those with pre-existing health conditions, self-employed professionals, parents
            seeking coverage for their children, elderly individuals, and anyone who wants to ensure access to
            quality healthcare and financial protection against unexpected medical costs.
            """);
        productList.add(product);

        // ---------------------- PROD_04 ----------------------
        product = new LinkedHashMap<>();
        product.put(PRODUCT_ID, "PROD_04");
        product.put(PRODUCT_NAME, "Young Adult Travel Insurance");
        product.put(PRODUCT_DESCRIPTION, """
            A comprehensive travel insurance plan designed for young adults who seek adventure, exploration, and
            peace of mind while traveling. It offers essential protection against unexpected events that may
            occur during domestic or international trips, allowing you to focus on enjoying your journey without worries.
            """);
        product.put(COVERAGES, """
            - Medical emergencies abroad: Covers medical expenses resulting from illness or accidents during your trip.
            - Trip cancellation or interruption: Provides reimbursement for non-refundable expenses if your trip is canceled or cut short due to covered reasons.
            - Lost or delayed baggage: Compensates for lost, stolen, or significantly delayed luggage.
            - Travel assistance services: Offers 24/7 support for emergencies, including medical evacuation, legal assistance, and travel advice.
            """);
        product.put(TARGET_AUDIENCE, """
            Young adults aged 20–45, of any gender, who travel for leisure, study, or work and seek reliable
            protection against travel-related risks. Ideal for frequent travelers, digital nomads, students
            studying abroad, or professionals on business trips who value safety, flexibility, and peace of mind
            while exploring the world.
            """);
        productList.add(product);

        // ---------------------- PROD_05 ----------------------
        product = new LinkedHashMap<>();
        product.put(PRODUCT_ID, "PROD_05");
        product.put(PRODUCT_NAME, "Pets Insurance");
        product.put(PRODUCT_DESCRIPTION, "Insurance that covers medical expenses for illnesses or accidents of your loved pet.");
        product.put(COVERAGES, """
            - Hospitalization: Covers costs of hospitalization due to illness or accident.
            - Surgical procedures: Covers expenses for surgeries required due to health issues.
            - Medical consultations: Provides coverage for medical consultations with specialists.
            """);
        product.put(TARGET_AUDIENCE, """
            Oriented to people of all ages, owners of pets such as dogs and cats, who want to provide them with protection against diseases.
            """);
        productList.add(product);

        // ===== Ingesta de productos al EmbeddingStore =====
        int index = 1;
        for (Map<String, String> p : productList) {
            Metadata metadata = new Metadata();
            metadata.put("index", index);
            metadata.put("productId", p.get(PRODUCT_ID));
            metadata.put("productName", p.get(PRODUCT_NAME));
            metadata.put("item_type", "Product");

            String productDescription = mapToString(p);
            Document doc = Document.from(productDescription, metadata);

            EmbeddingStoreIngestor.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build()
                    .ingest(doc);

            index++;
        }

        logger.info("Product description loaded into embedding store.");
        return embeddingStore;
    }


    private String mapToString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
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

        if (firstRun)
            populatePersistentEmbeddingStoreWithSampleData(embeddingStore, embeddingModel);

        //From now on we won't use this because we'll perform the semantic search manually:
        /*
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(7) // Number of fragments to retrieve.
                .build();
         */

        IChatAgentA mainChatAgent = AiServices.builder(IChatAgentA.class)
                .chatModel(chatModel)
                //.contentRetriever(contentRetriever) --> From now on we won't use this because we'll perform the semantic search manually
                .chatMemory(MessageWindowChatMemory.withMaxMessages(30))
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
        logger.info("LangChain4j Example Runner started...");
        final ProductRecommendationResult productRecommendationResult = createAgenteChatRecomendador();

        IChatAgentA chatAgentA = productRecommendationResult.getChatAgentA();
        EmbeddingModel embeddingModel = productRecommendationResult.getEmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = productRecommendationResult.getEmbeddingStore();
        IProfileExtractionAgent profileExtractionAgent = productRecommendationResult.getProfileExtractionAgent();

        ClientProfile clientProfile = new ClientProfile();
        String jsonData;
        String friendlyProfileDescription;

        System.out.println("Type your question here:");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String line = scanner.nextLine();
            if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                break;
            }

            jsonData = profileExtractionAgent.extractData(line);
            jsonData = jsonData.replaceAll("```json", "").replaceAll("```", "");
            clientProfile.applyJson(jsonData);
            friendlyProfileDescription = clientProfile.friendlyProfileDescription();

            // *** Perform a semantic search ***
            // 1) Get the embedding of the user's query:
            Embedding queryEmbedding = embeddingModel.embed(friendlyProfileDescription).content();

            // 2) Create EmbeddingSearchRequest:
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(5)
                    .minScore(0.80) // Adjust the threshold as needed
                    .build();

            // 3) Perform semantic search:
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

            // 4) Get the list of matches from the search result
            List<EmbeddingMatch<TextSegment>> relevantMatches = searchResult.matches();
            StringBuilder resultText = new StringBuilder();

            logger.info("Found {} relevant matches from searchResult.", relevantMatches.size());
            relevantMatches.forEach(match -> {
                        logger.debug("Match: score={}, text='{}', metadata={}",
                            match.score(),
                            match.embedded().text().substring(0, Math.min(100, match.embedded().text().length())),
                            match.embedded().metadata()
                        );
                        resultText.append(match.embedded().text());
                    }
            );
            // *** End of semantic search ***

            String respuesta = chatAgentA.chat(line, resultText.toString());
            System.out.printf("Agent response: %s%n", respuesta);
        }
        logger.info("LangChain4j Example Runner finished.");
    }

}