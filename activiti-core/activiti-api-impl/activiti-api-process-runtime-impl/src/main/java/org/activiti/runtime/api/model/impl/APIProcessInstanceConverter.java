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
package org.activiti.runtime.api.model.impl;

import java.util.Objects;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;

public class APIProcessInstanceConverter extends ListConverter<org.activiti.engine.runtime.ProcessInstance, ProcessInstance>
        implements ModelConverter<org.activiti.engine.runtime.ProcessInstance, ProcessInstance> {

    @Override
    public ProcessInstance from(org.activiti.engine.runtime.ProcessInstance internalProcessInstance) {
        ProcessInstanceImpl processInstance = new ProcessInstanceImpl();
        processInstance.setId(internalProcessInstance.getId());
        processInstance.setParentId(internalProcessInstance.getParentProcessInstanceId());
        processInstance.setName(internalProcessInstance.getName());
        processInstance.setProcessDefinitionId(internalProcessInstance.getProcessDefinitionId());
        processInstance.setProcessDefinitionKey(internalProcessInstance.getProcessDefinitionKey());
        processInstance.setProcessDefinitionVersion(internalProcessInstance.getProcessDefinitionVersion());
        processInstance.setInitiator(internalProcessInstance.getStartUserId());
        processInstance.setStartDate(internalProcessInstance.getStartTime());
        processInstance.setProcessDefinitionKey(internalProcessInstance.getProcessDefinitionKey());
        processInstance.setBusinessKey(internalProcessInstance.getBusinessKey());
        processInstance.setStatus(calculateStatus(internalProcessInstance));
        processInstance.setProcessDefinitionVersion(internalProcessInstance.getProcessDefinitionVersion());
        processInstance.setAppVersion(Objects.toString(internalProcessInstance.getAppVersion(), null));
        processInstance.setProcessDefinitionName(internalProcessInstance.getProcessDefinitionName());
        return processInstance;
    }

    private ProcessInstance.ProcessInstanceStatus calculateStatus(org.activiti.engine.runtime.ProcessInstance internalProcessInstance) {
        if (internalProcessInstance.isSuspended()) {
            return ProcessInstance.ProcessInstanceStatus.SUSPENDED;
        } else if (internalProcessInstance.isEnded()) {
            return ProcessInstance.ProcessInstanceStatus.COMPLETED;
        }else if(internalProcessInstance.getStartTime() == null){
            return ProcessInstance.ProcessInstanceStatus.CREATED;
        }
        return ProcessInstance.ProcessInstanceStatus.RUNNING;
    }
}
