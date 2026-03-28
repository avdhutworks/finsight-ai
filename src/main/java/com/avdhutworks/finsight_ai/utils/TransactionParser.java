package com.avdhutworks.finsight_ai.utils;

import com.avdhutworks.finsight_ai.api.model.Transaction;

import java.util.*;
import java.util.regex.*;

public class TransactionParser {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\d+\\.\\d{2}");

    public List<Transaction> extractTransactions(String text) {
        List<Transaction> transactions = new ArrayList<>();
        List<String> lines = Arrays.asList(text.split("\n"));

        for (String line : lines) {
            if (!isTransactionLine(line)) continue;
            double amount = extractAmountFromLine(line);
            if (amount > 0) {
                transactions.add(new Transaction(line, amount));
            }
        }
        return transactions;
    }

    private boolean isTransactionLine(String line) {
        String l = line.toLowerCase();
        return l.contains("upi") ||
                l.contains("atm") ||
                l.contains("pos") ||
                l.contains("imps") ||
                l.contains("neft") ||
                l.contains("card");
    }

    private double extractAmountFromLine(String line) {
        try {
            String clean = line.replaceAll(",", "");
            Matcher matcher = AMOUNT_PATTERN.matcher(clean);
            List<Double> values = new ArrayList<>();
            while (matcher.find()) {
                values.add(Double.parseDouble(matcher.group()));
            }
            if (values.size() < 2) return 0;

            // Case: [debit, balance]
            if (values.size() == 2) {
                return values.get(0);
            }

            // Case: [withdrawal, deposit, balance]
            if (values.size() >= 3) {
                double withdrawal = values.get(0);
                double deposit = values.get(1);
                return withdrawal > 0 ? withdrawal : 0;
            }
        } catch (Exception e) {
            System.out.println("Parsing error: " + e.getMessage());
        }
        return 0;
    }
}