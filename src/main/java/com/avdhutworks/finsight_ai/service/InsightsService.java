package com.avdhutworks.finsight_ai.service;

import com.avdhutworks.finsight_ai.api.model.Transaction;
import com.avdhutworks.finsight_ai.utils.TransactionParser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsightsService {
    private final CategorizationService categorizationService;
    private final TransactionParser parser = new TransactionParser();

    public InsightsService(CategorizationService categorizationService) {
        this.categorizationService = categorizationService;
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
}
