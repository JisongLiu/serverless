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

    private AmazonDynamoDBClient client = new AmazonDynamoDBClient();
    private DynamoDB dynamoDB = new DynamoDB(client);
    private Table addressTable = dynamoDB.getTable("Address");
    
    @Override
    public AddressResponse handleRequest(AddressRequest request, Context context) {
        if (request.item.zipcode != null && !request.item.zipcode.isEmpty()){
            ZIPValidator zipValidator = new ZIPValidator();
            if(!zipValidator.validate(request.item.zipcode)){
                throw new IllegalArgumentException("400 Bad Request -- the zip code is illegal");
            }
        }

        // TODO: re-factor each of the operations into a separate function
        // TODO: for create/update, check that address is valid through smartystreets
        // Create operation
        if (request.operation.equals("create")) {
        	return createAddress(request.item);
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
        	return updateAddress(request.item);
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
    
    public AddressResponse createAddress(Address addr) {
    	//Check whether the address exists
    	AddressValidator av = new AddressValidator();
    	String[] result = av.validateBySmartyStreet(addr.line1, addr.city, addr.state);
    	if(result==null){
    		throw new IllegalArgumentException("404 Not Found -- address doesn't exist!");
    	}
    	addr.id=result[0];
    	addr.line1=result[2];
    	addr.city=result[1];
    	addr.state=result[3];
    	addr.zipcode=result[3];
    	
    	
    	// Validate id field not null
        if (addr.id == null) throw new IllegalArgumentException("400 Bad Request -- id is required");
        
        // Check existence and write item to the table 
        PutItemSpec putItemSpec = new PutItemSpec()
                .withItem(addressToDBItem(addr))
                .withConditionExpression("attribute_not_exists(id)");
        try {
            addressTable.putItem(putItemSpec);
            return messageResponse("successfully added address entry");
        } catch (ConditionalCheckFailedException e) {
            throw new IllegalArgumentException("400 Bad Request -- id already exists");
        }
    }
    
    public AddressResponse updateAddress(Address addr) {
    	//Check whether the address exists
    	AddressValidator av = new AddressValidator();
    	String[] result = av.validateBySmartyStreet(addr.line1, addr.city, addr.state);
    	if(result==null){
    		throw new IllegalArgumentException("404 Not Found -- address doesn't exist!");
    	}
    	addr.id=result[0];
    	addr.line1=result[2];
    	addr.city=result[1];
    	addr.state=result[3];
    	addr.zipcode=result[3];

    	// Validate id field not null
        if (addr.id == null) throw new IllegalArgumentException("400 Bad Request -- id is required");

        Item address = addressTable.getItem(new PrimaryKey(Constants.ADDRESS_ID_KEY, addr.id));
        if (address == null) {
        	return createAddress(addr);
        }
        
        Map<String, String> expressName = new HashMap<>();
        Map<String, Object> expressValue = new HashMap<>();

        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("SET");
        if (addr.city != null && addr.city.length() > 0){
            expressName.put("#a", Constants.ADDRESS_CITY_KEY);
            expressValue.put(":val1", addr.city);
            updateQuery.append(" #a = :val1,");
        }
        if (addr.state != null && addr.state.length() > 0){
            expressName.put("#s", Constants.ADDRESS_STATE_KEY);
            expressValue.put(":val2", addr.state);
            updateQuery.append(" #s = :val2,");
        }
        if (addr.line1 != null && addr.line1.length() > 0){
            expressName.put("#f", Constants.ADDRESS_LINE1_KEY);
            expressValue.put(":val3", addr.line1);
            updateQuery.append(" #f = :val3,");
        }
        if (addr.line2 != null && addr.line2.length() > 0){
            expressName.put("#l", Constants.ADDRESS_LINE2_KEY);
            expressValue.put(":val4", addr.line2);
            updateQuery.append(" #l = :val4,");
        }
        if (addr.zipcode != null && addr.zipcode.length() > 0){
            expressName.put("#p", Constants.ADDRESS_ZIPCODE_KEY);
            expressValue.put(":val5", addr.zipcode);
            updateQuery.append(" #p = :val5,");
        }
        String queryString = updateQuery.substring(0, updateQuery.length()-1);
        try {
            addressTable.updateItem(Constants.ADDRESS_ID_KEY, addr.id, queryString,
                    expressName, expressValue);
            return messageResponse("address successfully updated");
        } catch (Exception e) {
            return messageResponse("address update failed: " + e.getMessage());
        }
    }
    
    public Item addressToDBItem(Address addr) {
        Item address = new Item();
        address.withPrimaryKey(Constants.ADDRESS_ID_KEY, addr.id);
        if (addr.line1 != null && !addr.line1.isEmpty()) address.withString(Constants.ADDRESS_LINE1_KEY, addr.line1);
        if (addr.line2 != null && !addr.line2.isEmpty()) address.withString(Constants.ADDRESS_LINE2_KEY, addr.line2);
        if (addr.city  != null && !addr.city.isEmpty()) address.withString(Constants.ADDRESS_CITY_KEY, addr.city);
        if (addr.state != null && !addr.state.isEmpty()) address.withString(Constants.ADDRESS_STATE_KEY, addr.state);
        if (addr.zipcode != null && !addr.zipcode.isEmpty()) address.withString(Constants.ADDRESS_ZIPCODE_KEY, addr.zipcode);
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