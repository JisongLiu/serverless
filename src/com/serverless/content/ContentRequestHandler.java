package com.serverless.content;

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

public class ContentRequestHandler implements RequestHandler<ContentRequest, ContentResponse> {	
    
    @Override
    public ContentResponse handleRequest(ContentRequest request, Context context) {
//        context.getLogger().log("Input: " + request.toString());
        
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table contentTable = dynamoDB.getTable("Content");
        
        // Create operation
        if (request.operation.equals("create")) {
        
            // Validate id field not null
            if (request.item.id == null) 
                throw new IllegalArgumentException("400 Bad Request -- id is required");
            
            // Check existence and write item to the table 
            PutItemSpec putItemSpec = new PutItemSpec()
                    .withItem(addContent(request))
                    .withConditionExpression("attribute_not_exists(id)");
            try {
                contentTable.putItem(putItemSpec);
                return messageResponse("Success!");
            } catch (ConditionalCheckFailedException e) {
                throw new IllegalArgumentException("400 Bad Request -- id already exists");
            }
        }
        
        // Query operation
        else if (request.operation.equals("query")) {
            List<Item> scanResult = new ArrayList<>();
            
            // Look up by id
            if (request.item.id != null && !request.item.id.isEmpty()) {
                // Check id existence 
                Item content = contentTable.getItem(new PrimaryKey(Constants.CONTENT_ID_KEY, request.item.id));
                if (content == null) {
                    throw new IllegalArgumentException("404 Not Found -- id does not exist");
                }
                // Return content with the given id
                scanResult.add(content);
                return queryResponse(scanResult);
                
            // Look up by name
            } else if (request.item.name != null && !request.item.name.isEmpty()) {
            	// Return a list of contents with the given name
                ScanRequest scanRequest = new ScanRequest().withTableName(Constants.CONTENT_TABLE_NAME);
                ScanResult allItems = client.scan(scanRequest);
                for (Map<String, AttributeValue> item : allItems.getItems()){
                    if (item.get(Constants.CONTENT_NAME_KEY) != null && item.get(Constants.CONTENT_NAME_KEY).getS().equals(request.item.name)) {
                        Item comment = contentTable.getItem(Constants.CONTENT_ID_KEY, item.get(Constants.CONTENT_ID_KEY).getS());
                        scanResult.add(comment);
                    }
                }
                return queryResponse(scanResult);
            
            // return 10 random items
            } else {
            	ScanRequest scanRequest = new ScanRequest()
            			.withTableName(Constants.CONTENT_TABLE_NAME)
            			.withLimit(Constants.SAMPLE_SIZE);
            	ScanResult sampleItems = client.scan(scanRequest);
            	
            	// TODO: clean up this code (lots of overlapping with queryResponse)
                ContentResponse resp = new ContentResponse("Success");
                for (Map<String, AttributeValue> mapEntry : sampleItems.getItems()) {
                    Content respItem = new Content();
                    respItem.id       = mapEntry.get(Constants.CONTENT_ID_KEY).getS();
                    respItem.name   = mapEntry.get(Constants.CONTENT_NAME_KEY) == null ? 
                    		null : mapEntry.get(Constants.CONTENT_NAME_KEY).getS();
                    respItem.type    = mapEntry.get(Constants.CONTENT_TYPE_KEY) == null ? 
                    		null : mapEntry.get(Constants.CONTENT_TYPE_KEY).getS();
                    respItem.franchises = mapEntry.get(Constants.CONTENT_FRANCHISES_KEY) == null ? 
                    		null : mapEntry.get(Constants.CONTENT_FRANCHISES_KEY).getSS();
                    respItem.series = mapEntry.get(Constants.CONTENT_SERIES_KEY) == null ? 
                    		null : mapEntry.get(Constants.CONTENT_SERIES_KEY).getSS();
                    respItem.episodes = mapEntry.get(Constants.CONTENT_EPISODES_KEY) == null ?
                    		null : mapEntry.get(Constants.CONTENT_EPISODES_KEY).getSS();
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
 			if (request.item.name != null && request.item.name.length() > 0){
 				expressName.put("#n", Constants.CONTENT_NAME_KEY);
 				expressValue.put(":val1", request.item.name);
 				updateQuery.append(" #n = :val1,");
 			}
 			if (request.item.type != null && request.item.type.length() > 0){
 				expressName.put("#t", Constants.CONTENT_TYPE_KEY);
 				expressValue.put(":val2", request.item.type);
 				updateQuery.append(" #t = :val2,");
 			}
 			if (request.item.franchises != null && request.item.franchises.size() > 0){
 				expressName.put("#f", Constants.CONTENT_FRANCHISES_KEY);
 				expressValue.put(":val3", request.item.franchises);
 				updateQuery.append(" #f = :val3,");
 			}
 			if (request.item.series != null && request.item.series.size() > 0){
 				expressName.put("#s", Constants.CONTENT_SERIES_KEY);
 				expressValue.put(":val4", request.item.series);
 				updateQuery.append(" #s = :val4,");
 			}
 			if (request.item.episodes != null && request.item.episodes.size() > 0){
 				expressName.put("#e", Constants.CONTENT_EPISODES_KEY);
 				expressValue.put(":val5", request.item.episodes);
 				updateQuery.append(" #e = :val5,");
 			}
 			String queryString = updateQuery.substring(0, updateQuery.length()-1);
 			try {
 				contentTable.updateItem("id", request.item.id, queryString,
 						expressName, expressValue);
 				return messageResponse("success updated");

 			} catch (Exception e) {
 				return messageResponse("failure!");
 			}
 		}
        
        // Delete operation
        else if (request.operation.equals("delete")) {
        	// Delete and check id existed before deletion
        	PrimaryKey pkey = new PrimaryKey("id", request.item.id);
            Item content = contentTable.getItem(pkey);
            if (content == null) {
            	throw new IllegalArgumentException("404 Not Found -- id does not exist");
            }
            
        	contentTable.deleteItem(pkey);
            List<Item> result = new ArrayList<>();
            result.add(content);
            return queryResponse(result);
        }
        
        throw new IllegalArgumentException("400 Bad Request -- invalid request");
    }
    
    public Item addContent(ContentRequest request) {
        Item content = new Item();
        content.withPrimaryKey(Constants.CONTENT_ID_KEY, request.item.id);
        if (request.item.name!=null) content.withString(Constants.CONTENT_NAME_KEY, request.item.name);
        if (request.item.type!=null) content.withString(Constants.CONTENT_TYPE_KEY, request.item.type);
        if (request.item.franchises!=null) content.withList(Constants.CONTENT_FRANCHISES_KEY, request.item.franchises);
        if (request.item.series!=null) content.withList(Constants.CONTENT_SERIES_KEY, request.item.series);
        if (request.item.episodes!=null) content.withList(Constants.CONTENT_EPISODES_KEY, request.item.episodes);
        return content;
    }
    
    public ContentResponse queryResponse(List<Item> items) {
        ContentResponse resp = new ContentResponse("Success");
        for (Item item: items) {
            Content respItem = new Content();
            respItem.id       = item.getString(Constants.CONTENT_ID_KEY);
            respItem.name   = item.getString(Constants.CONTENT_NAME_KEY);
            respItem.type   = item.getString(Constants.CONTENT_TYPE_KEY);
            respItem.franchises = item.getList(Constants.CONTENT_FRANCHISES_KEY);
            respItem.series = item.getList(Constants.CONTENT_SERIES_KEY);
            respItem.episodes = item.getList(Constants.CONTENT_EPISODES_KEY);
            resp.addItem(respItem);
        }
        return resp;
    }
    
    public ContentResponse messageResponse(String message){
        ContentResponse resp = new ContentResponse(message);
        return resp;
    }

}