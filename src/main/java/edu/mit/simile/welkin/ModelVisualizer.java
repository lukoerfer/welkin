package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Iterator;

import javax.swing.JComponent;

import edu.mit.simile.welkin.ModelCache.WLiteral;
import edu.mit.simile.welkin.ModelCache.WResource;
import edu.mit.simile.welkin.ModelCache.WStatement;
import edu.mit.simile.welkin.resource.PredicateUri;

public class ModelVisualizer extends JComponent implements Runnable {

    private static final long serialVersionUID = -2459104922600917338L;

    final static float xBORDER = 6.0f;
    final static float xBORDERs = xBORDER * 2.0f;
    final static float yBORDER = 3.0f;
    final static float yBORDERs = yBORDER * 2.0f;

    final float MIN_ALPHA = 1.0f;
    final float MAX_ALPHA = MIN_ALPHA + 80.0f;
    final float ALPHA_INC = 20.0f;
    final float ZOOM_FACTOR = 2.0f;

    final Color fixedColor = Color.green;
    final Color selectColor = Color.red;
    final Color edgeColor = new Color(150, 150, 150, 75);
    final Color edgeValueColor = new Color(50, 50, 50, 100);
    final Color nodeColor = Color.red;
    final Color externalNodeColor = Color.orange;
    final Color timeColor = Color.black;
    final Color tooltipBorderColor = Color.black;
    final Color pickedBGColor = new Color(255, 255, 0, 100);
    final Color pickedFontColor = Color.black;
    final Color highlightLiteralBGColor = new Color(255, 255, 255, 150);
    final Color highlightBGColor = new Color(255, 255, 0, 150);
    final Color highlightFontColor = Color.black;
    final Color groupColor = new Color(0, 0, 0, 200);
    final Color groupFontColor = groupColor;
    final Color groupBGColor = new Color(255, 255, 255, 100);
    final Color groupBorderColor = Color.black;

    final Font timeFont = new Font("Verdana", Font.PLAIN, 9);
    final Font edgeFont = new Font("Verdana", Font.PLAIN, 8);
    final Font zoomFont = new Font("Verdana", Font.PLAIN, 9);
    final Font pickedFont = new Font("Verdana", Font.BOLD, 11);
    final Font fixedFont = new Font("Verdana", Font.PLAIN, 10);
    final Font propertiesFont = new Font("Verdana", Font.PLAIN, 10);
    final Font highlightedFont = new Font("Verdana", Font.PLAIN, 10);

    public int delay = 50; // time (milliseconds)
    public float mass = 10.0f; // mass (kg)
    public float drag = 2.0f; // drag coefficient (kg / second)
    public float attraction = 5.0f; // force (kg * pixel / second^2) [/100]
    public float repulsion = 1.0f; // force (kg * pixel / second^2) [*100]

    float REPULSION_END = 40.0f; // distance (pixel)
    float REPULSION_ENDs = 2.0f * REPULSION_END;

    public boolean antialias = true;
    public boolean drawedges = true;
    public boolean drawnodes = true;
    public boolean drawarrows = false;
    public boolean drawicons = false;
    public boolean timing = true;
    public boolean drawgroups = true;
    public boolean drawedgevalues = false;
    public boolean background = true;
    public boolean highlightOnLabel = true;

    public boolean colors = false;

    ModelManager model;
    Welkin welkin;
    WResource pick;

    boolean pickfixed;
    boolean zoom = false;

    float zoomX = 0.0f;
    float zoomY = 0.0f;
    float alpha = MIN_ALPHA;
    
//  Partial Fade algorithm implementation
//  -------------------------------------
/*
    private TreeNode root;

    private float minX = 0;
    private float maxX = 0;
    private float minY = 0;
    private float maxY = 0;
*/
    
    class MyMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {
                zoom = true;
                zoomX = e.getX() - cx;
                zoomY = e.getY() - cy;
            } else {
                float x = e.getX() - cx;
                float y = e.getY() - cy;
                double bestdist = Double.MAX_VALUE;

                for (Iterator i = model.cache.resources.iterator(); i.hasNext();) {
                    WResource n = (WResource) i.next();
                    if(!n.isVisible()) continue;
                    if (n != null) {
                        float dx = n.x - x;
                        float dy = n.y - y;
                        float dist = dx * dx + dy * dy;
                        if (dist < bestdist) {
                            pick = n;
                            bestdist = dist;
                        }
                    }
                }

                if (pick != null) {
                    pickfixed = pick.fixed;
                    pick.fixed = true;
                    pick.x = x;
                    pick.y = y;
                }
            }
            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            if (zoom) {
                zoom = false;
            } else {
                if (pick != null) {
                    int x = e.getX() - (int) cx;
                    int y = e.getY() - (int) cy;
                    pick.x = x;
                    pick.y = y;
                    keepInsideCanvas(pick);
                    if (e.getClickCount() == 2) {
                        pick.fixed = !pickfixed;
                    } else {
                        pick.fixed = pickfixed;
                    }
                    pick = null;
                }
            }
            repaint();
        }
    }

    class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent e) {
            if (pick != null) {
                int x = e.getX() - (int) cx;
                int y = e.getY() - (int) cy;
                pick.x = x;
                pick.y = y;
                keepInsideCanvas(pick);
            } else if (zoom) {
                zoomX = e.getX() - cx;
                zoomY = e.getY() - cy;
            }
            repaint();
        }
    }

    Thread relaxer;

    float cx;
    float cy;
    float cw;
    float ch;

    public ModelVisualizer(ModelManager model, Welkin welkin) {
        this.addMouseMotionListener(new MyMouseMotionListener());
        this.addMouseListener(new MyMouseListener());
        setGraph(model);
        this.welkin = welkin;
    }

    public void setGraph(ModelManager model) {
        this.model = model;
        scramble();
    }

    public ModelManager getGraph() {
        return this.model;
    }

    public boolean isRunning() {
        return relaxer != null;
    }

    public void start() {
        relaxer = new Thread(this);
        relaxer.start();
    }

    public void stop() {
        relaxer = null;
    }

    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        this.cw = w;
        this.ch = h;
        this.cx = cw / 2.0f;
        this.cy = ch / 2.0f;
    }

    public void run() {
        Thread thisThread = Thread.currentThread();
        while (relaxer == thisThread) {
            simulate();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void circle() {
  
//    	 Partial Fade algorithm implementation
//    	 -------------------------------------
/*
        initFade();
*/
    	
        float r = Math.min(cx, cy) - 50.0f;
        float alpha = (float) (2.0d * Math.PI / model.cache.resources.size());

        int j = 0;
        for (Iterator it = model.cache.resources.iterator(); it.hasNext();) {
            WResource n = (WResource) it.next();
            n.x = (float) (r * Math.sin(alpha * j));
            n.y = (float) (r * Math.cos(alpha * j++));
        }
        
// Partial Fade algorithm implementation
// -------------------------------------
/*
        spaceDecomposition(root, minX, maxX, minY, maxY);
*/
       
        repaint();
    }

    public void scramble() {
    	
// Partial Fade algorithm implementation
// -------------------------------------
/*
       initFade();
*/
    	
        for (Iterator it = model.cache.resources.iterator(); it.hasNext();) {
            WResource n = (WResource) it.next();
            if (!n.fixed) {
                n.x = (float) ((double) (cx - 50.0f) * 2.0f * (Math.random() - 0.5d));
                n.y = (float) ((double) (cy - 50.0f) * 2.0f * (Math.random() - 0.5d));
            }
        }
        
// Partial Fade algorithm implementation
// -------------------------------------
 /*
        spaceDecomposition(root, minX, maxX, minY, maxY);
 */
        
        repaint();
    }

    public void shake() {
    	
// Partial Fade algorithm implementation
// -------------------------------------
/*
       initFade();
*/
    	
        for (Iterator it = model.cache.resources.iterator(); it.hasNext();) {
            WResource n = (WResource) it.next();
            if (!n.fixed) {
                n.x = (float) (n.x + (80 * Math.random() - 40));
                n.y = (float) (n.y + (80 * Math.random() - 40));
                keepInsideCanvas(n);
            }
        }
        
// Partial Fade algorithm implementation
// -------------------------------------
/*
        spaceDecomposition(root, minX, maxX, minY, maxY);
*/
        
        repaint();
    }

    void keepInsideCanvas(WResource n) {
        if (n.x < -cx) {
            n.x = -cx;
        } else if (n.x > cx) {
            n.x = cx;
        }
        if (n.y < -cy) {
            n.y = -cy;
        } else if (n.y > cy) {
            n.y = cy;
        }
    }

    long simulationTime;
    long drawingTime;

    float attractive(float d, float weight) {
        return attraction * weight * d / 100.0f;
    }

    float repulsive(float d) {
        if (d < REPULSION_END) {
            float r = 100.0f * repulsion * (d - REPULSION_END)
                    / (d * (d - REPULSION_ENDs));
            return Math.min(r, 500.0f);
        } else {
            return 0.0f;
        }
    }
    


// Partial Fade algorithm implementation
// -------------------------------------
/*
    float teta = 1;
    
    void initFade(){
       root = new TreeNode();
       minX = maxX = minY = maxY = 0;
    }
    
    void spaceDecomposition(FadeTreeNode node, float minX, float maxX, float minY, float maxY) {

        System.out.println("Decomposition: " + node.nodes.size());
        
        float cellWidth = (maxX - minX)/2;
        float middleX = minX + cellWidth;
        float middleY = minX + cellWidth;
        
        FadeTreeNode ul = new FadeTreeNode();
        FadeTreeNode ur = new FadeTreeNode();
        FadeTreeNode bl = new FadeTreeNode();
        FadeTreeNode br = new FadeTreeNode();
        
        node.center = massCenter(node, minX, minY);
        for(int i=0; i<node.nodes.size(); i++) {
            WResource wr = ((WResource)node.nodes.get(i));
            float distance = (float) Math.sqrt(Math.pow(node.center.x-wr.x,2)
            	+(Math.pow(node.center.y-wr.x,2)));
            
            if(cellWidth/distance > teta) {
                if( wr.x < middleX && wr.y < middleY) bl.addResource(wr);
                if( wr.x < middleX && wr.y > middleY) ul.addResource(wr);
                if( wr.x > middleX && wr.y < middleY) br.addResource(wr);
                if( wr.x > middleX && wr.y > middleY) ur.addResource(wr);
            } else
                System.out.println("************");
        }
        
        if(bl.nodes.size() > 0) {
            System.out.println("Decomposition bl: " + bl.nodes.size());
            //node.nodes.removeAll(bl.nodes);
            spaceDecomposition(bl, minX, middleX, minY, middleY);
        }
        if(br.nodes.size() > 0) {
            System.out.println("Decomposition br: " + br.nodes.size());
            //node.nodes.removeAll(br.nodes);
            spaceDecomposition(br, minX, middleX, middleY, maxY);
        }
        if(ul.nodes.size() > 0) {
            System.out.println("Decomposition ul: " + ul.nodes.size());
            //node.nodes.removeAll(ul.nodes);
            spaceDecomposition(ul, middleX, maxX, minY, middleY);
        }
        if(ur.nodes.size() > 0) {
            System.out.println("Decomposition ur: " + ur.nodes.size());
            //node.nodes.removeAll(ur.nodes);
            spaceDecomposition(ur, middleX, maxX, middleY, maxY);
        }
    }
    
    Point massCenter(FadeTreeNode tn, float x, float y) {
        float mcx = 0;
        float mcy = 0;
        for(int i=0; i<tn.nodes.size(); i++) {
            mcx += ((WResource)tn.nodes.get(i)).x;
            mcy += ((WResource)tn.nodes.get(i)).y;
        }   
        
        Point tReturn = new Point();
        tReturn.setLocation(mcx/tn.nodes.size(), mcy/tn.nodes.size());
        return tReturn;
    }
*/
    
    void simulate() {
        long startTime = 0;

        if (timing) startTime = System.currentTimeMillis();

        PredicateUri[] pu;
        float sum = 0;

        for (Iterator it = model.cache.resources.iterator(); it.hasNext();) {
            WResource n = (WResource) it.next();

            if (!n.isVisible()) continue;

            float xi = n.x;
            float yi = n.y;
            float vxi = n.vx;
            float vyi = n.vy;

            float fx = 0.0f;
            float fy = 0.0f;

            for (Iterator j = model.cache.resources.iterator(); j.hasNext();) {
                WResource m = (WResource) j.next();

                if (!m.isVisible()) continue;

                float xj = m.x;
                float yj = m.y;

                // calculate euclidean distance between n and m
                float deltax = xi - xj;
                float deltay = yi - yj;
                float d2 = deltax * deltax + deltay * deltay;
                float d = (float) Math.sqrt(d2);
                if (d == 0) d = 0.0001f; // avoid dividing by zero

                // get the weight of the link between n and m
                sum = 0;
                pu = model.cache.getEntries(n.hash,m.hash);
                if ((pu != null) && (pu.length > 0)) {
                    for (int i = 0; i < pu.length; i++) {
                        if(pu[i].included) sum += pu[i].weight;
                    }
                }
                float weight = sum > 1 ? 1.0f : sum;

                // attractive force
                float af = attractive(d, weight);

                // repulsion force
                float rf = repulsive(d);

                // resulting force
                float f = rf - af;

                // apply the forces
                fx += (deltax / d) * f;
                fy += (deltay / d) * f;
            }

            // repulsion force from the borders
            fx += repulsive(xi + cx) - repulsive(cx - xi);
            fy += repulsive(yi + cy) - repulsive(cy - yi);

            // drag
            fx -= drag * vxi;
            fy -= drag * vyi;

            // update speed with acceleration
            vxi += fx / mass;
            vyi += fy / mass;

            // update location with speed
            xi += vxi;
            yi += vyi;

            if (!n.fixed) {
                n.x = xi;
                n.y = yi;
                n.vx = vxi;
                n.vy = vyi;
                keepInsideCanvas(n);
            }
        }

        if (timing) simulationTime = System.currentTimeMillis() - startTime;

        repaint();
    }

    public float zoom(float x, float y) {
        if (alpha > MIN_ALPHA) {
            x -= zoomX;
            y -= zoomY;
            return ZOOM_FACTOR * (float) Math.exp(-((x * x) + (y * y)) / (alpha * alpha));
        } else {
            return 0.0f;
        }
    }

    public void paintComponent(Graphics g) {
        long startTime = 0;

        if (timing) startTime = System.currentTimeMillis();

        Graphics2D g2 = (Graphics2D) g;

        if (antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        if (background) {
            g2.setColor(Color.white);
            g2.fill(new Rectangle2D.Float(0.0f, 0.0f, cw, ch));
        }

        WStatement tmpStatement;
        if (drawedges) {
            g2.setFont(edgeFont);
            FontMetrics fm = g2.getFontMetrics(edgeFont);
            float ascent = fm.getAscent();

            for (Iterator nodes = model.cache.resources.iterator(); nodes.hasNext();) {
                WResource n1 = (WResource) nodes.next();
                if (!n1.isVisible()) continue;
                for (Iterator edges = n1.linkedObjectNodes.iterator(); edges .hasNext();) {
                	tmpStatement = (WStatement) edges.next();
                	if(!tmpStatement.predicate.included) continue;
                    WResource n2 = tmpStatement.object;
                    if (!n2.isVisible()) continue;
                    float z1 = zoom(n1.x, n1.y);
                    float z2 = zoom(n2.x, n2.y);
                    float x1 = n1.x + (n1.x - zoomX) * z1 + cx;
                    float x2 = n2.x + (n2.x - zoomX) * z2 + cx;
                    float y1 = n1.y + (n1.y - zoomY) * z1 + cy;
                    float y2 = n2.y + (n2.y - zoomY) * z2 + cy;

                    g2.setColor(edgeColor);
                    g2.draw(new Line2D.Float(x1, y1, x2, y2));

                    if (drawarrows) {
						double dx = x2 - x1;
						double dy = y2 - y1;
						double alfa;

						if (dx == 0) {
							if (dy > 0) alfa = -Math.PI / 2;
							else alfa = Math.PI / 2;
						} else alfa = -Math.atan(dy / dx);

						if (dx < 0) alfa = alfa + Math.PI;
						alfa = Math.toDegrees(alfa);

						if (alfa <= 20) alfa = alfa + 360;
						alfa = alfa +170;

						g2.fillArc(
							(int) ((x2 - 15)), (int) ((y2 - 15)),
							30, 30, (int) alfa, 20);
                    }

                    if (drawedgevalues) {
                        float x = (x2 + x1) / 2.0f;
                        float y = (y2 + y1) / 2.0f + ascent;
                        String value = "1.0";
                        // String value = String.valueOf(((Edge) n1.edgesTo.get(n2)).value);
                        g2.setColor(edgeValueColor);
                        g2.drawString(value, x, y);
                    }
                }
            }
        }

        if (drawnodes) {
            for (Iterator i = model.cache.resources.iterator(); i.hasNext();) {
                WResource n = (WResource) i.next();
                if (!n.isVisible()) continue;
                float z = zoom(n.x, n.y);
                float x = n.x + (n.x - zoomX) * z + cx;
                float y = n.y + (n.y - zoomY) * z + cy;
                
                if(drawicons) {
                	if(n.iconId== -1) {
		                Shape border, inside;
		                if (n.isNotSubject) {
		                    border = new Ellipse2D.Float(x - 3.0f, y - 3.0f, 6.0f, 6.0f);
		                    inside = new Ellipse2D.Float(x - 1.5f, y - 1.5f, 3.0f, 3.0f);
		                } else {
	
		                    border = new Rectangle2D.Float(x - 3.5f, y - 3.5f, 7.0f, 7.0f);
		                    inside = new Rectangle2D.Float(x - 3.0f, y - 3.0f, 6.0f, 6.0f);
		                }
		
		                if (n == pick) {
		                    g2.setColor(n.color);
		                    g2.fill(inside);
		                    g2.setColor(fixedColor);
		                    g2.draw(border);
		                } else {
		                    if (n.fixed) {
		                        g2.setColor(fixedColor);
		                        g2.fill(inside);
		                        g2.setColor(n.color);
		                        g2.draw(border);
		                    } else {
		                        g2.setColor(n.color);
		                        g2.fill(inside);
		                    }
		                }
                	} else {
                		g2.drawImage(welkin.iconsManager.getIconById(n.iconId), (int) x - 8, (int) y - 8, this);                		
                	}
                } else {
	                Shape border, inside;
	                if (n.isNotSubject) {
	                    border = new Ellipse2D.Float(x - 3.0f, y - 3.0f, 6.0f, 6.0f);
	                    inside = new Ellipse2D.Float(x - 1.5f, y - 1.5f, 3.0f, 3.0f);
	                } else {
	                    border = new Rectangle2D.Float(x - 3.5f, y - 3.5f, 7.0f, 7.0f);
	                    inside = new Rectangle2D.Float(x - 3.0f, y - 3.0f, 6.0f, 6.0f);
	                }
	
	                if (n == pick || (n.highlighted && !n.fixed)) {
	                    g2.setColor(n.color);
	                    g2.fill(inside);
	                    g2.setColor(fixedColor);
	                    g2.draw(border);
	                } else {
	                    if (n.fixed) {
	                        g2.setColor(fixedColor);
	                        g2.fill(inside);
	                        g2.setColor(n.color);
	                        g2.draw(border);
	                    } else {
	                        g2.setColor(n.color);
	                        g2.fill(inside);
	                        //g2.draw(border);
	                    }
	                }               	
                }
            }
        }

        AffineTransform t = g2.getTransform();

        for (Iterator it = model.cache.resources.iterator(); it.hasNext();) {
            WResource n = (WResource) it.next();

            if (!n.isVisible()) continue;

            if (zoom) {
                float z = zoom(n.x, n.y);
                if (z > ZOOM_FACTOR * 0.95f) {
                    float x = n.x + (n.x - zoomX) * z;
                    float y = n.y + (n.y - zoomY) * z;
                    float dx = x - zoomX;
                    float dy = y - zoomY;
                    double theta;
                    if (Math.abs(dx) < Math.abs(dy)) {
                        theta = Math.acos(dx / dy);
                        if (dy < 0.0f) theta += Math.PI;
                    } else {
                        theta = Math.asin(dy / dx);
                        if (dx < 0.0f) theta += Math.PI;
                    }
                    float d = (float) Math.sqrt((dx * dx) + (dy * dy));

                    FontMetrics fm = g2.getFontMetrics(zoomFont);
                    float width = fm.stringWidth(n.label) + xBORDERs;
                    float ascent = fm.getAscent();
                    float descent = fm.getDescent();
                    float height = ascent + descent + yBORDERs;

                    if (dx < 0.0f) {
                        theta -= Math.PI;
                        d = -d - width;
                    }

                    g2.translate(zoomX + cx, zoomY + cy);
                    g2.rotate(theta);

                    Shape rectangle = new RoundRectangle2D.Float(d, -ascent, width, height, height, height);
                    g2.setColor(highlightBGColor);
                    g2.fill(rectangle);
                    g2.setColor(tooltipBorderColor);
                    g2.draw(rectangle);
                    g2.setColor(highlightFontColor);
                    g2.setFont(zoomFont);
                    g2.drawString(n.label, d + xBORDER, yBORDER);
                    g2.setTransform(t);
                }
            } else {

                if ((n == pick) || n.fixed || n.highlighted) {

                	   int count = 0;

                	   Font subjectFont = null;
                    if (n == pick) {
                	     subjectFont = pickedFont;
                    } else if (n.fixed || n.highlighted) {
                		 subjectFont = fixedFont;
                    }

                    FontMetrics fm = g2.getFontMetrics(subjectFont);
                    float width = fm.stringWidth(n.label) + xBORDERs;
                    float ascent = fm.getAscent();
                    float descent = fm.getDescent();
                    float height = ascent + descent + yBORDERs;

                    float rectWidth = width;
                    float rectHeight = height + yBORDER;

                    if (n == pick) {
                        // count properties and find text length
                        fm = g2.getFontMetrics(propertiesFont);
                        float a = fm.getAscent();
                        float d = fm.getDescent();
                        float line = a + d + yBORDERs;

                        for (Iterator i = n.getLiterals(); i.hasNext();) {
                            WLiteral lit = (WLiteral) i.next();
                            float length = fm.stringWidth(lit.predicate.toString() + " -> " + lit.literal) + xBORDERs;
                            if (length > rectWidth) rectWidth = length;
                            rectHeight += line;
                            count++;
                        }
                    }

                    // make sure the properties box is placed in the canvas

                    float x = n.x;
                    float w = rectWidth + xBORDERs;
                    if ((x + w) < cx) {
                        // do nothing because it fits on the right
                    } else {
                    	    // does not fit on the right, so
                    		if ((x - w) > -cx) {
                    			// fits on the left, so translate the starting point
                    			x -= rectWidth;
                    		} else {
                              // does not fit at all, so center it
                              x = - rectWidth / 2.0f;
                    		}
                    }

                    float y = n.y;
                    float h = rectHeight + yBORDERs;
                    if ((y - h) > -cy) {
                        // translate because it fits on the top (which is our preferred position)
                    	   y -= rectHeight;
                    } else {
                    		// does not fit at the top, so
                    	    if ((y + h) < cy) {
                    	    		// do nothing because it fits at the bottom
                    	    } else {
                    	    		// does not fit at all, so center it
                              y = - rectHeight / 2.0f;
                    	    }
                    }

                    g2.translate(cx,cy); // translate to the center (since we use a center based coordinate system)

                    if (count > 0) {
                        // Draw the literal box
                        Shape rectangle = new RoundRectangle2D.Float(x, y, rectWidth, rectHeight, height, height);
                        g2.setColor(highlightLiteralBGColor);
                        g2.fill(rectangle);
                        g2.setColor(tooltipBorderColor);
                        g2.draw(rectangle);
                        g2.setFont(propertiesFont);
                        fm = g2.getFontMetrics(propertiesFont);
                        float asc = fm.getAscent();
                        float desc = fm.getDescent();
                        float lineHeight = asc + desc + 3.0f;

                        int i = 0;
                        for (Iterator itt = n.getLiterals(); itt.hasNext(); i++) {
                        	// Draw the literals
                            WLiteral lit = (WLiteral) itt.next();
                            g2.drawString(lit.predicate.toString() + " -> " + lit.literal, x + xBORDER, y + asc + lineHeight * i + yBORDER + height);
                        }
                    }

                    // Draw the subject
                    Shape rectangle = new RoundRectangle2D.Float(x, y, width, height, height, height);
                    g2.setColor(highlightBGColor);
                    g2.fill(rectangle);
                    g2.setColor(tooltipBorderColor);
                    g2.draw(rectangle);
                    g2.setColor(highlightFontColor);
                    g2.setFont(subjectFont);
                    g2.drawString(n.label, x + xBORDER, y + yBORDER + ascent);
                    g2.translate(-cx,-cy);
                }
            }
        }

        if (timing) {
            g2.translate(5, ch - 5);
            drawingTime = System.currentTimeMillis() - startTime;
            g.setColor(timeColor);
            g.setFont(timeFont);
            FontMetrics fm = g2.getFontMetrics(timeFont);
            int height = fm.getAscent() + fm.getDescent();
            g.drawString("nodes: " + model.cache.resources.size(), 0, -3 * height);
            g.drawString("edges: " + model.cache.statements.size(), 0, -2 * height);
            g.drawString("drawing: " + drawingTime + " ms", 0, -1 * height);
            g.drawString("calculation: " + simulationTime + " ms", 0, 0);
            g2.setTransform(t);
        }

        if (zoom) {
            if (alpha < MAX_ALPHA) {
                alpha += ALPHA_INC;
                repaint();
            }
        } else {
            if (alpha > MIN_ALPHA) {
                alpha -= ALPHA_INC;
                repaint();
            }
        }
    }

    static final Dimension minimum = new Dimension(200,120);
    static final Dimension preferred = new Dimension(300,200);

    public Dimension getMinimumSize() {
        return minimum;
    }

    public Dimension getPreferredSize() {
        return preferred;
    }
    
//  Partial Fade algorithm implementation
//  -------------------------------------
/*
    class FadeTreeNode {
        Point center;
        ArrayList nodes = new ArrayList();;
        ArrayList child = new ArrayList();;
        
        public void addResource(WResource node) {
            nodes.add(node);
        }
        
        public void addChild(WResource node) {
            nodes.add(node);
        }
    }
*/
}