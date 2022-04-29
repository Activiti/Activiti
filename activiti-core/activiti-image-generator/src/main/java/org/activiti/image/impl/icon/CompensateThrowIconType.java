/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.image.impl.icon;

import org.activiti.image.impl.ProcessDiagramSVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Element;

public class CompensateThrowIconType extends CompensateIconType {

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
        return "#585858";
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
}
