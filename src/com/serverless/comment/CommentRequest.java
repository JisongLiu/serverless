package com.serverless.comment;

public class CommentRequest {
	
	public String operation;
	public Comment item;
    
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