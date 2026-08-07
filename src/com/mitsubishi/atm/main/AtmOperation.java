package com.mitsubishi.atm.main;

import java.util.List;
import java.util.Scanner;

public class AtmOperation {
	private final CustomerService customerService;
    private final Scanner scanner;
    private boolean running = true;
    
    public AtmOperation(CustomerService customerService) {
        this.customerService = customerService;
        this.scanner = new Scanner(System.in);
    }
    
    public void run() {
        System.out.println("Mitsubishi ATM");
        System.out.println("Type 'help' for available commands");

        while (running) {
            System.out.print("atm> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            try {
                processCommand(cmd, parts);
            } catch (AtmException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format");
            }
        }
    }
    
    private void processCommand(String cmd, String[] parts) {
        switch (cmd) {
            case "login" : {
                if (parts.length < 2) {
                    System.out.println("Usage: login [name]");
                } else {
                    printLines(customerService.login(parts[1]));
                }
            }
            break;

            case "deposit" : {
                if (parts.length < 2) {
                    System.out.println("Usage: deposit [amount]");
                } else {
                    double amount = parseAmount(parts[1]);
                    printLines(customerService.deposit(amount));
                }
            }
            break;
            case "withdraw" : 
                if (parts.length < 2) {
                    System.out.println("Usage: withdraw [amount]");
                } else {
                    double amount = parseAmount(parts[1]);
                    printLines(customerService.withdraw(amount));
                }
                break;

            case "transfer" : {
                if (parts.length < 3) {
                    System.out.println("Usage: transfer [target] [amount]");
                } else {
                    double amount = parseAmount(parts[2]);
                    printLines(customerService.transfer(parts[1], amount));
                }
            }
            break;

            case "logout" : printLines(customerService.logout());break;

            case "balance" : printLines(customerService.accountSummary());break;

            case "help" : printHelp();break;

            case "exit" : {
                running = false;
                System.out.println("ATM session ended.");
            }
            break;
            case  "quit" : {
                running = false;
                System.out.println("ATM session ended.");
            }
            break;
            default : {
                System.out.println("Unknown command: " + cmd);
                printHelp();
            }
        }
    }

    private double parseAmount(String text) {
        String cleaned = text.replace("$", "").replace(",", "");
        return Double.parseDouble(cleaned);
    }

    private void printLines(List<String> lines) {
        for (String line : lines) {
            System.out.println(line);
        }
    }

    private void printHelp() {
    	System.out.println("Available commands:\n" +
    	        "  login [name]               Log in as customer (creates if new)\n" +
    	        "  deposit [amount]           Deposit amount\n" +
    	        "  withdraw [amount]          Withdraw amount\n" +
    	        "  transfer [target] [amount] Transfer amount to target\n" +
    	        "  logout                     Log out\n" +
    	        "  balance                    Show balance\n" +
    	        "  help                       Show this help\n" +
    	        "  exit / quit                Exit application");

    }
}
