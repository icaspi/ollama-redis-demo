package com.example.demo.config;

import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreProperties;
import org.springframework.ai.transformers.TransformersEmbeddingClient;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {
    @Bean
    public RedisVectorStore vectorStore(TransformersEmbeddingClient embeddingClient, RedisVectorStoreProperties properties) {

        var config = RedisVectorStore.RedisVectorStoreConfig.builder()
                        .withURI(properties.getUri())
                .withIndexName(properties.getIndex())
                .withPrefix(properties.getPrefix())
                .build();

        var vectorStore =  new RedisVectorStore(config, embeddingClient);
        vectorStore.afterPropertiesSet();
        return vectorStore;
    }
}
