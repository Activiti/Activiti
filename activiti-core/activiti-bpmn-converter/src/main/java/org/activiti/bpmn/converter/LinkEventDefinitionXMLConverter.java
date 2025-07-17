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
package org.activiti.bpmn.converter;

import org.activiti.bpmn.model.LinkEventDefinition;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static org.activiti.bpmn.constants.BpmnXMLConstants.ATTRIBUTE_ID;
import static org.activiti.bpmn.constants.BpmnXMLConstants.ATTRIBUTE_NAME;
import static org.activiti.bpmn.constants.BpmnXMLConstants.BPMN2_NAMESPACE;
import static org.activiti.bpmn.constants.BpmnXMLConstants.BPMN2_PREFIX;


public class LinkEventDefinitionXMLConverter {
    public static final String ELEMENT_EVENT_LINK_DEFINITION = "linkEventDefinition";
    public static final String ATTRIBUTE_LINK_SOURCE = "source";
    public static final String ATTRIBUTE_LINK_TARGET = "target";

    public void writeLinkDefinition(LinkEventDefinition eventDefinition, XMLStreamWriter xtw) throws Exception {
        xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_EVENT_LINK_DEFINITION, BPMN2_NAMESPACE);
        writeAttribute(xtw, ATTRIBUTE_ID, eventDefinition.getId());
        writeAttribute(xtw, ATTRIBUTE_NAME, eventDefinition.getName());
        writeElement(xtw, ATTRIBUTE_LINK_TARGET, eventDefinition.getTarget());
        if (eventDefinition.getSources() != null) {
            for (String source : eventDefinition.getSources()) {
                writeElement(xtw, ATTRIBUTE_LINK_SOURCE, source);
            }
        }
        xtw.writeEndElement();
    }

    private void writeAttribute(XMLStreamWriter xtw, String attributeName, String attributeValue) throws XMLStreamException {
        if (attributeValue != null) {
            xtw.writeAttribute(attributeName, attributeValue);
        }
    }

    private void writeElement(XMLStreamWriter xtw, String elementName, String elementValue) throws XMLStreamException {
        if (StringUtils.isNotEmpty(elementValue)) {
            xtw.writeStartElement(BPMN2_PREFIX, elementName, BPMN2_NAMESPACE);
            xtw.writeCharacters(elementValue);
            xtw.writeEndElement();
        }
    }
}
