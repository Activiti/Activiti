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
                                  final ProcessDiagramSVGGraphics2D svgGenerator);

    abstract public String getStrokeValue();

    abstract public String getStrokeWidth();
}
