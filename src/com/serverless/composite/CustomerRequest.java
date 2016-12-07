package com.serverless.composite;

public class CustomerRequest {
	
	public String operation;
	public Customer item;
    
    public String getOperation(){
    	return operation;
    }
    public void setOperation(String operation){
    	this.operation  = operation;
    }
    
    public Customer getItem(){
    	return item;
    }
    
    public void setItem(Customer item){
    	this.item = item;
    }

    public CustomerRequest() {}
}