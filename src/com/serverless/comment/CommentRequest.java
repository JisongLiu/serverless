package com.serverless.comment;

public class CommentRequest {
	
	public String object;
	public String operation;
	public Comment item;
	
	
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
    
    public Comment getItem(){
    	return item;
    }
    
    public void setItem(Comment item){
    	this.item = item;
    }

    public CommentRequest() {}
}