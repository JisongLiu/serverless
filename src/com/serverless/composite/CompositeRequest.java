package com.serverless.composite;

public class CompositeRequest {
	public String object;
	public String operation;
	public Address address;
	public Customer customer;
    
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

    public CompositeRequest() {
    }
}

