package com.serverless.composite;

public class ContentRequest {
	
	public String operation;
	public Content item;
    
    public String getOperation(){
    	return operation;
    }
    public void setOperation(String operation){
    	this.operation  = operation;
    }
    
    public Content getItem(){
    	return item;
    }
    
    public void setItem(Content item){
    	this.item = item;
    }

    public ContentRequest() {}
}