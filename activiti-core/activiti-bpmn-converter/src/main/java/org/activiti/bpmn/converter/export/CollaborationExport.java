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
package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MessageFlow;
import org.activiti.bpmn.model.Pool;
import org.apache.commons.lang3.StringUtils;

public class CollaborationExport implements BpmnXMLConstants {

  public static void writePools(BpmnModel model, XMLStreamWriter xtw) throws Exception {
    if (!model.getPools().isEmpty()) {
      xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_COLLABORATION, BPMN2_NAMESPACE);
      xtw.writeAttribute(ATTRIBUTE_ID, "Collaboration");
      for (Pool pool : model.getPools()) {
        xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_PARTICIPANT, BPMN2_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_ID, pool.getId());
        if (StringUtils.isNotEmpty(pool.getName())) {
          xtw.writeAttribute(ATTRIBUTE_NAME, pool.getName());
        }
        if (StringUtils.isNotEmpty(pool.getProcessRef())) {
          xtw.writeAttribute(ATTRIBUTE_PROCESS_REF, pool.getProcessRef());
        }
        xtw.writeEndElement();
      }

      for (MessageFlow messageFlow : model.getMessageFlows().values()) {
        xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_MESSAGE_FLOW, BPMN2_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_ID, messageFlow.getId());
        if (StringUtils.isNotEmpty(messageFlow.getName())) {
          xtw.writeAttribute(ATTRIBUTE_NAME, messageFlow.getName());
        }
        if (StringUtils.isNotEmpty(messageFlow.getSourceRef())) {
          xtw.writeAttribute(ATTRIBUTE_FLOW_SOURCE_REF, messageFlow.getSourceRef());
        }
        if (StringUtils.isNotEmpty(messageFlow.getTargetRef())) {
          xtw.writeAttribute(ATTRIBUTE_FLOW_TARGET_REF, messageFlow.getTargetRef());
        }
        if (StringUtils.isNotEmpty(messageFlow.getMessageRef())) {
          xtw.writeAttribute(ATTRIBUTE_MESSAGE_REF, messageFlow.getMessageRef());
        }
        xtw.writeEndElement();
      }

      xtw.writeEndElement();
    }
  }
}
