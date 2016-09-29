package com.serverless.asgn1;

import java.util.ArrayList;
import java.util.List;

public class CustomerResponse {
    String greetings;
    String errors;
    List<Item> item = new ArrayList<Item>();
    
    public String getErrors(){
        return errors;
    }
    
    public void setErrors(String errors){
        this.errors = errors;
    }

    public String getGreetings() {
        return greetings;
    }

    public void setGreetings(String greetings) {
        this.greetings = greetings;
    }

    public CustomerResponse(String greetings, String errors) {
        this.greetings = greetings;
        this.errors = errors;
    }

    public CustomerResponse() {
    }
    
    public List<Item> getItem(){
        return item;
    }
    
    public void addItem(Item item){
        this.item.add(item);
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

        public Item(String email) {
            this.email = email;
        }
        
        public Item(){
            
        }
    }
}