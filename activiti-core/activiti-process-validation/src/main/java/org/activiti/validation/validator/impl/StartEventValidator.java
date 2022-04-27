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

package org.activiti.validation.validator.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;


public class StartEventValidator extends ProcessLevelValidator {

  @Override
  protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
    List<StartEvent> startEvents = process.findFlowElementsOfType(StartEvent.class, false);
    validateEventDefinitionTypes(startEvents, process, errors);
    validateMultipleStartEvents(startEvents, process, errors);
  }

  protected void validateEventDefinitionTypes(List<StartEvent> startEvents, Process process, List<ValidationError> errors) {
    for (StartEvent startEvent : startEvents) {
      if (startEvent.getEventDefinitions() != null && !startEvent.getEventDefinitions().isEmpty()) {
        EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
        if (!(eventDefinition instanceof MessageEventDefinition) &&
            !(eventDefinition instanceof TimerEventDefinition) &&
            !(eventDefinition instanceof SignalEventDefinition)) {
            addError(errors, Problems.START_EVENT_INVALID_EVENT_DEFINITION,
                process, startEvent,
                "Unsupported event definition on start event");
          }
        }

    }
  }

  protected void validateMultipleStartEvents(List<StartEvent> startEvents, Process process, List<ValidationError> errors) {

    // Multiple none events are not supported
    List<StartEvent> noneStartEvents = new ArrayList<StartEvent>();
    for (StartEvent startEvent : startEvents) {
      if (startEvent.getEventDefinitions() == null || startEvent.getEventDefinitions().isEmpty()) {
        noneStartEvents.add(startEvent);
      }
    }

    if (noneStartEvents.size() > 1) {
      for (StartEvent startEvent : noneStartEvents) {
        addError(
            errors,
            Problems.START_EVENT_MULTIPLE_FOUND,
            process,
            startEvent,
            "Multiple none start events are not supported");
      }
    }

  }

}
