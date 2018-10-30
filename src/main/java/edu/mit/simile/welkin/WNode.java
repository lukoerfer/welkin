package edu.mit.simile.welkin;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

public abstract class WNode extends JPanel 
	implements ChangeListener, ActionListener {

	private static final boolean OPEN = true;
	private static final boolean CLOSE = false;
	
	protected JLabel iconLabel, label, weight;
	protected JCheckBox check;
	protected JSlider slider;
	
	protected int count;
	
    protected boolean isVisible, isAllowed, isLeaf;
    
    protected WNode father, me;
    protected Vector<WNode> children;
    
    public WNode() {
    	count = 0;
    	children = new Vector<WNode>();
    }
    
	protected final void openCloseNodeChildren (WNode node) {
        if(node.children.size()>0) {
            if(((WNode)node.children.get(0)).isVisible) {
            	processChildren(node, WNode.CLOSE);
            } else {
            	processChildren(node, WNode.OPEN);
            }
        }
    }
    
    private final void processChildren(WNode node, boolean command) {
        for(int i=0;i<node.children.size();i++) 
            ((WNode)node.children.get(i)).isVisible = command;
    }
    
    public final Dimension getDimension() {
        return new Dimension (
        		this.getLocation().x +
				iconLabel.getWidth() +
				check.getPreferredSize().width +
				slider.getPreferredSize().width +
        		label.getPreferredSize().width +
				weight.getPreferredSize().width,16);
    }
}
