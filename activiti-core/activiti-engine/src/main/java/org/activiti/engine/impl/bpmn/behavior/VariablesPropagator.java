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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;

public class VariablesPropagator {

    private ExecutionEntityManager executionEntityManager;

    private final VariablesCalculator variablesCalculator;

    public VariablesPropagator(VariablesCalculator variablesCalculator) {
        this.variablesCalculator = variablesCalculator;
    }

    public void propagate(DelegateExecution execution, Map<String, Object> availableVariables) {
        if ( availableVariables != null && !availableVariables.isEmpty()) {
            // in the case of a multi instance we need to set the available variables in the local execution scope so that
            // MultiInstanceBehaviour will manage to aggregate the results inside the result collection. Otherwise, the mapping logic is applied.
            if (execution.getParent().isMultiInstanceRoot()) {
                execution.setVariablesLocal(availableVariables);
            } else if (execution.getProcessInstanceId() != null) {
                final ExecutionEntity processInstanceEntity = getExecutionEntityManager().findById(execution.getProcessInstanceId());
                processInstanceEntity.setVariables(variablesCalculator.calculateOutPutVariables(
                    MappingExecutionContext.buildMappingExecutionContext(execution), availableVariables));
            }
        }
    }

    protected ExecutionEntityManager getExecutionEntityManager() {
        if (executionEntityManager == null) {
            executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
        }
        return executionEntityManager;
    }
}
