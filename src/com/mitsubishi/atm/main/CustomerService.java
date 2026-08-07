package com.mitsubishi.atm.main;

import java.util.List;

public interface CustomerService {
	 List<String> login(String name);

	    List<String> deposit(double amount);

	    List<String> withdraw(double amount);

	    List<String> transfer(String targetName, double amount);

	    List<String> logout();

	    List<String> accountSummary();

	    double getBalance();

	    String getcurrentCustomerName();

	    boolean isLoggedIn();
}
