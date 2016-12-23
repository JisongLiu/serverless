package com.serverless.address;

public class AddressRequest {
	public String object;
	public String operation;
	public Address item;
	
	public String getObject(){
		return object;
	}
	
	public void setObject(String object){
		this.object = object;
	}
    
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
