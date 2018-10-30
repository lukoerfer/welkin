package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ExceptionWin extends JDialog implements ActionListener{

	/**
     * 
     */
    private static final long serialVersionUID = 405343895227428900L;
    private JTextArea message;
	private JScrollPane spMessage;
	
	private JButton moreButton;
	
	private JTextArea stack;
	private JScrollPane spStack;
	
	private boolean more;
	
	public ExceptionWin(String title, String mess, String ss) {
		this.setResizable(false);
		this.setModal(true);
		this.setTitle("Welkin Error Message: " + title);
		this.getContentPane().setLayout(null);
		this.getContentPane().setBackground(new Color(236, 233, 216));
		
		message = new JTextArea();
		message.setText(mess);
		message.setEditable(false);
		message.setBorder(BorderFactory.createLineBorder(Color.black));
		spMessage = new JScrollPane(message);
		spMessage.setBounds(10,10,770,54);
		
		moreButton = new JButton();
		moreButton.setActionCommand("OC");
		moreButton.addActionListener(this);
		moreButton.setBounds(280,70,100,20);
		
		stack = new JTextArea();
		stack.setText(ss);
		stack.setEditable(false);
		stack.setBorder(BorderFactory.createLineBorder(Color.black));
		spStack = new JScrollPane(stack);
		spStack.setBounds(10,100,770,90);
		
		this.validate();
		this.repaint();
	}
	
	public void buildWindow(boolean more) {

		this.more = more;
		
		this.getContentPane().removeAll();
		this.getContentPane().add(spMessage);
		this.getContentPane().add(moreButton);
		
		if(more) {
			moreButton.setText("Less");
			this.getContentPane().add(spStack);
			this.setSize(800,240);
		} else {
			moreButton.setText("More");
			this.setSize(800,130);
		}
	}
	
	public void actionPerformed(ActionEvent evt) {
		if(evt.getActionCommand().equals("OC")) {
			buildWindow(!more);
			this.validate();
		}
	}
}
