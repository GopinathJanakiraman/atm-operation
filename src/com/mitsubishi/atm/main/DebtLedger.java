package com.mitsubishi.atm.main;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DebtLedger {
    private static final double EPS = 1e-9;

    // debtor -> creditor -> amount owed
    private final Map<String, Map<String, Double>> debtorToCreditor = new LinkedHashMap<>();

    // creditor -> debtor -> amount owed
    private final Map<String, Map<String, Double>> creditorToDebtor = new LinkedHashMap<>();

    public double getDebt(String debtor, String creditor) {
        Map<String, Double> creditors = debtorToCreditor.get(debtor);
        if (creditors == null) {
            return 0.0;
        }
        return creditors.getOrDefault(creditor, 0.0);
    }

    public void addDebt(String debtor, String creditor, double amount) {
        double rounded = CurrencyFormat.round(amount);
        if (rounded <= EPS) {
            return;
        }

        double current = getDebt(debtor, creditor);
        setDebt(debtor, creditor, CurrencyFormat.round(current + rounded));
    }

    public double reduceDebt(String debtor, String creditor, double maxAmount) {
        double current = getDebt(debtor, creditor);
        double applied = Math.min(current, CurrencyFormat.round(maxAmount));
        applied = CurrencyFormat.round(applied);

        if (applied <= EPS) {
            return 0.0;
        }

        setDebt(debtor, creditor, CurrencyFormat.round(current - applied));
        return applied;
    }

    public Map<String, Double> debtsOf(String debtor) {
        Map<String, Double> creditors = debtorToCreditor.get(debtor);
        if (creditors == null) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(creditors);
    }

    public Map<String, Double> debtorsOf(String creditor) {
        Map<String, Double> debtors = creditorToDebtor.get(creditor);
        if (debtors == null) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(debtors);
    }

    private void setDebt(String debtor, String creditor, double amount) {
        double rounded = CurrencyFormat.round(amount);

        if (rounded <= EPS) {
            Map<String, Double> creditors = debtorToCreditor.get(debtor);
            if (creditors != null) {
                creditors.remove(creditor);
                if (creditors.isEmpty()) {
                    debtorToCreditor.remove(debtor);
                }
            }

            Map<String, Double> debtors = creditorToDebtor.get(creditor);
            if (debtors != null) {
                debtors.remove(debtor);
                if (debtors.isEmpty()) {
                    creditorToDebtor.remove(creditor);
                }
            }

            return;
        }

        debtorToCreditor
                .computeIfAbsent(debtor, k -> new LinkedHashMap<>())
                .put(creditor, rounded);

        creditorToDebtor
                .computeIfAbsent(creditor, k -> new LinkedHashMap<>())
                .put(debtor, rounded);
    }
}
