package com.serverless.address;

public class ZIPValidator {

	
	public boolean validate(final String zip) {

		String zipCodePattern = "\\d{5}(-\\d{4})?";
    	return zip.matches(zipCodePattern);

	}
}