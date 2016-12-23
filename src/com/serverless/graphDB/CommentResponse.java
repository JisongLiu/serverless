package com.serverless.graphDB;

import java.util.*;

public class CommentResponse {
    String message;
    List<Comment> items;
    
    public String getMessage(){
    	return message;
    }
    public void setMessage(String message){
    	this.message = message;
    }
       
    public CommentResponse(String message) {
        this.message = message;
        this.items = new ArrayList<>();
    }

    public CommentResponse() {
    }
    
    public List<Comment> getItems(){
    	return items;
    }
    public void setItems(List<Comment> item){
    	this.items = item;
    }
    
    public void addItem(Comment item){
    	this.items.add(item);
    }
}