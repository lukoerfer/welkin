package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class GeneralTree extends JPanel {

    public static final Font font = new Font("Verdana", Font.PLAIN, 11);
    public static final Font bold = new Font("Verdana", Font.BOLD, 10);

    public static final Color BACKGROUND = Color.WHITE;
    public static final Color ACTIVE_FOREG = Color.BLACK;
    public static final Color PASSIVE_FOREG = Color.GRAY;
    public static final Color IDLE = Color.GRAY;
    
    private static final String ICON_PATH = "resources/icons/";
    private static final String LEAF_ICON = ICON_PATH + "leafIcon.gif"; 
    private static final String OPEN_ICON = ICON_PATH + "openIcon.gif"; 
    private static final String CLOSED_ICON = ICON_PATH + "closedIcon.gif"; 
    
    private static final ImageIcon ICON_LEAF = new ImageIcon(Welkin.class.getResource(LEAF_ICON));
    private static final ImageIcon ICON_OPEN = new ImageIcon(Welkin.class.getResource(OPEN_ICON));
    private static final ImageIcon ICON_CLOSED = new ImageIcon(Welkin.class.getResource(CLOSED_ICON));   
    
    private int maxWidth; // For panel dimension
    private int vPos, xPos; // Position of elements
    
    protected WNode treeRoot;
    protected List<WNode> elements;
    
    Welkin welkin;
    
    public GeneralTree(Welkin welkin) {
        this.welkin = welkin;
        
        clear();
    }
    
    public void clear() {
    	treeRoot = null;
        elements = new ArrayList<WNode>();
        
    	setEmptyTree();
    }
    
    protected void setEmptyTree() {
        JLabel emptyLabel = new JLabel(getEmptyLabelText());
        emptyLabel.setBounds(2,2,200,16);
        emptyLabel.setFont(font);
        emptyLabel.setForeground(IDLE);
        
        this.removeAll();
        this.setLayout(null);
        this.setBackground(BACKGROUND);
        this.add(emptyLabel);
        this.setPreferredSize(emptyLabel.getPreferredSize());
        this.repaint();
    }
    
    public void drawTree() {
    	initTree();
    	printNodes(treeRoot);
    	
        this.validate();
        this.repaint();
        
        refresh();
    }
    
    private void initTree() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(BACKGROUND);

        xPos=5;
        vPos=5;
    }
    
    public void reinit() {
    	treeRoot.check.setSelected(true);
    	forwardPropagation(true, treeRoot);
    }
    
    private void printNodes(WNode node) {
        if(node.isAllowed) {
        	boolean openFlag = true;
	        if(node.isVisible) {
		        node.setLocation(xPos,vPos);
		        maxWidth = maxWidth > (node.getDimension().width+50) ? maxWidth : (node.getDimension().width+50);
		        this.add(node);
		        vPos+=22;
		        
		        if(node.children.size()>0) {
		        	xPos+=15;
			        for(int i=0; i<node.children.size();i++) {
			            if(!((WNode) node.children.get(i)).isVisible) openFlag = false;
			            printNodes((WNode) node.children.get(i));
			        }
			        xPos-=15;
		        }
		        
		        if(node.children.size()==0) {
		            node.iconLabel.setIcon(ICON_LEAF);
		        } else if(openFlag) {
		        	    node.iconLabel.setIcon(ICON_OPEN);
		        } else {
		        	    node.iconLabel.setIcon(ICON_CLOSED);
		        }
		        
		        this.setPreferredSize(new Dimension(xPos+maxWidth, vPos+5));
	        }
        }
    }
    
    protected void forwardPropagation(boolean selection, WNode node) {
    	for(int i=0; i<node.children.size();i++) {
    		WNode childNode = (WNode)node.children.get(i);
    		childNode.check.setSelected(selection);
    		if(childNode.isLeaf) treatLeaf(childNode, selection);

    		forwardPropagation(selection, childNode);
    	}
    }

    protected void backwardPropagation(boolean selection, WNode node) {
    	if(node.father == null) return;
    	boolean somethingSelectedFlag = false;
    	for(int i=0; i<node.father.children.size(); i++) {
    		if(((WNode)node.father.children.get(i)).check.isSelected())
    			somethingSelectedFlag = true;
    	}
    	if(somethingSelectedFlag) 
    		node.father.check.setSelected(true);
    	else node.father.check.setSelected(false);
    	
    	backwardPropagation(selection, node.father);
    }

    public abstract void refresh();
    public abstract void createTree();
    public abstract void treatLeaf(WNode fn, boolean selection);
    public abstract String getRootLabelText();
    public abstract String getEmptyLabelText();
}
