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

package org.activiti.runtime.api.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.runtime.api.NotFoundException;
import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.model.ProcessDefinition;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.VariableInstance;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;
import org.activiti.runtime.api.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.runtime.api.model.payloads.GetProcessInstancesPayload;
import org.activiti.runtime.api.model.payloads.GetVariablesPayload;
import org.activiti.runtime.api.model.payloads.RemoveProcessVariablesPayload;
import org.activiti.runtime.api.model.payloads.ResumeProcessPayload;
import org.activiti.runtime.api.model.payloads.SetProcessVariablesPayload;
import org.activiti.runtime.api.model.payloads.SignalPayload;
import org.activiti.runtime.api.model.payloads.StartProcessPayload;
import org.activiti.runtime.api.model.payloads.SuspendProcessPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.query.impl.PageImpl;

public class ProcessRuntimeImpl implements ProcessRuntime {

    private final RepositoryService repositoryService;

    private final APIProcessDefinitionConverter processDefinitionConverter;

    private final RuntimeService runtimeService;

    private final APIProcessInstanceConverter processInstanceConverter;

    private final APIVariableInstanceConverter variableInstanceConverter;

    private final ProcessRuntimeConfiguration configuration;

    public ProcessRuntimeImpl(RepositoryService repositoryService,
                              APIProcessDefinitionConverter processDefinitionConverter,
                              RuntimeService runtimeService,
                              APIProcessInstanceConverter processInstanceConverter,
                              APIVariableInstanceConverter variableInstanceConverter,
                              ProcessRuntimeConfiguration configuration) {
        this.repositoryService = repositoryService;
        this.processDefinitionConverter = processDefinitionConverter;
        this.runtimeService = runtimeService;
        this.processInstanceConverter = processInstanceConverter;
        this.variableInstanceConverter = variableInstanceConverter;
        this.configuration = configuration;
    }

    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable) {
        return processDefinitions(pageable,
                                  null);
    }

    @Override
    public ProcessInstance processInstance(String processInstanceId) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (internalProcessInstance == null) {
            throw new NotFoundException("Unable to find process instance for the given id:'" + processInstanceId + "'");
        }
        return processInstanceConverter.from(internalProcessInstance);
    }

    @Override
    public Page<ProcessInstance> processInstances(Pageable pageable) {
        return processInstances(pageable,
                                null);
    }

    @Override
    public ProcessRuntimeConfiguration configuration() {
        return configuration;
    }

    @Override
    public ProcessInstance start(StartProcessPayload startProcessPayload) {
        return processInstanceConverter.from(runtimeService
                                                     .createProcessInstanceBuilder()
                                                     .processDefinitionId(startProcessPayload.getProcessDefinitionId())
                                                     .processDefinitionKey(startProcessPayload.getProcessDefinitionKey())
                                                     .businessKey(startProcessPayload.getBusinessKey())
                                                     .variables(startProcessPayload.getVariables())
                                                     .start());
    }

    @Override
    public ProcessInstance suspend(SuspendProcessPayload suspendProcessPayload) {
        runtimeService.suspendProcessInstanceById(suspendProcessPayload.getProcessInstanceId());
        return processInstanceConverter.from(runtimeService.createProcessInstanceQuery().processInstanceId(suspendProcessPayload.getProcessInstanceId()).singleResult());
    }

    @Override
    public ProcessInstance resume(ResumeProcessPayload resumeProcessPayload) {
        runtimeService.activateProcessInstanceById(resumeProcessPayload.getProcessInstanceId());
        return processInstanceConverter.from(runtimeService.createProcessInstanceQuery()
                                                     .processInstanceId(resumeProcessPayload.getProcessInstanceId()).singleResult());
    }

    @Override
    public ProcessInstance delete(DeleteProcessPayload deleteProcessPayload) {
        runtimeService.deleteProcessInstance(deleteProcessPayload.getProcessInstanceId(),
                                             deleteProcessPayload.getReason());
        org.activiti.engine.runtime.ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(deleteProcessPayload.getProcessInstanceId()).singleResult();
        if (processInstance != null) {
            return processInstanceConverter.from(processInstance);
        }
        return null;
    }

    @Override
    public List<VariableInstance> variables(GetVariablesPayload getVariablesPayload) {
        Map<String, org.activiti.engine.impl.persistence.entity.VariableInstance> variables = null;
        if(getVariablesPayload.isLocalOnly()) {
            variables = runtimeService.getVariableInstancesLocal(getVariablesPayload.getProcessInstanceId());
        }else{
            variables = runtimeService.getVariableInstances(getVariablesPayload.getProcessInstanceId());
        }
        return variableInstanceConverter.from(variables.values());


    }

    @Override
    public void removeVariables(RemoveProcessVariablesPayload removeProcessVariablesPayload) {
        if (removeProcessVariablesPayload.isLocalOnly()) {
            runtimeService.removeVariablesLocal(removeProcessVariablesPayload.getProcessInstanceId(),
                                                removeProcessVariablesPayload.getVariableNames());
        } else {
            runtimeService.removeVariables(removeProcessVariablesPayload.getProcessInstanceId(),
                                           removeProcessVariablesPayload.getVariableNames());
        }
    }

    @Override
    public void signal(SignalPayload signalPayload) {
        runtimeService.signalEventReceived(signalPayload.getName(),
                                           signalPayload.getVariables());
    }

    @Override
    public void setVariables(SetProcessVariablesPayload setProcessVariablesPayload) {
        if (setProcessVariablesPayload.isLocalOnly()) {
            runtimeService.setVariablesLocal(setProcessVariablesPayload.getProcessInstanceId(),
                                             setProcessVariablesPayload.getVariables());
        } else {
            runtimeService.setVariables(setProcessVariablesPayload.getProcessInstanceId(),
                                        setProcessVariablesPayload.getVariables());
        }
    }

    @Override
    public ProcessDefinition processDefinition(String processDefinitionKey) {
        org.activiti.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();

        if (processDefinition == null) {
            throw new NotFoundException("Unable to find process definition for the given key:'" + processDefinitionKey + "'");
        }
        return processDefinitionConverter.from(processDefinition);
    }

    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable,
                                                      GetProcessDefinitionsPayload getProcessDefinitionsPayload) {

        ProcessDefinitionQuery processDefinitionQuery = repositoryService
                .createProcessDefinitionQuery();
        if (getProcessDefinitionsPayload != null &&
                getProcessDefinitionsPayload.getProcessDefinitionKeys() != null &&
                !getProcessDefinitionsPayload.getProcessDefinitionKeys().isEmpty()) {
            processDefinitionQuery.processDefinitionKeys(getProcessDefinitionsPayload.getProcessDefinitionKeys());
        }
        return new PageImpl<>(processDefinitionConverter.from(processDefinitionQuery.list()),
                              Math.toIntExact(processDefinitionQuery.count()));
    }

    @Override
    public Page<ProcessInstance> processInstances(Pageable pageable,
                                                  GetProcessInstancesPayload getProcessInstancesPayload) {
        org.activiti.engine.runtime.ProcessInstanceQuery internalQuery = runtimeService.createProcessInstanceQuery();

        if (getProcessInstancesPayload != null) {
            if (getProcessInstancesPayload.getProcessDefinitionKeys() != null &&
                    !getProcessInstancesPayload.getProcessDefinitionKeys().isEmpty()) {
                internalQuery.processDefinitionKeys(getProcessInstancesPayload.getProcessDefinitionKeys());
            }
            if (getProcessInstancesPayload.getBusinessKey() != null &&
                    !getProcessInstancesPayload.getBusinessKey().isEmpty()) {
                internalQuery.processInstanceBusinessKey(getProcessInstancesPayload.getBusinessKey());
            }
        }
        return new PageImpl<>(processInstanceConverter.from(internalQuery.listPage(pageable.getStartIndex(),
                                                                                   pageable.getMaxItems())),
                              Math.toIntExact(internalQuery.count()));
    }
}
