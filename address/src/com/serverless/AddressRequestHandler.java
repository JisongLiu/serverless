package com.serverless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class AddressRequestHandler implements RequestHandler<AddressRequest, AddressResponse> {
    
    
    @Override
    public AddressResponse handleRequest(AddressRequest request, Context context) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table Address_table = dynamoDB.getTable("Address");
        
        if(request.item.zipCode!=null && !request.item.zipCode.isEmpty()){
            ZIPValidator zipValidator = new ZIPValidator();
            if(!zipValidator.validate(request.item.zipCode)){
                throw new IllegalArgumentException("400 Bad Request -- the zip Code is Illegal");
            }
        }

        // Create operation
        if (request.operation.equals("create")) {
            
            // Validate id field not null
            if (request.item.id == null) throw new IllegalArgumentException("400 Bad Request -- id is required");
            
            
            // Check existence and write item to the table 
            PutItemSpec putItemSpec = new PutItemSpec()
                    .withItem(addAddress(request))
                    .withConditionExpression("attribute_not_exists(id)");
            try {
                Address_table.putItem(putItemSpec);
                return messageResponse("Success!");
            } catch(ConditionalCheckFailedException e){
                throw new IllegalArgumentException("400 Bad Request -- id already exists");
            }
        }
        
        // Query operation
        else if (request.operation.equals("query")) {
            List<Item> scanResult = new ArrayList();
            if (request.item.id != null && !request.item.id.isEmpty()) {                
                
                Item Address = Address_table.getItem(new PrimaryKey("id", request.item.id));
                if (Address == null) {
                    throw new IllegalArgumentException("404 Not Found -- email does not exist");
                }
                scanResult.add(Address);
                return queryResponse(scanResult);
            }  
        } 
        
        // Update operation
        else if (request.operation.equals("update")) {

            Map<String, String> expressName = new HashMap();
            Map<String, Object> expressValue = new HashMap();

            StringBuilder updateQuery = new StringBuilder();
            updateQuery.append("SET");
            if (request.item.city != null && request.item.city.length() > 0){
                expressName.put("#a", "city");
                expressValue.put(":val1", request.item.city);
                updateQuery.append(" #a = :val1,");
            }
            if (request.item.street != null && request.item.street.length() > 0){
                expressName.put("#f", "street");
                expressValue.put(":val2", request.item.street);
                updateQuery.append(" #f = :val2,");
            }
            if (request.item.number != null && request.item.number.length() > 0){
                expressName.put("#l", "number");
                expressValue.put(":val3", request.item.number);
                updateQuery.append(" #l = :val3,");
            }
            if (request.item.zipCode != null && request.item.zipCode.length() > 0){
                expressName.put("#p", "zipCode");
                expressValue.put(":val4", request.item.zipCode);
                updateQuery.append(" #p = :val4,");
            }
            String queryString = updateQuery.substring(0, updateQuery.length()-1);
            try {
                UpdateItemOutcome outcome = Address_table.updateItem("id", request.item.id, queryString,
                        expressName, expressValue);
                return messageResponse("success updated");

            } catch (Exception e) {
                return messageResponse("failure!");
            }
        }
        
        // Delete operation
        else if (request.operation.equals("delete")) {

            // Delete and check address existed before deletion
            PrimaryKey pkey = new PrimaryKey("id", request.item.id);
            Item address = Address_table.getItem(pkey);
            if (address == null) {
                throw new IllegalArgumentException("404 Not Found -- address does not exist");
            }
            
            Address_table.deleteItem(pkey);
            List<Item> result = new ArrayList<>();
            result.add(address);
            return queryResponse(result);
            
        }
        
        return messageResponse("Invalid Request!");
    }
    
    public Item addAddress(AddressRequest request) {
        Item Address = new Item();
        Address.withPrimaryKey("id", request.item.id);
        if (request.item.number!=null) Address.withString("number", request.item.number);
        if (request.item.city!=null) Address.withString("city", request.item.city);
        if (request.item.street!=null) Address.withString("street", request.item.street);
        if (request.item.zipCode!=null) Address.withString("zipCode", request.item.zipCode);
        return Address;
    }
    
    public AddressResponse queryResponse(List<Item> items) {
        AddressResponse resp = new AddressResponse("Success");
        for(Item item: items){
            AddressResponse.Item respItem = resp.new Item();
            respItem.id = item.getString("id");
            respItem.city = item.getString("city");
            respItem.number = item.getString("number");
            respItem.street = item.getString("street");
            respItem.zipCode = item.getString("zipCode");
            resp.addItem(respItem);
        }
        return resp;
    }
    
    public AddressResponse messageResponse(String message){
        AddressResponse resp = new AddressResponse(message);
        return resp;
    }

}