package com.serverless;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.serverless.address.AddressRequest;
import com.serverless.address.AddressResponse;
import com.serverless.comment.Comment;
import com.serverless.comment.CommentRequest;
import com.serverless.comment.CommentResponse;
import com.serverless.composite.LambdaFunctionHandler.CommentService;
import com.serverless.content.ContentRequest;
import com.serverless.content.ContentResponse;
import com.serverless.customer.Customer;
import com.serverless.customer.CustomerRequest;
import com.serverless.customer.CustomerResponse;
public class SNSHandler implements RequestHandler<SNSEvent, Void> {
	
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
	
    public Void handleRequest(SNSEvent request, Context context){
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
    context.getLogger().log("Invocation started: " + timeStamp);
    String snsMsg = request.getRecords().get(0).getSNS().getMessage();
    System.out.println(snsMsg);
    JSONObject obj;
		try {
		    AWSLambdaClient lambda = new AWSLambdaClient();
			lambda.configureRegion(Regions.US_EAST_1);
			obj = new JSONObject(snsMsg);
			JSONObject itemObj = obj.getJSONObject("item");
			String object = obj.getString("object");
			String operation = obj.getString("operation");
			if(object.equals("comment")){
			    String id = obj.getJSONObject("item").getString("id");
			    String comment = obj.getJSONObject("item").getString("comment");
			    String content = obj.getJSONObject("item").getString("content");
			    String user = obj.getJSONObject("item").getString("user");
				CommentService service = LambdaInvokerFactory.build(CommentService.class, lambda);
				CommentRequest req = new CommentRequest();
				Comment com = new Comment(id);
				com.setComment(comment);
				com.setContent(content);
				com.setUser(user);
				req.setItem(com);
				req.setOperation(operation);
				CommentResponse res = service.CommentRequestHandler(req);
				context.getLogger().log(res.getMessage());
			} else if(object.equals("customer")){
			  
				 String email = itemObj.getString("email");
				 String firstname = itemObj.isNull("firstname")?null:itemObj.getString("firstname");
				 String lastname = itemObj.isNull("lastname")?null:itemObj.getString("lasttname");
				 String phonenumber = itemObj.isNull("phonenumber")?null:itemObj.getString("phonenumber");
				 String address_ref = itemObj.isNull("address_ref")?null:itemObj.getString("address_ref");
				 CustomerService service = LambdaInvokerFactory.build(CustomerService.class, lambda);
				 CustomerRequest req = new CustomerRequest();
				 Customer cus = new Customer();
				 cus.setEmail(email);
				 cus.setFirstname(firstname);
				 cus.setLastname(lastname);
				 cus.setPhonenumber(phonenumber);
				 cus.setAddress_ref(address_ref);
				 req.setItem(cus);
				 req.setOperation(operation);
				 CustomerResponse res = service.CustomerRequestHandler(req);
				 context.getLogger().log(res.getMessage());
				
			} else if(object.equals("address")){
				
			} else if(object.equals("content")) {
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
    context.getLogger().log("Invocation completed: " + timeStamp);
           return null;
     }
}
