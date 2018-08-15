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

import org.activiti.runtime.api.model.CloudProcessInstance;
import org.activiti.runtime.api.model.ProcessInstance;

public class CloudProcessInstanceImpl extends CloudRuntimeEntityImpl implements CloudProcessInstance {

    private String id;
    private String name;
    private String description;
    private Date startDate;
    private String initiator;
    private String businessKey;
    private ProcessInstanceStatus status;
    private String processDefinitionId;
    private String processDefinitionKey;

    public CloudProcessInstanceImpl() {
    }

    public CloudProcessInstanceImpl(ProcessInstance processInstance) {
        id = processInstance.getId();
        name = processInstance.getName();
        description = processInstance.getDescription();
        startDate = processInstance.getStartDate();
        initiator = processInstance.getInitiator();
        businessKey = processInstance.getBusinessKey();
        status = processInstance.getStatus();
        processDefinitionId = processInstance.getProcessDefinitionId();
        processDefinitionKey = processInstance.getProcessDefinitionKey();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    @Override
    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    @Override
    public ProcessInstanceStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessInstanceStatus status) {
        this.status = status;
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }
}
