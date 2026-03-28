package com.avdhutworks.finsight_ai.api.model;

import java.util.List;
import java.util.Map;

public record FinancialInsightsResponse(
        Map<String, Double> categoryTotals,
        Map<String, Double> merchantTotals,
        List<Map.Entry<String, Double>> topMerchants,
        List<String> ruleInsights,
        String aiInsights
) {
}
