package com.serverless.content;

public class ContentRequest {
	public String object;
	public String operation;
	public Content item;
	
	
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
    
    public Content getItem(){
    	return item;
    }
    
    public void setItem(Content item){
    	this.item = item;
    }

    public ContentRequest() {}
}