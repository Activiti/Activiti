package org.activiti.image.impl;

import java.util.Map;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;

public class ProcessDiagramSVGGraphics2D extends SVGGraphics2D {

    public ProcessDiagramSVGGraphics2D(Document domFactory) {
        super(domFactory);
        this.setDOMGroupManager(new ProcessDiagramDOMGroupManager(this.getGraphicContext(),
                                                                  this.getDOMTreeManager()));
    }

    @Override
    public void setRenderingHints(@SuppressWarnings("rawtypes") Map hints) {
        super.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(@SuppressWarnings("rawtypes") Map hints) {
        super.addRenderingHints(hints);
    }

    public void setCurrentGroupId(String id) {
        this.getExtendDOMGroupManager().setCurrentGroupId(id);
    }

    public ProcessDiagramDOMGroupManager getExtendDOMGroupManager() {
        return (ProcessDiagramDOMGroupManager) super.getDOMGroupManager();
    }
}
