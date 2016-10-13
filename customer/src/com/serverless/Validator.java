package com.serverless;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
	
	abstract class AbstractValidator {
		private Pattern pattern;
		private Matcher matcher;

		/**
		 * Validate hex with regular expression
		 *
		 * @param hex
		 *            hex for validation
		 * @return true valid hex, false invalid hex
		 */
		public boolean validate(final String hex) {

			matcher = pattern.matcher(hex);
			return matcher.matches();

		}
	}
	
	class EmailValidator extends AbstractValidator{

		private static final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		public EmailValidator() {
			super.pattern = Pattern.compile(EMAIL_PATTERN);
		}
	}
	
	class PhonenumberValidator extends AbstractValidator {
		
		private static final String PHONENUMBER_PATTERN = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
		
		public PhonenumberValidator() {
			super.pattern = Pattern.compile(PHONENUMBER_PATTERN);
		}
	}
}
