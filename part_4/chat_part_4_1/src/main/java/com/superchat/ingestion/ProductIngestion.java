package com.superchat.ingestion;

import com.superchat.model.Product;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.superchat.model.ProductFields.*;

@Slf4j
public final class ProductIngestion {

    private ProductIngestion(){}

    public static void ingestAll(List<Product> products,
                                 EmbeddingStore<TextSegment> store,
                                 EmbeddingModel embeddingModel) {
        for (Product p : products) {
            log.info("Ingesting product: {} - {}", p.id(), p.name());
            ProductIngestion.ingestProduct(p, store, embeddingModel);
        }
        log.info("Ingestion completed for {} products.", products.size());
    }

    public static void ingestProduct(Product p,
                                     EmbeddingStore<TextSegment> store,
                                     EmbeddingModel embeddingModel) {

        // ===== Segmento AUDIENCE =====
        Metadata audMd = new Metadata();
        audMd.put(META_PRODUCT_ID, p.id());
        audMd.put(META_NAME, p.name());
        audMd.put(META_AGE_MIN, p.ageMin());
        audMd.put(META_AGE_MAX, p.ageMax());
        audMd.put(META_CATEGORY, p.category());
        audMd.put(META_SEGMENT_TYPE, SEG_AUDIENCE);

        String audienceText = "Target Audience: " + p.audienceText().trim();
        TextSegment audienceSeg = TextSegment.from(audienceText, audMd);

        Embedding audienceEmb = embeddingModel.embed(audienceSeg).content();
        store.add(audienceEmb, audienceSeg);

        // ===== Segmento DETAILS =====
        Metadata detMd = new Metadata();
        detMd.put(META_PRODUCT_ID, p.id());
        detMd.put(META_NAME, p.name());
        detMd.put(META_AGE_MIN, p.ageMin());
        detMd.put(META_AGE_MAX, p.ageMax());
        detMd.put(META_CATEGORY, p.category());
        detMd.put(META_SEGMENT_TYPE, SEG_DETAILS);

        String detailsText = """
                Product ID: %s
                Product Name: %s
                Product Description: %s
                Coverages:
                %s
                Target Audience: %s
                """.formatted(
                p.id(), p.name(), p.description().trim(), p.coveragesText().trim(), p.audienceText().trim());

        TextSegment detailsSeg = TextSegment.from(detailsText, detMd);

        Embedding detailsEmb = embeddingModel.embed(detailsSeg).content();
        store.add(detailsEmb, detailsSeg);
    }
}
