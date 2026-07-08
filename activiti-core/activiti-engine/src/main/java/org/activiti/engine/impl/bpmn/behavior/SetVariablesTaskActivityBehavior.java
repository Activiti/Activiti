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

/**
 * Behavior of the built-in {@code setVariablesTask} service task
 * ({@code <serviceTask implementation="setVariablesTask"/>}).
 *
 * <p>It resolves the task's input mappings and writes each resolved value to the process variable
 * named by the mapping key (an already declared process variable), then leaves the task. It runs
 * synchronously in the engine - there is no connector invocation and no messaging round-trip.</p>
 */
public class SetVariablesTaskActivityBehavior extends AbstractBpmnActivityBehavior {

    private static final long serialVersionUID = 1L;

    private final VariablesCalculator variablesCalculator;

    public SetVariablesTaskActivityBehavior(VariablesCalculator variablesCalculator) {
        this.variablesCalculator = variablesCalculator;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Map<String, Object> variables = variablesCalculator.calculateInputVariables(execution);
        if (variables != null && !variables.isEmpty()) {
            execution.setVariables(variables);
        }
        leave(execution);
    }
}
