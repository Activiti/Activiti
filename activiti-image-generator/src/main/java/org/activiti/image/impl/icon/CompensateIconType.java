package org.activiti.image.impl.icon;

import org.activiti.image.impl.ProcessDiagramSVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Element;

public class CompensateIconType extends IconType {

    @Override
    public Integer getWidth() {
        return 15;
    }

    @Override
    public Integer getHeight() {
        return 16;
    }

    @Override
    public String getFillValue() {
        return "none";
    }

    @Override
    public String getStyleValue() {
        return null;
    }

    @Override
    public String getDValue() {
        return null;
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
                            "translate(" + (imageX - 8) + "," + (imageY - 6) + ")");

        Element polygonTag1 = svgGenerator.getDOMFactory().createElementNS(null,
                                                                           SVGGraphics2D.SVG_POLYGON_TAG);
        polygonTag1.setAttributeNS(null,
                                   "points",
                                   "14 8 14 22 7 15 ");
        polygonTag1.setAttributeNS(null,
                                   "fill",
                                   this.getFillValue());
        polygonTag1.setAttributeNS(null,
                                   "stroke",
                                   this.getStrokeValue());
        polygonTag1.setAttributeNS(null,
                                   "stroke-width",
                                   this.getStrokeWidth());
        polygonTag1.setAttributeNS(null,
                                   "stroke-linecap",
                                   "butt");
        polygonTag1.setAttributeNS(null,
                                   "stroke-linejoin",
                                   "miter");
        polygonTag1.setAttributeNS(null,
                                   "stroke-miterlimit",
                                   "10");
        gTag.appendChild(polygonTag1);

        Element polygonTag2 = svgGenerator.getDOMFactory().createElementNS(null,
                                                                           SVGGraphics2D.SVG_POLYGON_TAG);
        polygonTag2.setAttributeNS(null,
                                   "points",
                                   "21 8 21 22 14 15 ");
        polygonTag2.setAttributeNS(null,
                                   "fill",
                                   this.getFillValue());
        polygonTag2.setAttributeNS(null,
                                   "stroke",
                                   this.getStrokeValue());
        polygonTag2.setAttributeNS(null,
                                   "stroke-width",
                                   this.getStrokeWidth());
        polygonTag2.setAttributeNS(null,
                                   "stroke-linecap",
                                   "butt");
        polygonTag2.setAttributeNS(null,
                                   "stroke-linejoin",
                                   "miter");
        polygonTag2.setAttributeNS(null,
                                   "stroke-miterlimit",
                                   "10");
        gTag.appendChild(polygonTag2);

        svgGenerator.getExtendDOMGroupManager().addElement(gTag);
    }

    @Override
    public String getStrokeValue() {
        return "#585858";
    }

    @Override
    public String getStrokeWidth() {
        return "1.4";
    }

    @Override
    public String getAnchorValue() {
        return null;
    }
}
