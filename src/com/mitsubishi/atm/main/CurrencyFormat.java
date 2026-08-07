package com.mitsubishi.atm.main;

import java.util.Locale;

public class CurrencyFormat {
	 public static double round(double amount) {
	        return Math.round(amount * 100.0) / 100.0;
	    }

	    public static String format(double amount) {
	        double rounded = round(amount);

	        if (Math.abs(rounded) < 0.005) {
	            return "$0";
	        }

	        if (rounded == Math.rint(rounded)) {
	            return "$" + (long) rounded;
	        }

	        return String.format(Locale.US, "$%.2f", rounded);
	    }
}
