package edu.mit.simile.welkin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class IconsManagerPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 986669738190180364L;

    final Font edgeFont = new Font("Monospaced", Font.PLAIN, 12);
	
	ModelManager model;
	Welkin welkin;
	
	JButton defineButton;
	JButton clearButton;
	JTextField ruleField;
	JLabel iconLabel;
	
	ButtonGroup group;
	JCheckBox typeCheck;
	JCheckBox ruleCheck;
	
	JButton removeAllButton;
	JScrollPane scroll;
	
	JPanel tablePanel;
	ArrayList<TableElement> iconList;
	
	TablePanel tablePane;
	
    String iconsDirBase;
    
    int iconsCount = 0;
    
    TableElement bufferedElement;
	
    public IconsManagerPanel(ModelManager model, Welkin welkin) {

        this.model = model;
        this.welkin = welkin;
        this.setBackground(Color.WHITE);
        
        defineButton = new JButton("New");
        removeAllButton = new JButton("Remove All");
        removeAllButton.setActionCommand("RemoveAll");
        
        defineButton.addActionListener(this);
        removeAllButton.addActionListener(this); 
        
    	clearButton = new JButton("Clear Fields");
    	clearButton.setActionCommand("Clear");
    	clearButton.addActionListener(this);
        
        JPanel modifyButtons = new JPanel();
        modifyButtons.setLayout(new BoxLayout(modifyButtons, BoxLayout.X_AXIS));
        modifyButtons.add(Box.createHorizontalGlue());
        modifyButtons.add(clearButton);
        modifyButtons.add(removeAllButton);
        modifyButtons.add(Box.createHorizontalGlue());
        
        defineButton = new JButton("Add Element");
        defineButton.setActionCommand("Add");

    	ruleField = new JTextField();
    	iconLabel = new JLabel();
    	
    	iconLabel.addMouseListener(new MouseAdapter() {
    		public void mousePressed(MouseEvent e) {
	            try {
	                // Save File Manager
	                JFileChooser openWin;
	                if (iconsDirBase != null) openWin = new JFileChooser(iconsDirBase);
	                else openWin = new JFileChooser();

	                openWin.setFileFilter(new WFileFilter());

	                int returnVal = openWin.showOpenDialog(null);

	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(openWin.getSelectedFile().getAbsolutePath()));
	                	iconsDirBase = openWin.getSelectedFile().getParent();
	                	iconLabel.setIcon(icon);
	                	iconLabel.setText("");
	                }
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
			}
		});
    	
    	defineButton.addActionListener(this);
    	
    	typeCheck = new JCheckBox("type");
    	ruleCheck = new JCheckBox("regexp");
    	
    	group = new ButtonGroup();
    	group.add(typeCheck);
    	group.add(ruleCheck);
        
        JPanel newButtons = new JPanel();
        newButtons.setLayout(new BoxLayout(newButtons, BoxLayout.X_AXIS));
        newButtons.add(Box.createHorizontalGlue());
        newButtons.add(ruleCheck);
        newButtons.add(typeCheck);
        newButtons.add(defineButton);
        newButtons.add(Box.createHorizontalGlue());
        
        JPanel fieldButtons = new JPanel();
        fieldButtons.setLayout(new BoxLayout(fieldButtons, BoxLayout.X_AXIS));
        fieldButtons.add(iconLabel);
        fieldButtons.add(ruleField);
        
        JPanel allButtons = new JPanel();
        allButtons.setLayout(new BorderLayout());
        allButtons.add(newButtons, "North");
        allButtons.add(fieldButtons, "Center");
        allButtons.add(modifyButtons, "South");
        
        resetForm();
        
        tablePane = new TablePanel();
        scroll = new JScrollPane(tablePane);
        
        this.setLayout(new BorderLayout());
        this.add(scroll, BorderLayout.CENTER);
        this.add(allButtons, BorderLayout.SOUTH);
    }
    
    public void clear() {
		iconList.clear();
		tablePane.repaint();
		scroll.revalidate();
    }

    public void clearModel() {
    	welkin.iconsCheckbox.setSelected(false);
    	welkin.visualizer.drawicons = false;
    	model.clearIcons();
    	welkin.iconsCheckbox.setSelected(true);
    	welkin.visualizer.drawicons = true;
    	welkin.visualizer.repaint();
    }
    
	public void actionPerformed(ActionEvent a) {
		if (a.getActionCommand().equals("Clear")) {
			resetForm();
		} else if (a.getActionCommand().equals("Add")) {
			if( ruleField.getText().trim().length()>0) {
				if (bufferedElement != null) {
		            for (TableElement te : iconList) {
		            	if (te.id == bufferedElement.id) {
		            		te.icon = ((ImageIcon) iconLabel.getIcon()).getImage();
		            		te.rule = ruleField.getText().trim();
		            		te.type = typeCheck.isSelected();
		            		updateModel(bufferedElement.type, bufferedElement.rule);
		            		bufferedElement = null;
		            		break;
		            	}
		            }
				} else {
					iconList.add(new TableElement(iconsCount++, typeCheck.isSelected(), 
							((ImageIcon) iconLabel.getIcon()).getImage(), 
							ruleField.getText().trim()));
				}
				updateModel();
				resetForm();
				welkin.visualizer.repaint();
			}
		} else if(a.getActionCommand().equals("RemoveAll")) {
			iconList.clear();
			clearModel();
		}
		
		tablePane.repaint();
		scroll.revalidate();
	}
	
	public Image getIconById(int id) {
        for (TableElement te : iconList) {
        	if (te.id == id) {
                return te.icon;
            }
        }		
        return null;
	}
	
	private void updateModel() {
        for(TableElement te : iconList) {
        	model.updateIcons(te.id, te.type, te.rule);
        }
	}
	
	private void updateModel(boolean type, String rule) {
        model.updateIcons(type, rule);
	}
		
	public void resetForm() {
        ruleField.setText("");
    	iconLabel.setIcon(Welkin.createImageIcon(Welkin.HELP_ICON));
    	bufferedElement = null;
    	typeCheck.setSelected(true);
	}
	
	public Image getImage(int id) {
        for (TableElement te : iconList) {
        	if(te.id == id) {
        		return te.icon;
        	}
        }	
        return null;
	}
	
    class WFileFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals("gif") ||
                    extension.equals("jpeg") ||
                    extension.equals("jpg")) {
                        return true;
                }
        	}

            return false;
        }

        public String getDescription() {
            return "gif, jpeg or jpg icon file";
        }

        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }
    
    class TablePanel extends JPanel {
    	
        private static final long serialVersionUID = 8837906595322985835L;

        Image remove;
    	int maximumWidth = 140;
    	
    	public TablePanel () {
    		iconList = new ArrayList<TableElement>();
    		this.setBackground(Color.WHITE);
    		this.addMouseListener(new MyMouseListener());
    	    remove = Welkin.createImageIcon(Welkin.REMOVE_ICON).getImage();

            //Image img = Welkin.createImageIcon(Welkin.HELP_ICON).getImage();
            //iconList.add(new TableElement(iconsCount++, true, img, "type  Person"));
            //iconList.add(new TableElement(iconsCount++, true, img, "rule  http://pippo/"));
    	}
    	
    	public void addItem(TableElement element) {
    		iconList.add(element);
    	}
    	
        public void paintComponent(Graphics g) {
        	super.paintComponent(g);
        	
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(edgeFont);
            FontMetrics fm = g2.getFontMetrics(edgeFont);
            
            int dy = 0;
            
            for(TableElement te : iconList) {            	
            	maximumWidth = maximumWidth < (fm.stringWidth(te.rule) + te.icon.getWidth(this)+40)
            			? (fm.stringWidth(te.rule) + te.icon.getWidth(this)) : maximumWidth;
            	
            	te.x = 2 + 20;
            	te.y = dy + 2;
            	te.dx = 200;
            	te.dy = te.icon.getHeight(this)+4;
            	if(te.isSelected) {
            		g2.setColor(Color.LIGHT_GRAY);
            		g2.fillRect(20, dy + 2, maximumWidth+14, te.icon.getHeight(this)+4);
            		g2.setColor(Color.GRAY);
            		g2.drawRect(20, dy + 2, maximumWidth+14, te.icon.getHeight(this)+4);
            		if(te.type)g2.setColor(Color.BLACK);
            		else g2.setColor(Color.BLUE);
            		g2.drawString(te.rule, 30 + te.icon.getWidth(this), dy + 8 + te.icon.getHeight(this)/2);
            	} else {
            		g2.setColor(Color.GRAY);
            		g2.drawRect(20, dy + 2, maximumWidth+14, te.icon.getHeight(this)+4);
            		if(te.type)g2.setColor(Color.BLACK);
            		else g2.setColor(Color.BLUE);
            		g2.drawString(te.rule, 28 + te.icon.getWidth(this), dy + 8 + te.icon.getHeight(this)/2);
            	}
            	
            	g2.drawImage(remove, 2, dy + 5, this);
            	g2.drawImage(te.icon, 23, dy + 5, this);
            	dy += te.icon.getHeight(this)+ 4 + 4;
            	
            	this.setPreferredSize(
            			new Dimension(maximumWidth +40, 30));
            }
        }
        
        class MyMouseListener extends MouseAdapter {
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                for(TableElement te : iconList) {
                	if(x > 5 && x< 21 && y > te.y && y< (te.y+te.dy)) {
                		iconList.remove(te);
                		updateModel(te.type, te.rule);
                		break;
                	}
                	
					if (x > te.x && x< (te.x+te.dx) && y > te.y && y< (te.y+te.dy)) {
						te.isSelected = true;
					} else {
						te.isSelected = false;
					}
                }
              
        		tablePane.repaint();
        		scroll.revalidate();
        		welkin.visualizer.repaint();
            }
            
            public void mouseReleased(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (e.getClickCount() == 2) {
                    for(TableElement te : iconList) {
    					if (x > te.x && x< (te.x+te.dx) && y > te.y && y< (te.y+te.dy)) {
    						bufferedElement = new TableElement(te.id, te.type, te.icon, te.rule);
    						iconLabel.setIcon(new ImageIcon(te.icon));
    						ruleField.setText(te.rule);
    						if (te.type) {
                                typeCheck.setSelected(true);
                            } else {
                                ruleCheck.setSelected(true);
                            }
    						//iconList.remove(te);
    						break;
    					}
                    }
                } 
                
                repaint();
            }
        }
    }
    
    class TableElement {
    	public int id;
    	public int x, y, dx, dy;
    	public boolean type;
    	public Image icon;
    	public String rule;
    	public boolean isSelected;
    	
    	public TableElement(int id, boolean type, Image icon, String rule) {
    		this.id = id;
    		this.icon = icon;
    		this.rule = rule;
    		this.type = type;
    		isSelected = false;
    	}
    }
}
