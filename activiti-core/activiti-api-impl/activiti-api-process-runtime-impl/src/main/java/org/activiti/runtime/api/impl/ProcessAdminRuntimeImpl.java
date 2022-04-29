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
package org.activiti.runtime.api.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.DeleteProcessPayload;
import org.activiti.api.process.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.api.process.model.payloads.GetProcessInstancesPayload;
import org.activiti.api.process.model.payloads.GetVariablesPayload;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.api.process.model.payloads.RemoveProcessVariablesPayload;
import org.activiti.api.process.model.payloads.ResumeProcessPayload;
import org.activiti.api.process.model.payloads.SetProcessVariablesPayload;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.payloads.SuspendProcessPayload;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.runtime.api.query.impl.PageImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@PreAuthorize("hasRole('ACTIVITI_ADMIN')")
public class ProcessAdminRuntimeImpl implements ProcessAdminRuntime {

    private final RepositoryService repositoryService;

    private final APIProcessDefinitionConverter processDefinitionConverter;

    private final RuntimeService runtimeService;

    private final APIProcessInstanceConverter processInstanceConverter;

    private final APIVariableInstanceConverter variableInstanceConverter;

    private final ApplicationEventPublisher eventPublisher;

    private final ProcessVariablesPayloadValidator processVariablesValidator;

    public ProcessAdminRuntimeImpl(RepositoryService repositoryService,
                                   APIProcessDefinitionConverter processDefinitionConverter,
                                   RuntimeService runtimeService,
                                   APIProcessInstanceConverter processInstanceConverter,
                                   APIVariableInstanceConverter variableInstanceConverter,
                                   ApplicationEventPublisher eventPublisher,
                                   ProcessVariablesPayloadValidator processVariablesValidator) {
        this.repositoryService = repositoryService;
        this.processDefinitionConverter = processDefinitionConverter;
        this.runtimeService = runtimeService;
        this.processInstanceConverter = processInstanceConverter;
        this.variableInstanceConverter = variableInstanceConverter;
        this.eventPublisher = eventPublisher;
        this.processVariablesValidator = processVariablesValidator;
    }

    @Override
    public ProcessDefinition processDefinition(String processDefinitionId) {
        org.activiti.engine.repository.ProcessDefinition processDefinition;
        // try searching by Key if there is no matching by Id
        List<org.activiti.engine.repository.ProcessDefinition> list = repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionId)
            .deploymentIds(latestDeploymentIds())
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

    private Set<String> latestDeploymentIds() {
        return repositoryService.createDeploymentQuery()
            .latestVersion()
            .list()
            .stream()
            .map(org.activiti.engine.repository.Deployment::getId)
            .collect(Collectors.toSet());
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

        processVariablesValidator.checkStartProcessPayloadVariables(startProcessPayload, processDefinition.getId());

        return processInstanceConverter.from(runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(processDefinition.getId())
                .processDefinitionKey(processDefinition.getKey())
                .businessKey(startProcessPayload.getBusinessKey())
                .variables(startProcessPayload.getVariables())
                .name(startProcessPayload.getName())
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
            if (getProcessInstancesPayload.getParentProcessInstanceId() != null) {
                internalQuery.superProcessInstanceId(getProcessInstancesPayload.getParentProcessInstanceId());
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
            processInstance.setStatus(ProcessInstance.ProcessInstanceStatus.CANCELLED);
            return processInstance;
        }
        return null;
    }

    @Override
    @Transactional
    public void signal(SignalPayload signalPayload) {
        processVariablesValidator.checkSignalPayloadVariables(signalPayload,
                                                              null);

        eventPublisher.publishEvent(signalPayload);
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
    public ProcessInstance update(UpdateProcessPayload updateProcessPayload) {
        if (updateProcessPayload.getBusinessKey() != null) {
            runtimeService.updateBusinessKey(updateProcessPayload.getProcessInstanceId(), updateProcessPayload.getBusinessKey());
        }
        if (updateProcessPayload.getName() != null) {
            runtimeService.setProcessInstanceName(updateProcessPayload.getProcessInstanceId(), updateProcessPayload.getName());
        }
        return processInstanceConverter.from(runtimeService.createProcessInstanceQuery()
                .processInstanceId(updateProcessPayload.getProcessInstanceId()).singleResult());
    }

    @Override
    public void setVariables(SetProcessVariablesPayload setProcessVariablesPayload) {
        ProcessInstanceImpl processInstance = (ProcessInstanceImpl) processInstance(setProcessVariablesPayload.getProcessInstanceId());

        processVariablesValidator.checkPayloadVariables(setProcessVariablesPayload,
                                                        processInstance.getProcessDefinitionId());

        runtimeService.setVariables(setProcessVariablesPayload.getProcessInstanceId(),
                setProcessVariablesPayload.getVariables());

    }

    @Override
    public List<VariableInstance> variables(GetVariablesPayload getVariablesPayload) {
        processInstance(getVariablesPayload.getProcessInstanceId());

        Map<String, org.activiti.engine.impl.persistence.entity.VariableInstance> variables;
        variables = runtimeService.getVariableInstances(getVariablesPayload.getProcessInstanceId());

        return variableInstanceConverter.from(variables.values());
    }

    @Override
    public void removeVariables(RemoveProcessVariablesPayload removeProcessVariablesPayload) {
        runtimeService.removeVariables(removeProcessVariablesPayload.getProcessInstanceId(),
                removeProcessVariablesPayload.getVariableNames());
    }

    @Override
    @Transactional
    public void receive(ReceiveMessagePayload messagePayload) {
        processVariablesValidator.checkReceiveMessagePayloadVariables(messagePayload,
                                                                      null);
        eventPublisher.publishEvent(messagePayload);
    }

    @Override
    public ProcessInstance start(StartMessagePayload messagePayload) {
        String messageName = messagePayload.getName();
        String businessKey = messagePayload.getBusinessKey();
        Map<String, Object> variables = messagePayload.getVariables();

        processVariablesValidator.checkStartMessagePayloadVariables(messagePayload,
                                                                    null);

        ProcessInstance processInstance = processInstanceConverter.from(runtimeService.startProcessInstanceByMessage(messageName,
                                                                                                                     businessKey,
                                                                                                                     variables));
        return processInstance;
    }

}
