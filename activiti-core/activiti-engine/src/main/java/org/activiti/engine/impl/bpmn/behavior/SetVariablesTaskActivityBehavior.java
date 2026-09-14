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
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
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
 * <p>If an error occurs during variable calculation, it is caught and the task is marked as failed.
 * The process instance remains running but the activity is not left. Runtime bundle will detect
 * the failure through execution state and publish an integration error event to the query service.</p>
 */
public class SetVariablesTaskActivityBehavior extends AbstractBpmnActivityBehavior {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SetVariablesTaskActivityBehavior.class);

    private final VariablesCalculator variablesCalculator;

    public SetVariablesTaskActivityBehavior(VariablesCalculator variablesCalculator) {
        this.variablesCalculator = variablesCalculator;
    }

    @Override
    public void execute(DelegateExecution execution) {
        try {
            Map<String, Object> variables = variablesCalculator.calculateInputVariables(execution);
            if (variables != null && !variables.isEmpty()) {
                execution.setVariables(variables);
            }
            leave(execution);
        } catch (Exception e) {
            logger.error(
                "Error calculating variables for SetVariablesTask in execution {}. " +
                "Marking execution as failed without rolling back transaction.",
                execution.getId(),
                e
            );
            handleError(execution, e);
        }
    }

    private void handleError(DelegateExecution execution, Exception error) {
        try {
            // Store error information on execution for runtime bundle to detect
            execution.setVariable("_SET_VARIABLES_TASK_ERROR", Boolean.TRUE);
            execution.setVariable("_SET_VARIABLES_TASK_ERROR_MESSAGE", error.getMessage());
            execution.setVariable("_SET_VARIABLES_TASK_ERROR_CLASS", error.getClass().getName());

            logger.debug(
                "SetVariablesTask error stored on execution {}. Runtime bundle will publish integration error event.",
                execution.getId()
            );
        } catch (Exception e) {
            logger.warn("Failed to handle SetVariablesTask error for execution " + execution.getId(), e);
        }
    }
}
