package com.avdhutworks.finsight_ai.service;

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
    private final TransactionParser parser = new TransactionParser();
    private final MerchantExtractor merchantExtractor = new MerchantExtractor();

    public InsightsService(CategorizationService categorizationService, ChatClient.Builder builder) {
        this.categorizationService = categorizationService;
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

    public List<Map.Entry<String, Double>> getTopMerchants(Map<String, Double> map) {
        return map.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
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

            Analyze the spending data and provide:
            - Key observations
            - Saving suggestions

            Keep response short and practical.

            Data:
            """ + categoryMap.toString();
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
