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

import edu.mit.simile.welkin.resource.PartialUri;

public class ResourcesTree extends JPanel {

    private static final long serialVersionUID = 5323388104033551247L;

    final Font font = new Font("Verdana", Font.PLAIN, 11);
    final Font bold = new Font("Verdana", Font.BOLD, 10);

    public static final Color DEFAULT_URI_COLOR = Color.red;

    public static final String EMPTY_LABEL = "No Resources Loaded!";
    public static final String ROOT_LABEL = "Resources";

    public static final Color BACKGROUND = Color.WHITE;
    public static final Color IDLE = Color.GRAY;

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 360;
    public static final int INIT_VALUE = 0;

    static final String ICON_PATH = "resources/icons/";
    static final String OPEN_ICON = ICON_PATH + "openIcon.gif";
    static final String CLOSED_ICON = ICON_PATH + "closedIcon.gif";
    static final String LEAF_ICON = ICON_PATH + "leafIcon.gif";

    private int maxWidth = 200;

    Welkin welkin;
    FullNode root;
    List<FullNode> elements;

    int vPos;
    int xPos;

    public ResourcesTree(Welkin welkin) {
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
        root.incCount(welkin.wrapper.cache.resources.size());
        elements.add(root);

        for(Iterator it = welkin.wrapper.cache.resourcesBases.iterator(); it.hasNext();) {
        	PartialUri predicate = ((PartialUri)it.next());
        	createNode(root, Util.splitUriBases(predicate.getBase()), predicate, 0);
        }

        calculateValues(root, root.slider.getValue());
        this.displayTree();
    	this.repaint();

    	welkin.scrollingResTree.validate();
    }

    private void createNode(FullNode root, String[] parts, PartialUri all, int level) {

    	if(parts[0]==null) return; // TODO Blank Nodes

    	if(level == parts.length-1) {
        	FullNode tmp = new FullNode(parts[parts.length-1], all, root, true);
        	if(level == 0) tmp.isVisible = true;
        	else tmp.isVisible = false;
        	root.children.add(tmp);
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
    		if(level == 2) child.isVisible = false;
    		root.children.add(child);
    		createNode(child, parts, all, level);
    	}
    }

    private void displayTree() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(BACKGROUND);

        root.slider.getValue();

        xPos=5;
        vPos=5;
        printNodes(root);

        this.validate();
        this.repaint();

        welkin.scrollingResTree.validate();
    }

    private void printNodes(FullNode node) {
        if(node.isAllowed) {
        	boolean open = true;
	        if(node.isVisible) {
		        node.setLocation(xPos,vPos);
		        maxWidth = maxWidth > (node.getDimension().width+50) ? maxWidth : (node.getDimension().width+50);
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
        for(int i=0; i<node.children.size();i++) {
            ((FullNode) node.children.get(i)).adjustValue(ancestorValue);
            calculateValues((FullNode) node.children.get(i),ancestorValue);
        }

        welkin.notifyBaseUriColorChange();
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
    			welkin.wrapper.cache.setVisible(((FullNode)node.children.get(i)).resource, ((FullNode)node.children.get(i)).check.isSelected());
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

    public void crawlingTree(String prefix) {
        devisualizeAll();
        for(Iterator it=elements.iterator();it.hasNext();) {
            FullNode node = ((FullNode)it.next());
            if(node.resource != null && node.resource.getBase().startsWith(prefix))
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

        private static final long serialVersionUID = 1377946240285500759L;
        
        private JLabel iconLabel;
        private JCheckBox check;
        private JSlider slider;
        private JLabel label;
        private JLabel weight;

        boolean isVisible;
        boolean isAllowed;
        boolean isLeaf;

        int count = 0;

        FullNode me;
        FullNode father;
        Vector<FullNode> children = new Vector<FullNode>();
        PartialUri resource;

        FullNode(String labelT, PartialUri resource, FullNode father, boolean isLeaf) {
        	this.resource = resource;
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

            slider = new JSlider(JSlider.HORIZONTAL,MIN_VALUE,MAX_VALUE,INIT_VALUE);
            slider.addChangeListener(this);
            slider.setBackground(BACKGROUND);
            slider.setSize(new Dimension(60,14));
            slider.setPreferredSize(new Dimension(60,14));
            slider.setMajorTickSpacing(1);
            slider.setSnapToTicks(false);
            slider.setPaintTicks(false);

            this.label = new JLabel();
            this.label.setFont(font);
            this.label.setText(labelT);
            this.label.setBackground(BACKGROUND);

            weight = new JLabel();
            weight.setHorizontalAlignment(JTextField.RIGHT);
            weight.setBackground(BACKGROUND);
            weight.setFont(font);
            weight.setSize(30,16);
            weight.setBorder(null);
            if(resource != null) incCount(resource.getCount());

            this.add(iconLabel);
            this.add(check);
            this.add(slider);
            this.add(this.label);
            this.add(weight);

            this.isVisible = true;
            this.isAllowed = true;

            adjustValue(INIT_VALUE);

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
        	if(isLeaf) welkin.wrapper.cache.setVisible(resource, this.check.isSelected());
        	if(!welkin.running) welkin.visualizer.repaint();
        }

        public void incCount(int count) {
            this.count += count;
            weight.setText(" [" + this.count + "]");
        }

		public void adjustValue(float f) {
			slider.setValue((int)(f));
			adjustValue();
		}

		public void adjustValue() {
		    Color color = Color.getHSBColor(slider.getValue()/(float)MAX_VALUE,0.8f,1.0f);
			if (resource != null) resource.color = color;
			label.setForeground(color);
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
            adjustValue();
            calculateValues(this,slider.getValue());
            this.repaint();
        }

        public Dimension getDimension() {
            return new Dimension (
            		this.getLocation().x +
					iconLabel.getWidth() +
					check.getPreferredSize().width +
					slider.getPreferredSize().width +
            		label.getPreferredSize().width +
					weight.getPreferredSize().width,16);
        }
    }
}
