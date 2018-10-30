package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import edu.mit.simile.welkin.ModelCache.WResource;

public abstract class ModelChart extends JComponent {

    final static float TOLERANCE = 5.0f;

    final static int NONE = 0;
    final static int NORTH = 1;
    final static int SOUTH = 2;
    final static int EAST = 3;
    final static int WEST = 4;
    final static int WINDOW = 5;

    private int lowValue;
    private int highValue;
    private int lowCount;
    private int highCount;

    private int westDelta;
    private int eastDelta;
    //private int northDelta;
    //private int southDelta;

    private Map distributionByValue = new HashMap();
    private Map distributionByCount = new HashMap();

    private int maxValue = 150;
    private int maxCount = 150;

    private ModelManager model;

    private int draggingState = NONE;

    private List listenerList = new ArrayList();

    class MyMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            //int y = e.getY();
            westDelta = highValue - x;
            eastDelta = x - lowValue;
            //northDelta = highCount - y;
            //southDelta = y - lowCount;
        }

        public void mouseReleased(MouseEvent e) {
            draggingState = NONE;
            signalAction();
        }
    }

    class MyMouseMotionListener extends MouseMotionAdapter {
        JComponent parent;

        public MyMouseMotionListener(JComponent parent) {
            this.parent = parent;
        }

        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            //int y = e.getY();

            if (Math.abs(x - highValue) < TOLERANCE) {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            } else if (Math.abs(x - lowValue) < TOLERANCE) {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            //} else if (Math.abs(y - highCount) < TOLERANCE) {
            //    this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            //} else if (Math.abs(y - lowCount) < TOLERANCE) {
            //    this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            } else {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            //int y = e.getY();

            if (draggingState == NONE) {
                if (Math.abs(x - highValue) < TOLERANCE) {
                    draggingState = EAST;
                } else if (Math.abs(x - lowValue) < TOLERANCE) {
                    draggingState = WEST;
                //} else if (Math.abs(y - highCount) < TOLERANCE) {
                //    draggingState = NORTH;
                //} else if (Math.abs(y - lowCount) < TOLERANCE) {
                //    draggingState = SOUTH;
                } else {
                    draggingState = WINDOW;
                }
            } else {
                switch (draggingState) {
                    case(EAST):
                        highValue = x;
                        break;
                    case(WEST):
                        lowValue = x;
                        break;
                    //case(NORTH):
                    //    highCount = y;
                    //    break;
                    //case(SOUTH):
                    //    lowCount = y;
                    //    break;
                    case(WINDOW):
                        highValue = Math.min(x + westDelta,parent.getWidth() - 1);
                        lowValue = Math.max(x - eastDelta,0);
                        //highCount = Math.min(y + northDelta,parent.getHeight() - 1);
                        //lowCount = Math.max(y - southDelta,0);
                }
            }

            parent.repaint();
        }
    }

    class Count extends ArrayList {
        
        private static final long serialVersionUID = -1897099449786956511L;

        boolean visible = true;

        public Count(Object o) {
            this.add(o);
        }

        public void hide() {
            for (Iterator i = this.iterator(); i.hasNext();) {
                WResource r = (WResource) i.next();
                r.hide();
            }
            this.visible = false;
        }

        public void show() {
            for (Iterator i = this.iterator(); i.hasNext();) {
                WResource r = (WResource) i.next();
                r.show();
            }
            this.visible = false;
        }
    }

    public ModelChart(ModelManager model) {
        this.model = model;
        this.addMouseMotionListener(new MyMouseMotionListener(this));
        this.addMouseListener(new MyMouseListener());
    }

    public void addActionListener(ActionListener l) {
        listenerList.add(l);
    }

    public void removeFooListener(ActionListener l) {
        listenerList.remove(l);
    }

    public abstract int process(WResource node);

    public float unscale(float value,float max,float scale) {
        return (float) (Math.exp(value/scale * Math.log(max)));
    }

    void filter() {
        float w = getWidth() - 1.0f;
        float h = getHeight() - 1.0f;

        float hv = unscale(highValue,maxValue,w);
        float lv = unscale(lowValue,maxValue,w);
        float hc = unscale(h - lowCount,maxCount,h);
        float lc = unscale(h - highCount,maxCount,h);

        processValueVisibility(lv,hv);
        processCountVisibility(lc,hc);
    }

	public float scale(float value,float max,float scale) {
		if(value == 0.0f || value == 1.0f) value += 0.0000000001f;
		if(max == 0.0f || max == 1.0f) max += 0.0000000001f;
	    return (float) (Math.log(value) / Math.log(max)) * scale;
	}

    private void processCountVisibility(float low, float high) {
        for (Iterator i = this.distributionByCount.keySet().iterator(); i.hasNext();) {
            Count c = (Count) this.distributionByCount.get(i.next());
            float size = c.size();
            if (size < low || size > high) {
                c.hide();
            } else {
                c.show();
            }
        }
    }

    private void processValueVisibility(float low, float high) {
        for (Iterator i = this.distributionByValue.keySet().iterator(); i.hasNext();) {
            Integer v = (Integer) i.next();
            Count c = (Count) this.distributionByValue.get(v);
            float value = v.intValue();
            if (value < low || value > high) {
                c.hide();
            } else {
                c.show();
            }
        }
    }

    void analyze(boolean rescale) {
        this.distributionByValue.clear();
        //this.distributionByCount.clear();

        if (rescale) {
            this.maxCount = 0;
            this.maxValue = 0;
        }

        for (Iterator it = model.cache.resources.iterator(); it.hasNext();) {
            WResource n = (WResource) it.next();
            if (!n.isVisible()) continue;

            int value = process(n);
            Integer _value = new Integer(value);
            Count count = (Count) this.distributionByValue.get(_value);
            if (count == null) {
                count = new Count(n);
                this.distributionByValue.put(_value,count);
            } else {
                count.add(n);
                if (count.size() > maxCount) maxCount = count.size();
            }
            if (rescale && value > maxValue) maxValue = value;
        }

        reset();
		repaint();
    }

    void update() {
        analyze(false);
        repaint();
    }

    void clear() {
        this.maxCount = 150;
        this.maxValue = 150;
        update();
    }

    void reset() {
        this.lowValue = 0;
        this.highValue = getWidth() - 1;
        this.lowCount = 0;
        this.highCount = getHeight() - 1;
    }
    
    void reinit() {
    	reset();
    	update();
    }

    final static Color titleColor = Color.BLACK;
    final static Color axisColor = new Color(0x80,0x80,0x80);
    final static Color backgroundColor = Color.WHITE;
    final static Color gridColor = new Color(150, 150, 150, 100);
    final static Color drawColor = Color.RED;
    final static Color xFilterColor = new Color(128, 128, 128, 50);
    final static Color xFilterBorderColor = new Color(192, 192, 192, 100);
    //final static Color yFilterColor = new Color(0, 0, 128, 50);
    //final static Color yFilterBorderColor = new Color(0, 0, 192, 100);

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float h = getHeight() - 1.0f;
        float w = getWidth() - 1.0f;

        Shape chartRect = new Rectangle2D.Float(0,0,w,h);
        g2.setColor(backgroundColor);
        g2.fill(chartRect);
        g2.setColor(axisColor);
        g2.draw(chartRect);

        g2.translate(0.0f, h);

        // paint grid
        g2.setColor(gridColor);
        int xdecades = (int) Math.round(Math.log(maxValue) / Math.log(10.0d));
        for (int i = 0; i < xdecades; i++) {
            for (int j = 0; j < 10; j++) {
                float ox = (float) (j * Math.pow(10.0d,i));
                float x = scale(ox,maxValue,w);
                if (x < w) g2.draw(new Line2D.Float(x,0.0f,x,-h));
            }
        }
        int ydecades = (int) Math.round(Math.log(maxCount) / Math.log(10.0d));
        for (int i = 0; i < ydecades; i++) {
            for (int j = 0; j < 10; j++) {
                float oy = (float) (j * Math.pow(10.0d,i));
                float y = scale(oy,maxCount,h);
                if (y < h) g2.draw(new Line2D.Float(0.0f,-y,w,-y));
            }
        }

        // paint chart
        g2.setColor(drawColor);
        for (Iterator it = this.distributionByValue.keySet().iterator(); it.hasNext();) {
            Integer degree = (Integer) it.next();
            Count count = (Count) this.distributionByValue.get(degree);
            if (count != null && degree.intValue() > 0) {
                float x = scale(degree.floatValue(),maxValue,w);
                float y = scale(count.size(),maxCount,h);
                g2.draw(new Ellipse2D.Float(x-1.0f,-y-1.0f,2.0f,2.0f));
            }
        }

        // paint filters
        float hv = highValue;
        Shape westRect = new Rectangle2D.Float(hv,-h,w - hv,h);
        float lv = lowValue;
        Shape eastRect = new Rectangle2D.Float(0,-h,lv,h);
        //float hc = highCount;
        //Shape northRect = new Rectangle2D.Float(0,hc - h,w,h - hc);
        //float lc = lowCount;
        //Shape southRect = new Rectangle2D.Float(0,-h,w,lc);
        g2.setColor(xFilterColor);
        g2.fill(westRect);
        g2.fill(eastRect);
        g2.setColor(xFilterBorderColor);
        g2.draw(westRect);
        g2.draw(eastRect);
        //g2.setColor(yFilterColor);
        //g2.fill(northRect);
        //g2.fill(southRect);
        //g2.setColor(yFilterBorderColor);
        //g2.draw(northRect);
        //g2.draw(southRect);
    }

    private void signalAction() {
        for (Iterator i = listenerList.iterator(); i.hasNext();) {
            ActionListener l = (ActionListener) i.next();
            l.actionPerformed(new ActionEvent(this,0,"filtered"));
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x,y,width,height);
        reset();
    }

    public Dimension getMinimumSize() {
        return new Dimension(100,50);
    }
}

