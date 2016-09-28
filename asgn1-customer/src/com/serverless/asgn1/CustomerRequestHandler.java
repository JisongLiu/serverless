package com.serverless.asgn1;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class CustomerRequestHandler implements RequestHandler<CustomerRequest, CustomerResponse> {

    @Override
    public CustomerResponse handleRequest(CustomerRequest request, Context context) {
    	DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
			    new EnvironmentVariableCredentialsProvider()));
		Table table = dynamoDB.getTable("Customer");
		
    	if (request.operation.equals("create")) {
    		// TODO: return error if email already exists
    		if (request.item.email == null) return new CustomerResponse("No Success", "Must Have Email");
    		Item item = new Item();
    		item.withPrimaryKey("email", request.item.email);
    		if (request.item.lastname!=null) item.withString("lastname", request.item.lastname);
    		if (request.item.firstname!=null) item.withString("firstname", request.item.firstname);
    		if (request.item.phonenumber!=null) item.withString("phonenumber", request.item.phonenumber);
    		if (request.item.address_ref!=null) item.withString("address_ref", request.item.address_ref);
    		
    		// Write the item to the table 
    		table.putItem(item);
            return new CustomerResponse("Successfully updated!", "No error");
    	} else if (request.operation.equals("query")) {
    		if (request.item.email == null) {
    			return new CustomerResponse("Unsuccessful", "email is required");
    		} else {
    			// TODO: return error if item not found
    			Item customer = table.getItem("email", request.item.email);
    			CustomerResponse resp = new CustomerResponse("Success", "");
    			CustomerResponse.Item respItem = resp.new Item();
    			respItem.email = customer.getString("email");
    			respItem.firstname = customer.getString("firstname");
    			respItem.lastname = customer.getString("lastname");
    			respItem.phonenumber = customer.getString("phonenumber");
    			respItem.address_ref = customer.getString("address_ref");
    			resp.setItem(respItem);
    			return resp;
    		}
    	} else if (request.operation.equals("update")) {
    		
    	} else if (request.operation.equals("delete")) {
    		
    	} else {
    		// TODO: return error
    	}
    	
    	return new CustomerResponse("No Success", "Invalid Request!");
    }

}
