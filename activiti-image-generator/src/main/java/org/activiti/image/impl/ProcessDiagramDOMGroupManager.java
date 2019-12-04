package org.activiti.image.impl;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.DOMTreeManager;

public class ProcessDiagramDOMGroupManager extends DOMGroupManager {

    public ProcessDiagramDOMGroupManager(GraphicContext gc,
                                         DOMTreeManager domTreeManager) {
        super(gc,
              domTreeManager);
    }

    public void setCurrentGroupId(String id) {
        this.currentGroup.setAttribute("id",
                                       id);
    }
}