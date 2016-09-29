package com.serverless.asgn1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
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
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table customer_table = dynamoDB.getTable("Customer");
        
        // Create operation
        if (request.operation.equals("create")) {
        
            // Validate email field not null
            if (request.item.email == null) return messageResponse("Must have email!");
            
            // Validate email format
            if(!emailValidator.validate(request.item.email)) return messageResponse("Invalid email format!");
                                      
            // Check existence and write item to the table 
            PutItemSpec putItemSpec = new PutItemSpec()
            		.withItem(addCustomer(request))
            		.withConditionExpression("attribute_not_exists(email)");
            try{
                customer_table.putItem(putItemSpec);
                return messageResponse("Success!");
            }
            catch(ConditionalCheckFailedException e){
            	 return messageResponse("Email already exists!");
            }
        } 
        
        // Query operation
        else if (request.operation.equals("query")) {
            List<Item> scanResult = new ArrayList();
            
            // Look up by email
            if (request.item.email != null && !request.item.email.isEmpty()) {                
                // Validate email format
            	if(!emailValidator.validate(request.item.email)) return messageResponse("Invalid email format!"); 
            	
                Item customer = customer_table.getItem("email", request.item.email);
                // Check email existence 
                if(customer == null) return messageResponse("Email doesn't exist!");
                
                // Return customer with the given email
                scanResult.add(customer);
                return queryResponse(scanResult);
                
            // Look up by address
            } else if (request.item.address_ref != null && !request.item.address_ref.isEmpty()){
                // TODO: return error if item not found
                
                // Validate address format
                if (!request.item.address_ref.contains("Street")) {
                    return messageResponse("Invalid Address format!");
                }
                // Return a list of customers with the given address instead of just one
                ScanRequest scanRequest = new ScanRequest()
                        .withTableName("Customer");
                ScanResult allItems = client.scan(scanRequest);
                for (Map<String, AttributeValue> item : allItems.getItems()){
                    if (item.get("address_ref") != null && item.get("address_ref").getS().equals(request.item.address_ref)) {
                        Item customer = customer_table.getItem("email", item.get("email").getS());
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
            
        }
        
        return messageResponse("Invalid Request!");
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
        for(Item item: items){
            CustomerResponse.Item respItem = resp.new Item();
            respItem.email = item.getString("email");
            respItem.firstname = item.getString("firstname");
            respItem.lastname = item.getString("lastname");
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