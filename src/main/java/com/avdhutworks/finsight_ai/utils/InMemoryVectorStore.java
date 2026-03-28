package com.avdhutworks.finsight_ai.utils;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component(value = "vectorStore")
public class InMemoryVectorStore {
    private final EmbeddingModel embeddingModel;
    private final List<String> documents = new ArrayList<>();
    private final List<float[]> embeddings = new ArrayList<>();

    public InMemoryVectorStore(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public void addDocuments(List<String> chunks) {
        for (String chunk : chunks) {
            float[] vector = embeddingModel.embed(chunk);
            documents.add(chunk);
            embeddings.add(vector);
        }
    }

    public List<String> similaritySearch(String query, int topK) {
        float[] queryVector = embeddingModel.embed(query);
        List<Map.Entry<String, Double>> scored = new ArrayList<>();

        for (int i = 0; i < embeddings.size(); i++) {
            double score = cosineSimilarity(queryVector, embeddings.get(i));
            scored.add(Map.entry(documents.get(i), score));
        }

        return scored.stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .toList();
    }

    private double cosineSimilarity(float[] v1, float[] v2) {
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
