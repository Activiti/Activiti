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
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IntermediateThrowEventParseHandler extends AbstractActivityBpmnParseHandler<ThrowEvent> {

  private static final Logger logger = LoggerFactory.getLogger(IntermediateThrowEventParseHandler.class);

  public Class<? extends BaseElement> getHandledType() {
    return ThrowEvent.class;
  }

  protected void executeParse(BpmnParse bpmnParse, ThrowEvent intermediateEvent) {

    EventDefinition eventDefinition = null;
    if (!intermediateEvent.getEventDefinitions().isEmpty()) {
      eventDefinition = intermediateEvent.getEventDefinitions().get(0);
    }

    if (eventDefinition instanceof SignalEventDefinition) {
      SignalEventDefinition signalEventDefinition = (SignalEventDefinition) eventDefinition;
      intermediateEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateThrowSignalEventActivityBehavior(intermediateEvent, signalEventDefinition,
          bpmnParse.getBpmnModel().getSignal(signalEventDefinition.getSignalRef())));

    } else if (eventDefinition instanceof CompensateEventDefinition) {
      CompensateEventDefinition compensateEventDefinition = (CompensateEventDefinition) eventDefinition;
      intermediateEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateThrowCompensationEventActivityBehavior(intermediateEvent, compensateEventDefinition));

    } else if (eventDefinition instanceof MessageEventDefinition) {

        MessageEventDefinition messageEventDefinition = (MessageEventDefinition) eventDefinition;
        Message message = bpmnParse.getBpmnModel().getMessage(messageEventDefinition.getMessageRef());

        BpmnModel bpmnModel = bpmnParse.getBpmnModel();
        if (bpmnModel.containsMessageId(messageEventDefinition.getMessageRef())) {
          messageEventDefinition.setMessageRef(message.getName());
          messageEventDefinition.setExtensionElements(message.getExtensionElements());
        }

        intermediateEvent.setBehavior(bpmnParse.getActivityBehaviorFactory()
                                               .createThrowMessageEventActivityBehavior(intermediateEvent,
                                                                                        messageEventDefinition,
                                                                                        message));
    } else if (eventDefinition == null) {
      intermediateEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateThrowNoneEventActivityBehavior(intermediateEvent));
    } else {
      logger.warn("Unsupported intermediate throw event type for throw event " + intermediateEvent.getId());
    }
  }

  //
  // Seems not to be used anymore?
  //
  // protected CompensateEventDefinition
  // createCompensateEventDefinition(BpmnParse bpmnParse,
  // org.activiti.bpmn.model.CompensateEventDefinition eventDefinition,
  // ScopeImpl scopeElement) {
  // if(StringUtils.isNotEmpty(eventDefinition.getActivityRef())) {
  // if(scopeElement.findActivity(eventDefinition.getActivityRef()) == null) {
  // bpmnParse.getBpmnModel().addProblem("Invalid attribute value for 'activityRef': no activity with id '"
  // + eventDefinition.getActivityRef() +
  // "' in current scope " + scopeElement.getId(), eventDefinition);
  // }
  // }
  //
  // CompensateEventDefinition compensateEventDefinition = new
  // CompensateEventDefinition();
  // compensateEventDefinition.setActivityRef(eventDefinition.getActivityRef());
  // compensateEventDefinition.setWaitForCompletion(eventDefinition.isWaitForCompletion());
  //
  // return compensateEventDefinition;
  // }

}
