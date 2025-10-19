package com.superchat.semanticsearches;

import com.superchat.model.ClientProfile;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static com.superchat.model.ProductFields.*;

@Slf4j
public final class AudienceSearcher {
    private AudienceSearcher(){}

    public static List<String> findCandidateProductIds(ClientProfile profile,
                                                       EmbeddingModel embeddingModel,
                                                       EmbeddingStore<TextSegment> store,
                                                       int maxResults,
                                                       double minScore) {

        // 1) Build query for target audience
        String audienceQuery = profile.friendlyProfileDescription();
        Embedding queryForEmbeddings = embeddingModel.embed(audienceQuery).content();

        // 2) Search semantically in the store
        EmbeddingSearchRequest req = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryForEmbeddings)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();

        EmbeddingSearchResult<TextSegment> res = store.search(req);
        log.info("Found {} initial matches for audience query (semantic search).", res.matches().size());

        Integer age = profile.getAge() == null ? 0 : profile.getAge();

        // 3) Post-filter: only "audience" segments and by age range:
        List<EmbeddingMatch<TextSegment>> audienceMatches = res.matches().stream()
                .filter(m ->
                        SEG_AUDIENCE.equals(String.valueOf(m.embedded().metadata().getString(META_SEGMENT_TYPE))))
                .filter(m -> {
                    Integer min = m.embedded().metadata().getInteger(META_AGE_MIN);
                    Integer max = m.embedded().metadata().getInteger(META_AGE_MAX);
                    return (age >= min && age <= max);
                })
                .toList();
        log.info("After post-filtering, {} matches remain for audience query.", audienceMatches.size());

        if (audienceMatches.isEmpty()) {
            return Collections.emptyList();
        }

        // 4) Consolidate by productId → keep the best score per product:
        Map<String, EmbeddingMatch<TextSegment>> bestPerProduct =
                audienceMatches.stream().collect(Collectors.toMap(
                        m -> String.valueOf(m.embedded().metadata().getString(META_PRODUCT_ID)),
                        m -> m,
                        (m1, m2) -> m1.score() >= m2.score() ? m1 : m2
                ));

        // 5) Sort by score desc and cut to maxResults:
        return bestPerProduct.values().stream()
                .sorted(Comparator.comparingDouble(EmbeddingMatch<TextSegment>::score).reversed())
                .limit(3) // Get top N products
                .map(m -> String.valueOf(m.embedded().metadata().getString(META_PRODUCT_ID)))
                .toList();
    }
}
