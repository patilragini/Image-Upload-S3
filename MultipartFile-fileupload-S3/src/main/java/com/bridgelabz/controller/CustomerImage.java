package com.bridgelabz.controller;

public class CustomerImage {

    public CustomerImage(){}
 
    public CustomerImage(String key, String url) {
       this.key = key;
       this.url =url;  
    }
    private long id;
 
  
    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private String key;
 
 
    private String url;
 
}