package com.serverless.composite;

public class CompositeRequest {
	public String object;
	public String operation;
	public Address address;
	public Customer customer;
	public Content content;
	public Comment comment;
    
    public String getOperation(){
    	return operation;
    }
    public void setOperation(String operation){
    	this.operation  = operation;
    }
    
    public Address getAddress(){
    	return address;
    }
    
    public void setAddress(Address address){
    	this.address = address;
    }
    
    public Customer getCustomer(){
    	return customer;
    }
    
    public void setCustomer(Customer customer){
    	this.customer = customer;
    }
    
    public String getObject(){
    	return object;
    }
    
    public void setObject(String object){
    	this.object = object;
    }
    
    public Comment getComment(){
    	return comment;
    }
    
    public void setComment(Comment comment){
    	this.comment = comment;
    }
    
    public Content getContent(){
    	return content;
    }
    
    public void setContent(Content content){
    	this.content = content;
    }

    public CompositeRequest() {
    }
}

