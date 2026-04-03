package com.avdhutworks.finsight_ai.service;

import com.avdhutworks.finsight_ai.api.model.Merchant;
import com.avdhutworks.finsight_ai.api.model.Transaction;
import com.avdhutworks.finsight_ai.utils.MerchantExtractor;
import com.avdhutworks.finsight_ai.utils.TransactionParser;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsightsService {
    private final CategorizationService categorizationService;
    private final ChatClient chatClient;
    private final DocumentService documentService;
    private final TransactionParser parser = new TransactionParser();
    private final MerchantExtractor merchantExtractor = new MerchantExtractor();

    public InsightsService(CategorizationService categorizationService, DocumentService documentService, ChatClient.Builder builder) {
        this.categorizationService = categorizationService;
        this.documentService = documentService;
        this.chatClient = builder.build();
    }

    public Map<String, Double> calculateCategoryTotals(List<String> chunks) {
        Map<String, Double> categoryMap = new HashMap<>();

        for (String chunk : chunks) {
            List<Transaction> transactions = parser.extractTransactions(chunk);
            for (Transaction txn : transactions) {
                String category = categorizationService.categorize(txn.description());
                categoryMap.put(
                        category,
                        categoryMap.getOrDefault(category, 0.0) + txn.amount()
                );
            }
        }
        return categoryMap;
    }

    public Map<String, Double> calculateMerchantTotals(List<String> chunks) {
        Map<String, Double> merchantMap = new HashMap<>();
        for (String chunk : chunks) {
            List<Transaction> transactions = parser.extractTransactions(chunk);
            for (Transaction txn : transactions) {
                String merchant = merchantExtractor.extractMerchant(txn.description());
                merchantMap.put(
                        merchant,
                        merchantMap.getOrDefault(merchant, 0.0) + txn.amount()
                );
            }
        }
        return merchantMap;
    }

    public List<Merchant> getTopMerchants(Map<String, Double> map) {
        return map.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(entry -> new Merchant(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<String> generateRuleBasedInsights(Map<String, Double> categoryMap) {
        List<String> insights = new ArrayList<>();
        double total = categoryMap.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            double percent = (entry.getValue() / total) * 100;
            if (percent > 40) {
                insights.add("High spending on " + entry.getKey() + " (" + Math.round(percent) + "%)");
            }
            if (entry.getKey().equalsIgnoreCase("Food") && percent > 25) {
                insights.add("Frequent food ordering detected. Consider reducing outside food.");
            }
        }
        return insights;
    }

    public String generateAiInsights(Map<String, Double> categoryMap) {
        String prompt = """
            You are a financial advisor.
            
            Based on the spending data, generate:
            
            1. 2-3 key insights
            2. 2-3 saving suggestions
            
            STRICT RULES:
            - Keep response VERY SHORT
            - Each point must be one line only
            - No paragraphs
            - No explanations
            - Use bullet points
            - Focus only on useful, actionable insights
            
            Format:
            
            Insights:
            - ...
            - ...
            
            Suggestions:
            - ...
            - ...
            
            Data:
            """ + categoryMap.toString();
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    public String answerStructuredQuestion(String question) {
        Map<String, Double> categoryMap =
                calculateCategoryTotals(documentService.getChunks());
        String q = question.toLowerCase();
        if (q.contains("food")) {
            return buildResponse("Food", categoryMap);
        }
        if (q.contains("shopping")) {
            return buildResponse("Shopping", categoryMap);
        }
        if (q.contains("travel")) {
            return buildResponse("Travel", categoryMap);
        }
        if (q.contains("bill") || q.contains("chemist") || q.contains("medical")) {
            return buildResponse("Bills", categoryMap);
        }
        return null; // fallback to LLM
    }

    private String buildResponse(String category, Map<String, Double> map) {
        double amount = map.getOrDefault(category, 0.0);
        if (amount == 0) {
            return "No spending found for " + category + ".";
        }
        return "You spent ₹" + Math.round(amount) + " on " + category + ".";
    }
}
