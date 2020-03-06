package org.activiti.image.impl.icon;

import org.activiti.image.impl.ProcessDiagramSVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Element;

public class MessageIconType extends IconType {

    @Override
    public String getFillValue() {
        return "#585858";
    }

    @Override
    public String getStrokeValue() {
        return "none";
    }

    @Override
    public String getStrokeWidth() {
        return "1";
    }

    @Override
    public String getDValue() {
        return " m0 1.5  l0 13  l17 0  l0 -13  z M1.5 3  L6 7.5  L1.5 12  z M3.5 3  L13.5 3  L8.5 8  z m12 0  l0 9  l-4.5 -4.5  z M7 8.5  L8.5 10  L10 8.5  L14.5 13  L2.5 13  z";
    }

    public void drawIcon(final int imageX,
                         final int imageY,
                         final int iconPadding,
                         final ProcessDiagramSVGGraphics2D svgGenerator) {
        Element gTag = svgGenerator.getDOMFactory().createElementNS(null,
                                                                    SVGGraphics2D.SVG_G_TAG);
        gTag.setAttributeNS(null,
                            "transform",
                            "translate(" + (imageX - 1) + "," + (imageY - 2) + ")");

        Element pathTag = svgGenerator.getDOMFactory().createElementNS(null,
                                                                       SVGGraphics2D.SVG_PATH_TAG);
        pathTag.setAttributeNS(null,
                               "d",
                               this.getDValue());
        pathTag.setAttributeNS(null,
                               "fill",
                               this.getFillValue());
        pathTag.setAttributeNS(null,
                               "stroke",
                               this.getStrokeValue());
        pathTag.setAttributeNS(null,
                               "stroke-widthh",
                               this.getStrokeWidth());

        gTag.appendChild(pathTag);
        svgGenerator.getExtendDOMGroupManager().addElement(gTag);
    }

    @Override
    public String getAnchorValue() {
        return null;
    }

    @Override
    public String getStyleValue() {
        return null;
    }

    @Override
    public Integer getWidth() {
        return 17;
    }

    @Override
    public Integer getHeight() {
        return 13;
    }
}
