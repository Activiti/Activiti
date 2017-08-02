/*
 * Copyright 2017 Alfresco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.services.history.events;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "VariableCreatedEvent")
public class VariableCreatedEventEntity extends ProcessEngineEventEntity {

    protected String variableName;
    protected String variableValue;
    protected String variableType;
    protected String taskId;

    public VariableCreatedEventEntity() {
    }

    public VariableCreatedEventEntity(Long timestamp,
                                      String eventType,
                                      String executionId,
                                      String processDefinitionId,
                                      String processInstanceId,
                                      String variableName,
                                      String variableValue,
                                      String variableType,
                                      String taskId) {
        super(timestamp,
              eventType,
              executionId,
              processDefinitionId,
              processInstanceId);
        this.variableName = variableName;
        this.variableValue = variableValue;
        this.variableType = variableType;
        this.taskId = taskId;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public String getVariableType() {
        return variableType;
    }

    public String getTaskId() {
        return taskId;
    }
}
