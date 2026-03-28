package com.avdhutworks.finsight_ai.api.model;

import java.util.Map;

public record StatementSummaryResponse(
        double totalSpending,
        String topCategory,
        Map<String, Double> categoryBreakdown
){}
