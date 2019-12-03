package org.activiti.image.impl.icon;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Element;

public class ErrorThrowIconType extends ErrorIconType {

    @Override
    public String getFillValue() {
        return "#585858";
    }

    @Override
    public String getDValue() {
        return " M20.820839 9.171502  L17.36734 22.58992  L11.54138 12.281818999999999  L7.3386512 18.071607  L11.048949 4.832305699999999  L16.996148 14.132659  L20.820839 9.171502  z";
    }

    public void drawIcon(final int imageX,
                         final int imageY,
                         final int iconPadding,
                         final SVGGraphics2D svgGenerator) {
        Element gTag = svgGenerator.getDOMFactory().createElementNS(null,
                                                                    SVGGraphics2D.SVG_G_TAG);
        gTag.setAttributeNS(null,
                            "transform",
                            "translate(" + (imageX - 4) + "," + (imageY - 4) + ")");

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
        svgGenerator.getDOMTreeManager().appendGroup(gTag,
                                                     null);
    }

    @Override
    public String getAnchorValue() {
        return null;
    }

    @Override
    public String getStyleValue() {
        return "stroke-width:1.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:10";
    }

    @Override
    public String getStrokeWidth() {
        return null;
    }
}
