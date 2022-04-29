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

package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.CollectionUtil;
import org.apache.commons.lang3.StringUtils;

/**


 */
public class StartEventParseHandler extends AbstractActivityBpmnParseHandler<StartEvent> {

  @Override
  public Class<? extends BaseElement> getHandledType() {
    return StartEvent.class;
  }

  @Override
  protected void executeParse(BpmnParse bpmnParse, StartEvent element) {
    if (element.getSubProcess() != null && element.getSubProcess() instanceof EventSubProcess) {
      if (CollectionUtil.isNotEmpty(element.getEventDefinitions())) {
        EventDefinition eventDefinition = element.getEventDefinitions().get(0);
        if (eventDefinition instanceof MessageEventDefinition) {
          MessageEventDefinition messageDefinition = (MessageEventDefinition) eventDefinition;
          BpmnModel bpmnModel = bpmnParse.getBpmnModel();
          String messageRef = messageDefinition.getMessageRef();
          if (bpmnModel.containsMessageId(messageRef)) {
            Message message = bpmnModel.getMessage(messageRef);
            messageDefinition.setMessageRef(message.getName());
            messageDefinition.setExtensionElements(message.getExtensionElements());
          }
          element.setBehavior(bpmnParse.getActivityBehaviorFactory().createEventSubProcessMessageStartEventActivityBehavior(element, messageDefinition));

        } else if (eventDefinition instanceof ErrorEventDefinition) {
          element.setBehavior(bpmnParse.getActivityBehaviorFactory().createEventSubProcessErrorStartEventActivityBehavior(element));
        }
      }

    } else if (CollectionUtil.isEmpty(element.getEventDefinitions())) {
      element.setBehavior(bpmnParse.getActivityBehaviorFactory().createNoneStartEventActivityBehavior(element));
    }

    if (element.getSubProcess() == null && (CollectionUtil.isEmpty(element.getEventDefinitions()) ||
        bpmnParse.getCurrentProcess().getInitialFlowElement() == null)) {

      bpmnParse.getCurrentProcess().setInitialFlowElement(element);
    }

      checkStartFormKey(bpmnParse.getCurrentProcessDefinition(), element);
  }

    private void checkStartFormKey(ProcessDefinitionEntity processDefinition, StartEvent startEvent) {
        if (StringUtils.isNotEmpty(startEvent.getFormKey())) {
            processDefinition.setStartFormKey(true);
        }
    }

}
