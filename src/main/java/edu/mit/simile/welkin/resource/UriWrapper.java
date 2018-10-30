package edu.mit.simile.welkin.resource;

import org.openrdf.model.URI;

public class UriWrapper {

	private URI uri;
	
	public UriWrapper(URI uri) {
		this.uri = uri;
	}
	
	public String getUri() {
		return uri.toString();
	}
	
    public int hashCode() { 
    	return uri.hashCode(); 
    }
    
    public boolean equals(Object obj) {
    	return uri.equals(((UriWrapper)obj).uri);
    }
}
