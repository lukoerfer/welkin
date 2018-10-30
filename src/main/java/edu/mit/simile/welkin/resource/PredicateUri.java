package edu.mit.simile.welkin.resource;

import org.openrdf.model.URI;

public class PredicateUri extends UriWrapper {

	public float weight;
	public boolean included;
	private int count;
	
	public PredicateUri(URI uri, float weight) {
		super(uri);
		this.weight = weight;
		this.included = true;
		this.count = 1;
	}
	
	public void incCount() {
		this.count++;
	}
	
	public int getCount() {
		return count;
	}
}
