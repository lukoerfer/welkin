package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;

import edu.mit.simile.welkin.resource.ResourceUri;

public class ResourceUriBasePanel extends JComponent {

    private static final long serialVersionUID = -670863445422612695L;

    private static final int HTOP = 13;
    private static final int XTOP = 2;
    private static final int HROW = 18;

    public static final Color DEFAULT_URI_COLOR = Color.red;

    final Font font = new Font("Verdana", Font.PLAIN, 11);
    FontMetrics fm;

    Welkin welkin;

    Set<ResourcesBaseRow> resourcesBases = new HashSet<ResourcesBaseRow>();

    class ResourcesBaseRow {
        int x,y;
        boolean on=false;
        ResourceUri ns;

        ResourcesBaseRow(ResourceUri ns, int x, int y) {
            this.ns = ns;
            this.x = x;
            this.y = y;
        }
    }

    class MyMouseListener extends MouseAdapter implements ActionListener{

        JColorChooser jcc;
        ResourceUri namespace;

        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                for(Iterator i=resourcesBases.iterator();i.hasNext();) {
                    ResourcesBaseRow nsr = (ResourcesBaseRow) i.next();
                    if(e.getPoint().x>=nsr.x && e.getPoint().x<=nsr.x+8
                      && e.getPoint().y>=nsr.y-12 && e.getPoint().y<=nsr.y+4) {
                        namespace = nsr.ns;
                        jcc = new JColorChooser();
                        JDialog chooser = JColorChooser.createDialog(Welkin.frame,"Pick the namespace color", true, jcc, this, this);
                        chooser.setVisible(true);
                    }
                }
            }
            repaint();
        }

        public void actionPerformed(ActionEvent evt) {
            if(evt.getActionCommand().equals("OK")) {
                namespace.color = jcc.getColor();
                welkin.notifyBaseUriColorChange();
            }
        }

        public void mouseReleased(MouseEvent e) {
            repaint();
        }
    }

    class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            for(Iterator i=resourcesBases.iterator();i.hasNext();) {
                ResourcesBaseRow nsr = (ResourcesBaseRow) i.next();
                if(e.getPoint().x>=nsr.x && e.getPoint().x<=nsr.x+10
                  && e.getPoint().y>=nsr.y-12 && e.getPoint().y<=nsr.y+4) {
                    nsr.on=true;
                } else {
                    nsr.on=false;
                }
            }
            repaint();
        }
    }

    ResourceUriBasePanel (Welkin welkin) {
        this.welkin = welkin;
        this.setLayout(null);

        this.addMouseListener(new MyMouseListener());
        this.addMouseMotionListener(new MyMouseMotionListener());
    }

    public void init() {
        resourcesBases = new HashSet<ResourcesBaseRow>();

        int maxWidth=0;
        int shift=0;
        for(Iterator i=welkin.wrapper.cache.resourcesBases.iterator();i.hasNext();) {
        	ResourceUri urib = (ResourceUri)i.next();
            resourcesBases.add(new ResourcesBaseRow(urib,XTOP,HTOP+(HROW*shift++)));
            maxWidth=fm.stringWidth(urib.getUri())>maxWidth? fm.stringWidth(urib.getUri()):maxWidth;
        }

        if(fm!=null) this.setPreferredSize(new Dimension(maxWidth+20,HROW*resourcesBases.size()+4));
        this.repaint();
    }

    public void clear() {
        resourcesBases = new HashSet<ResourcesBaseRow>();
        this.setPreferredSize(new Dimension(10,10));
        this.repaint();
    }

    public void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);
        g2.setFont(font);
        fm = g2.getFontMetrics(font);

        g2.fill(new Rectangle2D.Float(0.0f, 0.0f, this.getWidth(), this.getHeight()));

        for(ResourcesBaseRow baseUriRow : resourcesBases) {
            if (baseUriRow.on) g2.setColor(ResourceUriBasePanel.DEFAULT_URI_COLOR);
            else g2.setColor(Color.black);
            g2.drawString(baseUriRow.ns.getUri() ,baseUriRow.x+11,baseUriRow.y);
            g2.setColor(baseUriRow.ns.color);
            Shape shape = new Rectangle(baseUriRow.x,baseUriRow.y-8,8,8);
            g2.fill(shape);
        }
    }
}
