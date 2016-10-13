package com.serverless.asgn1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZIPValidator {

	
	public boolean validate(final String zip) {

		String zipCodePattern = "\\d{5}(-\\d{4})?";
    	return zip.matches(zipCodePattern);

	}
}