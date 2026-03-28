package com.avdhutworks.finsight_ai.service;

import com.avdhutworks.finsight_ai.api.model.QuestionRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinSightService {

    private final ChatClient chatClient;
    private final List<String> chunks = new ArrayList<>();

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
            "Food", List.of("swiggy", "zomato", "zepto", "blinkit", "sweets", "juice", "foods", "chicken", "hotel", "restaurant"),
            "Shopping", List.of("amazon", "flipkart", "wear", "mobile", "marketplace", "store", "mart", "retail"),
            "Bills", List.of("medico", "chemist", "hospital", "health", "dental", "clinic", "insurance",
                    "rent", "electricity", "bill", "recharge", "airtel", "jio", "google india digital"),
            "Travel", List.of("uber", "ola", "fuel", "petrol", "metro", "irctc"),
            "Family/Transfer", List.of("parab", "transfer")
    );

    public FinSightService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public void storeText(String text) {
        chunks.clear();

        int chunkSize = 500;
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
        }
    }

    public List<String> getChunks() {
        return chunks;
    }

    public String sendContentOnAsk(String context, QuestionRequest questionRequest) {
        String prompt = """
                        You are a financial assistant.

                        Answer the question ONLY based on the provided transactions.
                        If answer is not found, say "Not enough data".

                        Transactions:
                        """ + context + """

                        Question:
                        """ + questionRequest.question();
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }

    public List<String> findRelevantChunks(String question) {
        List<String> relevantChunks = new ArrayList<>();
        String q = question.toLowerCase();

        String detectedCategory = detectCategoryFromQuestion(q);
        for (String chunk : chunks) {
            String category = categorize(chunk);
            if (detectedCategory != null && category.equalsIgnoreCase(detectedCategory)) {
                relevantChunks.add(chunk);
            }
            else if (q.contains("total") || q.contains("spending")) {
                relevantChunks.add(chunk);
            }
        }
        if (relevantChunks.isEmpty()) {
            return chunks.subList(0, Math.min(3, chunks.size()));
        }
        return relevantChunks;
    }

    private String detectCategoryFromQuestion(String question) {
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (question.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        if (question.contains("food")) return "Food";
        if (question.contains("shopping")) return "Shopping";
        if (question.contains("travel")) return "Travel";
        if (question.contains("bill")) return "Bills";
        return null;
    }

    public Map<String, Double> categorizeSpending() {
        String context = String.join("\n", chunks);
        String prompt = """
            You are a financial assistant.

            Analyze the below bank transactions and return JSON:

            {
              "Food": total_amount,
              "Travel": total_amount,
              "Shopping": total_amount,
              "Bills": total_amount,
              "Others": total_amount
            }

            Only return JSON. No explanation.

            Transactions:
            """ + context;

        try {
            String response = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            return parseJsonToMap(response);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private Map<String, Double> parseJsonToMap(String response) {
        Map<String, Double> categoryMap = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();

            int start = response.indexOf("{");
            int end = response.lastIndexOf("}");
            if (start == -1 || end == -1) return categoryMap;

            String json = response.substring(start, end + 1);
            json = json.replaceAll("(\\d),(\\d)", "$1$2");

            JsonNode root = mapper.readTree(json);
            JsonNode transactions = root.get("transactions");

            if (transactions != null && transactions.isArray()) {
                for (JsonNode txn : transactions) {
                    String description = "";
                    double amount = 0.0;

                    if (txn.has("description") && !txn.get("description").isNull()) {
                        description = txn.get("description").asText();
                    }

                    if (txn.has("debit") && !txn.get("debit").isNull()) {
                        amount = txn.get("debit").asDouble();
                    } else {
                        continue;
                    }
                    if (amount <= 0) continue;
                    if (description.contains("self") || description.contains("own account")) {
                        continue;
                    }
                    String category = categorize(description);
                    categoryMap.put(
                            category,
                            categoryMap.getOrDefault(category, 0.0) + amount
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("Parsing error: " + e.getMessage());
        }

        return categoryMap;
    }

    private String categorize(String description) {
        String desc = description.toLowerCase();
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (desc.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        if (desc.contains("upi") && desc.contains("to")) {
            return "Family/Transfer";
        }
        return "Others";
    }
}
