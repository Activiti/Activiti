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

public class ErrorIconType extends IconType {

    @Override
    public String getFillValue() {
        return "none";
    }

    @Override
    public String getStrokeValue() {
        return "#585858";
    }

    @Override
    public String getDValue() {
        return " M21.820839 10.171502  L18.36734 23.58992  L12.541380000000002 13.281818999999999  L8.338651200000001 19.071607  L12.048949000000002 5.832305699999999  L17.996148000000005 15.132659  L21.820839 10.171502  z";
    }

    public void drawIcon(final int imageX,
                         final int imageY,
                         final int iconPadding,
                         final ProcessDiagramSVGGraphics2D svgGenerator) {
        Element gTag = svgGenerator.getDOMFactory().createElementNS(null,
                                                                    SVGGraphics2D.SVG_G_TAG);
        gTag.setAttributeNS(null,
                            "transform",
                            "translate(" + (imageX - 6) + "," + (imageY - 3) + ")");

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
    public String getAnchorValue() {
        return null;
    }

    @Override
    public String getStyleValue() {
        return "fill:none;stroke-width:1.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:10";
    }

    @Override
    public Integer getWidth() {
        return 17;
    }

    @Override
    public Integer getHeight() {
        return 22;
    }

    @Override
    public String getStrokeWidth() {
        return null;
    }
}
