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
import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.model.ProcessDefinition;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.VariableInstance;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;
import org.activiti.runtime.api.model.payloads.GetVariablesPayload;
import org.activiti.runtime.api.model.payloads.RemoveVariablesPayload;
import org.activiti.runtime.api.model.payloads.ResumeProcessPayload;
import org.activiti.runtime.api.model.payloads.SetVariablesPayload;
import org.activiti.runtime.api.model.payloads.SignalPayload;
import org.activiti.runtime.api.model.payloads.StartProcessPayload;
import org.activiti.runtime.api.model.payloads.SuspendProcessPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.query.ProcessDefinitionFilter;
import org.activiti.runtime.api.query.ProcessInstanceFilter;
import org.activiti.runtime.api.query.impl.PageImpl;

public class ProcessRuntimeImpl implements ProcessRuntime {

    private final RepositoryService repositoryService;

    private final APIProcessDefinitionConverter processDefinitionConverter;

    private final RuntimeService runtimeService;

    private final APIProcessInstanceConverter processInstanceConverter;

    private final ProcessRuntimeConfiguration configuration;

    public ProcessRuntimeImpl(RepositoryService repositoryService,
                              APIProcessDefinitionConverter processDefinitionConverter,
                              RuntimeService runtimeService,
                              APIProcessInstanceConverter processInstanceConverter,
                              ProcessRuntimeConfiguration configuration) {
        this.repositoryService = repositoryService;
        this.processDefinitionConverter = processDefinitionConverter;
        this.runtimeService = runtimeService;
        this.processInstanceConverter = processInstanceConverter;
        this.configuration = configuration;
    }

    @Override
    public Page<ProcessDefinition> processDefinitions() {
        return processDefinitions(Pageable.of(0,
                                              configuration.maxPagedResults()),
                                  null);
    }

    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable) {
        return processDefinitions(pageable,
                                  null);
    }

    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable,
                                                      ProcessDefinitionFilter filter) {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService
                .createProcessDefinitionQuery();
        if (filter != null && filter.hasProcessDefinitionKeys()) {
            processDefinitionQuery.processDefinitionKeys(filter.getProcessDefinitionKeys());
        }
        return new PageImpl<>(processDefinitionConverter.from(processDefinitionQuery.list()),
                              Math.toIntExact(processDefinitionQuery.count()));
    }

    @Override
    public ProcessDefinition processDefinitionByKey(String processDefinitionKey) {
        List<org.activiti.engine.repository.ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).list();

        if (processDefinitions == null || processDefinitions.isEmpty()) {
            throw new NotFoundException("Unable to find process definition for the given key:'" + processDefinitionKey + "'");
        }
        return processDefinitionConverter.from(processDefinitions.get(0));
    }

    @Override
    public ProcessDefinition processDefinitionById(String processDefinitionId) {
        org.activiti.engine.repository.ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinitionId);
        if (definition == null) {
            throw new NotFoundException("Unable to find process definition for the given id:'" + processDefinitionId + "'");
        }
        return processDefinitionConverter.from(definition);
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
        return processInstanceConverter.from(
                internalProcessInstance);
    }

    @Override
    public Page<ProcessInstance> processInstances(Pageable pageable) {
        return processInstances(pageable,
                                null);
    }

    @Override
    public Page<ProcessInstance> processInstances(Pageable pageable,
                                                  ProcessInstanceFilter filter) {
        org.activiti.engine.runtime.ProcessInstanceQuery internalQuery = runtimeService.createProcessInstanceQuery();
        if (filter != null && filter.hasProcessDefinitionKeys()) {
            internalQuery.processDefinitionKeys(filter.getProcessDefinitionKeys());
        }
        return new PageImpl<>(processInstanceConverter.from(internalQuery.listPage(pageable.getStartIndex(),
                                                                                   pageable.getMaxItems())),
                              Math.toIntExact(internalQuery.count()));
    }

//    @Override
//    public SignalPayload sendSignalWith() {
//        return new SignalPayloadImpl(runtimeService);
//    }
//
//    @Override
//    public void sendSignal(String name) {
//        sendSignalWith().name(name).doIt();
//    }

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
        if(processInstance != null) {
            return processInstanceConverter.from(processInstance);
        }
        return null;
    }

    @Override
    public Page<VariableInstance> variables(GetVariablesPayload getVariablesPayload) {
        return null; // need to implement the paged ones
    }

    @Override
    public void removeVariables(RemoveVariablesPayload removeVariablesPayload) {
        if (removeVariablesPayload.isLocalOnly()) {
            runtimeService.removeVariablesLocal(removeVariablesPayload.getProcessInstanceId(),
                                                removeVariablesPayload.getVariableNames());
        } else {
            runtimeService.removeVariables(removeVariablesPayload.getProcessInstanceId(),
                                           removeVariablesPayload.getVariableNames());
        }
    }

    @Override
    public void signal(SignalPayload signalPayload) {
        runtimeService.signalEventReceived(signalPayload.getName(),
                                           signalPayload.getVariables());
    }

    @Override
    public void setVariables(SetVariablesPayload setVariablesPayload) {
        if(setVariablesPayload.isLocalOnly()){
            runtimeService.setVariablesLocal(setVariablesPayload.getProcessInstanceId(),
                                        setVariablesPayload.getVariables());
        }else {
            runtimeService.setVariables(setVariablesPayload.getProcessInstanceId(),
                                        setVariablesPayload.getVariables());
        }
    }
}
