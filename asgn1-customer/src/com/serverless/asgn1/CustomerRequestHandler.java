package com.serverless.asgn1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class CustomerRequestHandler implements RequestHandler<CustomerRequest, CustomerResponse> {
    private EmailValidator emailValidator;
    
    public CustomerRequestHandler(){
        emailValidator = new EmailValidator();
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
            if(!emailValidator.validate(request.item.email))
                throw new IllegalArgumentException("400 Bad Request -- invalid email format");
            
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
            List<Item> scanResult = new ArrayList();
            
            // Look up by email
            if (request.item.email != null && !request.item.email.isEmpty()) {                
                // Validate email format
            	if (!emailValidator.validate(request.item.email))
            		throw new IllegalArgumentException("400 Bad Request -- invalid email format");

                // Check email existence 
                Item customer = customerTable.getItem(new PrimaryKey("email", request.item.email));
                if (customer == null) {
                    throw new IllegalArgumentException("404 Not Found -- email does not exist");
                }
                
                if (request.item.address_ref != null && request.item.address_ref.equals("requested")) {
                	// Return an address
                    String addressId = customer.getString("address_ref");
                    if (addressId == null) {
                    	throw new IllegalArgumentException("400 Bad Request -- customer does not have an address");
                    }

                    Table addressTable = dynamoDB.getTable("Address");
                    Item address = addressTable.getItem(new PrimaryKey("id", addressId));
                    if (address == null) {
                    	throw new IllegalArgumentException("404 Not Found -- address not found");
                    }

                    CustomerResponse resp = new CustomerResponse("Success");
                    CustomerResponse.Item addressEntry = resp.new Item();
                    List<String> addressComponents = Arrays.asList(
                    		address.getString("number"),
                    		address.getString("street"),
                    		address.getString("city"),
                    		address.getString("zipCode"));
                    addressEntry.address_ref = String.join(" ", addressComponents);
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
                ScanRequest scanRequest = new ScanRequest().withTableName("Customer");
                ScanResult allItems = client.scan(scanRequest);
                for (Map<String, AttributeValue> item : allItems.getItems()){
                    if (item.get("address_ref") != null && item.get("address_ref").getS().equals(request.item.address_ref)) {
                        Item customer = customerTable.getItem("email", item.get("email").getS());
                        scanResult.add(customer);
                    }
                }
                
                return queryResponse(scanResult);
            }
        }
        
        // Update operation
        else if (request.operation.equals("update")) {
            
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
        customer.withPrimaryKey("email", request.item.email);
        if (request.item.lastname!=null) customer.withString("lastname", request.item.lastname);
        if (request.item.firstname!=null) customer.withString("firstname", request.item.firstname);
        if (request.item.phonenumber!=null) customer.withString("phonenumber", request.item.phonenumber);
        if (request.item.address_ref!=null) customer.withString("address_ref", request.item.address_ref);
        return customer;
    }
    
    public CustomerResponse queryResponse(List<Item> items) {
        CustomerResponse resp = new CustomerResponse("Success");
        for (Item item: items) {
            CustomerResponse.Item respItem = resp.new Item();
            respItem.email       = item.getString("email");
            respItem.firstname   = item.getString("firstname");
            respItem.lastname    = item.getString("lastname");
            respItem.phonenumber = item.getString("phonenumber");
            respItem.address_ref = item.getString("address_ref");
            resp.addItem(respItem);
        }
        return resp;
    }
    
    public CustomerResponse messageResponse(String message){
        CustomerResponse resp = new CustomerResponse(message);
        return resp;
    }

}