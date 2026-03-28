package com.avdhutworks.finsight_ai.service;

import com.avdhutworks.finsight_ai.utils.InMemoryVectorStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagService {

    private final ChatClient chatClient;
    private final InMemoryVectorStore vectorStore;
    private final CategorizationService categorizationService;

    public RagService(ChatClient.Builder builder,
                      InMemoryVectorStore vectorStore,
                      CategorizationService categorizationService) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.categorizationService = categorizationService;
    }

    public List<String> getHybridChunks(String question) {
        List<String> vectorChunks = vectorStore.similaritySearch(question, 5);
        List<String> filtered = new ArrayList<>();
        for (String chunk : vectorChunks) {
            String category = categorizationService.categorize(chunk);
            filtered.add(chunk);
        }
        return filtered.isEmpty() ? vectorChunks : filtered;
    }

    public String ask(String context, String question) {
        String prompt = """
                You are a financial assistant.

                Answer ONLY from below data.

                """ + context + """

                Question: """ + question;

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
