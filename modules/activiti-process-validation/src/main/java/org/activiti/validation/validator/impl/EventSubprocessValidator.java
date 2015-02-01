/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class EventSubprocessValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<EventSubProcess> eventSubprocesses = process.findFlowElementsOfType(EventSubProcess.class);
		for (EventSubProcess eventSubprocess : eventSubprocesses) {
			
			List<StartEvent> startEvents = process.findFlowElementsInSubProcessOfType(eventSubprocess, StartEvent.class);
			for (StartEvent startEvent : startEvents) {
				if (startEvent.getEventDefinitions() != null && !startEvent.getEventDefinitions().isEmpty()) {
					 EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
					 if (!(eventDefinition instanceof org.activiti.bpmn.model.ErrorEventDefinition) 
		            && !(eventDefinition instanceof MessageEventDefinition)
		            && !(eventDefinition instanceof SignalEventDefinition)) {
						 addError(errors, Problems.EVENT_SUBPROCESS_INVALID_START_EVENT_DEFINITION, process, eventSubprocess, "start event of event subprocess must be of type 'error', 'message' or 'signal'");
					 }
				}
			}
			
		}
	}

}
