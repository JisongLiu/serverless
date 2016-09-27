package com.serverless.asgn1;

public class ResponseClass {
    String greetings;
    String errors;
    
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
}


