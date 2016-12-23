package com.serverless.graphDB;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.Constants;
import com.serverless.customer.Customer;
import com.serverless.customer.CustomerResponse;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;



public class GraphDBRequestHandler implements RequestHandler<GraphDBRequest, Object> {
	
	@Override
    public Object handleRequest(GraphDBRequest request, Context context) {
		Driver driver = GraphDatabase.driver( "bolt://hobby-benonmpgjildgbkeccapglol.dbs.graphenedb.com:24786", AuthTokens.basic( "serverlessApp", "w225WDsgJXte0iVVdhNa" ) );
		Session session = driver.session();
		String query = "";
        if (request.object.equals("customer")) {
        	String firstname = request.customer.firstname;
        	String lastname = request.customer.lastname;
        	String email = request.customer.email;
        	//Customer c = request.customer;
        	switch (request.operation) {
        	case "create":
        		query = "CREATE (a:Person {firstname:'"+firstname+"',lastname:'"+lastname+"', email:'"+email+"'})";
        		try {
            		session.run( query );
                } catch (Exception e) {
                    throw new IllegalArgumentException("400 Bad Request -- email already exists");
                }
        		return new CustomerResponse("Success");
        		//session.run( query );
        	case "query":
        		StatementResult result = session.run( "MATCH (a:Person) WHERE a.email = '"+email+"' RETURN a" );
        		CustomerResponse resp = new CustomerResponse("Success");
        		if(!result.hasNext()){
        		    return new CustomerResponse("No such customer");
        		}
        		while ( result.hasNext() )
        		{
        		    Record record = result.next();
        		    Customer c = new Customer();
                    c.email	  = record.get( "email" ).asString();
                    String firstName    = record.get( "firstname" ).asString();
                    String lastName    = record.get( "lastname" ).asString();
                    c.firstname = firstName;
                    c.lastname = lastName;
                    c.phonenumber = record.get( "phonenumber" ).asString();
                    resp.addItem(c);
        		}
                return resp;
        	case "update":
        		query = "MATCH (a:Person) WHERE a.email = '"+email+"' SET a.firstname = '"+firstname+"', a.lastname = '"+lastname+"'";
        		try {
            		session.run( query );
                } catch (ConditionalCheckFailedException e) {
                    throw new IllegalArgumentException("400 Bad Request -- email doesn't exist");
                }
        		return new CustomerResponse("Success");
        	case "delete":
        		query = "MATCH (a:Person) WHERE a.email = '"+email+"' DETACH DELETE a";
        		session.run( query );
        		return new CustomerResponse("Success");
     		default:
     			throw new IllegalArgumentException("400 Bad Request -- unsupported operation " + request.operation);
        	}

        } else if (request.object.equals("content")) {
        	String name = request.content.name;
        	String id = request.content.id;
        	String type = request.content.type;
        	switch (request.operation) {
        	case "create":
        		query = "CREATE (a:Content {id:'"+id;
        		if(name!=null){
            		query = query + "', name:'"+name;
        		}
        		if(type!=null){
            		query = query + "', type:'"+type;
        		}
        		query = query + "'})";
        		try {
            		session.run( query );
                } catch (ConditionalCheckFailedException e) {
                    throw new IllegalArgumentException("400 Bad Request -- email already exists");
                }
        		return new ContentResponse("Success");
        		//session.run( query );
        	case "query":
        		StatementResult result = session.run( "MATCH (a:Content) WHERE a.id = '"+id+"' RETURN a.name AS name" );
        		ContentResponse resp = new ContentResponse("Success");
        		if(!result.hasNext()){
        		    return new CustomerResponse("No such content");
        		}
        		while ( result.hasNext() )
        		{
        		    Record record = result.next();
        		    Content c = new Content();
                    c.name	  = record.get( "name" ).asString();
                    resp.addItem(c);
        		}
                return resp;
        	case "update":
        		query = "MATCH (a:Content) WHERE a.id = '"+id+"' SET a.name = '"+name+"', a.type = '"+type+"'";
        		try {
            		session.run( query );
                } catch (ConditionalCheckFailedException e) {
                    throw new IllegalArgumentException("400 Bad Request -- email doesn't exist");
                }
        		return new ContentResponse("Success");
        	case "delete":
        		query = "MATCH (a:Content) WHERE a.id = '"+id+"' DETACH DELETE a";        		session.run( query );
        		return new ContentResponse("Success");
     		default:
     			throw new IllegalArgumentException("400 Bad Request -- unsupported operation " + request.operation);
        	}
        } else if (request.object.equals("comment")) {
        	String comment = request.comment.comment;
        	String id = request.comment.id;
        	String user = request.comment.user;
        	String ct = request.comment.content;
        	switch (request.operation) {
        	case "create":
        		query = "match (ct:Content), (p:Person) where ct.id = '"+ct+"' AND p.email = '"+user+"' create (c:Comment {ID:'"+id+"', content:'"+comment+"'}), (c)-[:comment_on]->(ct), (p)-[:author]->(c)";
        		
        		try {
            		session.run( query );
                } catch (ConditionalCheckFailedException e) {
                    throw new IllegalArgumentException("400 Bad Request -- email already exists");
                }
        		return new CommentResponse("Success");
        		//session.run( query );
        	case "query":
        		if(user!=null && ct!=null){
        			query = "MATCH (c)-[:comment_on]->(ct), (p)-[:author]->(c) where ct.id='"+ct+"' AND p.email='"+user+"' RETURN c.ID AS cid, c.content AS cc, ct.id AS ctid, p.email AS pe LIMIT 25";
        		}
        		if(user!=null && ct==null){
        			query = "MATCH (c)-[:comment_on]->(ct), (p)-[:author]->(c) where p.email='"+user+"' RETURN c.ID AS cid, c.content AS cc, ct.id AS ctid, p.email AS pe LIMIT 25";
        		} 
        		if(user==null && ct!=null){
        			query = "MATCH (c)-[:comment_on]->(ct), (p)-[:author]->(c) where ct.id='"+ct+"' RETURN c.ID AS cid, c.content AS cc, ct.id AS ctid, p.email AS pe LIMIT 25";
        		}
        		StatementResult result = session.run(query);
        		CommentResponse resp = new CommentResponse("Success");
        		if(!result.hasNext()){
        		    return new CustomerResponse("No such comment");
        		}
        		while ( result.hasNext() )
        		{
        		    Record record = result.next();
        		    Comment c = new Comment();
                    c.user	  = record.get( "pe" ).asString();
                    c.id 	  = record.get( "cid" ).asString();
                    c.comment 	  = record.get( "cc" ).asString();
                    c.content 	  = record.get( "ctid" ).asString();
                    resp.addItem(c);
        		}
                return resp;
        	
        	case "delete":
        		query = "MATCH (a:Comment) WHERE a.id = '"+id+"' DETACH DELETE a";        		session.run( query );
        		return new CommentResponse("Success");
     		default:
     			throw new IllegalArgumentException("400 Bad Request -- unsupported operation " + request.operation);
        	}
        } 
        return null;
    }
	
}
