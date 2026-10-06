/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.Map;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behavior of the built-in "set variables" service task
 * ({@code <serviceTask implementation="set-variables.EXECUTE"/>}).
 *
 * <p>It resolves the task's input mappings and writes each resolved value to the process variable
 * named by the mapping key (an already declared process variable), then leaves the task. The engine
 * configures the task as asynchronous during parsing to create a transaction boundary before the
 * variable update, while still performing the same in-memory variable calculation and assignment
 * logic.</p>
 *
 * <p>Errors during variable calculation and assignment are handled as BPMN errors for visibility
 * and process-level error handling:
 * <ul>
 *   <li>If a {@link BpmnError} is caught (from explicit error in expressions), it is propagated as-is
 *   <li>If an {@link ActivitiException} occurs (mapping errors, evaluation errors), it is converted
 *       to a BPMN error with error code "SET_VARIABLES_MAPPING_ERROR" to allow process designers
 *       to attach error boundary events for handling
 * </ul>
 * This ensures variable mapping/calculation errors are visible and can be handled at the process level
 * rather than silently failing or escalating as uncaught exceptions.
 * </p>
 */
public class SetVariablesTaskActivityBehavior extends TaskActivityBehavior {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SetVariablesTaskActivityBehavior.class);

    private final VariablesCalculator variablesCalculator;

    public SetVariablesTaskActivityBehavior(VariablesCalculator variablesCalculator) {
        this.variablesCalculator = variablesCalculator;
    }

    @Override
    public void execute(DelegateExecution execution) {
        boolean noErrors = true;
        Map<String, Object> variables = null;
        try {
            variables = variablesCalculator.calculateInputVariables(execution);
        } catch (Exception e) {
            LOGGER.warn(
                "Exception while executing set-variables task {}: {}",
                execution.getCurrentFlowElement().getId(),
                e.getMessage()
            );

            noErrors = false;
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof BpmnError) {
                ErrorPropagation.propagateError((BpmnError) rootCause, execution);
            } else {
                // Convert any variable mapping/calculation errors to a BPMN error for process-level handling
                ErrorPropagation.propagateError("SET_VARIABLES_MAPPING_ERROR", execution);
            }
        }
        if (noErrors) {
            if (variables != null && !variables.isEmpty()) {
                execution.setVariables(variables);
            }
            leave(execution);
        }
    }
}
