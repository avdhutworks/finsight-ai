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
        return chatClient
                .prompt()
                .user("You are a financial assistant. Based on below data:\n"
                        + context +
                        "\nAnswer this question: " + questionRequest.question())
                .call()
                .content();
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
        if (desc.contains("swiggy") || desc.contains("zomato") ||
                desc.contains("zepto") || desc.contains("blinkit") ||
                desc.contains("sweets") || desc.contains("juice") ||
                desc.contains("foods") || desc.contains("chicken") ||
                desc.contains("hotel") || desc.contains("restaurant")) {
            return "Food";
        }
        if (desc.contains("amazon") || desc.contains("flipkart") ||
                desc.contains("wear") || desc.contains("mobile") ||
                desc.contains("marketplace") || desc.contains("store") ||
                desc.contains("mart") || desc.contains("retail")) {
            return "Shopping";
        }
        if (desc.contains("medico") || desc.contains("chemist") ||
                desc.contains("hospital") || desc.contains("health") ||
                desc.contains("dental") || desc.contains("clinic") ||
                desc.contains("insurance")) {
            return "Bills";
        }
        if (desc.contains("rent") || desc.contains("electricity") ||
                desc.contains("bill") || desc.contains("recharge") ||
                desc.contains("airtel") || desc.contains("jio") ||
                desc.contains("google india digital")) {
            return "Bills";
        }
        if (desc.contains("uber") || desc.contains("ola") ||
                desc.contains("fuel") || desc.contains("petrol") ||
                desc.contains("metro") || desc.contains("irctc")) {
            return "Travel";
        }
        if (desc.contains("parab") || desc.contains("transfer") ||
                desc.contains("upi") && desc.contains("to")) {
            return "Family/Transfer";
        }

        return "Others";
    }
}
