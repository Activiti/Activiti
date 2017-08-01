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
@DiscriminatorValue(value = VariableUpdatedEventEntity.VARIABLE_UPDATED_EVENT)
public class VariableUpdatedEventEntity extends ProcessEngineEventEntity {

    protected static final String VARIABLE_UPDATED_EVENT = "VariableUpdatedEvent";

    private String variableName;
    private String variableValue;
    private String variableType;
    private String taskId;

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
