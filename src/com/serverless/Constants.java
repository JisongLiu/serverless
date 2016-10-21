package com.serverless;

public final class Constants {
	public static final int SAMPLE_SIZE = 10; // number of customer/address returned when a sample is requested
	
	public static final String CUSTOMER_TABLE_NAME    = "Customer";
	public static final String CUSTOMER_EMAIL_KEY     = "email";
	public static final String CUSTOMER_ADDRESS_KEY   = "address_ref";
	public static final String CUSTOMER_FIRSTNAME_KEY = "firstname";
	public static final String CUSTOMER_LASTNAME_KEY  = "lastname";
	public static final String CUSTOMER_PHONE_NO_KEY  = "phonenumber";
	
	public static final String ADDRESS_TABLE_NAME  = "Address";
	public static final String ADDRESS_ID_KEY      = "id";
	public static final String ADDRESS_LINE1_KEY   = "line1";
	public static final String ADDRESS_LINE2_KEY   = "line2";
	public static final String ADDRESS_CITY_KEY    = "city";
	public static final String ADDRESS_STATE_KEY   = "state";
	public static final String ADDRESS_ZIPCODE_KEY = "zipcode";
}
