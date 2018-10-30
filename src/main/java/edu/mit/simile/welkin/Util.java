package edu.mit.simile.welkin;


public class Util {
    
    public static String[] splitUri(String uri) {
    	String[] parts = null;

    	if(uri.startsWith("urn:")) { // urn:*:*
    		parts = new String[2];
    		int lastIndex = uri.lastIndexOf(":");
    		parts[1] = uri.substring(lastIndex);
    		parts[0] = uri.substring(0, lastIndex);
    	} else if(uri.startsWith("http:")|| uri.startsWith("https:")) { // http://*/*#* 
    		parts = new String[3];
    		int lastIndex = uri.indexOf("#");
    		if(lastIndex==-1) lastIndex = uri.lastIndexOf("/");
        	parts[2] = uri.substring(lastIndex);
        	int slashIndex = uri.indexOf('/',7);
    		parts[1] = uri.substring(slashIndex, lastIndex);
    		parts[0] = uri.substring(0, slashIndex);
    	} else if(uri.startsWith("file:")) { // urn:*:*
    		parts = new String[2];
    		int lastIndex = uri.lastIndexOf("/");
    		parts[1] = uri.substring(lastIndex);
    		parts[0] = uri.substring(0, lastIndex);
    	}
    	
    	return parts;
    }
    
    public static String[] splitUriBases(String uri) {
    	String[] parts = null;
    	
    	if(uri.startsWith("urn:")) { // urn:*:*
    		parts = new String[1];
    		int lastIndex = uri.lastIndexOf(":");
    		parts[0] = uri.substring(0, lastIndex);
    	} else if(uri.startsWith("http:")|| uri.startsWith("https:")) { // http://*/*#* 
    		parts = new String[2];
    		int lastIndex = uri.indexOf("#");
    		if(lastIndex==-1) lastIndex = uri.lastIndexOf('/');
    		if(lastIndex==-1) lastIndex = uri.length()-1;
        	int slashIndex = uri.indexOf('/',7);
        	if(lastIndex>6) {
	    		if(slashIndex<lastIndex) parts[1] = uri.substring(slashIndex, lastIndex);
	    		parts[0] = uri.substring(0, slashIndex);
        	}
    	} else if(uri.startsWith("file:")) { // urn:*:*
    		parts = new String[1];
    		int lastIndex = uri.lastIndexOf("/");
    		parts[0] = uri.substring(0, lastIndex);
    	}
    	
    	return parts;
    }
    
//    public static String getUriBase(String uri) {
//
//    	if(uri.startsWith("urn:")) { // urn:*:*
//    		int lastIndex = uri.lastIndexOf(":");
//    		return uri.substring(0, lastIndex);
//    	} else if(uri.startsWith("http:")) { // http://*/*#* 
//    		int lastIndex = uri.indexOf("#");
//    		if(lastIndex==-1) lastIndex = uri.lastIndexOf("/");
//    		return uri.substring(0, lastIndex);
//    	} else if(uri.startsWith("file:")) { // urn:*:*
//    		int lastIndex = uri.lastIndexOf("/");
//    		return uri.substring(0, lastIndex);
//    	}
//    	
//    	return null;
//    }
}
