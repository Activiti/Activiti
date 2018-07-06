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
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.runtime.api.NotFoundException;
import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.model.FluentProcessDefinition;
import org.activiti.runtime.api.model.FluentProcessInstance;
import org.activiti.runtime.api.model.builder.SignalPayload;
import org.activiti.runtime.api.model.builder.impl.SignalPayloadImpl;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.query.ProcessDefinitionFilter;
import org.activiti.runtime.api.query.ProcessInstanceFilter;
import org.activiti.runtime.api.query.impl.PageImpl;

public class  ProcessRuntimeImpl implements ProcessRuntime {

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
    public Page<FluentProcessDefinition> processDefinitions(Pageable pageable) {
        return processDefinitions(pageable, null);
    }

    @Override
    public Page<FluentProcessDefinition> processDefinitions(Pageable pageable,
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
    public FluentProcessDefinition processDefinitionByKey(String processDefinitionKey) {
        List<org.activiti.engine.repository.ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).list();

        if (processDefinitions == null || processDefinitions.isEmpty()) {
            throw new NotFoundException("Unable to find process definition for the given key:'" + processDefinitionKey + "'");
        }
        return processDefinitionConverter.from(processDefinitions.get(0));
    }

   @Override
   public FluentProcessDefinition processDefinitionById(String processDefinitionId) {
        org.activiti.engine.repository.ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinitionId);
       if (definition == null) {
           throw new NotFoundException("Unable to find process definition for the given id:'" + processDefinitionId + "'");
       }
       return processDefinitionConverter.from(definition);
   }


    @Override
    public FluentProcessInstance processInstance(String processInstanceId) {
        ProcessInstance internalProcessInstance = runtimeService
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
    public Page<FluentProcessInstance> processInstances(Pageable pageable) {
        return processInstances(pageable,
                                null);
    }

    @Override
    public Page<FluentProcessInstance> processInstances(Pageable pageable,
                                                  ProcessInstanceFilter filter) {
        org.activiti.engine.runtime.ProcessInstanceQuery internalQuery = runtimeService.createProcessInstanceQuery();
        if (filter != null && filter.hasProcessDefinitionKeys()) {
            internalQuery.processDefinitionKeys(filter.getProcessDefinitionKeys());
        }
        return new PageImpl<>(processInstanceConverter.from(internalQuery.listPage(pageable.getStartIndex(),
                                                                                   pageable.getMaxItems())),
                              Math.toIntExact(internalQuery.count()));
    }

    @Override
    public SignalPayload sendSignalWith() {
        return new SignalPayloadImpl(runtimeService);
    }

    @Override
    public void sendSignal(String name) {
        sendSignalWith().name(name).doIt();
    }

    @Override
    public ProcessRuntimeConfiguration configuration() {
        return configuration;
    }
}
