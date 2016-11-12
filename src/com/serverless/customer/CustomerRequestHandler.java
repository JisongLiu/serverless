package com.serverless.customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.Constants;
import com.serverless.DBManager;

public class CustomerRequestHandler implements RequestHandler<CustomerRequest, CustomerResponse> {	
    private Validator validator;
    private Validator.EmailValidator emailValidator;
    private Validator.PhonenumberValidator phonenumberValidator;
    private Validator.NameValidator nameValidator;

    private Table customerTable = DBManager.getTable(Constants.CUSTOMER_TABLE_NAME);
    
    public CustomerRequestHandler() {
        validator = new Validator();
        emailValidator = validator.new EmailValidator();
        phonenumberValidator = validator.new PhonenumberValidator();
        nameValidator = validator.new NameValidator();
    }
    
    @Override
    public CustomerResponse handleRequest(CustomerRequest request, Context context) {
    	switch (request.operation) {
    	case "create":
        	return createCustomer(request.item);
    	case "query":
        	return queryCustomer(request.item);
    	case "update":
 			return updateCustomer(request.item);
    	case "delete":
 			return deleteCustomer(request.item);
 		default:
 			throw new IllegalArgumentException("400 Bad Request -- unsupported operation " + request.operation);
    	}
    }
    
    public CustomerResponse createCustomer(Customer c) {
        // Validate email field not null
        if (c.email == null) 
            throw new IllegalArgumentException("400 Bad Request -- email is required");
        
        // Validate email format
        if (!emailValidator.validate(c.email))
            throw new IllegalArgumentException("400 Bad Request -- invalid email format");
        
        // Validate phone number format
        if (c.phonenumber != null && !c.phonenumber.isEmpty()) {
            if (!phonenumberValidator.validate(c.phonenumber)) {
                throw new IllegalArgumentException("400 Bad Request -- invalid phone number format");
            }
        }
        
        // Validate name format
        if (c.firstname != null && !c.firstname.isEmpty()) {
        	if (!nameValidator.validate(c.firstname)) {
        		throw new IllegalArgumentException("400 Bad Request -- invalid name format");
        	}
        }
        if (c.lastname != null && !c.lastname.isEmpty()) {
        	if (!nameValidator.validate(c.lastname)) {
        		throw new IllegalArgumentException("400 Bad Request -- invalid name format");
        	}
        }
        
        // Check existence and write item to the table 
        PutItemSpec putItemSpec = new PutItemSpec()
                .withItem(customerToDBItem(c))
                .withConditionExpression("attribute_not_exists(email)");
        try {
            customerTable.putItem(putItemSpec);
            return messageResponse("Success!");
        } catch (ConditionalCheckFailedException e) {
            throw new IllegalArgumentException("400 Bad Request -- email already exists");
        }
    }
    
    public CustomerResponse queryCustomer(Customer c) {
        List<Item> scanResult = new ArrayList<>();
        
        // Look up by email
        if (c.email != null && !c.email.isEmpty()) {                
            // Validate email format
        	if (!emailValidator.validate(c.email))
        		throw new IllegalArgumentException("400 Bad Request -- invalid email format");

            // Check email existence 
            Item customer = customerTable.getItem(new PrimaryKey(Constants.CUSTOMER_EMAIL_KEY, c.email));
            if (customer == null) {
                throw new IllegalArgumentException("404 Not Found -- email does not exist");
            }
            
            // Return customer with the given email
            scanResult.add(customer);
            return dbItemsToResponse(scanResult);
        } else if (c.address_ref != null && !c.address_ref.isEmpty()) {
            // Return a list of customers with the given address
            ScanRequest scanRequest = new ScanRequest().withTableName(Constants.CUSTOMER_TABLE_NAME);
            ScanResult allItems = DBManager.client.scan(scanRequest);
            for (Map<String, AttributeValue> item : allItems.getItems()){
                if (item.get(Constants.CUSTOMER_ADDRESS_KEY) != null && item.get(Constants.CUSTOMER_ADDRESS_KEY).getS().equals(c.address_ref)) {
                    Item customer = customerTable.getItem(Constants.CUSTOMER_EMAIL_KEY, item.get(Constants.CUSTOMER_EMAIL_KEY).getS());
                    scanResult.add(customer);
                }
            }
            
            return dbItemsToResponse(scanResult);
        } else {
            // Return 10 arbitrary items
        	ScanRequest scanRequest = new ScanRequest()
        			.withTableName(Constants.CUSTOMER_TABLE_NAME)
        			.withLimit(Constants.SAMPLE_SIZE);
        	ScanResult sampleItems = DBManager.client.scan(scanRequest);
        	
            CustomerResponse resp = new CustomerResponse("Success");
            for (Map<String, AttributeValue> mapEntry : sampleItems.getItems()) {
                Function<String, String> getField = key -> {
                    return mapEntry.get(key) == null ? null : mapEntry.get(key).getS();
                };
                
                Customer respItem = new Customer();
                respItem.email       = mapEntry.get(Constants.CUSTOMER_EMAIL_KEY).getS();
                respItem.firstname   = getField.apply(Constants.CUSTOMER_FIRSTNAME_KEY);
                respItem.lastname    = getField.apply(Constants.CUSTOMER_LASTNAME_KEY);
                respItem.phonenumber = getField.apply(Constants.CUSTOMER_PHONE_NO_KEY);
                respItem.address_ref = getField.apply(Constants.CUSTOMER_ADDRESS_KEY);
                resp.addItem(respItem);
            }
            
            return resp;
        }
    }
    
    public CustomerResponse updateCustomer(Customer c) {
		Map<String, String> expressName = new HashMap<>();
		Map<String, Object> expressValue = new HashMap<>();

		StringBuilder updateQuery = new StringBuilder();
		updateQuery.append("SET");
		if (c.address_ref != null && c.address_ref.length() > 0){
			expressName.put("#a", Constants.CUSTOMER_ADDRESS_KEY);
			expressValue.put(":val1", c.address_ref);
			updateQuery.append(" #a = :val1,");
		}
		if (c.firstname != null && c.firstname.length() > 0){
			expressName.put("#f", Constants.CUSTOMER_FIRSTNAME_KEY);
			expressValue.put(":val2", c.firstname);
			updateQuery.append(" #f = :val2,");
		}
		if (c.lastname != null && c.lastname.length() > 0){
			expressName.put("#l", Constants.CUSTOMER_LASTNAME_KEY);
			expressValue.put(":val3", c.lastname);
			updateQuery.append(" #l = :val3,");
		}
		if (c.phonenumber != null && c.phonenumber.length() > 0){
            if (!phonenumberValidator.validate(c.phonenumber)) {
                throw new IllegalArgumentException("400 Bad Request -- invalid phone number format");
            }
			expressName.put("#p", Constants.CUSTOMER_PHONE_NO_KEY);
			expressValue.put(":val4", c.phonenumber);
			updateQuery.append(" #p = :val4,");
		}
		String queryString = updateQuery.substring(0, updateQuery.length()-1);
		try {
			customerTable.updateItem("email", c.email, queryString,
					expressName, expressValue);
			return messageResponse("customer successfully updated");

		} catch (Exception e) {
			return messageResponse("customer update failed: " + e.getMessage());
		}
    }
    
    public CustomerResponse deleteCustomer(Customer c) {
        // Validate email format
    	if (!emailValidator.validate(c.email))
    		throw new IllegalArgumentException("400 Bad Request -- invalid email format");
    	
    	// Delete and check email existed before deletion
    	PrimaryKey pkey = new PrimaryKey("email", c.email);
        Item customer = customerTable.getItem(pkey);
        if (customer == null) {
        	throw new IllegalArgumentException("404 Not Found -- email does not exist");
        }
        
    	customerTable.deleteItem(pkey);
        List<Item> result = new ArrayList<>();
        result.add(customer);
        return dbItemsToResponse(result);
    }
    
    private Item customerToDBItem(Customer c) {
        Item customer = new Item();
        customer.withPrimaryKey(Constants.CUSTOMER_EMAIL_KEY, c.email);
        if (c.lastname    != null) customer.withString(Constants.CUSTOMER_LASTNAME_KEY,  c.lastname);
        if (c.firstname   != null) customer.withString(Constants.CUSTOMER_FIRSTNAME_KEY, c.firstname);
        if (c.phonenumber != null) customer.withString(Constants.CUSTOMER_PHONE_NO_KEY,  c.phonenumber);
        if (c.address_ref != null) customer.withString(Constants.CUSTOMER_ADDRESS_KEY,   c.address_ref);
        return customer;
    }
    
    private CustomerResponse dbItemsToResponse(List<Item> items) {
        CustomerResponse resp = new CustomerResponse("Success");
        for (Item item: items) {
            Customer respItem = new Customer();
            respItem.email       = item.getString(Constants.CUSTOMER_EMAIL_KEY);
            respItem.firstname   = item.getString(Constants.CUSTOMER_FIRSTNAME_KEY);
            respItem.lastname    = item.getString(Constants.CUSTOMER_LASTNAME_KEY);
            respItem.phonenumber = item.getString(Constants.CUSTOMER_PHONE_NO_KEY);
            respItem.address_ref = item.getString(Constants.CUSTOMER_ADDRESS_KEY);
            resp.addItem(respItem);
        }
        return resp;
    }
    
    private CustomerResponse messageResponse(String message){
        return new CustomerResponse(message);
    }

}