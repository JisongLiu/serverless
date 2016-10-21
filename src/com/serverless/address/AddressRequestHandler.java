package com.serverless.address;

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

public class AddressRequestHandler implements RequestHandler<AddressRequest, AddressResponse> {
	
    @Override
    public AddressResponse handleRequest(AddressRequest request, Context context) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table addressTable = dynamoDB.getTable("Address");
        
        if (request.item.zipcode != null && !request.item.zipcode.isEmpty()){
            ZIPValidator zipValidator = new ZIPValidator();
            if(!zipValidator.validate(request.item.zipcode)){
                throw new IllegalArgumentException("400 Bad Request -- the zip code is illegal");
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
                addressTable.putItem(putItemSpec);
                return messageResponse("Success!");
            } catch(ConditionalCheckFailedException e){
                throw new IllegalArgumentException("400 Bad Request -- id already exists");
            }
        }
        
        // Query operation
        else if (request.operation.equals("query")) {
            List<Item> scanResult = new ArrayList<>();
            if (request.item.id != null && !request.item.id.isEmpty()) {                
                
                Item address = addressTable.getItem(new PrimaryKey(Constants.ADDRESS_ID_KEY, request.item.id));
                if (address == null) {
                    throw new IllegalArgumentException("404 Not Found -- email does not exist");
                }
                scanResult.add(address);
                return queryResponse(scanResult);
            } else {
            	// return 10 arbitrary addresses
	        	ScanRequest scanRequest = new ScanRequest()
	        			.withTableName(Constants.ADDRESS_TABLE_NAME)
	        			.withLimit(Constants.SAMPLE_SIZE);
	        	ScanResult sampleItems = client.scan(scanRequest);
	        	
	        	// TODO: clean up this code (lots of overlapping with queryResponse)
	            AddressResponse resp = new AddressResponse("Success");
	            for (Map<String, AttributeValue> mapEntry : sampleItems.getItems()) {
	                Address respItem = new Address();
	                respItem.id    = mapEntry.get(Constants.ADDRESS_ID_KEY).getS();
	                respItem.line1 = mapEntry.get(Constants.ADDRESS_LINE1_KEY).getS();
	                respItem.line2 = mapEntry.get(Constants.ADDRESS_LINE2_KEY) == null ? 
	                		null : mapEntry.get(Constants.ADDRESS_LINE2_KEY).getS();
	                respItem.city  = mapEntry.get(Constants.ADDRESS_CITY_KEY) == null ? 
	                		null : mapEntry.get(Constants.ADDRESS_CITY_KEY).getS();
	                respItem.state = mapEntry.get(Constants.ADDRESS_STATE_KEY) == null ? 
	                		null : mapEntry.get(Constants.ADDRESS_STATE_KEY).getS();
	                respItem.zipcode = mapEntry.get(Constants.ADDRESS_ZIPCODE_KEY) == null ? 
	                		null : mapEntry.get(Constants.ADDRESS_ZIPCODE_KEY).getS();
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
            if (request.item.city != null && request.item.city.length() > 0){
                expressName.put("#a", Constants.ADDRESS_CITY_KEY);
                expressValue.put(":val1", request.item.city);
                updateQuery.append(" #a = :val1,");
            }
            if (request.item.line1 != null && request.item.line1.length() > 0){
                expressName.put("#f", Constants.ADDRESS_LINE1_KEY);
                expressValue.put(":val2", request.item.line1);
                updateQuery.append(" #f = :val2,");
            }
            if (request.item.line2 != null && request.item.line2.length() > 0){
                expressName.put("#l", Constants.ADDRESS_LINE2_KEY);
                expressValue.put(":val3", request.item.line2);
                updateQuery.append(" #l = :val3,");
            }
            if (request.item.zipcode != null && request.item.zipcode.length() > 0){
                expressName.put("#p", Constants.ADDRESS_ZIPCODE_KEY);
                expressValue.put(":val4", request.item.zipcode);
                updateQuery.append(" #p = :val4,");
            }
            String queryString = updateQuery.substring(0, updateQuery.length()-1);
            try {
                addressTable.updateItem(Constants.ADDRESS_ID_KEY, request.item.id, queryString,
                        expressName, expressValue);
                return messageResponse("address successfully updated");

            } catch (Exception e) {
                return messageResponse("failure");
            }
        }
        
        // Delete operation
        else if (request.operation.equals("delete")) {

            // Delete and check address existed before deletion
            PrimaryKey pkey = new PrimaryKey(Constants.ADDRESS_ID_KEY, request.item.id);
            Item address = addressTable.getItem(pkey);
            if (address == null) {
                throw new IllegalArgumentException("404 Not Found -- address does not exist");
            }
            
            addressTable.deleteItem(pkey);
            List<Item> result = new ArrayList<>();
            result.add(address);
            return queryResponse(result);
        }
        
        return messageResponse("invalid request");
    }
    
    public Item addAddress(AddressRequest request) {
        Item address = new Item();
        address.withPrimaryKey(Constants.ADDRESS_ID_KEY, request.item.id);
        if (request.item.line1 != null) address.withString(Constants.ADDRESS_LINE1_KEY, request.item.line1);
        if (request.item.line2 != null) address.withString(Constants.ADDRESS_LINE2_KEY, request.item.line2);
        if (request.item.city  != null) address.withString(Constants.ADDRESS_CITY_KEY, request.item.city);
        if (request.item.state != null) address.withString(Constants.ADDRESS_STATE_KEY, request.item.state);
        if (request.item.zipcode != null) address.withString(Constants.ADDRESS_ZIPCODE_KEY, request.item.zipcode);
        return address;
    }
    
    public AddressResponse queryResponse(List<Item> items) {
        AddressResponse resp = new AddressResponse("Success");
        for(Item item: items) {
            Address respItem = new Address();
            respItem.id = item.getString(Constants.ADDRESS_ID_KEY);
            respItem.line1 = item.getString(Constants.ADDRESS_LINE1_KEY);
            respItem.line2 = item.getString(Constants.ADDRESS_LINE2_KEY);
            respItem.city = item.getString(Constants.ADDRESS_CITY_KEY);
            respItem.state = item.getString(Constants.ADDRESS_STATE_KEY);
            respItem.zipcode = item.getString(Constants.ADDRESS_ZIPCODE_KEY);
            resp.addItem(respItem);
        }
        return resp;
    }
    
    public AddressResponse messageResponse(String message){
        AddressResponse resp = new AddressResponse(message);
        return resp;
    }

}