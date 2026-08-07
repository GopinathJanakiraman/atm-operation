package com.mitsubishi.atm.main;

public interface CustomerRepository {
	Customer findByName(String name);
	Customer findOrCreate(String name);
    boolean exists(String name);
}
