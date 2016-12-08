package com.serverless.content;

import java.util.List;

public class Content {
	public String id;
	public String name;
	public String type;
	public List<String> franchises;
	public List<String> series;
	public List<String> episodes;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public List<String> getFranchises() { return franchises; }
	public void setFranchises(List<String> franchises) { this.franchises = franchises; }

	public List<String> getSeries() { return series; }
	public void setSeries(List<String> series) { this.series = series; }

	public List<String> getEpisodes() { return episodes; }
	public void setEpisodes(List<String> episodes) { this.episodes = episodes; }

	public Content(String id) { this.id = id; }
	public Content() {}
}
