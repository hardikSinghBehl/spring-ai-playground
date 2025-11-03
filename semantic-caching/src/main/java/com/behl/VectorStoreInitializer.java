package com.behl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@Component
@Profile("populate-db")
class VectorStoreInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreInitializer.class);

    private final PineconeVectorStore pineconeVectorStore;
    private final ResourcePatternResolver resourcePatternResolver;

    VectorStoreInitializer(PineconeVectorStore pineconeVectorStore, ResourcePatternResolver resourcePatternResolver) {
        this.pineconeVectorStore = pineconeVectorStore;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        log.info("Starting vector store initialization");
        var documents = new ArrayList<Document>();

        var resources = resourcePatternResolver.getResources("classpath:documents/*.md");
        log.info("Found {} markdown files to process", resources.length);

        Arrays.stream(resources).forEach(resource -> {
            log.debug("Processing file: {}", resource.getFilename());
            var markdownDocumentReader = new MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig());
            documents.addAll(markdownDocumentReader.read());
        });
        pineconeVectorStore.add(new TokenTextSplitter().split(documents));
        log.info("Successfully added {} documents", documents.size());
    }

}