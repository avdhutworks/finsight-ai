package com.avdhutworks.finsight_ai.api;

import com.avdhutworks.finsight_ai.api.model.QuestionRequest;
import com.avdhutworks.finsight_ai.service.DocumentService;
import com.avdhutworks.finsight_ai.service.InsightsService;
import com.avdhutworks.finsight_ai.service.RagService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finSight")
public class FinSightController {

    private final DocumentService documentService;
    private final RagService ragService;
    private final InsightsService insightsService;

    public FinSightController(DocumentService documentService,
                              RagService ragService,
                              InsightsService insightsService) {
        this.documentService = documentService;
        this.ragService = ragService;
        this.insightsService = insightsService;
    }

    @PostMapping(value = "/upload/pdf", consumes = "multipart/form-data")
    public String uploadAndProcessStatement(
            @Parameter(
                    description = "File to upload",
                    content = @Content(mediaType = "application/octet-stream")
            )
            @RequestParam("file") MultipartFile file) {
        try {
            String text = new PDFTextStripper()
                    .getText(PDDocument.load(file.getInputStream()));
            documentService.storeText(text);
            return "PDF processed and stored successfully";
        } catch (Exception e) {
            return "Error processing statement: " + e.getMessage();
        }
    }

    @PostMapping(value = "/ask", consumes = "application/json")
    public String askQuestion(@RequestBody QuestionRequest questionRequest) {
        List<String> chunks = ragService.getHybridChunks(questionRequest.question());
        String context = String.join("\n", chunks);
        return ragService.ask(context, questionRequest.question());
    }
}
