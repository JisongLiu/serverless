package com.serverless.asgn1;

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
            
            // Look up by email
            if (request.item.email != null && !request.item.email.isEmpty()) {                
                // Validate email format
            	if(!emailValidator.validate(request.item.email)) return messageResponse("Invalid email format!"); 
            	
                Item customer = customer_table.getItem("email", request.item.email);
                // Check email existence 
                if(customer == null) return messageResponse("Email doesn't exist!");
                
                return successQueryResponse(customer);
                
            // Look by address
            } else if (request.item.address_ref != null && !request.item.address_ref.isEmpty()){
                // TODO: return error if item not found
                
                // Validate address format
                if (!request.item.address_ref.contains("Street")) {
                    return messageResponse("Invalid Address format!");
                }
                // TODO: return a list of customers with the given address instead of just one
                ScanRequest scanRequest = new ScanRequest()
                        .withTableName("Customer");
                ScanResult result = client.scan(scanRequest);
                for (Map<String, AttributeValue> item : result.getItems()){
                    if (item.get("address_ref") != null && item.get("address_ref").getS().equals(request.item.address_ref)) {
                        Item customer = customer_table.getItem("email", item.get("email").getS());
                        return successQueryResponse(customer);
                    }
                }
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
    
    public CustomerResponse successQueryResponse(Item customer) {
        CustomerResponse resp = new CustomerResponse("Success", "No error");
        CustomerResponse.Item respItem = resp.new Item();
        respItem.email = customer.getString("email");
        respItem.firstname = customer.getString("firstname");
        respItem.lastname = customer.getString("lastname");
        respItem.phonenumber = customer.getString("phonenumber");
        respItem.address_ref = customer.getString("address_ref");
        resp.setItem(respItem);
        return resp;
    }
    
    public CustomerResponse messageResponse(String message){
    	CustomerResponse resp = new CustomerResponse(message);
        return resp;
    }

}