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

import edu.mit.simile.welkin.resource.PredicateUri;

public class PredicateNode extends WNode {

    private static final long serialVersionUID = -158987654236638241L;

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 10;
    public static final int INIT_VALUE = 10;
    public static final float FACTOR = 10;

    final PredTree tree;
    final PredicateUri predicate;

    public PredicateNode(final PredTree tree, String labelT, PredicateUri predicate,
    		WNode father, boolean isLeaf) {
    	this.predicate = predicate;
    	this.father = father;
    	this.isLeaf = isLeaf;
    	this.tree = tree;
    	this.me=this;

        this.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        this.setBackground(GeneralTree.BACKGROUND);

        iconLabel = new JLabel();
        iconLabel.setSize(20,18);

        check = new JCheckBox();
        check.setSelected(true);
        check.setBackground(Color.WHITE);
        check.addActionListener(this);

        weight = new JLabel();
        weight.setHorizontalAlignment(JTextField.RIGHT);
        weight.setBackground(PredTree.BACKGROUND);
        weight.setFont(GeneralTree.font);
        weight.setSize(30,16);
        weight.setBorder(null);
        if(predicate != null) incCount(predicate.getCount());

        slider = new JSlider(JSlider.HORIZONTAL,MIN_VALUE,MAX_VALUE,INIT_VALUE);
        slider.addChangeListener(this);
        slider.setBackground(PredTree.BACKGROUND);
        slider.setSize(new Dimension(60,18));
        slider.setPreferredSize(new Dimension(60,18));
        slider.setMajorTickSpacing(1);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(false);

        this.label = new JLabel();
        this.label.setFont(PredTree.font);
        this.label.setText(labelT);
        this.label.setBackground(PredTree.BACKGROUND);

        this.add(iconLabel);
        this.add(check);
        this.add(slider);
        this.add(this.label);
        this.add(weight);

        adjustValue(INIT_VALUE);

        this.isVisible = true;
        this.isAllowed = true;

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

	public void actionPerformed(ActionEvent arg0) {
    	tree.forwardPropagation(this.check.isSelected(), this);
    	tree.backwardPropagation(this.check.isSelected(), this);
    	if(predicate!=null) predicate.included = this.check.isSelected();
    	tree.notification();
	}

    public void incCount(int count) {
    	this.count += count;
    	weight.setText(" [" + this.count + "]");
    }

	public void adjustValue(int value) {
		slider.removeChangeListener(this);
		slider.setValue(value);
		slider.addChangeListener(this);
		adjustNode(value);
	}

	public void adjustNode(float value) {
		if(predicate!=null) predicate.weight = value;
		setLook();
	}

    public void stateChanged(ChangeEvent e) {
		adjustNode(slider.getValue()/FACTOR) ;
		tree.calculateValues(this, slider.getValue());
		tree.notification();
    }

    float sliderValue;
    public void setLook() {
        sliderValue = slider.getValue();
        if(predicate!=null)
        if (sliderValue==MIN_VALUE) {
            label.setForeground(PredTree.PASSIVE_FOREG);
        } else if (sliderValue==MAX_VALUE) {
            label.setFont(PredTree.bold);
        } else {
            label.setForeground(PredTree.ACTIVE_FOREG);
            label.setFont(PredTree.font);
        }
    }
}
