package edu.mit.simile.welkin.resource;

import java.awt.Color;


public class PartialUri {
	private int count;
	private String base;
	public Color color;
	
	public PartialUri(String base, Color color) {
		this.count = 1;
		this.base = base;
		this.color = color;
	}
	
	public int getCount() {
		return count;
	}
	
	public void incCount() {
		count++;
	}
	
	public String getBase() {
		return base;
	}
	
    public int hashCode() { 
    	return base.hashCode(); 
    }
    
    public boolean equals(Object obj) {
    	if(obj == null || !(obj instanceof PartialUri)) return false;
    	return ((PartialUri)obj).base.equals(base);
    }
}
