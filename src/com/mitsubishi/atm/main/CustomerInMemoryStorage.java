package com.mitsubishi.atm.main;

import java.util.HashMap;
import java.util.Map;

public class CustomerInMemoryStorage implements  CustomerRepository{
	private final Map<String, Customer> customers = new HashMap<>();

	@Override
	public Customer findByName(String name) {
		// TODO Auto-generated method stub
		return customers.get(name);
	}

	@Override
	public Customer findOrCreate(String name) {
		// TODO Auto-generated method stub
		return customers.computeIfAbsent(name, Customer::new);
	}

	@Override
	public boolean exists(String name) {
		// TODO Auto-generated method stub
		return customers.containsKey(name);
	}

}
