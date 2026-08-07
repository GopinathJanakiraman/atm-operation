package com.mitsubishi.atm.main;

public class Customer {
	private final String name;
    private double balance;

    public Customer(String name) {
        this.name = name;
        this.balance = 0.0;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void credit(double amount) {
        if (amount <= 0) {
            throw new AtmException("Amount must be positive");
        }
        balance += amount;
    }

    public void debit(double amount) {
        if (amount <= 0) {
            throw new AtmException("Amount must be positive");
        }
        if (balance < amount) {
            throw new AtmException("Insufficient funds");
        }
        balance -= amount;
    }

}
