package com.serverless.address;

import java.util.*;

public class AddressResponse {
    String message;
    List<Address> items;
    
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }
       
    public AddressResponse(String message) {
        this.message = message;
        this.items = new ArrayList<>();
    }

    public AddressResponse() {
    }
    
    public List<Address> getItems(){
        return items;
    }
    public void setItems(List<Address> item){
        this.items = item;
    }
    
    public void addItem(Address item){
        this.items.add(item);
    }
}