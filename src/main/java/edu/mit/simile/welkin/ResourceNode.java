package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;

import edu.mit.simile.welkin.resource.PartialUri;

public class ResourceNode extends WNode {

    private static final long serialVersionUID = -5827294881835432391L;

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 360;
    public static final int INIT_VALUE = 0;

    final ResTree tree;
    final PartialUri resource;

	public ResourceNode(final ResTree tree, String labelT, PartialUri resource,
			WNode father, boolean isLeaf) {
    	this.resource = resource;
    	this.father = father;
    	this.isLeaf = isLeaf;
    	this.tree = tree;
        me=this;

        this.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        this.setBackground(GeneralTree.BACKGROUND);

        iconLabel = new JLabel();
        iconLabel.setSize(20,18);

        check = new JCheckBox();
        check.setSelected(true);
        check.setBackground(Color.WHITE);
        check.addActionListener(this);

        slider = new JSlider(JSlider.HORIZONTAL,MIN_VALUE,MAX_VALUE,INIT_VALUE);
        slider.addChangeListener(this);
        slider.setBackground(GeneralTree.BACKGROUND);
        slider.setSize(new Dimension(60,18));
        slider.setPreferredSize(new Dimension(60,18));
        slider.setMajorTickSpacing(1);
        slider.setSnapToTicks(false);
        slider.setPaintTicks(false);

        this.label = new JLabel();
        this.label.setFont(GeneralTree.font);
        this.label.setText(labelT);
        this.label.setBackground(GeneralTree.BACKGROUND);

        weight = new JLabel();
        weight.setHorizontalAlignment(JTextField.RIGHT);
        weight.setBackground(GeneralTree.BACKGROUND);
        weight.setFont(GeneralTree.font);
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

        this.setSize(getDimension().width,20);

        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(e.getPoint().x<=20) {
                    openCloseNodeChildren(me);
                    tree.drawTree();
                }
            }
        });
    }

	public void stateChanged(ChangeEvent arg0) {
        adjustValue();
        tree.calculateValues(this,slider.getValue());
        tree.notification();
	}

	public void actionPerformed(ActionEvent arg0) {
    	tree.forwardPropagation(this.check.isSelected(), this);
    	tree.backwardPropagation(this.check.isSelected(), this);
    	if(isLeaf) tree.treatLeaf(this, this.check.isSelected());
    	tree.notification();
	}

    public void incCount(int count) {
    	this.count += count;
    	weight.setText(" [" + this.count + "]");
    }

	public void adjustValue() {
	    Color color = Color.getHSBColor(slider.getValue()/(float)MAX_VALUE,0.8f,1.0f);
		if (resource != null) resource.color = color;
		label.setForeground(color);
	}

	public void adjustValue(int value) {
		slider.setValue(value);
		adjustValue();
	}
}
