/**
 * 
 */
package com.mitsubishi.atm.main;

/**
 * 
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomerServiceImpl implements CustomerService {
    private static final double EPS = 1e-9;

    private final CustomerRepository customerRepository;
    private final DebtLedger debts;
    private Customer currentCustomer;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this(customerRepository, new DebtLedger());
    }

    public CustomerServiceImpl(CustomerRepository customerRepository, DebtLedger debts) {
        this.customerRepository = customerRepository;
        this.debts = debts;
    }

    @Override
    public List<String> login(String name) {
        String normalized = name == null ? "" : name.trim();

        if (normalized.isEmpty()) {
            throw new AtmException("Name is required");
        }

        currentCustomer = customerRepository.findOrCreate(normalized);

        List<String> lines = new ArrayList<>();
        lines.add("Hello, " + currentCustomer.getName() + "!");
        lines.addAll(summaryLines(currentCustomer));

        return lines;
    }

    @Override
    public List<String> deposit(double amount) {
        ensureLoggedIn();

        double remaining = validateAndRound(amount);
        List<String> lines = new ArrayList<>();

        Map<String, Double> creditors = debts.debtsOf(currentCustomer.getName());

        for (Map.Entry<String, Double> entry : creditors.entrySet()) {
            if (remaining <= EPS) {
                break;
            }

            String creditorName = entry.getKey();
            double owed = entry.getValue();
            double requestedPayment = CurrencyFormat.round(Math.min(remaining, owed));

            if (requestedPayment <= EPS) {
                continue;
            }

            double applied = debts.reduceDebt(currentCustomer.getName(), creditorName, requestedPayment);

            if (applied <= EPS) {
                continue;
            }

            Customer creditor = customerRepository.findByName(creditorName);
            if (creditor == null) {
                creditor = customerRepository.findOrCreate(creditorName);
            }

            creditor.credit(applied);

            lines.add("Transferred " + CurrencyFormat.format(applied) + " to " + creditorName);
            remaining = CurrencyFormat.round(remaining - applied);
        }

        if (remaining > EPS) {
            currentCustomer.credit(remaining);
        }

        lines.addAll(summaryLines(currentCustomer));
        return lines;
    }

    @Override
    public List<String> withdraw(double amount) {
        ensureLoggedIn();

        double value = validateAndRound(amount);
        currentCustomer.debit(value);

        return new ArrayList<>(summaryLines(currentCustomer));
    }

    @Override
    public List<String> transfer(String targetName, double amount) {
        ensureLoggedIn();

        double remaining = validateAndRound(amount);

        String target = targetName == null ? "" : targetName.trim();

        if (target.isEmpty()) {
            throw new AtmException("Target is required");
        }

        if (currentCustomer.getName().equals(target)) {
            throw new AtmException("Cannot transfer to yourself");
        }

        Customer targetUser = customerRepository.findByName(target);

        if (targetUser == null) {
            throw new AtmException("Target user does not exist");
        }

        List<String> lines = new ArrayList<>();
        double cashMoved = 0.0;

        /*
         * 1) If the target owes the current user CurrencyFormat,
         *    transferring to the target first reduces that debt.
         *
         * Example:
         * Bob owes Alice 40.
         * Alice transfers Bob 30.
         * Bob now owes Alice 10, and Alice's balance does not change.
         */
        double owedByTarget = debts.getDebt(targetUser.getName(), currentCustomer.getName());

        if (owedByTarget > EPS && remaining > EPS) {
            double applied = debts.reduceDebt(
                    targetUser.getName(),
                    currentCustomer.getName(),
                    remaining
            );

            remaining = CurrencyFormat.round(remaining - applied);
        }

        /*
         * 2) If the current user already owes the target CurrencyFormat,
         *    use available balance to pay that debt first.
         */
        double owedToTarget = debts.getDebt(currentCustomer.getName(), targetUser.getName());

        if (owedToTarget > EPS && remaining > EPS) {
            double cash = CurrencyFormat.round(Math.min(
                    currentCustomer.getBalance(),
                    Math.min(owedToTarget, remaining)
            ));

            if (cash > EPS) {
                currentCustomer.debit(cash);
                targetUser.credit(cash);

                double repaid = debts.reduceDebt(
                        currentCustomer.getName(),
                        targetUser.getName(),
                        cash
                );

                cashMoved = CurrencyFormat.round(cashMoved + repaid);
                remaining = CurrencyFormat.round(remaining - repaid);
            }
        }

        /*
         * 3) Transfer any remaining requested amount using available balance.
         */
        if (remaining > EPS) {
            double cash = CurrencyFormat.round(Math.min(currentCustomer.getBalance(), remaining));

            if (cash > EPS) {
                currentCustomer.debit(cash);
                targetUser.credit(cash);

                cashMoved = CurrencyFormat.round(cashMoved + cash);
                remaining = CurrencyFormat.round(remaining - cash);
            }
        }

        if (cashMoved > EPS) {
            lines.add("Transferred " + CurrencyFormat.format(cashMoved) + " to " + targetUser.getName());
        }

        /*
         * 4) If there is still an unfulfilled amount,
         *    it becomes debt owed by the current user to the target.
         */
        if (remaining > EPS) {
            debts.addDebt(currentCustomer.getName(), targetUser.getName(), remaining);
        }

        lines.addAll(summaryLines(currentCustomer));
        return lines;
    }

    @Override
    public List<String> logout() {
        ensureLoggedIn();

        String name = currentCustomer.getName();
        currentCustomer = null;
        List<String> list = Arrays.asList("Goodbye, " + name + "!");

        return list;
    }

    @Override
    public List<String> accountSummary() {
        ensureLoggedIn();
        return summaryLines(currentCustomer);
    }

    @Override
    public double getBalance() {
        ensureLoggedIn();
        return currentCustomer.getBalance();
    }

    @Override
    public String getcurrentCustomerName() {
        return currentCustomer != null ? currentCustomer.getName() : null;
    }

    @Override
    public boolean isLoggedIn() {
        return currentCustomer != null;
    }

    private void ensureLoggedIn() {
        if (currentCustomer == null) {
            throw new AtmException("Not logged in");
        }
    }

    private double validateAndRound(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new AtmException("Amount must be positive");
        }

        double rounded = CurrencyFormat.round(amount);

        if (rounded <= EPS) {
            throw new AtmException("Amount must be positive");
        }

        return rounded;
    }

    private List<String> summaryLines(Customer customer) {
        List<String> lines = new ArrayList<>();

        lines.add("Your balance is " + CurrencyFormat.format(customer.getBalance()));

        debts.debtsOf(customer.getName()).forEach((creditor, owed) ->
                lines.add("Owed " + CurrencyFormat.format(owed) + " to " + creditor)
        );

        debts.debtorsOf(customer.getName()).forEach((debtor, owed) ->
                lines.add("Owed " + CurrencyFormat.format(owed) + " from " + debtor)
        );

        return lines;
    }

	
}
