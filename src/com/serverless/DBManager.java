package com.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class DBManager {
    public static AmazonDynamoDBClient client = new AmazonDynamoDBClient();
    public static DynamoDB dynamoDB = new DynamoDB(client);

    public static Table getTable(String name) {
    	return dynamoDB.getTable(name);
    }
}
