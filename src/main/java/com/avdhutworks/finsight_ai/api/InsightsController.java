package com.avdhutworks.finsight_ai.api;

import com.avdhutworks.finsight_ai.api.model.StatementSummaryResponse;
import com.avdhutworks.finsight_ai.service.DocumentService;
import com.avdhutworks.finsight_ai.service.InsightsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/insights")
public class InsightsController {

    private final DocumentService documentService;
    private final InsightsService insightsService;

    public InsightsController(DocumentService documentService,
                              InsightsService insightsService) {
        this.documentService = documentService;
        this.insightsService = insightsService;
    }

    @GetMapping("/summary")
    public StatementSummaryResponse getSummary() {
        List<String> chunks = documentService.getChunks();
        Map<String, Double> categoryMap =
                insightsService.calculateCategoryTotals(chunks);
        double total = categoryMap.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        String topCategory = categoryMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        return new StatementSummaryResponse(
                total,
                topCategory,
                categoryMap
        );
    }
}
