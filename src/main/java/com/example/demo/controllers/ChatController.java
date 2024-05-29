package com.example.demo.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/ollama/")
public class ChatController {

    private final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final OllamaChatClient chatClient;
    private final RedisVectorStore vectorStore;
    @Value("classpath:/prompts/prompt.st")
    private Resource promptTemplateDocument;

    public ChatController(OllamaChatClient chatClient, RedisVectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }


    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message", defaultValue = "Are you real?") String message) {
        var similarDocuments = this.vectorStore.similaritySearch(
                SearchRequest.query(message)
                        .withTopK(3)
        );

        log.info("Replying to a query: {} with {} documents", message, similarDocuments.size());
        similarDocuments.forEach(document ->
            log.info("Document content: {}", document.getContent()));

        var documentStrings = similarDocuments.stream().map(Document::getContent).toList();

        var promptParameter = new HashMap<String, Object>();
        promptParameter.put("input", message);
        promptParameter.put("documents", String.join("\n", documentStrings));

        var promptTemplate = new PromptTemplate(this.promptTemplateDocument);
        var  prompt = promptTemplate.create(promptParameter);

        return this.chatClient.call(prompt)
                .getResult()
                .getOutput()
                .getContent();
    }

}
