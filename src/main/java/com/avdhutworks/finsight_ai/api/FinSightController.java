package com.avdhutworks.finsight_ai.api;

import com.avdhutworks.finsight_ai.api.model.QuestionRequest;
import com.avdhutworks.finsight_ai.api.model.StatementSummaryResponse;
import com.avdhutworks.finsight_ai.service.FinSightService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController(value = "/api/v1/finSight")
public class FinSightController {

    @Autowired
    private FinSightService finSightService;

    @PostMapping(value = "/upload/pdf", consumes = "multipart/form-data")
    public String uploadAndProcessStatement(@RequestPart("file") MultipartFile file) {
        try {
            PDDocument document = PDDocument.load(file.getInputStream());
            PDFTextStripper stripper = new PDFTextStripper();

            String text = stripper.getText(document);
            document.close();

            finSightService.storeText(text);

            return "PDF processed and stored successfully";

        } catch (Exception e) {
            return "Error processing statement: " + e.getMessage();
        }
    }

    @PostMapping(value = "/ask", consumes = "application/json")
    public String askQuestion(@RequestBody QuestionRequest questionRequest) {
        List<String> relevantChunks = finSightService.findRelevantChunks(questionRequest.question());
        String context = String.join("\n", relevantChunks);
        return finSightService.sendContentOnAsk(context, questionRequest);
    }

    @GetMapping(value = "/summary")
    public StatementSummaryResponse getSummary() {

        Map<String, Double> categoryMap = finSightService.categorizeSpending();

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
