package com.serverless.asgn1;

import java.util.*;

public class CustomerResponse {
    String message;
    List<Item> items;
    
    public String getMessage(){
    	return message;
    }
    public void setMessage(String message){
    	this.message = message;
    }
       
    public CustomerResponse(String message) {
        this.message = message;
        items = new ArrayList();
    }

    public CustomerResponse() {
    }
    
    public List<Item> getItem(){
    	return items;
    }
    public void setItem(List<Item> item){
    	this.items = item;
    }
    
    public void addItem(Item item){
    	this.items.add(item);
    }
    
    class Item {
        public String email;
        public String lastname;
        public String firstname;
        public String phonenumber;
        public String address_ref;
        
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getAddress(){
        	return address_ref;
        }
        public void setAddress(String address){
        	this.address_ref = address;
        }

        public Item(String email) {
            this.email = email;
        }
        
        public Item(){
        	
        }
    }
}