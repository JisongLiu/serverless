package com.serverless.comment;

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

public class CommentRequestHandler implements RequestHandler<CommentRequest, CommentResponse> {	
    
    @Override
    public CommentResponse handleRequest(CommentRequest request, Context context) {
//        context.getLogger().log("Input: " + request.toString());
        
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table commentTable = dynamoDB.getTable("Comment");
        
        // Create operation
        if (request.operation.equals("create")) {
        
            // Validate id field not null
            if (request.item.id == null) 
                throw new IllegalArgumentException("400 Bad Request -- id is required");
            
            // Check existence and write item to the table 
            PutItemSpec putItemSpec = new PutItemSpec()
                    .withItem(addComment(request))
                    .withConditionExpression("attribute_not_exists(id)");
            try {
                commentTable.putItem(putItemSpec);
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
                Item comment = commentTable.getItem(new PrimaryKey(Constants.COMMENT_ID_KEY, request.item.id));
                if (comment == null) {
                    throw new IllegalArgumentException("404 Not Found -- id does not exist");
                }
                // Return content with the given id
                scanResult.add(comment);
                return queryResponse(scanResult);
                
            // Look up by user
            } else if (request.item.user != null && !request.item.user.isEmpty()) {
            	// Return a list of comments with the given user
                ScanRequest scanRequest = new ScanRequest().withTableName(Constants.COMMENT_TABLE_NAME);
                ScanResult allItems = client.scan(scanRequest);
                for (Map<String, AttributeValue> item : allItems.getItems()){
                    if (item.get(Constants.COMMENT_USER_KEY) != null && item.get(Constants.COMMENT_USER_KEY).getS().equals(request.item.user)) {
                        Item comment = commentTable.getItem(Constants.COMMENT_ID_KEY, item.get(Constants.COMMENT_ID_KEY).getS());
                        scanResult.add(comment);
                    }
                }
                return queryResponse(scanResult);
            
            // return 10 random items
            } else {
            	ScanRequest scanRequest = new ScanRequest()
            			.withTableName(Constants.COMMENT_TABLE_NAME)
            			.withLimit(Constants.SAMPLE_SIZE);
            	ScanResult sampleItems = client.scan(scanRequest);
            	
            	// TODO: clean up this code (lots of overlapping with queryResponse)
                CommentResponse resp = new CommentResponse("Success");
                for (Map<String, AttributeValue> mapEntry : sampleItems.getItems()) {
                    Comment respItem = new Comment();
                    respItem.id       = mapEntry.get(Constants.COMMENT_ID_KEY).getS();
                    respItem.user   = mapEntry.get(Constants.COMMENT_USER_KEY) == null ? 
                    		null : mapEntry.get(Constants.COMMENT_USER_KEY).getS();
                    respItem.content    = mapEntry.get(Constants.COMMENT_CONTENT_KEY) == null ? 
                    		null : mapEntry.get(Constants.COMMENT_CONTENT_KEY).getS();
                    respItem.comment = mapEntry.get(Constants.COMMENT_COMMENT_KEY) == null ? 
                    		null : mapEntry.get(Constants.COMMENT_COMMENT_KEY).getS();
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
 			if (request.item.user != null && request.item.user.length() > 0){
 				expressName.put("#u", Constants.COMMENT_USER_KEY);
 				expressValue.put(":val1", request.item.user);
 				updateQuery.append(" #u = :val1,");
 			}
 			if (request.item.content != null && request.item.content.length() > 0){
 				expressName.put("#c", Constants.COMMENT_CONTENT_KEY);
 				expressValue.put(":val2", request.item.content);
 				updateQuery.append(" #c = :val2,");
 			}
 			if (request.item.comment != null && request.item.comment.length() > 0){
 				expressName.put("#m", Constants.COMMENT_COMMENT_KEY);
 				expressValue.put(":val3", request.item.comment);
 				updateQuery.append(" #m = :val3,");
 			}
 			String queryString = updateQuery.substring(0, updateQuery.length()-1);
 			try {
 				commentTable.updateItem("id", request.item.id, queryString,
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
            Item comment = commentTable.getItem(pkey);
            if (comment == null) {
            	throw new IllegalArgumentException("404 Not Found -- id does not exist");
            }
            
        	commentTable.deleteItem(pkey);
            List<Item> result = new ArrayList<>();
            result.add(comment);
            return queryResponse(result);
        }
        
        throw new IllegalArgumentException("400 Bad Request -- invalid request");
    }
    
    public Item addComment(CommentRequest request) {
        Item comment = new Item();
        comment.withPrimaryKey(Constants.COMMENT_ID_KEY, request.item.id);
        if (request.item.user!=null) comment.withString(Constants.COMMENT_USER_KEY, request.item.user);
        if (request.item.content!=null) comment.withString(Constants.COMMENT_CONTENT_KEY, request.item.content);
        if (request.item.comment!=null) comment.withString(Constants.COMMENT_COMMENT_KEY, request.item.comment);
        return comment;
    }
    
    public CommentResponse queryResponse(List<Item> items) {
        CommentResponse resp = new CommentResponse("Success");
        for (Item item: items) {
            Comment respItem = new Comment();
            respItem.id       = item.getString(Constants.COMMENT_ID_KEY);
            respItem.user   = item.getString(Constants.COMMENT_USER_KEY);
            respItem.content   = item.getString(Constants.COMMENT_CONTENT_KEY);
            respItem.comment = item.getString(Constants.COMMENT_COMMENT_KEY);
            resp.addItem(respItem);
        }
        return resp;
    }
    
    public CommentResponse messageResponse(String message){
        CommentResponse resp = new CommentResponse(message);
        return resp;
    }

}