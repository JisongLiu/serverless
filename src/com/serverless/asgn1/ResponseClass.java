package com.serverless.asgn1;

public class ResponseClass {
    String greetings;
    String errors;
    Item item;
    
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

    public ResponseClass(String greetings, String errors) {
        this.greetings = greetings;
        this.errors = errors;
    }

    public ResponseClass() {
    }
    
    public Item getItem(){
    	return item;
    }
    
    public void setItem(Item item){
    	this.item = item;
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