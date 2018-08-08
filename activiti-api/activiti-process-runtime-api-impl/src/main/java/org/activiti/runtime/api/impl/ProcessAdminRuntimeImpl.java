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

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.runtime.api.NotFoundException;
import org.activiti.runtime.api.ProcessAdminRuntime;
import org.activiti.runtime.api.model.ProcessDefinition;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.builders.ProcessPayloadBuilder;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.ProcessInstanceImpl;
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;
import org.activiti.runtime.api.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.runtime.api.model.payloads.GetProcessInstancesPayload;
import org.activiti.runtime.api.model.payloads.RemoveProcessVariablesPayload;
import org.activiti.runtime.api.model.payloads.ResumeProcessPayload;
import org.activiti.runtime.api.model.payloads.SetProcessVariablesPayload;
import org.activiti.runtime.api.model.payloads.SignalPayload;
import org.activiti.runtime.api.model.payloads.StartProcessPayload;
import org.activiti.runtime.api.model.payloads.SuspendProcessPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.query.impl.PageImpl;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('ACTIVITI_ADMIN')")
public class ProcessAdminRuntimeImpl implements ProcessAdminRuntime {

    private final RepositoryService repositoryService;

    private final APIProcessDefinitionConverter processDefinitionConverter;

    private final RuntimeService runtimeService;

    private final APIProcessInstanceConverter processInstanceConverter;

    public ProcessAdminRuntimeImpl(RepositoryService repositoryService,
                                   APIProcessDefinitionConverter processDefinitionConverter,
                                   RuntimeService runtimeService,
                                   APIProcessInstanceConverter processInstanceConverter) {
        this.repositoryService = repositoryService;
        this.processDefinitionConverter = processDefinitionConverter;
        this.runtimeService = runtimeService;
        this.processInstanceConverter = processInstanceConverter;
    }


    @Override
    public ProcessDefinition processDefinition(String processDefinitionId) {
        org.activiti.engine.repository.ProcessDefinition processDefinition;
        // try searching by Key if there is no matching by Id
        List<org.activiti.engine.repository.ProcessDefinition> list = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionId)
                .orderByProcessDefinitionVersion()
                .asc()
                .list();
        if (!list.isEmpty()) {
            processDefinition = list.get(0);
        } else {
            processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
        }
        return processDefinitionConverter.from(processDefinition);
    }


    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable) {
        return processDefinitions(pageable,
                ProcessPayloadBuilder.processDefinitions().build());
    }

    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable,
                                                      GetProcessDefinitionsPayload getProcessDefinitionsPayload) {
        if (getProcessDefinitionsPayload == null) {
            throw new IllegalStateException("payload cannot be null");
        }
        ProcessDefinitionQuery processDefinitionQuery = repositoryService
                .createProcessDefinitionQuery();
        if (getProcessDefinitionsPayload.hasDefinitionKeys()) {
            processDefinitionQuery.processDefinitionKeys(getProcessDefinitionsPayload.getProcessDefinitionKeys());
        }
        return new PageImpl<>(processDefinitionConverter.from(processDefinitionQuery.list()),
                Math.toIntExact(processDefinitionQuery.count()));
    }


    @Override
    public ProcessInstance start(StartProcessPayload startProcessPayload) {
        ProcessDefinition processDefinition = null;
        if (startProcessPayload.getProcessDefinitionId() != null) {
            processDefinition = processDefinition(startProcessPayload.getProcessDefinitionId());
        }
        if (processDefinition == null && startProcessPayload.getProcessDefinitionKey() != null) {
            processDefinition = processDefinition(startProcessPayload.getProcessDefinitionKey());
        }
        if (processDefinition == null) {
            throw new IllegalStateException("At least Process Definition Id or Key needs to be provided to start a process");
        }
        return processInstanceConverter.from(runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(startProcessPayload.getProcessDefinitionId())
                .processDefinitionKey(startProcessPayload.getProcessDefinitionKey())
                .businessKey(startProcessPayload.getBusinessKey())
                .variables(startProcessPayload.getVariables())
                .name(startProcessPayload.getProcessInstanceName())
                .start());
    }


    @Override
    public Page<ProcessInstance> processInstances(Pageable pageable) {
        return processInstances(pageable,
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

            if (getProcessInstancesPayload.isSuspendedOnly()) {
                internalQuery.suspended();
            }

            if (getProcessInstancesPayload.isActiveOnly()) {
                internalQuery.active();
            }

        }
        return new PageImpl<>(processInstanceConverter.from(internalQuery.listPage(pageable.getStartIndex(),
                pageable.getMaxItems())),
                Math.toIntExact(internalQuery.count()));

    }

    @Override
    public ProcessInstance delete(DeleteProcessPayload deleteProcessPayload) {
        ProcessInstanceImpl processInstance = (ProcessInstanceImpl) processInstance(deleteProcessPayload.getProcessInstanceId());
        runtimeService.deleteProcessInstance(deleteProcessPayload.getProcessInstanceId(),
                deleteProcessPayload.getReason());
        if (processInstance != null) {
            processInstance.setStatus(ProcessInstance.ProcessInstanceStatus.DELETED);
            return processInstance;
        }
        return null;
    }

    @Override
    public void signal(SignalPayload signalPayload) {
        //@TODO: define security policies for signalling
        runtimeService.signalEventReceived(signalPayload.getName(),
                signalPayload.getVariables());
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
    public void removeVariables(RemoveProcessVariablesPayload removeProcessVariablesPayload) {
        if (removeProcessVariablesPayload.isLocalOnly()) {
            runtimeService.removeVariablesLocal(removeProcessVariablesPayload.getProcessInstanceId(),
                    removeProcessVariablesPayload.getVariableNames());
        } else {
            runtimeService.removeVariables(removeProcessVariablesPayload.getProcessInstanceId(),
                    removeProcessVariablesPayload.getVariableNames());
        }
    }

}
