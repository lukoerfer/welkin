package edu.mit.simile.welkin.resource;

import java.awt.Color;

import org.openrdf.model.URI;

public class ResourceUri extends UriWrapper {

	public Color color;
	
	public ResourceUri(URI uri, Color color) {
		super(uri);
		this.color = color;
	}
}
