package com.serverless.address;

public class Address {
    public String id;
    public String line1;
    public String line2;
    public String city;
    public String state;
    public String zipcode;
    
    public String getid() { return id; }
    public void setid(String id) { this.id = id; }

    public void setLine1(String line1) { this.line1 = line1; }
    public String getLine1() { return this.line1; }

    public void setLine2(String line2) { this.line2 = line2; }
    public String getLine2() { return this.line2; }
    
    public void setCity(String city) { this.city = city; }
    public String getCity() { return this.city; }
    
    public void setstate(String state) { this.state = state; }
    public String getstate() { return this.state; }
    
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }
    public String getZipcode() { return this.zipcode; }
    
    public Address(String id) {
        this.id = id;
    }
    
    public Address() {}
}
