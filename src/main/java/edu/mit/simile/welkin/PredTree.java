package edu.mit.simile.welkin;

import java.util.Iterator;

import edu.mit.simile.welkin.resource.PredicateUri;

public class PredTree extends GeneralTree {
	
    private static final long serialVersionUID = -377138988135382200L;

    private static final String EMPTY_LABEL = "No Predicates Loaded!";
    private static final String ROOT_LABEL = "Predicates";
	
	public PredTree(Welkin welkin) {
		super(welkin);
	}
	
	public void createTree() {
		buildTree();
		drawTree();
	}
	
    private void buildTree() {
        treeRoot = new PredicateNode(this, ROOT_LABEL, null, null, false);
        elements.add(treeRoot);
        
        PredicateUri tmpPred;
        for(Iterator it = welkin.wrapper.cache.predicates.iterator(); it.hasNext();) {
        	tmpPred = ((PredicateUri)it.next());
            createNode((PredicateNode)treeRoot, 
            		Util.splitUri(tmpPred.getUri()), tmpPred, 0);
        }
    }
    
    private PredicateNode childNode;
    private void createNode(PredicateNode root, String[] parts, PredicateUri all, int level) {
        if(level == parts.length-1) {
        	childNode = new PredicateNode(this, parts[2], all, root, true);
            childNode.isVisible = (level == 0);
            root.children.add(childNode);
            elements.add(childNode);
            return;
        }

        boolean flag = false;
        for(int i = 0; i < root.children.size(); i++) {
            if(((PredicateNode)root.children.get(i)).label.getText().equals(parts[level])) {
                level++;
                createNode(((PredicateNode)root.children.get(i)), parts, all, level);
                ((PredicateNode)root.children.get(i)).incCount(all.getCount());
                flag = true;
            }
        }
        
        if(!flag) {
        	childNode = new PredicateNode(this, parts[level++], all, root, false);
            if(level==2) childNode.isVisible = false;
            root.children.add(childNode);
            elements.add(childNode);
            createNode(childNode, parts, all, level);
        }
    }
    
    private PredicateNode tmpNode;
    public void calculateValues(PredicateNode node, int ancestorValue) {
	    for(int i=0; i<node.children.size();i++) {
	    	tmpNode = ((PredicateNode) node.children.get(i));
	    	tmpNode.adjustValue(ancestorValue);
	        calculateValues(tmpNode,ancestorValue);
	    }
	}

	public void refresh() {
		welkin.scrollingPredTree.validate();		
	}
	
	public void notification() {
		welkin.notifyTreeChange();
	}

	public void treatLeaf(WNode fn, boolean selection) {
		((PredicateNode)fn).predicate.included = selection;
	}
	
    public String getRootLabelText() { return ROOT_LABEL; }
    public String getEmptyLabelText() { return EMPTY_LABEL; }
}
