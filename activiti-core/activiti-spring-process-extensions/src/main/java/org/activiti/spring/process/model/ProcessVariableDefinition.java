/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.spring.process.model;

import java.util.Objects;

public class ProcessVariableDefinition extends VariableDefinition {

    private String processDefinitionName;
    private String processDefinitionId;
    private String processDefinitionKey;

    public ProcessVariableDefinition() {}

    public ProcessVariableDefinition(
        String processDefinitionName,
        String processDefinitionId,
        String processDefinitionKey,
        String type,
        Object value
    ) {
        super(type, value);
        this.processDefinitionName = processDefinitionName;
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKey = processDefinitionKey;

    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }
    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }
    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }
    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProcessVariableDefinition that)) return false;
        return Objects.equals(getProcessDefinitionName(), that.getProcessDefinitionName())
            && Objects.equals(getProcessDefinitionId(), that.getProcessDefinitionId())
            && Objects.equals(getProcessDefinitionKey(), that.getProcessDefinitionKey())
            && Objects.equals(getDescription(), that.getDescription())
            && Objects.equals(getName(), that.getName())
            && Objects.equals(isRequired(), that.isRequired())
            && Objects.equals(getId(), that.getId())
            && Objects.equals(isAnalytics(), that.isAnalytics())
            && Objects.equals(isEphemeral(), that.isEphemeral())
            && Objects.equals(getDisplayName(), that.getDisplayName())
            && Objects.equals(getDisplay(), that.getDisplay())
            && Objects.equals(getValue(), that.getValue())
            && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProcessDefinitionName(), getProcessDefinitionId(), getProcessDefinitionKey(),
            getDescription(), getName(), isRequired(), getId(), isAnalytics(), isEphemeral(), getDisplayName(), getDisplay(), getValue(), getType());
    }
}
