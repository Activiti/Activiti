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
package org.activiti.bpmn.converter.parser;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MessageFlow;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageFlowParser implements BpmnXMLConstants {

  protected static final Logger LOGGER = LoggerFactory.getLogger(MessageFlowParser.class.getName());

  public void parse(XMLStreamReader xtr, BpmnModel model) throws Exception {
    String id = xtr.getAttributeValue(null, ATTRIBUTE_ID);
    if (StringUtils.isNotEmpty(id)) {
      MessageFlow messageFlow = new MessageFlow();
      messageFlow.setId(id);

      String name = xtr.getAttributeValue(null, ATTRIBUTE_NAME);
      if (StringUtils.isNotEmpty(name)) {
        messageFlow.setName(name);
      }

      String sourceRef = xtr.getAttributeValue(null, ATTRIBUTE_FLOW_SOURCE_REF);
      if (StringUtils.isNotEmpty(sourceRef)) {
        messageFlow.setSourceRef(sourceRef);
      }

      String targetRef = xtr.getAttributeValue(null, ATTRIBUTE_FLOW_TARGET_REF);
      if (StringUtils.isNotEmpty(targetRef)) {
        messageFlow.setTargetRef(targetRef);
      }

      String messageRef = xtr.getAttributeValue(null, ATTRIBUTE_MESSAGE_REF);
      if (StringUtils.isNotEmpty(messageRef)) {
        messageFlow.setMessageRef(messageRef);
      }

      model.addMessageFlow(messageFlow);
    }
  }
}
