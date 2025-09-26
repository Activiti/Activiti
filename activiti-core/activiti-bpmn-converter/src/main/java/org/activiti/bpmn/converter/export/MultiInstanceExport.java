/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;
import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.apache.commons.lang3.StringUtils;

public class MultiInstanceExport implements BpmnXMLConstants {

    public static void writeMultiInstance(Activity activity, XMLStreamWriter xtw) throws Exception {
        if (activity == null || activity.getLoopCharacteristics() == null) {
            return;
        }

        MultiInstanceLoopCharacteristics multiInstanceObject = activity.getLoopCharacteristics();

        xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_MULTIINSTANCE, BPMN2_NAMESPACE);

        BpmnXMLUtil.writeDefaultAttribute(
            ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL,
            String.valueOf(multiInstanceObject.isSequential()).toLowerCase(),
            xtw
        );

        writeAttributeIfNotEmpty(ATTRIBUTE_MULTIINSTANCE_COLLECTION, multiInstanceObject.getInputDataItem(), xtw);
        writeAttributeIfNotEmpty(ATTRIBUTE_MULTIINSTANCE_VARIABLE, multiInstanceObject.getElementVariable(), xtw);
        writeAttributeIfNotEmpty(
            ATTRIBUTE_MULTIINSTANCE_INDEX_VARIABLE,
            multiInstanceObject.getElementIndexVariable(),
            xtw
        );

        writeElementIfNotEmpty(ELEMENT_MULTIINSTANCE_CARDINALITY, multiInstanceObject.getLoopCardinality(), xtw);
        writeElementIfNotEmpty(ELEMENT_MULTI_INSTANCE_DATA_OUTPUT, multiInstanceObject.getLoopDataOutputRef(), xtw);

        if (StringUtils.isNotEmpty(multiInstanceObject.getOutputDataItem())) {
            xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_MULTI_INSTANCE_OUTPUT_DATA_ITEM, BPMN2_NAMESPACE);
            xtw.writeAttribute(ATTRIBUTE_NAME, multiInstanceObject.getOutputDataItem());
            xtw.writeEndElement();
        }

        writeElementIfNotEmpty(ELEMENT_MULTIINSTANCE_CONDITION, multiInstanceObject.getCompletionCondition(), xtw);

        xtw.writeEndElement();
    }

    private static void writeAttributeIfNotEmpty(String attributeName, String value, XMLStreamWriter xtw)
        throws Exception {
        if (StringUtils.isNotEmpty(value)) {
            BpmnXMLUtil.writeQualifiedAttribute(attributeName, value, xtw);
        }
    }

    private static void writeElementIfNotEmpty(String elementName, String value, XMLStreamWriter xtw) throws Exception {
        if (StringUtils.isNotEmpty(value)) {
            xtw.writeStartElement(BPMN2_PREFIX, elementName, BPMN2_NAMESPACE);
            xtw.writeCharacters(value);
            xtw.writeEndElement();
        }
    }
}
