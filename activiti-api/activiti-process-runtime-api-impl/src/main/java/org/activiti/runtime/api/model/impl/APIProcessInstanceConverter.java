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

import org.activiti.engine.RuntimeService;
import org.activiti.runtime.api.model.FluentProcessInstance;
import org.activiti.runtime.api.model.ProcessInstance;

public class APIProcessInstanceConverter extends ListConverter<org.activiti.engine.runtime.ProcessInstance, FluentProcessInstance>
        implements ModelConverter<org.activiti.engine.runtime.ProcessInstance, FluentProcessInstance> {

    private final RuntimeService runtimeService;
    private final APIVariableInstanceConverter variableInstanceConverter;

    public APIProcessInstanceConverter(RuntimeService runtimeService,
                                       APIVariableInstanceConverter variableInstanceConverter) {
        this.runtimeService = runtimeService;
        this.variableInstanceConverter = variableInstanceConverter;
    }

    @Override
    public FluentProcessInstance from(org.activiti.engine.runtime.ProcessInstance internalProcessInstance) {
        FluentProcessInstanceImpl processInstance = new FluentProcessInstanceImpl(runtimeService,
                                                                             variableInstanceConverter);
        processInstance.setId(internalProcessInstance.getId());
        processInstance.setName(internalProcessInstance.getName());
        processInstance.setDescription(internalProcessInstance.getDescription());
        processInstance.setProcessDefinitionId(internalProcessInstance.getProcessDefinitionId());
        processInstance.setInitiator(internalProcessInstance.getStartUserId());
        processInstance.setStartDate(internalProcessInstance.getStartTime());
        processInstance.setProcessDefinitionKey(internalProcessInstance.getProcessDefinitionKey());
        processInstance.setBusinessKey(internalProcessInstance.getBusinessKey());
        processInstance.setStatus(calculateStatus(internalProcessInstance));
        return processInstance;
    }

    private ProcessInstance.ProcessInstanceStatus calculateStatus(org.activiti.engine.runtime.ProcessInstance internalProcessInstance) {
        if (internalProcessInstance.isSuspended()) {
            return org.activiti.runtime.api.model.ProcessInstance.ProcessInstanceStatus.SUSPENDED;
        } else if (internalProcessInstance.isEnded()) {
            return org.activiti.runtime.api.model.ProcessInstance.ProcessInstanceStatus.COMPLETED;
        }
        return org.activiti.runtime.api.model.ProcessInstance.ProcessInstanceStatus.RUNNING;
    }
}
