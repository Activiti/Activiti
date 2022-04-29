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
package org.activiti.spring.boot.process.validation;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

public class AsyncPropertyValidator extends ProcessLevelValidator {

    @Override
    protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
        validateFlowElementsInContainer(process, errors, process);
    }

    protected void validateFlowElementsInContainer(FlowElementsContainer container, List<ValidationError> errors, Process process) {
        for (FlowElement flowElement : container.getFlowElements()) {
            if (flowElement instanceof FlowElementsContainer) {
                FlowElementsContainer subProcess = (FlowElementsContainer) flowElement;
                validateFlowElementsInContainer(subProcess, errors, process);
            }

            if ((flowElement instanceof FlowNode) && ((FlowNode) flowElement).isAsynchronous()) {
                addWarning(errors, Problems.FLOW_ELEMENT_ASYNC_NOT_AVAILABLE, process , flowElement, "Async property is not available when asyncExecutor is disabled.");
            }

            if ((flowElement instanceof Event)) {
                ((Event) flowElement).getEventDefinitions().stream().forEach(event -> {
                    if (event instanceof TimerEventDefinition) {
                        addWarning(errors, Problems.EVENT_TIMER_ASYNC_NOT_AVAILABLE, process, flowElement, "Timer event is not available when asyncExecutor is disabled.");
                    } else if ((event instanceof SignalEventDefinition) && ((SignalEventDefinition) event).isAsync() ) {
                        addWarning(errors, Problems.SIGNAL_ASYNC_NOT_AVAILABLE, process, flowElement, "Async property is not available when asyncExecutor is disabled.");
                    }
                });
            }
        }
    }
}
