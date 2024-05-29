package com.example.demo.components;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ReferenceDocsLoader {

    private static final Logger log = LoggerFactory.getLogger(ReferenceDocsLoader.class);

    private static final String KEY_SET_FILENAME = "set_document_names";

    private final RedisVectorStore vectorStore;
    private final RedisVectorStoreProperties properties;

    private final ResourcePatternResolver resourcePatternResolver;

    public ReferenceDocsLoader(RedisVectorStore vectorStore, RedisVectorStoreProperties properties, ResourcePatternResolver resourcePatternResolver) {
        this.vectorStore = vectorStore;
        this.properties = properties;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @PostConstruct
    @SneakyThrows
    public void setupDb() {
        log.info("*** Setting up the db ***");
        log.info("*** Index information ***");
        Map<String, Object> indexInfo = vectorStore.getJedis().ftInfo(properties.getIndex());
        indexInfo.forEach((k, v) -> log.info("{}: {}", k, v));

        Resource[] resources = resourcePatternResolver.getResources("classpath:/docs/*.pdf");

        if (resources.length == 0) {
            log.info("No documents found in classpath:/docs/*.pdf");
            return;
        }

        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfBottomTextLinesToDelete(0)
                        .withNumberOfTopPagesToSkipBeforeDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();

        Arrays.stream(resources).forEach(resource -> {
            var resourceName = resource.getFilename();
            if (!this.vectorStore.getJedis().sismember(KEY_SET_FILENAME, resourceName)) {
                log.info("Resource {} doesn't exists in the vector store", resourceName);
                var pdfReader = new PagePdfDocumentReader(resource, config);
                var textSplitter = new TokenTextSplitter();
                var documentList = pdfReader.get();
                var splitDocuments = textSplitter.apply(documentList);
                this.vectorStore.add(splitDocuments);
                this.vectorStore.getJedis().sadd(KEY_SET_FILENAME, resourceName);
            } else {
                log.info("Resource {} exists in the vector store", resourceName);
            }
        });

    }
}
