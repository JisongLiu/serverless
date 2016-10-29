package com.serverless.customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class CustomerRequestHandler implements RequestHandler<CustomerRequest, CustomerResponse> {	
    private Validator validator;
    private Validator.EmailValidator emailValidator;
    private Validator.PhonenumberValidator phonenumberValidator;
    
    public CustomerRequestHandler() {
        validator = new Validator();
        emailValidator = validator.new EmailValidator();
        phonenumberValidator = validator.new PhonenumberValidator();
    }
    
    @Override
    public CustomerResponse handleRequest(CustomerRequest request, Context context) {
//        context.getLogger().log("Input: " + request.toString());
        
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table customerTable = dynamoDB.getTable("Customer");
        
        // Create operation
        if (request.operation.equals("create")) {
        
            // Validate email field not null
            if (request.item.email == null) 
                throw new IllegalArgumentException("400 Bad Request -- email is required");
            
            // Validate email format
            if (!emailValidator.validate(request.item.email))
                throw new IllegalArgumentException("400 Bad Request -- invalid email format");
            
            // Validate phone number format
            if (request.item.phonenumber != null && !request.item.phonenumber.isEmpty()) {
                if (!phonenumberValidator.validate(request.item.phonenumber)) {
                    throw new IllegalArgumentException("400 Bad Request -- invalid phone number format");
                }
            }
            
            // Check existence and write item to the table 
            PutItemSpec putItemSpec = new PutItemSpec()
                    .withItem(addCustomer(request))
                    .withConditionExpression("attribute_not_exists(email)");
            try {
                customerTable.putItem(putItemSpec);
                return messageResponse("Success!");
            } catch (ConditionalCheckFailedException e) {
                throw new IllegalArgumentException("400 Bad Request -- email already exists");
            }
        }
        
        // Query operation
        else if (request.operation.equals("query")) {
            List<Item> scanResult = new ArrayList<>();
            
            // Look up by email
            if (request.item.email != null && !request.item.email.isEmpty()) {                
                // Validate email format
            	if (!emailValidator.validate(request.item.email))
            		throw new IllegalArgumentException("400 Bad Request -- invalid email format");

                // Check email existence 
                Item customer = customerTable.getItem(new PrimaryKey(Constants.CUSTOMER_EMAIL_KEY, request.item.email));
                if (customer == null) {
                    throw new IllegalArgumentException("404 Not Found -- email does not exist");
                }
                
                if (request.item.address_ref != null && request.item.address_ref.equals("requested")) {
                	// Return an address
                    String addressId = customer.getString(Constants.CUSTOMER_ADDRESS_KEY);
                    if (addressId == null) {
                    	throw new IllegalArgumentException("400 Bad Request -- customer does not have an address");
                    }

                    Table addressTable = dynamoDB.getTable("Address");
                    Item address = addressTable.getItem(new PrimaryKey(Constants.ADDRESS_ID_KEY, addressId));
                    if (address == null) {
                    	throw new IllegalArgumentException("404 Not Found -- address not found");
                    }

                    CustomerResponse resp = new CustomerResponse("Success");
                    Customer addressEntry = new Customer();
                    String addressString = address.getString(Constants.ADDRESS_LINE1_KEY) + " " +
                    		address.getString(Constants.ADDRESS_LINE2_KEY) + ", " +
                    		address.getString(Constants.ADDRESS_CITY_KEY) + ", " +
                    		address.getString(Constants.ADDRESS_STATE_KEY) + " " +
                    		address.getString(Constants.ADDRESS_ZIPCODE_KEY);
                    addressEntry.address_ref = addressString;
                    resp.items.add(addressEntry);
                    
                    return resp;
                } else {
	                // Return customer with the given email
	                scanResult.add(customer);
	                return queryResponse(scanResult);
                }
                
            // Look up by address
            } else if (request.item.address_ref != null && !request.item.address_ref.isEmpty()) {

                // Return a list of customers with the given address
                ScanRequest scanRequest = new ScanRequest().withTableName(Constants.CUSTOMER_TABLE_NAME);
                ScanResult allItems = client.scan(scanRequest);
                for (Map<String, AttributeValue> item : allItems.getItems()){
                    if (item.get(Constants.CUSTOMER_ADDRESS_KEY) != null && item.get(Constants.CUSTOMER_ADDRESS_KEY).getS().equals(request.item.address_ref)) {
                        Item customer = customerTable.getItem(Constants.CUSTOMER_EMAIL_KEY, item.get(Constants.CUSTOMER_EMAIL_KEY).getS());
                        scanResult.add(customer);
                    }
                }
                
                return queryResponse(scanResult);
                
            // return 10 random items
            } else {
            	ScanRequest scanRequest = new ScanRequest()
            			.withTableName(Constants.CUSTOMER_TABLE_NAME)
            			.withLimit(Constants.SAMPLE_SIZE);
            	ScanResult sampleItems = client.scan(scanRequest);
            	
            	// TODO: clean up this code (lots of overlapping with queryResponse)
                CustomerResponse resp = new CustomerResponse("Success");
                for (Map<String, AttributeValue> mapEntry : sampleItems.getItems()) {
                    Customer respItem = new Customer();
                    respItem.email       = mapEntry.get(Constants.CUSTOMER_EMAIL_KEY).getS();
                    respItem.firstname   = mapEntry.get(Constants.CUSTOMER_FIRSTNAME_KEY) == null ? 
                    		null : mapEntry.get(Constants.CUSTOMER_FIRSTNAME_KEY).getS();
                    respItem.lastname    = mapEntry.get(Constants.CUSTOMER_LASTNAME_KEY) == null ? 
                    		null : mapEntry.get(Constants.CUSTOMER_LASTNAME_KEY).getS();
                    respItem.phonenumber = mapEntry.get(Constants.CUSTOMER_PHONE_NO_KEY) == null ? 
                    		null : mapEntry.get(Constants.CUSTOMER_PHONE_NO_KEY).getS();
                    respItem.address_ref = mapEntry.get(Constants.CUSTOMER_ADDRESS_KEY) == null ? 
                    		null : mapEntry.get(Constants.CUSTOMER_ADDRESS_KEY).getS();
                    resp.addItem(respItem);
                }
                
                return resp;
            }
        }
        
        // Update operation
 		else if (request.operation.equals("update")) {

 			Map<String, String> expressName = new HashMap<>();
 			Map<String, Object> expressValue = new HashMap<>();

 			StringBuilder updateQuery = new StringBuilder();
 			updateQuery.append("SET");
 			if (request.item.address_ref != null && request.item.address_ref.length() > 0){
 				expressName.put("#a", Constants.CUSTOMER_ADDRESS_KEY);
 				expressValue.put(":val1", request.item.address_ref);
 				updateQuery.append(" #a = :val1,");
 			}
 			if (request.item.firstname != null && request.item.firstname.length() > 0){
 				expressName.put("#f", Constants.CUSTOMER_FIRSTNAME_KEY);
 				expressValue.put(":val2", request.item.firstname);
 				updateQuery.append(" #f = :val2,");
 			}
 			if (request.item.lastname != null && request.item.lastname.length() > 0){
 				expressName.put("#l", Constants.CUSTOMER_LASTNAME_KEY);
 				expressValue.put(":val3", request.item.lastname);
 				updateQuery.append(" #l = :val3,");
 			}
 			if (request.item.phonenumber != null && request.item.phonenumber.length() > 0){
                if (!phonenumberValidator.validate(request.item.phonenumber)) {
                    throw new IllegalArgumentException("400 Bad Request -- invalid phone number format");
                }
 				expressName.put("#p", Constants.CUSTOMER_PHONE_NO_KEY);
 				expressValue.put(":val4", request.item.phonenumber);
 				updateQuery.append(" #p = :val4,");
 			}
 			String queryString = updateQuery.substring(0, updateQuery.length()-1);
 			try {
 				customerTable.updateItem("email", request.item.email, queryString,
 						expressName, expressValue);
 				return messageResponse("success updated");

 			} catch (Exception e) {
 				return messageResponse("failure!");
 			}
 		}
        
        // Delete operation
        else if (request.operation.equals("delete")) {
            // Validate email format
        	if (!emailValidator.validate(request.item.email))
        		throw new IllegalArgumentException("400 Bad Request -- invalid email format");
        	
        	// Delete and check email existed before deletion
        	PrimaryKey pkey = new PrimaryKey("email", request.item.email);
            Item customer = customerTable.getItem(pkey);
            if (customer == null) {
            	throw new IllegalArgumentException("404 Not Found -- email does not exist");
            }
            
        	customerTable.deleteItem(pkey);
            List<Item> result = new ArrayList<>();
            result.add(customer);
            return queryResponse(result);
        }
        
        throw new IllegalArgumentException("400 Bad Request -- invalid request");
    }
    
    public Item addCustomer(CustomerRequest request) {
        Item customer = new Item();
        customer.withPrimaryKey(Constants.CUSTOMER_EMAIL_KEY, request.item.email);
        if (request.item.lastname!=null) customer.withString(Constants.CUSTOMER_LASTNAME_KEY, request.item.lastname);
        if (request.item.firstname!=null) customer.withString(Constants.CUSTOMER_FIRSTNAME_KEY, request.item.firstname);
        if (request.item.phonenumber!=null) customer.withString(Constants.CUSTOMER_PHONE_NO_KEY, request.item.phonenumber);
        if (request.item.address_ref!=null) customer.withString(Constants.CUSTOMER_ADDRESS_KEY, request.item.address_ref);
        return customer;
    }
    
    public CustomerResponse queryResponse(List<Item> items) {
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
    
    public CustomerResponse messageResponse(String message){
        CustomerResponse resp = new CustomerResponse(message);
        return resp;
    }

}