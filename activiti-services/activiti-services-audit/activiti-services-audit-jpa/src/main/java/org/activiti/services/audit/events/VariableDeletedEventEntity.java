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

package org.activiti.services.audit.events;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "VariableDeletedEvent")
public class VariableDeletedEventEntity extends ProcessEngineEventEntity {

    private String variableName;
    private String variableType;
    private String taskId;

    public VariableDeletedEventEntity() {
    }

    public VariableDeletedEventEntity(Long id,
                                      Long timestamp,
                                      String eventType,
                                      String executionId,
                                      String processDefinitionId,
                                      String processInstanceId,
                                      String variableName,
                                      String variableType,
                                      String taskId) {
        super(id,
              timestamp,
              eventType,
              executionId,
              processDefinitionId,
              processInstanceId);
        this.variableName = variableName;
        this.variableType = variableType;
        this.taskId = taskId;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableType() {
        return variableType;
    }

    public String getTaskId() {
        return taskId;
    }
}
