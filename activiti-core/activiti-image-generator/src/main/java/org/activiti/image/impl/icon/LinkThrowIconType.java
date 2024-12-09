package org.activiti.image.impl.icon;

import org.activiti.image.impl.ProcessDiagramSVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Element;

public class LinkThrowIconType extends IconType{
    @Override
    public Integer getWidth() {
        return 17;
    }

    @Override
    public Integer getHeight() {
        return 15;
    }

    @Override
    public String getAnchorValue() {
        return null;
    }

    @Override
    public String getFillValue() {
        return "#585858";
    }

    @Override
    public String getStyleValue() {
        return "stroke-width:1.4;stroke-miterlimit:4;stroke-dasharray:none";
    }

    @Override
    public String getDValue() {
        return "M 0 0 L 0 0 M 4 4 L 8 4 L 8 3 L 10 5 L 8 7 L 8 6 L 4 6 L 4 4 z";
    }

    @Override
    public void drawIcon(int imageX,
                         int imageY,
                         int iconPadding,
                         ProcessDiagramSVGGraphics2D svgGenerator) {
        Element gTag = svgGenerator.getDOMFactory().createElementNS(null,
            SVGGraphics2D.SVG_G_TAG);
        gTag.setAttributeNS(null,
            "transform",
            "translate(" + (imageX - 7) + "," + (imageY - 7) + ")");

        Element pathTag = svgGenerator.getDOMFactory().createElementNS(null,
            SVGGraphics2D.SVG_PATH_TAG);
        pathTag.setAttributeNS(null,
            "d",
            this.getDValue());
        pathTag.setAttributeNS(null,
            "style",
            this.getStyleValue());
        pathTag.setAttributeNS(null,
            "fill",
            this.getFillValue());
        pathTag.setAttributeNS(null,
            "stroke",
            this.getStrokeValue());

        gTag.appendChild(pathTag);
        svgGenerator.getExtendDOMGroupManager().addElement(gTag);
    }

    @Override
    public String getStrokeValue() {
        return "#585858";
    }

    @Override
    public String getStrokeWidth() {
        return null;
    }
}
