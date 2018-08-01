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

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.runtime.api.*;
import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.model.*;
import org.activiti.runtime.api.model.impl.*;
import org.activiti.runtime.api.model.payloads.*;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.query.impl.PageImpl;
import org.activiti.runtime.api.security.SecurityManager;
import org.activiti.spring.security.policies.ActivitiForbiddenException;
import org.activiti.spring.security.policies.SecurityPoliciesManager;
import org.activiti.spring.security.policies.SecurityPolicy;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@PreAuthorize("hasRole('ROLE:ACTIVITI_ADMIN')")
public class ProcessAdminRuntimeImpl implements ProcessAdminRuntime {

    private final RepositoryService repositoryService;

    private final APIProcessDefinitionConverter processDefinitionConverter;

    private final RuntimeService runtimeService;

    private final APIProcessInstanceConverter processInstanceConverter;

    private final APIVariableInstanceConverter variableInstanceConverter;

    private final ProcessRuntimeConfiguration configuration;

    private final UserGroupManager userGroupManager;

    private final SecurityManager securityManager;

    private final SecurityPoliciesManager securityPoliciesManager;

    public ProcessAdminRuntimeImpl(RepositoryService repositoryService,
                                   APIProcessDefinitionConverter processDefinitionConverter,
                                   RuntimeService runtimeService,
                                   UserGroupManager userGroupManager,
                                   SecurityManager securityManager,
                                   SecurityPoliciesManager securityPoliciesManager,
                                   APIProcessInstanceConverter processInstanceConverter,
                                   APIVariableInstanceConverter variableInstanceConverter,
                                   ProcessRuntimeConfiguration configuration) {
        this.repositoryService = repositoryService;
        this.processDefinitionConverter = processDefinitionConverter;
        this.runtimeService = runtimeService;
        this.userGroupManager = userGroupManager;
        this.securityManager = securityManager;
        this.securityPoliciesManager = securityPoliciesManager;
        this.processInstanceConverter = processInstanceConverter;
        this.variableInstanceConverter = variableInstanceConverter;
        this.configuration = configuration;
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
        if (!securityPoliciesManager.canRead(internalProcessInstance.getProcessDefinitionKey())) {
            throw new ActivitiObjectNotFoundException("You cannot read the process instance with Id:'"
                    + processInstanceId + "' due to security policies violation");
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


}
