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
package org.activiti.api.runtime.event.impl;

import java.util.Objects;
import java.util.UUID;

import org.activiti.api.model.shared.event.RuntimeEvent;

public abstract class RuntimeEventImpl<ENTITY_TYPE, EVENT_TYPE extends Enum<?>> implements RuntimeEvent<ENTITY_TYPE, EVENT_TYPE> {

    private String id;
    private Long timestamp;
    private String processInstanceId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private Integer processDefinitionVersion;
    private String businessKey;
    private String parentProcessInstanceId;

    private ENTITY_TYPE entity;

    public RuntimeEventImpl() {
        id = UUID.randomUUID().toString();
        timestamp = System.currentTimeMillis();
    }

    public RuntimeEventImpl(ENTITY_TYPE entity) {
        this();
        this.entity = entity;
    }

    public RuntimeEventImpl(String id,
                            Long timestamp,
                            ENTITY_TYPE entity) {
        this.id = id;
        this.timestamp = timestamp;
        this.entity = entity;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ENTITY_TYPE getEntity() {
        return entity;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public Integer getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    @Override
    public String getBusinessKey() {
        return businessKey;
    }

    @Override
    public String getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(String parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    protected void setEntity(ENTITY_TYPE entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RuntimeEventImpl [id=")
               .append(id)
               .append(", timestamp=")
               .append(timestamp)
               .append(", processInstanceId=")
               .append(processInstanceId)
               .append(", processDefinitionId=")
               .append(processDefinitionId)
               .append(", processDefinitionKey=")
               .append(processDefinitionKey)
               .append(", processDefinitionVersion=")
               .append(processDefinitionVersion)
               .append(", businessKey=")
               .append(businessKey)
               .append(", parentProcessInstanceId=")
               .append(parentProcessInstanceId)
               .append(", entity=")
               .append(entity)
               .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessKey,
                            entity,
                            id,
                            parentProcessInstanceId,
                            processDefinitionId,
                            processDefinitionKey,
                            processDefinitionVersion,
                            processInstanceId,
                            timestamp,
                            getEventType());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RuntimeEventImpl other = (RuntimeEventImpl) obj;
        return Objects.equals(businessKey, other.businessKey)
                && Objects.equals(entity, other.entity)
                && Objects.equals(id, other.id)
                && Objects.equals(parentProcessInstanceId, other.parentProcessInstanceId)
                && Objects.equals(processDefinitionId, other.processDefinitionId)
                && Objects.equals(processDefinitionKey, other.processDefinitionKey)
                && Objects.equals(processDefinitionVersion, other.processDefinitionVersion)
                && Objects.equals(processInstanceId, other.processInstanceId)
                && Objects.equals(timestamp, other.timestamp);
    }

}
