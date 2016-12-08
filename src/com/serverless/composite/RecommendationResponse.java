package com.serverless.composite;

import java.util.List;

import com.serverless.content.Franchise;

public class RecommendationResponse {
    String message;
    List<Franchise> items;
    
    public String getMessage() { return message; }
    public void setMessage(String message){ this.message = message; }
    
    public List<Franchise> getItems() { return items; }
    public void setItems(List<Franchise> items) { this.items = items; }
    
    public RecommendationResponse() {}
}
