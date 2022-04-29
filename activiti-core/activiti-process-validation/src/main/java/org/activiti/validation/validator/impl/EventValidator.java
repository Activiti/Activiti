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

package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * Validates rules that apply to all events (start event, boundary event, etc.)
 *

 */
public class EventValidator extends ProcessLevelValidator {

  @Override
  protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
    List<Event> events = process.findFlowElementsOfType(Event.class);
    for (Event event : events) {
      if (event.getEventDefinitions() != null) {
        for (EventDefinition eventDefinition : event.getEventDefinitions()) {

          if (eventDefinition instanceof MessageEventDefinition) {
            handleMessageEventDefinition(bpmnModel, process, event, eventDefinition, errors);
          } else if (eventDefinition instanceof SignalEventDefinition) {
            handleSignalEventDefinition(bpmnModel, process, event, eventDefinition, errors);
          } else if (eventDefinition instanceof TimerEventDefinition) {
            handleTimerEventDefinition(process, event, eventDefinition, errors);
          } else if (eventDefinition instanceof CompensateEventDefinition) {
            handleCompensationEventDefinition(bpmnModel, process, event, eventDefinition, errors);
          }

        }
      }
    }
  }

  protected void handleMessageEventDefinition(BpmnModel bpmnModel, Process process, Event event, EventDefinition eventDefinition, List<ValidationError> errors) {
    MessageEventDefinition messageEventDefinition = (MessageEventDefinition) eventDefinition;

    if (StringUtils.isEmpty(messageEventDefinition.getMessageRef())) {

      if (StringUtils.isEmpty(messageEventDefinition.getMessageExpression())) {
        // message ref should be filled in
        addError(errors, Problems.MESSAGE_EVENT_MISSING_MESSAGE_REF, process, event, "attribute 'messageRef' is required");
      }

    } else if (!bpmnModel.containsMessageId(messageEventDefinition.getMessageRef())) {
      // message ref should exist
      addError(errors, Problems.MESSAGE_EVENT_INVALID_MESSAGE_REF, process, event, "Invalid 'messageRef': no message with that id can be found in the model");
    }
  }

  protected void handleSignalEventDefinition(BpmnModel bpmnModel, Process process, Event event, EventDefinition eventDefinition, List<ValidationError> errors) {
    SignalEventDefinition signalEventDefinition = (SignalEventDefinition) eventDefinition;

    if (StringUtils.isEmpty(signalEventDefinition.getSignalRef())) {

      if (StringUtils.isEmpty(signalEventDefinition.getSignalExpression())) {
        addError(errors, Problems.SIGNAL_EVENT_MISSING_SIGNAL_REF, process, event, "signalEventDefinition does not have mandatory property 'signalRef'");
      }

    } else if (!bpmnModel.containsSignalId(signalEventDefinition.getSignalRef())) {
      addError(errors, Problems.SIGNAL_EVENT_INVALID_SIGNAL_REF, process, event, "Invalid 'signalRef': no signal with that id can be found in the model");
    }
  }

  protected void handleTimerEventDefinition(Process process, Event event, EventDefinition eventDefinition, List<ValidationError> errors) {
    TimerEventDefinition timerEventDefinition = (TimerEventDefinition) eventDefinition;
    if (StringUtils.isEmpty(timerEventDefinition.getTimeDate()) && StringUtils.isEmpty(timerEventDefinition.getTimeCycle()) && StringUtils.isEmpty(timerEventDefinition.getTimeDuration())) {
      // neither date, cycle or duration configured
      addError(errors, Problems.EVENT_TIMER_MISSING_CONFIGURATION, process, event, "Timer needs configuration (either timeDate, timeCycle or timeDuration is needed)");
    }
  }

  protected void handleCompensationEventDefinition(BpmnModel bpmnModel, Process process, Event event, EventDefinition eventDefinition, List<ValidationError> errors) {
    CompensateEventDefinition compensateEventDefinition = (CompensateEventDefinition) eventDefinition;

    // Check activityRef
    if ((StringUtils.isNotEmpty(compensateEventDefinition.getActivityRef()) && process.getFlowElement(compensateEventDefinition.getActivityRef(), true) == null)) {
      addError(errors, Problems.COMPENSATE_EVENT_INVALID_ACTIVITY_REF, process, event, "Invalid attribute value for 'activityRef': no activity with the given id");
    }
  }

}
