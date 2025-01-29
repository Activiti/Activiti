/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.bpmn.converter.child;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.LinkEventDefinition;

import javax.xml.stream.XMLStreamReader;

import static org.activiti.bpmn.converter.LinkEventDefinitionXMLConverter.ATTRIBUTE_LINK_SOURCE;

public class LinkEventSourceParser extends BaseChildElementParser {

    public String getElementName() {
        return ATTRIBUTE_LINK_SOURCE;
    }

    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {

        if (parentElement instanceof LinkEventDefinition linkEventDefinition) {
            linkEventDefinition.addSource(xtr.getElementText());
        }
    }
}
