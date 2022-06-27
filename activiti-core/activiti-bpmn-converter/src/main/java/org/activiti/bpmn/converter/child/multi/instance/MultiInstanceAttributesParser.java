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
package org.activiti.bpmn.converter.child.multi.instance;

import static org.activiti.bpmn.constants.BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE;
import static org.activiti.bpmn.constants.BpmnXMLConstants.ATTRIBUTE_MULTIINSTANCE_COLLECTION;
import static org.activiti.bpmn.constants.BpmnXMLConstants.ATTRIBUTE_MULTIINSTANCE_INDEX_VARIABLE;
import static org.activiti.bpmn.constants.BpmnXMLConstants.ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL;
import static org.activiti.bpmn.constants.BpmnXMLConstants.ATTRIBUTE_MULTIINSTANCE_VARIABLE;
import static org.activiti.bpmn.constants.BpmnXMLConstants.ELEMENT_MULTIINSTANCE;

import javax.xml.stream.XMLStreamReader;
import org.activiti.bpmn.converter.child.ElementParser;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;

public class MultiInstanceAttributesParser implements
    ElementParser<MultiInstanceLoopCharacteristics> {

    @Override
    public boolean canParseCurrentElement(XMLStreamReader reader) {
        return reader.isStartElement() && ELEMENT_MULTIINSTANCE.equalsIgnoreCase(reader.getLocalName());
    }

    @Override
    public void setInformation(XMLStreamReader reader,
        MultiInstanceLoopCharacteristics loopCharacteristics) {
        loopCharacteristics.setInputDataItem(reader.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE,
            ATTRIBUTE_MULTIINSTANCE_COLLECTION));
        loopCharacteristics.setElementVariable(reader.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE,
            ATTRIBUTE_MULTIINSTANCE_VARIABLE));
        loopCharacteristics.setElementIndexVariable(reader.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE,
            ATTRIBUTE_MULTIINSTANCE_INDEX_VARIABLE));

        String isSequentialValue = reader.getAttributeValue(null,
            ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL);
        if (isSequentialValue != null) {
            loopCharacteristics.setSequential(Boolean.valueOf(isSequentialValue));
        }
    }
}
