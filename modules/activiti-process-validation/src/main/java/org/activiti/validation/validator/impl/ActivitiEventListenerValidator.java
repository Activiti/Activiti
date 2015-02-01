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
import org.activiti.bpmn.model.EventListener;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class ActivitiEventListenerValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<EventListener> eventListeners = process.getEventListeners();
		if (eventListeners != null) {
			for (EventListener eventListener : eventListeners) {
				
				if (eventListener.getImplementationType() != null 
						&& eventListener.getImplementationType().equals(ImplementationType.IMPLEMENTATION_TYPE_INVALID_THROW_EVENT)) {
					
					addError(errors, Problems.EVENT_LISTENER_INVALID_THROW_EVENT_TYPE, process, eventListener, 
							"Invalid or unsupported throw event type on event listener");
					
			  } else if (eventListener.getImplementationType() == null || eventListener.getImplementationType().length() == 0) {
			  	
					addError(errors, Problems.EVENT_LISTENER_IMPLEMENTATION_MISSING, process, eventListener, 
							"Element 'class', 'delegateExpression' or 'throwEvent' is mandatory on eventListener");
					
				} else if (eventListener.getImplementationType() != null) {
					
					if(!ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(eventListener.getImplementationType())
							&& !ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(eventListener.getImplementationType())
							&& !ImplementationType.IMPLEMENTATION_TYPE_THROW_SIGNAL_EVENT.equals(eventListener.getImplementationType())
							&& !ImplementationType.IMPLEMENTATION_TYPE_THROW_GLOBAL_SIGNAL_EVENT.equals(eventListener.getImplementationType())
							&& !ImplementationType.IMPLEMENTATION_TYPE_THROW_MESSAGE_EVENT.equals(eventListener.getImplementationType())
							&& !ImplementationType.IMPLEMENTATION_TYPE_THROW_ERROR_EVENT.equals(eventListener.getImplementationType())) {
						addError(errors, Problems.EVENT_LISTENER_INVALID_IMPLEMENTATION, process, eventListener, "Unsupported implementation type for event listener");
					}
					
				}
				
			}
			
		}
	}
		
}
