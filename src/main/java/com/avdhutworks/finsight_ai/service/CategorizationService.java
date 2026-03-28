package com.avdhutworks.finsight_ai.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CategorizationService {

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
            "Food", List.of("swiggy", "zomato", "zepto", "blinkit", "sweets"),
            "Shopping", List.of("amazon", "flipkart", "wear"),
            "Bills", List.of("medico", "chemist", "rent", "electricity"),
            "Travel", List.of("uber", "ola", "fuel"),
            "Family/Transfer", List.of("parab", "transfer")
    );

    public String categorize(String description) {
        String desc = description.toLowerCase();
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (desc.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return "Others";
    }
}
