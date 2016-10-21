package com.serverless.address;

public class AddressRequest {
	public String operation;
	public Address item;
    
    public String getOperation(){
    	return operation;
    }
    public void setOperation(String operation){
    	this.operation  = operation;
    }
    
    public Address getItem(){
    	return item;
    }
    
    public void setItem(Address item){
    	this.item = item;
    }

    public AddressRequest() {
    }
}
