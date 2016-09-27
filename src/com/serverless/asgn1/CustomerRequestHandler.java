package com.serverless.asgn1;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


public class CustomerRequestHandler implements RequestHandler<RequestClass, ResponseClass> {

    @Override
    public ResponseClass handleRequest(RequestClass request, Context context) {
     //   context.getLogger().log("Input: " + input);
    	DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
			    new EnvironmentVariableCredentialsProvider()));
    	
    	if(request.operation.equals("create")){
    		Table table = dynamoDB.getTable("Customer");
    		if(request.item.email == null) return new ResponseClass("No Success", "Must Have Email");
    		Item item = new Item();
    		item.withPrimaryKey("email", request.item.email);
    		if(request.item.lastname!=null) item.withString("lastname", request.item.lastname);
    		if(request.item.firstname!=null) item.withString("firstname", request.item.firstname);
    		if(request.item.phonenumber!=null) item.withString("phonenumber", request.item.phonenumber);
    		if(request.item.address_ref!=null) item.withString("address_ref", request.item.address_ref);
    		
    		// Write the item to the table 
    		PutItemOutcome outcome = table.putItem(item);		
            return new ResponseClass("Successfully updated!", "No error");
    	} else if (request.operation.equals("query")) {
    		
    	} else if (request.operation.equals("update")) {
    		
    	} else if (request.operation.equals("delete")) {
    		
    	} else {
    		// TODO: return error
    	}
    	
    	return new ResponseClass("No Success", "Invalid Request!");
    }

}

