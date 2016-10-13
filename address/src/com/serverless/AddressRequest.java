package com.serverless;

public class AddressRequest {
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

    public AddressRequest() {
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
