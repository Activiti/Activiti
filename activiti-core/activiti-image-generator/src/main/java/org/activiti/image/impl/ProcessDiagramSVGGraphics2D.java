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
package org.activiti.image.impl;

import java.util.Map;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;

public class ProcessDiagramSVGGraphics2D extends SVGGraphics2D {

    public ProcessDiagramSVGGraphics2D(Document domFactory) {
        super(domFactory);
        this.setDOMGroupManager(new ProcessDiagramDOMGroupManager(this.getGraphicContext(),
                                                                  this.getDOMTreeManager()));
    }

    @Override
    public void setRenderingHints(@SuppressWarnings("rawtypes") Map hints) {
        super.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(@SuppressWarnings("rawtypes") Map hints) {
        super.addRenderingHints(hints);
    }

    public void setCurrentGroupId(String id) {
        this.getExtendDOMGroupManager().setCurrentGroupId(id);
    }

    public ProcessDiagramDOMGroupManager getExtendDOMGroupManager() {
        return (ProcessDiagramDOMGroupManager) super.getDOMGroupManager();
    }
}
