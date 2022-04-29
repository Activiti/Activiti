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

public abstract class TaskIconType extends IconType {

    @Override
    public String getAnchorValue() {
        return "top left";
    }

    @Override
    public String getStrokeValue() {
        return null;
    }

    @Override
    public String getFillValue() {
        return null;
    }

    @Override
    public Integer getWidth() {
        return null;
    }

    @Override
    public Integer getHeight() {
        return null;
    }

    @Override
    public String getStrokeWidth() {
        return null;
    }

    public void drawIcon(final int imageX,
                         final int imageY,
                         final int iconPadding,
                         final ProcessDiagramSVGGraphics2D svgGenerator) {
        Element gTag = svgGenerator.getDOMFactory().createElementNS(null,
                                                                    SVGGraphics2D.SVG_G_TAG);
        gTag.setAttributeNS(null,
                            "transform",
                            "translate(" + (imageX + iconPadding) + "," + (imageY + iconPadding) + ")");

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
        svgGenerator.getExtendDOMGroupManager().addElement(gTag);
    }
}
