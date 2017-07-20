package org.activiti.image.impl.icon;

import org.apache.batik.svggen.SVGGraphics2D;

public abstract class IconType {

    abstract public Integer getWidth();

    abstract public Integer getHeight();

    abstract public String getAnchorValue();

    abstract public String getFillValue();

    abstract public String getStyleValue();

    abstract public String getDValue();

    abstract public void drawIcon(final int imageX,
                                  final int imageY,
                                  final int iconPadding,
                                  final SVGGraphics2D svgGenerator);

    abstract public String getStrokeValue();

    abstract public String getStrokeWidth();
}
