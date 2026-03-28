package com.avdhutworks.finsight_ai.utils;

public class MerchantExtractor {

    public String extractMerchant(String description) {
        String desc = description.toLowerCase();
        desc = desc.replaceAll("upi-|neft-|imps-|pos-|atm-", "");
        desc = desc.replaceAll("[^a-zA-Z ]", " ");
        desc = desc.replaceAll("\\s+", " ").trim();
        String[] words = desc.split(" ");
        StringBuilder merchant = new StringBuilder();

        for (String word : words) {
            if (word.length() > 3 &&
                    !isNoise(word)) {
                merchant.append(capitalize(word)).append(" ");
            }
        }
        String result = merchant.toString().trim();
        return result.isEmpty() ? "Others" : result;
    }

    private boolean isNoise(String word) {
        return word.equals("upi") ||
                word.equals("bank") ||
                word.equals("transfer") ||
                word.equals("payment") ||
                word.equals("ref") ||
                word.equals("to");
    }

    private String capitalize(String word) {
        return word.substring(0,1).toUpperCase() + word.substring(1);
    }
}
