package org.test.ia.mariadbtest;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.MariaDBVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class MariadbTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(MariadbTestApplication.class, args);
    }

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return new MariaDBVectorStore.Builder(jdbcTemplate, embeddingModel)
                .withDistanceType(MariaDBVectorStore.MariaDBDistanceType.EUCLIDEAN)
                .withInitializeSchema(true)
                .build();
    }

}
