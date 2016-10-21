package com.serverless.customer;

import java.util.*;

public class CustomerResponse {
    String message;
    List<Customer> items;
    
    public String getMessage(){
    	return message;
    }
    public void setMessage(String message){
    	this.message = message;
    }
       
    public CustomerResponse(String message) {
        this.message = message;
        this.items = new ArrayList<>();
    }

    public CustomerResponse() {
    }
    
    public List<Customer> getItems(){
    	return items;
    }
    public void setItems(List<Customer> item){
    	this.items = item;
    }
    
    public void addItem(Customer item){
    	this.items.add(item);
    }
}