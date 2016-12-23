package com.serverless.graphDB;

import java.util.*;

public class ContentResponse {
    String message;
    List<Content> items;
    
    public String getMessage(){
    	return message;
    }
    public void setMessage(String message){
    	this.message = message;
    }
       
    public ContentResponse(String message) {
        this.message = message;
        this.items = new ArrayList<>();
    }

    public ContentResponse() {
    }
    
    public List<Content> getItems(){
    	return items;
    }
    public void setItems(List<Content> item){
    	this.items = item;
    }
    
    public void addItem(Content item){
    	this.items.add(item);
    }
}