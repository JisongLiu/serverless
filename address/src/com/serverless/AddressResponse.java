package com.serverless;

import java.util.*;

public class AddressResponse {
    String message;
    List<Item> items;
    
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }
       
    public AddressResponse(String message) {
        this.message = message;
        items = new ArrayList();
    }

    public AddressResponse() {
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
        public String id;
        public String city;
        public String street;
        public String number;
        public String zipCode;
        
        public String getid() {
            return id;
        }

        public void setid(String id) {
            this.id = id;
        }
        
        public Item(String id) {
            this.id = id;
        }
        
        public Item(){
            
        }
    }
}