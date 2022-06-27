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

package org.activiti.runtime.api.impl;

import java.util.Map;

import org.activiti.bpmn.model.UserTask;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.activiti.engine.impl.bpmn.behavior.VariablesPropagator;

public class MappingAwareUserTaskBehavior extends UserTaskActivityBehavior {

    private VariablesCalculator variablesCalculator;

    public MappingAwareUserTaskBehavior(UserTask userTask, VariablesCalculator variablesCalculator, VariablesPropagator variablesPropagator) {
        super(userTask, variablesPropagator);
        this.variablesCalculator = variablesCalculator;
    }

    @Override
    protected Map<String, Object> calculateInputVariables(DelegateExecution execution) {
        return variablesCalculator.calculateInputVariables(execution);
    }

}
