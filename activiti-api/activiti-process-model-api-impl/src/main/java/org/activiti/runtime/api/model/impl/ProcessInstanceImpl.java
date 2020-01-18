/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.model.impl;

import java.util.Date;
import java.util.Objects;

import org.activiti.runtime.api.model.ProcessInstance;

public class ProcessInstanceImpl implements ProcessInstance {

    private String id;
    private String name;
    private String description;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String initiator;
    private Date startDate;
    private String businessKey;
    private ProcessInstanceStatus status;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public String getInitiator() {
        return initiator;
    }

    @Override
    public String getBusinessKey() {
        return businessKey;
    }

    @Override
    public ProcessInstanceStatus getStatus() {
        return status;
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    public String getProcessDefinitionKey() { return processDefinitionKey; }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public void setStatus(ProcessInstanceStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProcessInstanceImpl that = (ProcessInstanceImpl) o;
        return Objects.equals(id,
                              that.id) &&
                Objects.equals(name,
                               that.name) &&
                Objects.equals(description,
                               that.description) &&
                Objects.equals(processDefinitionId,
                               that.processDefinitionId) &&
                Objects.equals(processDefinitionKey,
                               that.processDefinitionKey) &&
                Objects.equals(initiator,
                               that.initiator) &&
                Objects.equals(startDate,
                               that.startDate) &&
                Objects.equals(businessKey,
                               that.businessKey) &&
                status == that.status;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id,
                            name,
                            description,
                            processDefinitionId,
                            processDefinitionKey,
                            initiator,
                            startDate,
                            businessKey,
                            status);
    }

    @Override
    public String toString() {
        return "ProcessInstance{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", processDefinitionId='" + processDefinitionId + '\'' +
                ", processDefinitionKey='" + processDefinitionKey + '\'' +
                ", initiator='" + initiator + '\'' +
                ", startDate=" + startDate +
                ", businessKey='" + businessKey + '\'' +
                ", status=" + status +
                '}';
    }
}
