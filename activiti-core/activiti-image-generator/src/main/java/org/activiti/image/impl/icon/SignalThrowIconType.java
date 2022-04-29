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

public class SignalThrowIconType extends IconType {

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
        return " M7.7124971 20.247342  L22.333334 20.247342  L15.022915000000001 7.575951200000001  L7.7124971 20.247342  z";
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
