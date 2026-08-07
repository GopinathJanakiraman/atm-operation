package com.mitsubishi.atm.main;

public class StartAtm {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CustomerRepository userRepository = new CustomerInMemoryStorage();
		CustomerService customerService = new CustomerServiceImpl(userRepository);
       AtmOperation cli = new AtmOperation(customerService);
        cli.run();
	}

}
