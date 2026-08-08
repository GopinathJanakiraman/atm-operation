package com.mitsubishi.atm.main;

import java.util.LinkedHashMap;
import java.util.Map;

public class DebtLedger {

    // debtor -> creditor -> amount owed
    private final Map<String, Map<String, Double>> debts =
            new LinkedHashMap<String, Map<String, Double>>();

    /**
     * Returns how much debtor owes creditor.
     */
    public double getDebt(String debtor, String creditor) {
        Map<String, Double> creditors = debts.get(debtor);

        if (creditors == null) {
            return 0.0;
        }

        Double amount = creditors.get(creditor);

        if (amount == null) {
            return 0.0;
        }

        return amount;
    }

    /**
     * Increases the debt owed by debtor to creditor.
     */
    public void addDebt(String debtor, String creditor, double amount) {
        amount = round(amount);

        if (amount <= 0.0) {
            return;
        }

        double current = getDebt(debtor, creditor);
        setDebt(debtor, creditor, current + amount);
    }

    /**
     * Reduces the debt owed by debtor to creditor.
     * Returns the amount actually reduced.
     */
    public double reduceDebt(String debtor, String creditor, double maxAmount) {
        maxAmount = round(maxAmount);

        double current = getDebt(debtor, creditor);

        double applied = Math.min(current, maxAmount);
        applied = round(applied);

        if (applied <= 0.0) {
            return 0.0;
        }

        setDebt(debtor, creditor, current - applied);

        return applied;
    }

    /**
     * Returns all creditors of this debtor.
     *
     * Example:
     * Bob owes Alice 40 and Charlie 10.
     *
     * debtsOf("Bob") returns:
     * {
     *     "Alice": 40,
     *     "Charlie": 10
     * }
     */
    public Map<String, Double> debtsOf(String debtor) {
        Map<String, Double> creditors = debts.get(debtor);

        if (creditors == null) {
            return new LinkedHashMap<String, Double>();
        }

        return new LinkedHashMap<String, Double>(creditors);
    }

    /**
     * Returns all debtors who owe this creditor.
     *
     * Example:
     * Bob owes Alice 40.
     *
     * debtorsOf("Alice") returns:
     * {
     *     "Bob": 40
     * }
     */
    public Map<String, Double> debtorsOf(String creditor) {
        Map<String, Double> result = new LinkedHashMap<String, Double>();

        for (Map.Entry<String, Map<String, Double>> entry : debts.entrySet()) {
            String debtor = entry.getKey();
            Map<String, Double> creditors = entry.getValue();

            Double amount = creditors.get(creditor);

            if (amount != null && amount > 0.0) {
                result.put(debtor, amount);
            }
        }

        return result;
    }

    private void setDebt(String debtor, String creditor, double amount) {
        amount = round(amount);

        Map<String, Double> creditors = debts.get(debtor);

        if (amount <= 0.0) {
            if (creditors != null) {
                creditors.remove(creditor);

                if (creditors.isEmpty()) {
                    debts.remove(debtor);
                }
            }

            return;
        }

        if (creditors == null) {
            creditors = new LinkedHashMap<String, Double>();
            debts.put(debtor, creditors);
        }

        creditors.put(creditor, amount);
    }

    private double round(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
