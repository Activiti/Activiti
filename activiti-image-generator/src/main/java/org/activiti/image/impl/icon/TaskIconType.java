package org.activiti.image.impl.icon;

import org.activiti.bpmn.model.GraphicInfo;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Element;

public abstract class TaskIconType {

    public String getAnchorValue() {
        return "top left";
    }

    abstract public String getStyleValue();

    abstract public String getDValue();

    public void drawIcon(final GraphicInfo graphicInfo,
                         final int iconPadding,
                         final SVGGraphics2D svgGenerator) {
        Element gTag = svgGenerator.getDOMFactory().createElementNS(null,
                                                                    SVGGraphics2D.SVG_G_TAG);
        gTag.setAttributeNS(null,
                            "transform",
                            "translate(" + (graphicInfo.getX() + iconPadding) + "," + (graphicInfo.getY() + iconPadding) + ")");

        Element pathTag = svgGenerator.getDOMFactory().createElementNS(null,
                                                                       SVGGraphics2D.SVG_PATH_TAG);
        pathTag.setAttributeNS(null,
                               "d",
                               this.getDValue());
        pathTag.setAttributeNS(null,
                               "anchors",
                               this.getAnchorValue());
        pathTag.setAttributeNS(null,
                               "style",
                               this.getStyleValue());

        gTag.appendChild(pathTag);
        svgGenerator.getDOMTreeManager().appendGroup(gTag,
                                                     null);
    }
}
