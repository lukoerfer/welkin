package edu.mit.simile.welkin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.openrdf.rio.turtle.TurtleParser;

import edu.mit.simile.welkin.ModelCache.WResource;
import edu.mit.simile.welkin.ModelCache.WStatement;
import edu.mit.simile.welkin.resource.PredicateUri;

public class ModelManager implements RDFHandler {

    public static final int RDFXML = 1;
    public static final int TURTLE = 2;
    
    private final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    
    public RDFParser parser;
    public ModelCache cache = new ModelCache();
    
    public void handleStatement(Statement stmt) 
		throws RDFHandlerException 
	{    
    	Resource resource = stmt.getSubject();
    	URI uri = stmt.getPredicate();
    	Value value = stmt.getObject();
    	
        WResource sub = null;
        if (resource instanceof URI) {
            sub = cache.addResource(resource, false);
        } else if (resource instanceof BNode) {
            sub = cache.addBlankResource(resource.toString());
            cache.addBlankResourcesUri(resource.toString());
        }
         
        if (value instanceof URI) {
            WResource obj = cache.addResource(value, true);
        	
            PredicateUri puri = cache.addPredicatesUri(uri);
        	
            sub.addObjectStatement(cache.getStatement(sub, puri, obj));
            cache.addStatement(sub.hash, obj.hash, puri);
            
        } else if (value instanceof Literal) {
            sub.addLiteral(cache.getLiteral(uri , value.toString()));
            
            // Load labels
            // TODO Do we need to put the uri in the literals?
            if(uri.toString().equals(RDFS_LABEL)) {
                sub.label = value.toString();
            }
        } else if (value instanceof BNode) {
            WResource obj = cache.addBlankResource(value.toString());
            PredicateUri puri = cache.addPredicatesUri(uri);
            sub.addObjectStatement(cache.getStatement(sub, puri, obj));
            cache.addStatement(sub.hash, obj.hash, puri);
            cache.addPredicatesUri(uri);
        }
	}
    
    // -----------------------
    //      Model issues
    // -----------------------
    public boolean addModel(FileInputStream in, int type, String baseUri) {
        try {
            initParser(type);
            parser.parse(in, baseUri);
            return true;
        } catch (Exception e) {
            cache.clear();
            ExceptionWin ew = new ExceptionWin("Loading Error", 
                    e.getMessage(), e.toString());
            ew.buildWindow(false);
            ew.setVisible(true);
            return false;
        }
    }
    
    public boolean addModel(InputStream in, int type, String baseUri) {
        try {
            initParser(type);
            parser.parse(in, baseUri);
            return true;
        } catch (RDFParseException e) {
            cache.clear();
            ExceptionWin ew = new ExceptionWin("Loading Error", 
                    "Line: " + e.getLineNumber() + 
                    " Column: " + e.getColumnNumber() + "\n" + e.getMessage(), e.toString());
            ew.buildWindow(false);
            ew.setVisible(true);
            return false;
        } catch (Exception e) {
            cache.clear();
            ExceptionWin ew = new ExceptionWin("Loading Error", 
                    e.getMessage(), e.toString());
            ew.buildWindow(false);
            ew.setVisible(true);
            return false;
        }
    }
    
    /**
     * Adds a model without a base uri.
     * @param in	The inputstream
     * @param type	Type of the parser
     */
    public boolean addModel(FileInputStream in, int type) {
        return addModel(in, type, "");
    }
    
    /**
     * Adds a model without a base uri.
     * @param in	The inputstream
     * @param type	Type of the parser
     */
    public boolean addModel(InputStream in, int type) {
        return addModel(in, type, "");
    }
    
    public void clear() {
        cache.clear();
    }
    
    public boolean isEmpty() {
    	return !(cache.resources.size()>0);
    } 

    // -----------------------
    //    Icons management
    // -----------------------
    public void updateIcons(int id, boolean type, String rule) {
    	WResource node;
    	if(type) {
        	for(Iterator it=cache.resources.iterator(); it.hasNext();) {
                node = ((WResource) it.next());
                WStatement statement;
                for(Iterator ite=node.linkedObjectNodes.iterator(); ite.hasNext();) {
                	statement = ((WStatement)ite.next());
                	if(statement.predicate.getUri().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                		if(((WResource)statement.object).unique.equals(rule)) {
                			node.iconId = id;
                		}
                	}
                }
        	}
    	} else {
        	for(Iterator it=cache.resources.iterator(); it.hasNext();) {
                node = ((WResource) it.next());
				if(node.unique.startsWith(rule)) {
					node.iconId = id;
				}
        	}    		
    	}
    }
    
    public void updateIcons(boolean type, String rule) {
    	WResource node;
    	if(type) {
        	for(Iterator it=cache.resources.iterator(); it.hasNext();) {
                node = ((WResource) it.next());
                WStatement statement;
                for(Iterator ite=node.linkedObjectNodes.iterator(); ite.hasNext();) {
                	statement = ((WStatement)ite.next());
                	if(statement.predicate.getUri().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                		if(((WResource)statement.object).unique.equals(rule)) {
                			node.iconId = -1;
                		}
                	}
                }
        	}
    	} else {
        	for(Iterator it=cache.resources.iterator(); it.hasNext();) {
                node = ((WResource) it.next());
				if(node.unique.startsWith(rule)) {
					node.iconId = -1;
				}
        	}    
    	}
    }
    
    public void clearIcons() {
       	for(Iterator it=cache.resources.iterator(); it.hasNext();) {
            ((WResource) it.next()).iconId = -1;
       	}
    }
    
    // -----------------------
    //       Highlights
    // -----------------------
    public void highlightNode(String text, boolean highlight, boolean highlightOnLabel) {
    	WResource node;
    	for(Iterator it=cache.resources.iterator(); it.hasNext();) {
            node = ((WResource) it.next());
            if(highlightOnLabel) {
	            if(node.label.lastIndexOf(text)!=-1)
	                node.highlighted = true;
            } else {
	            if(node.unique.lastIndexOf(text)!=-1)
	                node.highlighted = true;               
            }
        }
    }

    public void clearHighlights() {
        for(Iterator it=cache.resources.iterator(); it.hasNext();) {
            ((WResource) it.next()).highlighted = false;
        }
    }
    
    // -----------------------
    //  Parser initialization
    // -----------------------
    /**
     * Returns the right parser type.
     * @param type	The parser type
     * @return The requested parser.
     */
    private void initParser(int type) {
        if(type == RDFXML) setXmlRdfParserInstance();
        else if(type == TURTLE) setTurtleParserInstance();
        else throw new IllegalArgumentException("Wrong rdf parser type!");
        
        parser.setRDFHandler(this);
        parser.setVerifyData(true);
        parser.setStopAtFirstError(false);
    }
    
    /**
     * Inits the instance of the xml/rdf Rio parser if present
     * otherwise it will create one.
     * @return The parse instance.
     */
    private void setXmlRdfParserInstance() {
       if (parser==null || !(parser instanceof RDFXMLParser)) {
           parser = new RDFXMLParser();
       }
    }
    
    /**
     * Inits the instance of the xml/rdf Rio parser if present
     * otherwise it will create one.
     * @return The parse instance.
     */
    private void setTurtleParserInstance() {
       if (parser==null || !(parser instanceof TurtleParser)) {
           parser = new TurtleParser();
       }
    }

    // --------------------------
    //  Interface methods, unused
    // --------------------------
	public void endRDF() throws RDFHandlerException {
		// ignore
	}

	public void handleComment(String arg0) throws RDFHandlerException {
		// ignore
	}

	public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
		// ignore
	}

	public void startRDF() throws RDFHandlerException {
		// ignore		
	}
}
