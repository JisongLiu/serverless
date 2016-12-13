package com.serverless.content;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.serverless.DBManager;
import com.serverless.comment.Comment;
import com.serverless.comment.CommentResponse;

public class ContentRequestHandler implements RequestHandler<ContentRequest, ContentResponse> {	

    private Table contentTable = DBManager.getTable(Constants.CONTENT_TABLE_NAME);
    
    @Override
    public ContentResponse handleRequest(ContentRequest request, Context context) {
    	
    	switch (request.operation) {
    	case "create":
        	return createContent(request.item);
    	case "query":
        	return queryContent(request.item);
    	case "update":
 			return updateContent(request.item);
    	case "delete":
 			return deleteContent(request.item);
 		default:
 			throw new IllegalArgumentException("400 Bad Request -- unsupported operation " + request.operation);
    	}
    }
    
    public ContentResponse createContent(Content c) {
    	// Validate id field not null
        if (c.id == null) 
            throw new IllegalArgumentException("400 Bad Request -- id is required");
        
        // Check existence and write item to the table 
        PutItemSpec putItemSpec = new PutItemSpec()
                .withItem(addContent(c))
                .withConditionExpression("attribute_not_exists(id)");
        try {
            contentTable.putItem(putItemSpec);
            return messageResponse("Success!");
        } catch (ConditionalCheckFailedException e) {
            throw new IllegalArgumentException("400 Bad Request -- id already exists");
        }
    }
    
    public ContentResponse queryContent(Content c) {
    	List<Item> scanResult = new ArrayList<>();
        
        // Look up by id
        if (c.id != null && !c.id.isEmpty()) {
            // Check id existence 
            Item content = contentTable.getItem(new PrimaryKey(Constants.CONTENT_ID_KEY, c.id));
            if (content == null) {
                throw new IllegalArgumentException("404 Not Found -- id does not exist");
            }
            // Return content with the given id
            scanResult.add(content);
            return queryResponse(scanResult);
            
        // Look up by name
        } else if (c.name != null && !c.name.isEmpty()) {
        	// Return a list of contents with the given name
            ScanRequest scanRequest = new ScanRequest().withTableName(Constants.CONTENT_TABLE_NAME);
            ScanResult allItems = DBManager.client.scan(scanRequest);
            for (Map<String, AttributeValue> item : allItems.getItems()){
                if (item.get(Constants.CONTENT_NAME_KEY) != null && item.get(Constants.CONTENT_NAME_KEY).getS().equals(c.name)) {
                    Item comment = contentTable.getItem(Constants.CONTENT_ID_KEY, item.get(Constants.CONTENT_ID_KEY).getS());
                    scanResult.add(comment);
                }
            }
            return queryResponse(scanResult);
        
        // return all items
        } else {
        	ScanRequest scanRequest = new ScanRequest()
        			.withTableName(Constants.CONTENT_TABLE_NAME);
        	ScanResult sampleItems = DBManager.client.scan(scanRequest);
        	
        	
        	// TODO: clean up this code (lots of overlapping with queryResponse)
            ContentResponse resp = new ContentResponse("Success");
            for (Map<String, AttributeValue> mapEntry : sampleItems.getItems()) {
                Content respItem = new Content();
                respItem.id       = mapEntry.get(Constants.CONTENT_ID_KEY).getS();
                respItem.name   = mapEntry.get(Constants.CONTENT_NAME_KEY) == null ? 
                		null : mapEntry.get(Constants.CONTENT_NAME_KEY).getS();
                respItem.type    = mapEntry.get(Constants.CONTENT_TYPE_KEY) == null ? 
                		null : mapEntry.get(Constants.CONTENT_TYPE_KEY).getS();
                respItem.franchises = ContentRequestHandler.getStringList(mapEntry, Constants.CONTENT_FRANCHISES_KEY);
                respItem.series = ContentRequestHandler.getStringList(mapEntry, Constants.CONTENT_SERIES_KEY);
                respItem.episodes = ContentRequestHandler.getStringList(mapEntry, Constants.CONTENT_EPISODES_KEY);
                resp.addItem(respItem);
            }
            
            return resp;
        }
    }
    
    public ContentResponse updateContent(Content c) {
    	Map<String, String> expressName = new HashMap<>();
			Map<String, Object> expressValue = new HashMap<>();

			StringBuilder updateQuery = new StringBuilder();
			updateQuery.append("SET");
			if (c.name != null && c.name.length() > 0){
				expressName.put("#n", Constants.CONTENT_NAME_KEY);
				expressValue.put(":val1", c.name);
				updateQuery.append(" #n = :val1,");
			}
			if (c.type != null && c.type.length() > 0){
				expressName.put("#t", Constants.CONTENT_TYPE_KEY);
				expressValue.put(":val2", c.type);
				updateQuery.append(" #t = :val2,");
			}
			if (c.franchises != null && c.franchises.size() > 0){
				expressName.put("#f", Constants.CONTENT_FRANCHISES_KEY);
				expressValue.put(":val3", c.franchises);
				updateQuery.append(" #f = :val3,");
			}
			if (c.series != null && c.series.size() > 0){
				expressName.put("#s", Constants.CONTENT_SERIES_KEY);
				expressValue.put(":val4", c.series);
				updateQuery.append(" #s = :val4,");
			}
			if (c.episodes != null && c.episodes.size() > 0){
				expressName.put("#e", Constants.CONTENT_EPISODES_KEY);
				expressValue.put(":val5", c.episodes);
				updateQuery.append(" #e = :val5,");
			}
			String queryString = updateQuery.substring(0, updateQuery.length()-1);
			try {
				contentTable.updateItem("id", c.id, queryString,
						expressName, expressValue);
				return messageResponse("success updated");

			} catch (Exception e) {
				return messageResponse("failure!");
			}
    }
    
    public ContentResponse deleteContent(Content c) {
    	// Delete and check id existed before deletion
    	PrimaryKey pkey = new PrimaryKey("id", c.id);
        Item content = contentTable.getItem(pkey);
        if (content == null) {
        	throw new IllegalArgumentException("404 Not Found -- id does not exist");
        }
        
    	contentTable.deleteItem(pkey);
        List<Item> result = new ArrayList<>();
        result.add(content);
        return queryResponse(result);
    }
    
    public Item addContent(Content c) {
        Item content = new Item();
        content.withPrimaryKey(Constants.CONTENT_ID_KEY, c.id);
        if (c.name!=null) content.withString(Constants.CONTENT_NAME_KEY, c.name);
        if (c.type!=null) content.withString(Constants.CONTENT_TYPE_KEY, c.type);
        if (c.franchises!=null) content.withList(Constants.CONTENT_FRANCHISES_KEY, c.franchises);
        if (c.series!=null) content.withList(Constants.CONTENT_SERIES_KEY, c.series);
        if (c.episodes!=null) content.withList(Constants.CONTENT_EPISODES_KEY, c.episodes);
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

    public static List<String> getStringList(Map<String, AttributeValue> entry, String key) {
    	if (entry.get(key) == null)
    		return null;
    	
    	List<String> arr = new ArrayList<>();
        List<AttributeValue> valList = entry.get(key).getL();
        for (AttributeValue val : valList) {
        	arr.add(val.getS());
        }
        
        return arr;
    }
    
}