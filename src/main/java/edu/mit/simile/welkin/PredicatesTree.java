package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.mit.simile.welkin.resource.PredicateUri;

public class PredicatesTree extends JPanel {

    private static final long serialVersionUID = -3281774983109916406L;

    final Font font = new Font("Verdana", Font.PLAIN, 11);
    final Font bold = new Font("Verdana", Font.BOLD, 10);

    public static final String EMPTY_LABEL = "No Predicates Loaded!";
    public static final String ROOT_LABEL = "Predicates";

    public static final Color BACKGROUND = Color.WHITE;
    public static final Color ACTIVE_FOREG = Color.BLACK;
    public static final Color PASSIVE_FOREG = Color.GRAY;
    public static final Color IDLE = Color.GRAY;

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 10;
    public static final int INIT_VALUE = 10;
    public static final float FACTOR = 10;

    static final String ICON_PATH = "resources/icons/";
    static final String OPEN_ICON = ICON_PATH + "openIcon.gif";
    static final String CLOSED_ICON = ICON_PATH + "closedIcon.gif";
    static final String LEAF_ICON = ICON_PATH + "leafIcon.gif";

    Welkin welkin;
    FullNode root;
    List<FullNode> elements;

    int maxWidth = 0;
    int vPos;
    int xPos;

    public PredicatesTree(Welkin welkin) {
        this.welkin = welkin;
        clear();
    }

    public void clear() {
        root = null;
        elements = new ArrayList<FullNode>();
        setEmptyTree();
    }

    public void setEmptyTree() {
        JLabel emptyLabel = new JLabel(EMPTY_LABEL);
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

    public void buildTree() {
        root = new FullNode(ROOT_LABEL, null, null, false);
        //root.incCount(welkin.wrapper.cache.predicates.size());
        elements.add(root);

        for(Iterator it = welkin.wrapper.cache.predicates.iterator(); it.hasNext();) {
            PredicateUri predicate = ((PredicateUri)it.next());
            String[] parts = Util.splitUri(predicate.getUri());
            createNode(root, parts, predicate, 0);
        }

        calculateValues(root, root.value);
        this.displayTree();
        this.repaint();

        welkin.scrollingPredTree.validate();
    }

    private void createNode(FullNode root, String[] parts, PredicateUri all, int level) {
        if(level == parts.length-1) {
            FullNode tmp = new FullNode(parts[2], all, root, true);
            if(level == 0) tmp.isVisible = true;
            else tmp.isVisible = false;
            root.children.add(tmp);
            elements.add(tmp);
            return;
        }

        boolean flag = false;
        for(int i = 0; i < root.children.size(); i++) {
            if(((FullNode)root.children.get(i)).label.getText().equals(parts[level])) {
                level++;
                createNode(((FullNode)root.children.get(i)), parts, all, level);
                ((FullNode)root.children.get(i)).incCount(all.getCount());
                flag = true;
            }
        }

        if(!flag) {
            FullNode child = new FullNode(parts[level++], all, root, false);
            if(level==2) child.isVisible = false;
            root.children.add(child);
            elements.add(child);
            createNode(child, parts, all, level);
        }
    }

    private void displayTree() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(BACKGROUND);

        root.value = root.slider.getValue()/FACTOR;

        xPos=5;
        vPos=5;
        printNodes(root);

        this.validate();
        this.repaint();

        welkin.scrollingPredTree.validate();
    }

    private void printNodes(FullNode node) {
        if(node.isAllowed) {
        boolean open = true;
        if(node.isVisible) {
	        	node.setLocation(xPos,vPos);
	        	maxWidth = maxWidth > (node.getDimension().width) ? maxWidth : (node.getDimension().width);
	        	this.add(node);
	        	vPos+=22;

	            if(node.children.size()>0) xPos+=15;
	            for(int i=0; i<node.children.size();i++) {
	            	if(!((FullNode) node.children.get(i)).isVisible) open = false;
	                printNodes((FullNode) node.children.get(i));
	            }
	            if(node.children.size()>0) xPos-=15;
	        }

	        if(node.children.size()==0) {
	            node.iconLabel.setIcon(new ImageIcon(Welkin.class.getResource(LEAF_ICON)));
	        } else if(open) {
	        	node.iconLabel.setIcon(new ImageIcon(Welkin.class.getResource(OPEN_ICON)));
	        } else {
	        	node.iconLabel.setIcon(new ImageIcon(Welkin.class.getResource(CLOSED_ICON)));
	        }

	        this.setPreferredSize(new Dimension(xPos+maxWidth, vPos+5));
        }
    }

    private void calculateValues(FullNode node, float ancestorValue) {
    	    if(node.predicate!=null)
    			node.adjustValue();
        for(int i=0; i<node.children.size();i++) {
            //((FullNode) node.children.get(i)).sum = ancestorValue;
            ((FullNode) node.children.get(i)).adjustValue(Math.min(ancestorValue,1));
            calculateValues((FullNode) node.children.get(i),((FullNode) node.children.get(i)).value);
            ((FullNode) node.children.get(i)).setFace();
        }
        PredicatesTree.this.welkin.notifyTreeChange();
    }

    /*private void visualizeAll() {
        for(Iterator it=elements.iterator();it.hasNext();) {
            ((FullNode)it.next()).isAllowed = true;
        }
    }*/

    private void devisualizeAll() {
        for(Iterator it=elements.iterator();it.hasNext();) {
            ((FullNode)it.next()).isAllowed = false;
        }
    }

    private void forwardPropagation(boolean selection, FullNode node) {
    	for(int i=0; i<node.children.size();i++) {
    		((FullNode)node.children.get(i)).check.setSelected(selection);
    		if(((FullNode)node.children.get(i)).isLeaf) {
    			((FullNode)node.children.get(i)).predicate.included = selection;
    		}

    		forwardPropagation(selection, (FullNode)node.children.get(i));
    	}
    }

    private void backwardPropagation(boolean selection, FullNode node) {

    	boolean somethingSelectedFlag = false;
    	if(node.father == null) return;
    	for(int i=0; i<node.father.children.size(); i++) {
    		if(((FullNode)node.father.children.get(i)).check.isSelected())
    			somethingSelectedFlag = true;
    	}

    	if(somethingSelectedFlag)
    		node.father.check.setSelected(true);
    	else node.father.check.setSelected(false);

    	backwardPropagation(selection, node.father);
    }

//    public boolean isSelected(String path) {
//    	for(int i=0; i<elements.size(); i++) {
//    		if(((FullNode)elements.get(i)).isLeaf)
//    			if(((FullNode)elements.get(i)).absolute.equals(path))
//    				if(((FullNode)elements.get(i)).check.isSelected())
//    					return true;
//    	}
//
//    	return false;
//    }

    public void crawlingTree(String prefix) {
        devisualizeAll();
        for(Iterator it=elements.iterator();it.hasNext();) {
            FullNode node = ((FullNode)it.next());
            if(node.absolute != null && node.absolute.getUri().startsWith(prefix))
                setAllowedBranch(node);
        }
        displayTree();
    }

    private void setAllowedBranch(FullNode node) {
        if(node == null) return;
        else {
            node.isAllowed = true;
            setAllowedBranch(node.father);
        }
    }

    class FullNode extends JPanel implements ChangeListener, ActionListener {

        private static final long serialVersionUID = 6323122109663260148L;
        
        private JLabel iconLabel;
        private JCheckBox check;
        private JLabel weight;
        private JSlider slider;
        private JLabel label;

        PredicateUri absolute;

        /**
         * Actual value of the node
         */
        private float value;

        /**
         * Sum of values of the ancestors
         */
        //private float sum;

        boolean isVisible;
        boolean isAllowed;
        boolean isLeaf;

        int count = 0;

        FullNode me;
        FullNode father;
        Vector<FullNode> children = new Vector<FullNode>();
        PredicateUri predicate;

        FullNode(String labelT, PredicateUri predicate, FullNode father, boolean isLeaf) {
        	this.predicate = predicate;
        	this.father = father;
        	this.isLeaf = isLeaf;
            me=this;

            this.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
            this.setBackground(BACKGROUND);

            iconLabel = new JLabel();
            iconLabel.setSize(20,18);

            check = new JCheckBox();
            check.setSelected(true);
            check.setBackground(Color.WHITE);
            check.addActionListener(this);

            weight = new JLabel();
            weight.setHorizontalAlignment(JTextField.RIGHT);
            weight.setBackground(BACKGROUND);
            weight.setFont(font);
            weight.setSize(30,16);
            weight.setBorder(null);
            if(predicate != null) incCount(predicate.getCount());

            slider = new JSlider(JSlider.HORIZONTAL,MIN_VALUE,MAX_VALUE,INIT_VALUE);
            slider.addChangeListener(this);
            slider.setBackground(BACKGROUND);
            slider.setSize(new Dimension(60,14));
            slider.setPreferredSize(new Dimension(60,14));
            slider.setMajorTickSpacing(1);
            slider.setSnapToTicks(true);
            slider.setPaintTicks(false);

            this.label = new JLabel();
            this.label.setFont(font);
            this.label.setText(labelT);
            this.label.setBackground(BACKGROUND);

            this.add(iconLabel);
            this.add(check);
            this.add(slider);
            this.add(this.label);
            this.add(weight);

            this.value = INIT_VALUE;

            this.isVisible = true;
            this.isAllowed = true;

            setFace();

            this.setSize(getDimension().width,18);

            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if(e.getPoint().x<=20) {
                        openCloseNodeChildren(me);
                        displayTree();
                    }
                }
            });
        }

        public void actionPerformed(ActionEvent evt) {
        	forwardPropagation(this.check.isSelected(), this);
        	backwardPropagation(this.check.isSelected(), this);
        	if(predicate!=null) predicate.included = this.check.isSelected();
        	welkin.notifyTreeChange();
        }

        public void incCount(int count) {
        	this.count += count;
        	weight.setText(" [" + this.count + "]");
        }

		public void adjustValue(float f) {
			value = f;
			slider.setValue((int)(f*10));
			if(predicate!=null) predicate.weight = f ;
		}

		public void adjustValue() {
			value = (float)slider.getValue()/10;
			if(predicate!=null) predicate.weight = value ;
		}

		private void openCloseNodeChildren (FullNode node) {
            if(node.children.size()>0) {
                if(((FullNode)node.children.get(0)).isVisible) {
                    closeChildren(node);
                } else {
                    openChildren(node);
                }
            }
        }

        private void openChildren(FullNode node) {
            for(int i=0;i<node.children.size();i++)
                ((FullNode)node.children.get(i)).isVisible = true;
        }

        private void closeChildren(FullNode node) {
            for(int i=0;i<node.children.size();i++)
                ((FullNode)node.children.get(i)).isVisible = false;
        }

        public void stateChanged(ChangeEvent e) {
            this.setFace();
            this.value = Math.min(slider.getValue()/FACTOR,1);
            calculateValues(this,value);
            this.repaint();
        }

        private void setFace() {
            float sliderValue = slider.getValue();
            if(predicate!=null)
            if (sliderValue==0) {
                label.setForeground(PASSIVE_FOREG);
            } else if (sliderValue==10) {
                label.setFont(bold);
            } else {
                label.setForeground(ACTIVE_FOREG);
                label.setFont(font);
            }
        }

        public Dimension getDimension() {
            return new Dimension (this.getLocation().x+
            		iconLabel.getWidth()+
            		check.getPreferredSize().width +
					slider.getPreferredSize().width
            		+label.getPreferredSize().width+weight.getPreferredSize().width,16);
        }
    }
}
