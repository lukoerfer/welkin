package edu.mit.simile.welkin;

import java.util.Iterator;

import edu.mit.simile.welkin.ModelCache.WResource;
import edu.mit.simile.welkin.ModelCache.WStatement;

public class ClusteringCoefficientChart extends ModelChart {

    private static final long serialVersionUID = 9047771091320516567L;

    public ClusteringCoefficientChart(ModelManager model) {
        super(model);
    }

    public int process(WResource node) {
        int neighbors = 0;
        int neighborEdges = 0;
        for (Iterator it = node.linkedObjectNodes.iterator(); it.hasNext();) {
            WStatement e = (WStatement) it.next();
            if (e.object.isVisible()) {
                neighbors++;
                for (Iterator it2 = e.object.linkedObjectNodes.iterator(); it2.hasNext();) {
                    WStatement e2 = (WStatement) it2.next();
                    if ((e2.object.isVisible()) && (node.linkedSubjectNodes.contains(e2) || node.linkedObjectNodes.contains(e2))) {
                        neighborEdges++;
                    }
                }
                for (Iterator it2 = e.object.linkedSubjectNodes.iterator(); it2.hasNext();) {
                    WStatement e2 = (WStatement) it2.next();
                    if ((e2.object.isVisible()) && (node.linkedSubjectNodes.contains(e2) || node.linkedObjectNodes.contains(e2))) {
                        neighborEdges++;
                    }
                }
            }
        }
        for (Iterator it = node.linkedSubjectNodes.iterator(); it.hasNext();) {
            WStatement e = (WStatement) it.next();
            if (e.subject.isVisible()) {
                neighbors++;
                for (Iterator it2 = e.subject.linkedObjectNodes.iterator(); it2.hasNext();) {
                    WStatement e2 = (WStatement) it2.next();
                    if ((e2.object.isVisible()) && (node.linkedSubjectNodes.contains(e2) || node.linkedObjectNodes.contains(e2))) {
                        neighborEdges++;
                    }
                }
                for (Iterator it2 = e.subject.linkedSubjectNodes.iterator(); it2.hasNext();) {
                    WStatement e2 = (WStatement) it2.next();
                    if ((e2.object.isVisible()) && (node.linkedSubjectNodes.contains(e2) || node.linkedObjectNodes.contains(e2))) {
                        neighborEdges++;
                    }
                }
            }
        }
        if (neighbors > 0) {
            return (100 * neighborEdges) / (neighbors * neighbors);
        } else {
            return 0;
        }
    }
}

