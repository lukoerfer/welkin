package edu.mit.simile.welkin;

import java.util.Iterator;

import edu.mit.simile.welkin.resource.PartialUri;

public class ResTree extends GeneralTree {

    private static final long serialVersionUID = -1208417333265906095L;

    private static final String EMPTY_LABEL = "No Resources Loaded!";
    private static final String ROOT_LABEL = "Resources";
	
	public ResTree(Welkin welkin) {
		super(welkin);
	}
    
	public void refresh() {
		welkin.scrollingResTree.validate();
	}

	public void createTree() {
		buildTree();
		drawTree();
	}
	
    private void buildTree() {
        treeRoot = new ResourceNode(this, ROOT_LABEL, null, null, false);
        elements.add(treeRoot);
        
        PartialUri tmpPred;
        for(Iterator it = welkin.wrapper.cache.resourcesBases.iterator(); it.hasNext();) {
        	tmpPred = ((PartialUri)it.next());
            createNode((ResourceNode)treeRoot, 
            		Util.splitUriBases(tmpPred.getBase()), tmpPred, 0);
        }
    }
    
    private ResourceNode childNode;
    private void createNode(ResourceNode root, String[] parts, PartialUri all, int level) {
    	
    	if(parts == null || parts[0]==null) return; // TODO Blank Nodes
    	
    	if(level == parts.length-1) {
    	    if(parts[parts.length-1]==null) return;
    		childNode = new ResourceNode(this, parts[parts.length-1], all, root, true);
        	if(level == 0) childNode.isVisible = true;
        	else childNode.isVisible = false;
        	root.children.add(childNode);
    		return;
    	}
    	boolean flag = false;
    	for(int i = 0; i < root.children.size(); i++) {
    		if(((ResourceNode)root.children.get(i)).label.getText().equals(parts[level])) {
     			level++;
    			createNode(((ResourceNode)root.children.get(i)), parts, all, level);
    			((ResourceNode)root.children.get(i)).incCount(all.getCount());
    			flag = true;
    		}
    	}
    	
    	if(!flag) {
    	    if(parts[level]==null) return;
    		childNode = new ResourceNode(this, parts[level++], all, root, false);
    		if(level == 2) childNode.isVisible = false;
    		root.children.add(childNode); 
    		createNode(childNode, parts, all, level);
    	}
    }
    
	public void notification() {
		welkin.notifyBaseUriColorChange();
	}

	public void treatLeaf(WNode fn, boolean selection) {
		welkin.wrapper.cache.setVisible(((ResourceNode)fn).resource, selection);
	}
    
    private ResourceNode tmpNode;
    public void calculateValues(ResourceNode node, int ancestorValue) {
	    for(int i=0; i<node.children.size();i++) {
	    	tmpNode = ((ResourceNode) node.children.get(i));
	    	tmpNode.adjustValue(ancestorValue);
	        calculateValues(tmpNode,ancestorValue);
	    }
	}

    public String getRootLabelText() { return ROOT_LABEL; }
    public String getEmptyLabelText() { return EMPTY_LABEL; }
}
