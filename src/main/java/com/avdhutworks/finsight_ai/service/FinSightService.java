package com.avdhutworks.finsight_ai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinSightService {

    private final List<String> chunks = new ArrayList<>();

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
}
