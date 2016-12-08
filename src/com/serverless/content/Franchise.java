package com.serverless.content;

import java.util.List;

public class Franchise {
	public String id;
	public String name;
	public String type;
	public List<Series> series;
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	
	public List<Series> getSeries() { return series; }
	public void setSeries(List<Series> series) { this.series = series; }
	
	public Franchise() {}
}
