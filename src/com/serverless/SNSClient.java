package com.serverless;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;

public class SNSClient {
	public AmazonSNSClient ini()
	{
		AmazonSNSClient amazonSNSClient = new AmazonSNSClient();
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		amazonSNSClient.setRegion(usEast1);
		return amazonSNSClient;
	}
	public void publish(AmazonSNSClient amazonSNSClient, String message, String object)
	{	
		switch (object) {
    	case "address":
    		amazonSNSClient.publish("arn:aws:sns:us-east-1:041368893479:address", message);
    	case "customer":
    		amazonSNSClient.publish("arn:aws:sns:us-east-1:041368893479:customer", message);
    	case "content":
    		amazonSNSClient.publish("arn:aws:sns:us-east-1:041368893479:content", message);
    	case "comment":
    		amazonSNSClient.publish("arn:aws:sns:us-east-1:041368893479:comment", message);
		}
	}
}
