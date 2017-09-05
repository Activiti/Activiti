/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.query.events.handlers;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.activiti.services.query.model.QVariable;
import org.activiti.services.query.model.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessVariableUpdateHandler {

    private final VariableUpdater variableUpdater;

    @Autowired
    public ProcessVariableUpdateHandler(VariableUpdater variableUpdater) {
        this.variableUpdater = variableUpdater;
    }

    public void handle(Variable updatedVariable) {
        String variableName = updatedVariable.getName();
        String processInstanceId = updatedVariable.getProcessInstanceId();
        BooleanExpression predicate = QVariable.variable.name.eq(variableName)
                .and(
                        QVariable.variable.processInstanceId.eq(String.valueOf(processInstanceId))
                );
        variableUpdater.update(updatedVariable,
                               predicate,
                               "Unable to find variable named '" + variableName + "' for process instance '" + processInstanceId + "'");
    }
}
