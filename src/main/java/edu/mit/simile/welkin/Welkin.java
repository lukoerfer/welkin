package edu.mit.simile.welkin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;


public class Welkin extends JApplet implements ActionListener, ItemListener {

    private static final long serialVersionUID = 1994781549666813074L;

    public static final String NAME = "Welkin";
    public static Properties props = new Properties();
    static {
    	try {
    		java.net.URL url = ClassLoader.getSystemResource("edu/mit/simile/welkin/Welkin.properties");
    		props.load(url.openStream());
    	} catch (Exception e) {
    		props.setProperty("welkin.version", "");
    		props.setProperty("welkin.year", "");
    	}
    }
    public static final String VERSION = props.getProperty("welkin.version");
    public static final String YEAR = props.getProperty("welkin.year");
    
    static final int X_WIN_DIM = 860;
    static final int Y_WIN_DIM = 700;

    static final Font titleFont = new Font("Verdana", Font.PLAIN, 9);

    static final String ICON_PATH = "resources/icons/";
    static final String LOGO_SMALL_ICON = ICON_PATH + "logo-small.gif";
    static final String LOGO_ICON = ICON_PATH + "sombrero.png";
    static final String START_ICON = ICON_PATH + "start.gif";
    static final String STOP_ICON = ICON_PATH + "stop.gif";
    static final String UNSELECTED_ICON = ICON_PATH + "file.gif";
    static final String SELECTED_ICON = ICON_PATH + "selected.gif";
    static final String REMOVE_ICON = ICON_PATH + "remove16.gif";
    static final String HELP_ICON = ICON_PATH + "help16.gif";

    static JFrame frame;

    JColorChooser jc;
    JDialog chooser;

    PredTree predTree;
    ResTree resTree;

    ModelVisualizer visualizer;
    ModelManager wrapper;
    ModelChart inDegreeChart;
    ModelChart outDegreeChart;
    ModelChart clustCoeffChart;
    IconsManagerPanel iconsManager;

    boolean running = false;

    JLabel delayLabel = new JLabel("Delay(ms)");
    JLabel massLabel = new JLabel("Mass");
    JLabel dragLabel = new JLabel("Drag");
    JLabel attractionLabel = new JLabel("Attraction");
    JLabel repulsionLabel = new JLabel("Repulsion");

    JButton dataLoadButton;
    JButton dataClearButton;
    JButton filterResetButton;
    JButton iconsClearButton;
    JButton iconsLoadButton;
    JButton aboutButton;

    JButton controlButton;
    JButton circleButton;
    JButton scrambleButton;
    JButton shakeButton;
    JButton highlightButton;
    JButton clearButton;

    JCheckBox antialiasCheckbox;
    JCheckBox nodesCheckbox;
    JCheckBox edgesCheckbox;
    JCheckBox arrowCheckbox;
    JCheckBox iconsCheckbox;
    JCheckBox edgeValuesCheckbox;
    JCheckBox groupsCheckbox;
    JCheckBox timeCheckbox;
    JCheckBox backgroundCheckbox;
    JCheckBox highlightOnLabelCheckbox;

    JRadioButton inOutColorsRadio;
    JRadioButton namespaceColorsRadio;
    JTextField colorsFilteringField;
    JButton resetColorsButton;
    JButton pickColorButton;

    JTextField delayField;
    JTextField massField;
    JTextField dragField;
    JTextField attractionField;
    JTextField repulsionField;
    JTextField highlightField;

    JScrollPane scrollingResTree;
    JScrollPane scrollingPredTree;

    ImageIcon startIcon;
    ImageIcon stopIcon;
    ImageIcon logoIcon;

    JDialog about;

    String dirBase;

    class About extends JComponent {

        private static final long serialVersionUID = -7950957335480657618L;

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.drawImage(createImageIcon(LOGO_ICON).getImage(),0,0,null);
            g2.setFont(new Font("Verdana", Font.BOLD, 30));
            g2.setColor(new Color(206,50,8));
            g2.drawString(NAME,13,261);
            g2.setFont(new Font("Verdana", Font.PLAIN, 12));
            g2.setColor(new Color(128,128,128));
            g2.drawString("A Graphical RDF Browser",18,285);
            g2.setFont(new Font("Verdana", Font.PLAIN, 12));
            g2.setColor(new Color(192,192,192));
            g2.drawString("Version " + VERSION,18,301);
            g2.setFont(new Font("Verdana", Font.PLAIN, 10));
            g2.setColor(new Color(128,128,128));
            g2.drawString("http://simile.mit.edu/welkin/",18,317);
            g2.setColor(new Color(128,128,128));
            g2.drawString("Copyright (c) Massachusetts Institute of Technology",18,365);
        }

        public Dimension getPreferredSize() {
            return new Dimension(300, 370);
        }
    }

    class AboutMouseAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            about.setVisible(false);
        }
    }

    void about() {
        if (about == null) {
            about = new JDialog(frame, "About " + NAME + "...", false);
            about.setSize(300,400);
            MouseAdapter ma = new AboutMouseAdapter();
            about.addMouseListener(ma);
            JButton okButton = new JButton("Ok");
            JPanel bottom = new JPanel();
            okButton.addMouseListener(ma);
            bottom.setLayout(new BorderLayout());
            bottom.add(okButton,BorderLayout.EAST);
            bottom.setBorder(BorderFactory.createEmptyBorder(3,25,3,25));
            About aboutPane = new About();
            Container panel = about.getContentPane();
            panel.setLayout(new BorderLayout());
            panel.setBackground(Color.white);
            panel.add(aboutPane, BorderLayout.NORTH);
            panel.add(bottom, BorderLayout.SOUTH);
        }

        // NOTE: these lines center the about dialog in the
        // current window. Some older Swing versions have
        // a bug in getLocationOnScreen() and they may not
        // make this behave properly.
        if (frame != null) {
            Point p = frame.getLocationOnScreen();
            Dimension d1 = frame.getSize();
            about.pack();
            Dimension d2 = about.getSize();
            about.setLocation(
                p.x + (d1.width - d2.width) / 2,
                p.y + (d1.height - d2.height) / 2
            );
            about.setVisible(true);
        }
    }

    // called by the applet sandbox
    public void init() {
        
        System.out.println("Starting " + NAME + " " + VERSION);

        initPanel(true);
        
    	try {
    		URL dataURL = null;
            String url = getParameter("url");
            String data = getParameter("data");
    		if (url != null || data != null) {
	    		if (url != null) {
					dataURL = new URL(url);
	    		} else if (data != null) {
	    		    String base = getDocumentBase().toString();
	    		    dataURL = new URL(base.substring(0,base.lastIndexOf('/') + 1) + data);
	    		}

                load(dataURL);
    		}
		} catch (Exception e) {
			System.out.println(e + " " + e.getMessage());
		}
    }

    public void initPanel(boolean applet){

        wrapper = new ModelManager();
        visualizer = new ModelVisualizer(wrapper, this);

        inDegreeChart = new InDegreeChart(wrapper);
        inDegreeChart.addActionListener(this);
        outDegreeChart = new OutDegreeChart(wrapper);
        outDegreeChart.addActionListener(this);
        clustCoeffChart = new ClusteringCoefficientChart(wrapper);
        clustCoeffChart.addActionListener(this);

        iconsManager = new IconsManagerPanel(wrapper, this);
        
        visualizer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        predTree = new PredTree(this);
        scrollingPredTree = new JScrollPane(predTree);

        resTree = new ResTree(this);
        scrollingResTree = new JScrollPane(resTree);

        JPanel dataControls = new JPanel();
        dataControls.setLayout(new BorderLayout());
        
        JPanel dataPane = new JPanel();
        dataPane.setLayout(new BorderLayout());
        
        if (!applet) {
        	filterResetButton = new JButton("Reset Filtering");
            dataClearButton = new JButton("Clear");
            dataLoadButton = new JButton("Load");

            filterResetButton.addActionListener(this);
            dataClearButton.addActionListener(this);
            dataLoadButton.addActionListener(this);
            
            aboutButton = new JButton("About");
            aboutButton.addActionListener(this);
            
            JPanel dataButtons = new JPanel();
            dataButtons.setLayout(new BoxLayout(dataButtons, BoxLayout.X_AXIS));
            dataButtons.add(Box.createHorizontalGlue());
            
            dataButtons.add(dataLoadButton);
            dataButtons.add(dataClearButton);
            dataButtons.add(Box.createRigidArea(new Dimension(10,0)));
            dataButtons.add(filterResetButton);
            
            dataButtons.add(Box.createHorizontalGlue());
            
            dataControls.add(dataButtons, BorderLayout.SOUTH);
            
            dataPane.add(dataButtons, BorderLayout.WEST);
            dataPane.add(aboutButton, BorderLayout.EAST);
        } 

        startIcon = createImageIcon(START_ICON);
        stopIcon = createImageIcon(STOP_ICON);

        if ((startIcon != null) && (stopIcon != null)) {
            controlButton = new JButton("Start", startIcon);
            controlButton.setVerticalTextPosition(AbstractButton.CENTER);
            controlButton.setHorizontalTextPosition(AbstractButton.TRAILING);
        } else {
            controlButton = new JButton("Start");
        }

        circleButton = new JButton("Circle");
        scrambleButton = new JButton("Scramble");
        shakeButton = new JButton("Shake");
        highlightButton = new JButton("Highlight");
        clearButton = new JButton("Clear");

        antialiasCheckbox = new JCheckBox("Antialias",visualizer.antialias);
        nodesCheckbox = new JCheckBox("Nodes",visualizer.drawnodes);
        edgesCheckbox = new JCheckBox("Edges",visualizer.drawedges);
        arrowCheckbox = new JCheckBox("Arrows",visualizer.drawarrows);
        iconsCheckbox = new JCheckBox("Icons",visualizer.drawicons);
        edgeValuesCheckbox = new JCheckBox("Edge Values",visualizer.drawedgevalues);
        timeCheckbox = new JCheckBox("Timing",visualizer.timing);
        backgroundCheckbox = new JCheckBox("Background",visualizer.background);
        highlightOnLabelCheckbox = new JCheckBox("Label",visualizer.highlightOnLabel);

        inOutColorsRadio = new JRadioButton("Internal/External",true);
        namespaceColorsRadio = new JRadioButton("Uri Bases");

        ButtonGroup group = new ButtonGroup();
        group.add(inOutColorsRadio);
        group.add(namespaceColorsRadio);

        colorsFilteringField = new JTextField("",4);
        resetColorsButton = new JButton("Reset");
        pickColorButton = new JButton("Pick Color");

        delayField = new JTextField(Integer.toString(visualizer.delay),4);
        massField = new JTextField(Float.toString(visualizer.mass),4);
        dragField = new JTextField(Float.toString(visualizer.drag),4);
        attractionField = new JTextField(Float.toString(visualizer.attraction),4);
        repulsionField = new JTextField(Float.toString(visualizer.repulsion),4);
        highlightField = new JTextField("",15);

        delayField.addActionListener(this);
        massField.addActionListener(this);
        dragField.addActionListener(this);
        attractionField.addActionListener(this);
        repulsionField.addActionListener(this);
        highlightField.addActionListener(this);
        controlButton.addActionListener(this);
        circleButton.addActionListener(this);
        scrambleButton.addActionListener(this);
        shakeButton.addActionListener(this);
        highlightButton.addActionListener(this);
        clearButton.addActionListener(this);

        antialiasCheckbox.addItemListener(this);
        nodesCheckbox.addItemListener(this);
        edgesCheckbox.addItemListener(this);
        arrowCheckbox.addItemListener(this);
        iconsCheckbox.addItemListener(this);
        edgeValuesCheckbox.addItemListener(this);
        timeCheckbox.addItemListener(this);
        backgroundCheckbox.addItemListener(this);
        highlightOnLabelCheckbox.addItemListener(this);

        inOutColorsRadio.addItemListener(this);
        resetColorsButton.addActionListener(this);
        pickColorButton.addActionListener(this);

        JPanel highlight = new JPanel();
        highlight.setLayout(new BoxLayout(highlight, BoxLayout.X_AXIS));
        highlight.add(Box.createHorizontalGlue());
        highlight.add(highlightField);
        highlight.add(Box.createRigidArea(new Dimension(5,0)));
        highlight.add(highlightOnLabelCheckbox);
        highlight.add(Box.createRigidArea(new Dimension(5,0)));
        highlight.add(highlightButton);
        highlight.add(Box.createRigidArea(new Dimension(5,0)));
        highlight.add(clearButton);
        highlight.add(Box.createHorizontalGlue());
        highlight.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

        JPanel parameters = new JPanel();
		parameters.setLayout(new BoxLayout(parameters, BoxLayout.X_AXIS));
		parameters.add(Box.createHorizontalGlue());
		parameters.add(delayLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(delayField);
		parameters.add(Box.createRigidArea(new Dimension(30,0)));
		parameters.add(massLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(massField);
		parameters.add(Box.createRigidArea(new Dimension(30,0)));
		parameters.add(dragLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(dragField);
		parameters.add(Box.createRigidArea(new Dimension(30,0)));
		parameters.add(attractionLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(attractionField);
		parameters.add(Box.createRigidArea(new Dimension(30,0)));
		parameters.add(repulsionLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(repulsionField);
		parameters.add(Box.createHorizontalGlue());
		parameters.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

        JPanel drawing = new JPanel();
        drawing.setLayout(new BoxLayout(drawing, BoxLayout.X_AXIS));
        drawing.add(controlButton);
        drawing.add(Box.createHorizontalGlue());
        drawing.add(circleButton);
        drawing.add(scrambleButton);
        drawing.add(shakeButton);
        drawing.add(Box.createHorizontalGlue());
        drawing.add(nodesCheckbox);
        drawing.add(edgesCheckbox);
        drawing.add(arrowCheckbox);
        drawing.add(iconsCheckbox);
        //drawing.add(edgeValuesCheckbox);
        drawing.add(Box.createHorizontalGlue());
        //drawing.add(timeCheckbox);
        drawing.add(antialiasCheckbox);
        drawing.add(backgroundCheckbox);
        drawing.add(Box.createHorizontalGlue());
        drawing.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

		JTabbedPane toolsPane = new JTabbedPane(JTabbedPane.TOP);
		toolsPane.addTab("Drawing", drawing);
		toolsPane.addTab("Highlight",highlight);
		toolsPane.addTab("Parameters",parameters);

        JPanel inDegreePane = new JPanel();
        inDegreePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "In Degree", TitledBorder.TOP, TitledBorder.LEFT, titleFont));
        inDegreePane.setLayout(new BorderLayout());
        inDegreePane.add(inDegreeChart, BorderLayout.CENTER);

        JPanel outDegreePane = new JPanel();
        outDegreePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Out Degree", TitledBorder.TOP, TitledBorder.LEFT, titleFont));
        outDegreePane.setLayout(new BorderLayout());
        outDegreePane.add(outDegreeChart, BorderLayout.CENTER);

        JPanel clustCoeffPane = new JPanel();
        clustCoeffPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Clustering Coefficient", TitledBorder.TOP, TitledBorder.LEFT, titleFont));
        clustCoeffPane.setLayout(new BorderLayout());
        clustCoeffPane.add(clustCoeffChart, BorderLayout.CENTER);

        JPanel charts = new JPanel();
        charts.setLayout(new BoxLayout(charts, BoxLayout.X_AXIS));
        charts.add(inDegreePane);
        charts.add(outDegreePane);
        charts.add(clustCoeffPane);
        charts.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane chartPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,visualizer,charts);
        chartPane.setOneTouchExpandable(true);
        chartPane.setResizeWeight(0.8);
        if (applet) {
            chartPane.setDividerLocation(10000);
        } else {
            chartPane.setDividerLocation(0.8);
        }
        chartPane.setBorder(BorderFactory.createEmptyBorder());
        
        JSplitPane treePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollingPredTree,scrollingResTree);
        treePane.setOneTouchExpandable(false);
        treePane.setResizeWeight(0.50);
        treePane.setBorder(BorderFactory.createEmptyBorder());
        
		JTabbedPane infoPane = new JTabbedPane(JTabbedPane.BOTTOM);
		infoPane.addTab("Trees", treePane);
		infoPane.addTab("Icons", iconsManager);

        JSplitPane bodyPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,infoPane,chartPane);
        bodyPane.setOneTouchExpandable(true);
        bodyPane.setResizeWeight(0.2);
        if (applet) {
            bodyPane.setDividerLocation(0.0);
        } else {
            bodyPane.setDividerLocation(0.2);
        }
        bodyPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

        this.getContentPane().setLayout(new BorderLayout());
        if (!applet) this.getContentPane().add(dataPane, BorderLayout.NORTH);
        this.getContentPane().add(bodyPane, BorderLayout.CENTER);
        this.getContentPane().add(toolsPane, BorderLayout.SOUTH);
    }
    
    public String getAppletInfo() {
        return NAME;
    }  
    
    public void start() {
		super.start();
    	if (running) internalStart();
    }
    
    public void stop() {
		super.stop();
        if (running) internalStop();
    }

	public void internalStart() {
		running = true;
		visualizer.start();
		controlButton.setText("Stop");
		if (stopIcon != null) controlButton.setIcon(stopIcon);
	}

	public void internalStop() {
		running = false;
		visualizer.stop();
		controlButton.setText("Start");
		if (startIcon != null) controlButton.setIcon(startIcon);
	}

    public void notifyTreeChange() {
        wrapper.cache.uriBasedVisualization(predTree);
        if(!running) visualizer.repaint();
        inDegreeChart.update();
        outDegreeChart.update();
        clustCoeffChart.update();
    }

    public void notifyBaseUriColorChange() {
        wrapper.cache.adjustResourcesUriBaseColor();
        if(!running) visualizer.repaint();
    }

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
        if (source == antialiasCheckbox) {
            visualizer.antialias = selected;
        } else if (source == nodesCheckbox) {
            visualizer.drawnodes = selected;
        } else if (source == edgesCheckbox) {
            visualizer.drawedges = selected;
        } else if (source == arrowCheckbox) {
            visualizer.drawarrows = selected;
        } else if (source == iconsCheckbox) {
            visualizer.drawicons = selected;
		} else if (source == edgeValuesCheckbox) {
			visualizer.drawedgevalues = selected;
        } else if (source == timeCheckbox) {
            visualizer.timing = selected;
		} else if (source == backgroundCheckbox) {
			visualizer.background = selected;
		} else if (source == highlightOnLabelCheckbox) {
			visualizer.highlightOnLabel = selected;
        }
        visualizer.repaint();
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == controlButton) {
            if (visualizer.isRunning()) {
            	    internalStop();
            } else {
            	    internalStart();
            }
        } else if (source == circleButton) {
            visualizer.circle();
        } else if (source == scrambleButton) {
            visualizer.scramble();
        } else if (source == shakeButton) {
            visualizer.shake();
		} else if (source == dataLoadButton) {
            try {
                // Save File Manager
                JFileChooser openWin;
                if(dirBase != null) openWin = new JFileChooser(dirBase);
                else openWin = new JFileChooser();

                openWin.setFileFilter(new WFileFilter());

                int returnVal = openWin.showOpenDialog(null);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadFile(openWin.getSelectedFile());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
		} else if (source == dataClearButton) {
            if (visualizer.isRunning()) {
                internalStop();
            }
            wrapper.clear();
            predTree.clear();
            inDegreeChart.clear();
            outDegreeChart.clear();
            clustCoeffChart.clear();
            resTree.clear();
            iconsManager.clear();
            scrollingResTree.revalidate();
            scrollingPredTree.revalidate();
		} else if (source == filterResetButton) {
			if(wrapper.isEmpty()) return;
            if (visualizer.isRunning()) {
                internalStop();
            }
            resTree.reinit();
            predTree.reinit();
            inDegreeChart.reinit();
            outDegreeChart.reinit();
            clustCoeffChart.reinit(); 

        } else if (source == aboutButton) {
            about();
        } else if (source == delayField) {
            visualizer.delay = Integer.parseInt(delayField.getText());
        } else if (source == massField) {
            visualizer.mass = Float.parseFloat(massField.getText());
        } else if (source == dragField) {
            visualizer.drag = Float.parseFloat(dragField.getText());
        } else if (source == attractionField) {
            visualizer.attraction = Float.parseFloat(attractionField.getText());
        } else if (source == repulsionField) {
            visualizer.repulsion = Float.parseFloat(repulsionField.getText());
        } else if ((source == highlightField) || (source == highlightButton)) {
            visualizer.getGraph().highlightNode(highlightField.getText(),true,visualizer.highlightOnLabel);
        } else if (source == clearButton) {
            visualizer.getGraph().clearHighlights();
        } else if (source == inDegreeChart) {
            inDegreeChart.filter();
            outDegreeChart.update();
            clustCoeffChart.update();
        } else if (source == outDegreeChart) {
            inDegreeChart.update();
            outDegreeChart.filter();
            clustCoeffChart.update();
        } else if (source == clustCoeffChart) {
            inDegreeChart.update();
            outDegreeChart.update();
            clustCoeffChart.filter();
        }
        visualizer.repaint();
    }
    
    private void loadFile(File fileName) throws IOException {
        dirBase = fileName.getParent();
        load(fileName.toURL());
    }
    
    private void load(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        String type = connection.getContentType();
        
        if (type == null) {
            type = "application/unknown";
            String fileName = url.getFile(); 
            int extIndex = fileName.lastIndexOf(".");
            if (extIndex > 0) {
                String ext = fileName.substring(extIndex + 1);
                if (ext.equals("n3") || ext.equals("turtle")) {
                    // FIXME(SM): turtle is a subset of N3, but that's what's mostly used of it
                    // this might return an error in valid N3 files, but until RIO supports
                    // N3 this is the easiest way.
                    type = "text/rdf+n3";
                } else if (ext.equals("rdf") || ext.equals("rdfs") || ext.equals ("owl")) {
                    type = "application/rdf+xml";
                }
            }
        }
        
        boolean res = false;

        InputStream stream = connection.getInputStream();

        if (stream == null) {
            throw new IllegalArgumentException("Data URL '" + url + "' could not be loaded.");
        }
        
        if (type.startsWith("text/rdf+n3")) {
            res = wrapper.addModel(stream, ModelManager.TURTLE);
        } else if (type.startsWith("application/rdf+xml") || type.startsWith("application/xml")) {
            res = wrapper.addModel(stream, ModelManager.RDFXML);
        } else {
            throw new IllegalArgumentException("I don't know how to read '" + url + "' content of type '" + type + "'.");
        }
        
        stream.close();

        if (res) { 
            visualizer.setGraph(wrapper);
            predTree.createTree();
            inDegreeChart.analyze(true);
            outDegreeChart.analyze(true);
            clustCoeffChart.analyze(true);
            resTree.createTree();
            this.notifyBaseUriColorChange();
            scrollingResTree.revalidate();
            scrollingPredTree.revalidate(); 
        } else {
            throw new IllegalArgumentException("Problem loading model from '" + url + "'.");
        }       
    }

	protected static ImageIcon createImageIcon(String path) {
		URL imgURL = Welkin.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find image resource: " + path);
			return null;
		}
	}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

		Welkin welkin = new Welkin();
		welkin.initPanel(false);

        frame = new JFrame(NAME);
        URL logo = Welkin.class.getResource(LOGO_SMALL_ICON);
        if (logo != null) {
        	    frame.setIconImage(Toolkit.getDefaultToolkit().createImage(logo));
        } else {
        	    System.err.println("Couldn't find image resource: " + LOGO_ICON);
        }

        frame.getContentPane().add(welkin, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setSize(X_WIN_DIM, Y_WIN_DIM);
        frame.setVisible(true);
    }

    class WFileFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals("rdf") ||
                    extension.equals("rdfs") ||
                    extension.equals("owl") ||
                    extension.equals("n3") ||
                    extension.equals("turtle")) {
                        return true;
                }
        	}

            return false;
        }

        public String getDescription() {
            return "RDF, RDFS, OWL, n3 or turtle data file";
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
}