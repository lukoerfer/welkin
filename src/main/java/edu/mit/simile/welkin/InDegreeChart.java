package edu.mit.simile.welkin;

import java.util.Iterator;

import edu.mit.simile.welkin.ModelCache.WResource;
import edu.mit.simile.welkin.ModelCache.WStatement;

public class InDegreeChart extends ModelChart {

    private static final long serialVersionUID = 3019966974649961795L;

    public InDegreeChart(ModelManager model) {
        super(model);
    }

    public int process(WResource node) {
        int counter = 0;
        for (Iterator it = node.linkedSubjectNodes.iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof WStatement) {
                WResource n = ((WStatement) o).subject;
                if (n.isVisible()) counter++;
            }
        }
        return counter;
    }
}

