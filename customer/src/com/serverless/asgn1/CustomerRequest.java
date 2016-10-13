package com.serverless.asgn1;

public class CustomerRequest {
	
	public String operation;
	public Item item;
    
    public String getOperation(){
    	return operation;
    }
    public void setOperation(String operation){
    	this.operation  = operation;
    }
    
    public Item getItem(){
    	return item;
    }
    
    public void setItem(Item item){
    	this.item = item;
    }

    public CustomerRequest() {
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