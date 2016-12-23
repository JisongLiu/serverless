package com.serverless.graphDB;

public class Customer {
    public String email;
    public String firstname;
    public String lastname;
    public String phonenumber;
    public String address_ref;
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    
    public String getAddress_ref() { return address_ref; }
    public void setAddress_ref(String address) { this.address_ref = address; }

    public Customer(String email) {
        this.email = email;
    }
    
    public Customer() {
    	
    }
}
