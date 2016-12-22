package com.serverless.composite;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.Constants;
import com.serverless.DBManager;
import com.serverless.SNSClient;
import com.serverless.address.AddressRequest;
import com.serverless.address.AddressResponse;
import com.serverless.comment.CommentRequest;
import com.serverless.comment.CommentResponse;
import com.serverless.content.Content;
import com.serverless.content.ContentRequest;
import com.serverless.content.ContentRequestHandler;
import com.serverless.content.ContentResponse;
import com.serverless.content.Episode;
import com.serverless.content.Franchise;
import com.serverless.content.Property;
import com.serverless.content.Series;
import com.serverless.customer.CustomerRequest;
import com.serverless.customer.CustomerResponse;
import com.sun.istack.internal.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;


public class LambdaFunctionHandler implements RequestHandler<CompositeRequest, Object> {
	
	public interface AddressService {
		  @LambdaFunction(functionName="AddressRequestHandler")
		  AddressResponse AddressRequestHandler(AddressRequest input);
		}
	
	public interface CustomerService {
		  @LambdaFunction(functionName="CustomerRequestHandler")
		  CustomerResponse CustomerRequestHandler(CustomerRequest input);
		}
	public interface ContentService {
		  @LambdaFunction(functionName="ContentRequestHandler")
		  ContentResponse ContentRequestHandler(ContentRequest input);
		}
	public interface CommentService {
		  @LambdaFunction(functionName="CommentRequestHandler")
		  CommentResponse CommentRequestHandler(CommentRequest input);
		}
	
    @Override
    public Object handleRequest(CompositeRequest request, Context context) {
    	
    	SNSClient snsclient = new SNSClient();
 		AmazonSNSClient amazonSNSClient = snsclient.ini();
// 		System.out.println("-----------" + amazonSNSClient.toString() + "------------");
 		ObjectMapper mapper = new ObjectMapper();
    	String jsonMessage;
    	
        if (request.object.equals("address")) {
        	//System.out.println("hello");
        	AWSLambdaClient lambda = new AWSLambdaClient();
        	lambda.configureRegion(Regions.US_EAST_1);
        	AddressService service = LambdaInvokerFactory.build(AddressService.class, lambda);
        	AddressRequest req = new AddressRequest();
        	req.setOperation(request.getOperation());
        	req.setItem(request.getAddress());
//        	AddressResponse res = service.AddressRequestHandler(req);
        	AddressResponse res = new AddressResponse();
        	
        	// Publish to SNS address topic
			try {
				jsonMessage = mapper.writeValueAsString(req);
				snsclient.publish(amazonSNSClient, jsonMessage, "address");
				res.setMessage("Published to SNS address topic");
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return res;
        } else if (request.object.equals("customer")) {
        	//System.out.println("hello2");
        	AWSLambdaClient lambda = new AWSLambdaClient();
        	lambda.configureRegion(Regions.US_EAST_1);
        	CustomerService service = LambdaInvokerFactory.build(CustomerService.class, lambda);
        	CustomerRequest req = new CustomerRequest();
        	req.setOperation(request.getOperation());
        	req.setItem(request.getCustomer());
//        	CustomerResponse res = service.CustomerRequestHandler(req);
        	AddressResponse res = new AddressResponse();
        	
        	// Publish to SNS customer topic
			try {
				jsonMessage = mapper.writeValueAsString(req);
				snsclient.publish(amazonSNSClient, jsonMessage, "customer");
				res.setMessage("Published to SNS customer topic");
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return res;
        } else if (request.object.equals("content")) {
        	String email = request.customer.email;
        	
        	List<Franchise> items = getRecommendation(email);
        	RecommendationResponse resp = new RecommendationResponse();
        	resp.setMessage("success");
        	resp.setItems(items);

        	return resp;
        } else if (request.object.equals("comment")) {
        	//System.out.println("hello2");
        	AWSLambdaClient lambda = new AWSLambdaClient();
        	lambda.configureRegion(Regions.US_EAST_1);
        	CommentService service = LambdaInvokerFactory.build(CommentService.class, lambda);
        	CommentRequest req = new CommentRequest();
        	req.setOperation(request.getOperation());
        	req.setItem(request.getComment());
//        	CommentResponse res = service.CommentRequestHandler(req);
        	AddressResponse res = new AddressResponse();
        	
        	// Publish to SNS comment topic
			try {
				jsonMessage = mapper.writeValueAsString(req);
				snsclient.publish(amazonSNSClient, jsonMessage, "comment");
				res.setMessage("Published to SNS comment topic");
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return res;
        }
        
        return null;
    }
    
    public List<Franchise> getRecommendation(String email) {
    	ScanRequest scanRequest = new ScanRequest()
    			.withTableName(Constants.CONTENT_TABLE_NAME);
    	ScanResult sampleItems = DBManager.client.scan(scanRequest);
    	
        HashMap<String, Content> contentTable = new HashMap<>();
        HashSet<Content> franchises = new HashSet<>();
        for (Map<String, AttributeValue> mapEntry : sampleItems.getItems()) {
            Content c = new Content();
            c.id     = mapEntry.get(Constants.CONTENT_ID_KEY).getS();
            c.name   = mapEntry.get(Constants.CONTENT_NAME_KEY).getS();
            c.type   = mapEntry.get(Constants.CONTENT_TYPE_KEY).getS();
            c.franchises = ContentRequestHandler.getStringList(mapEntry, Constants.CONTENT_FRANCHISES_KEY);
            c.series = ContentRequestHandler.getStringList(mapEntry, Constants.CONTENT_SERIES_KEY);
            c.episodes = ContentRequestHandler.getStringList(mapEntry, Constants.CONTENT_EPISODES_KEY);
            contentTable.put(c.id, c);
            
            if (c.type.equals("franchise")) {
            	franchises.add(c);
            }
        }
        
        System.out.println(contentTable.size());
        
        List<Franchise> recommendations = new ArrayList<>();
        for (Content cf : franchises) {
        	Franchise f = new Franchise();
        	f.id = cf.id;
        	f.name = cf.name;
        	f.type = cf.type;
        	if (cf.series == null) {
        		continue;
        	}

        	List<Series> series = new ArrayList<>();
        	for (String sid : cf.series) {
        		Content cs = contentTable.get(sid);
        		Series s = new Series();
        		s.id = cs.id;
        		s.name = cs.name;
        		s.type = cs.type;
        		
        		if (cs.episodes == null) {
            		System.out.println(cs.name + " has no episodes");
        			continue;
        		}
        		
        		List<Episode> episodes = new ArrayList<>();
        		for (String eid : cs.episodes) {
        			Content ce = contentTable.get(eid);
        			Episode e = new Episode();
        			e.id = ce.id;
        			e.name = ce.name;
        			e.type = ce.type;
        			episodes.add(e);
        		}
        		s.episodes = episodes;
        		series.add(s);
        	}
        	f.series = series;
            recommendations.add(f);
        }
        
        System.out.println("recommendations.size() = " + recommendations.size());
        return recommendations;
    }

}
