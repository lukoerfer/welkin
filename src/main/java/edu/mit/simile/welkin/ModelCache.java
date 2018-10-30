package edu.mit.simile.welkin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import edu.mit.simile.welkin.resource.PartialUri;
import edu.mit.simile.welkin.resource.PredicateUri;

public class ModelCache {

    public static final String ANONYMOUS_RES = "Anonymous";

    boolean blankNodes = false;
    Set resources = new HashSet();
    List predicates = new ArrayList();
    List resourcesBases = new ArrayList();
    HashMap statements = new HashMap();
    ValueFactory factory = new ValueFactoryImpl();

    public void clear() {
        blankNodes = false;
        resources.clear();
        predicates.clear();
        resourcesBases.clear();
        statements.clear();
    }

    public void addStatement(int hashSubject, int hashObject, PredicateUri predicate) {
        if (hashSubject == hashObject) return;
        WDoubleHashKey key = new WDoubleHashKey(hashSubject, hashObject);
        Object value = statements.get(key);
        if (value != null) {
            Object[] values = (Object[]) value;
            PredicateUri newValues[] = new PredicateUri[values.length+1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = predicate;
        } else {
        	PredicateUri[] values = new PredicateUri[1];
            values[0] = predicate;
            statements.put(key, values);
        }
    }

    PredicateUri ns;
    public PredicateUri addPredicatesUri(URI uri) {
    	ns = new PredicateUri(uri, 1);
    	int index = predicates.indexOf(ns);
        if(index == -1) {
            predicates.add(ns);
            return ns;
        }
        ns = (PredicateUri) predicates.get(index);
        ns.incCount();
        return ns;
    }

    private PartialUri pu;
    public PartialUri addResourcesUri(URI uri) {
    	pu = new PartialUri(uri.getNamespace(), Color.RED);
        int index = resourcesBases.indexOf(pu);
        if(index == -1) {
            resourcesBases.add(pu);
            return pu;
        }
        pu = (PartialUri) resourcesBases.get(index);
        pu.incCount();
        return pu;
    }

    public PartialUri addBlankResourcesUri(String uri) {
    	pu = new PartialUri(ANONYMOUS_RES, Color.RED);
        int index = resourcesBases.indexOf(pu);
        if(index == -1) {
            resourcesBases.add(pu);
            return pu;
        }
        pu = (PartialUri) resourcesBases.get(index);
        pu.incCount();
        return pu;
    }

    WDoubleHashKey tmp = new WDoubleHashKey();
    public PredicateUri[] getEntries(int hashSubject, int hashObject) {
        tmp.x = hashSubject;
        tmp.y = hashObject;
        return (PredicateUri[]) statements.get(tmp);
    }

    public WResource addResource(String unique, float[] coordinates, boolean isNodeFixed, boolean isNotSubject) {
        WResource resource;
        if(coordinates!=null) {
            resource = new WResource(unique, coordinates[0], coordinates[1], isNodeFixed, isNotSubject);
        } else {
            resource = new WResource(unique, isNotSubject);
        }

        if (resources.add(resource)) return resource;
        else return null;
    }

    public WResource addResource(Object value, boolean isNotSubject) {
        WResource res = new WResource(value.toString(), isNotSubject);
        if (resources.add(res)) {
        	addResourcesUri((URI)value);
        	return res;
        } else {
            for(Iterator it = resources.iterator(); it.hasNext();) {
                WResource tmp = (WResource) it.next();
                if(res.equals(tmp)) {
                	if(!isNotSubject) tmp.isNotSubject = false;
                	return tmp;
                }
            }
        }
        return null;
    }

    public WResource addBlankResource(String unique) {
        if(!blankNodes) {
            blankNodes = true;
        }

        WResource res = new WResource(unique, true, true);
        if (resources.add(res)) return res;
        else {
            for(Iterator it = resources.iterator(); it.hasNext();) {
                WResource tmp = (WResource) it.next();
                if(res.equals(tmp)) return tmp;
            }
        }
        return null;
    }

    public void setUriColor(String prefix, Color color) {
        for(Iterator it = resourcesBases.iterator(); it.hasNext();) {
        	PartialUri uri = (PartialUri) it.next();
            if(uri.getBase().startsWith(prefix))
                uri.color = color;
        }
    }

    public void setVisible(PartialUri resource, boolean isVisible) {
        for (Iterator it = resources.iterator(); it.hasNext();) {
            WResource res = (WResource) it.next();
            if(res.unique.startsWith(resource.getBase())) {
                if (isVisible) {
                    res.show();
                } else {
                    res.hide();
                }
            }
        }
    }

    public WResource getResource(String unique) {
        for (Iterator it = resources.iterator(); it.hasNext();) {
            WResource tmp = (WResource) it.next();
            if (tmp.unique.equals(unique))
                return tmp;
        }
        return null;
    }

    public WStatement getStatement(WResource subject, PredicateUri predicate, WResource object) {
        return new WStatement(subject, predicate, object);
    }

    public WLiteral getLiteral(URI predicate, String literal) {
        return new WLiteral(predicate, literal);
    }

    public void setLabel(String unique, String label) {
        getResource(unique).label = label;
    }

    Set validatedNodes = new HashSet();

    public void uriBasedVisualization(PredTree tree) {
        validatedNodes.clear();
        for (Iterator it = resources.iterator(); it.hasNext();) {
            WResource node = (WResource) it.next();

            boolean flag = false;
            for (Iterator i = node.linkedObjectNodes.iterator(); i.hasNext();) {
                WStatement edge = (WStatement) i.next();
                if (edge.predicate.weight>0 && edge.predicate.included) {
                    flag = true;
                    validatedNodes.add(edge.object);
                }
            }

            if (flag) {
                validatedNodes.add(node);
            } else {
                node.hide();
            }
        }

        for (Iterator it = validatedNodes.iterator(); it .hasNext();) {
            ((WResource) it.next()).show();
        }
    }

    public void adjustResourcesUriBaseColor() {
        for(Iterator i=resourcesBases.iterator();i.hasNext();) {
        	PartialUri ns = (PartialUri) i.next();
	        for(Iterator it=resources.iterator();it.hasNext();) {
	            WResource tmp = (WResource) it.next();
	            if(blankNodes && ns.getBase().equals(ModelCache.ANONYMOUS_RES) && tmp.isBlankNode ) {
	                tmp.color = ns.color;
	            } else if(tmp.unique.startsWith(ns.getBase())) {
	                tmp.color = ns.color;
	            }
	        }
        }
    }

    public class WResource {
        float x, y, vx, vy;

        final String unique;

        boolean visibility = true;

        boolean fixed = false;
        boolean isBlankNode = false;
        boolean highlighted = false;
        boolean isNotSubject = true;

        Set linkedSubjectNodes = new HashSet();
        Set linkedObjectNodes = new HashSet();
        Set linkedLiterals = new HashSet();

        int hash = 0;
        int iconId=-1;
        
        Color color;
        String label;

        WResource(final String unique, final boolean isNotSubject) {
            this(unique,isNotSubject,false);
        }

        WResource(final String unique, final boolean isNotSubject, boolean isBlank) {
            this.unique = this.label = unique;
            this.isBlankNode = isBlank;
            this.isNotSubject = isNotSubject;
            calculateHashCode();
        }

        WResource(final String unique, final float x, final float y, final boolean isFixed, final boolean isNotSubject) {
            this.unique = this.label = unique;
            calculateHashCode();

            this.fixed = isFixed;
            this.isNotSubject = isNotSubject;
            this.x = x;
            this.y = y;
        }

        void addObjectStatement(WStatement statement) {
            linkedObjectNodes.add(statement);
            statement.object.linkedSubjectNodes.add(statement);
        }

        void addLiteral(WLiteral literal) {
            linkedLiterals.add(literal);
        }

        Iterator getLiterals() {
            return linkedLiterals.iterator();
        }

        boolean isVisible() {
            return this.visibility;
        }

        void show() {
            this.visibility = true;
        }

        void hide() {
            this.visibility = false;
        }

        boolean isObjectOf(final WResource res) {
            for (Iterator it = linkedObjectNodes.iterator(); it.hasNext();) {
                if (((WStatement) it.next()).object.equals(res))
                    return true;
            }
            return false;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof WResource)) return false;

            if (this.unique.equals(((WResource) o).unique)) return true;
            else return false;
        }

        public int hashCode() { return hash; }

        private void calculateHashCode() {
            int h = hash;
            if (h == 0) {
                int off = 0;
                char val[] = unique.toCharArray();
                int len = unique.length();

                for (int i = 0; i < len; i++) {
                    h = 31 * h + val[off++];
                }
                hash = h;
            }
        }
    }

    public class WLiteral {
        final URI predicate;
        final String literal;

        WLiteral(final URI predicate, final String literal) {
            this.predicate = predicate;
            this.literal = literal;
        }

        public boolean equals(Object obj) {
            if(obj instanceof WLiteral) {
                WLiteral lit = (WLiteral)obj;
                if(lit.literal.equals(literal) && lit.predicate.equals(predicate))
                        return true;
            }
            return false;
        }

        public int hashCode() {
            int h = 0;
            int off = 0;
            char val[] = literal.toCharArray();
            int len = literal.length();

            for (int i = 0; i < len; i++) {
                h = 31 * h + val[off++];
            }

            return predicate.hashCode() + 16 * h;
        }
    }

    public class WStatement {
        final PredicateUri predicate;
        final WResource subject, object;

        WStatement(final WResource subject, final PredicateUri predicate,  final WResource object) {
            this.predicate = predicate;
            this.subject = subject;
            this.object = object;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof WStatement)) return false;

            if (subject.equals(((WStatement)o).subject)
                    && object.equals(((WStatement)o).object)
                    && predicate.equals(((WStatement)o).predicate)) return true;
            else return false;
        }

        public int hashCode() {
            return subject.hashCode()+predicate.hashCode()*5+object.hashCode()*10;
        }
    }

    class WDoubleHashKey {
        int x,y;
        public WDoubleHashKey () {}
        public WDoubleHashKey(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        public boolean equals(Object obj) {
        	if (obj instanceof WDoubleHashKey) {
        	    WDoubleHashKey pt = (WDoubleHashKey)obj;
        	    return (x == pt.x) && (y == pt.y);
        	}
        	return false;
        }

        public int hashCode() {
        	long bits = java.lang.Double.doubleToLongBits(x);
        	bits ^= java.lang.Double.doubleToLongBits(y) * 31;
        	return (((int) bits) ^ ((int) (bits >> 32)));
            }
    }

}