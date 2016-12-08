package com.serverless.composite;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.regions.Regions;
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
    
        if (request.object.equals("address")) {
        	//System.out.println("hello");
        	AWSLambdaClient lambda = new AWSLambdaClient();
        	lambda.configureRegion(Regions.US_EAST_1);
        	AddressService service = LambdaInvokerFactory.build(AddressService.class, lambda);
        	AddressRequest req = new AddressRequest();
        	req.setOperation(request.getOperation());
        	req.setItem(request.getAddress());
        	AddressResponse res = service.AddressRequestHandler(req);

        	return res;
        	
        } else if (request.object.equals("customer")) {
        	//System.out.println("hello2");
        	AWSLambdaClient lambda = new AWSLambdaClient();
        	lambda.configureRegion(Regions.US_EAST_1);
        	CustomerService service = LambdaInvokerFactory.build(CustomerService.class, lambda);
        	CustomerRequest req = new CustomerRequest();
        	req.setOperation(request.getOperation());
        	req.setItem(request.getCustomer());
        	CustomerResponse res = service.CustomerRequestHandler(req);

        	return res;


        } else if (request.object.equals("content")) {
        	//System.out.println("hello2");
        	AWSLambdaClient lambda = new AWSLambdaClient();
        	lambda.configureRegion(Regions.US_EAST_1);
        	ContentService service = LambdaInvokerFactory.build(ContentService.class, lambda);
        	ContentRequest req = new ContentRequest();
        	req.setOperation(request.getOperation());
        	if(request.content==null){
        		req.setItem(new Content());
        	} else{
        		req.setItem(request.getContent());
        	}
        	ContentResponse res = service.ContentRequestHandler(req);

        	return res;


        } else if (request.object.equals("comment")) {
        	//System.out.println("hello2");
        	AWSLambdaClient lambda = new AWSLambdaClient();
        	lambda.configureRegion(Regions.US_EAST_1);
        	CommentService service = LambdaInvokerFactory.build(CommentService.class, lambda);
        	CommentRequest req = new CommentRequest();
        	req.setOperation(request.getOperation());
        	req.setItem(request.getComment());
        	CommentResponse res = service.CommentRequestHandler(req);

        	return res;


        } 
        return null;
    }

}
