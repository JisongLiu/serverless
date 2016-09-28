package com.serverless.asgn1;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class CustomerRequestHandler implements RequestHandler<CustomerRequest, CustomerResponse> {

    @Override
    public CustomerResponse handleRequest(CustomerRequest request, Context context) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table customer_table = dynamoDB.getTable("Customer");
        
        // Create operation
        if (request.operation.equals("create")) {
            // TODO: return error if email already exists
            
            // Validate email field not null
            if (request.item.email == null) return new CustomerResponse("No Success", "Must Have Email");
            
            // Write the item to the table 
            customer_table.putItem(addCustomer(request));
            return new CustomerResponse("Successfully updated!", "No error");
        } 
        
        // Query operation
        else if (request.operation.equals("query")) {
            
            // Look up by email
            if (request.item.email != null && !request.item.email.isEmpty()) {
                // TODO: return error if item not found
                
                // Validate email format
                if (!request.item.email.contains("@")) {
                    return new CustomerResponse("No Success", "Email form not valid");
                }
                Item customer = customer_table.getItem("email", request.item.email);
                return getCustomer(customer);
                
            // Look by address
            } else if (request.item.address_ref != null && !request.item.address_ref.isEmpty()){
                // TODO: return error if item not found
                
                // Validate address format
                if (!request.item.address_ref.contains("Street")) {
                    return new CustomerResponse("No Success", "Address form not valid");
                }
                // TODO: return a list of customers with the given address instead of just one
                ScanRequest scanRequest = new ScanRequest()
                        .withTableName("Customer");
                ScanResult result = client.scan(scanRequest);
                for (Map<String, AttributeValue> item : result.getItems()){
                    if (item.get("address_ref") != null && item.get("address_ref").getS().equals(request.item.address_ref)) {
                        Item customer = customer_table.getItem("email", item.get("email").getS());
                        return getCustomer(customer);
                    }
                }
            }
        } 
        
        // Update operation
        else if (request.operation.equals("update")) {
            
        } 
        
        // Delete operation
        else if (request.operation.equals("delete")) {
            
        } else {
            // TODO: return error
        }
        
        return new CustomerResponse("No Success", "Invalid Request!");
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
    
    public CustomerResponse getCustomer(Item customer) {
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

}