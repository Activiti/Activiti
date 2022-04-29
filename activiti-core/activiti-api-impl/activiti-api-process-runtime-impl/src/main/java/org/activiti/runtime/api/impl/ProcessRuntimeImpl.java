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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.Deployment;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessDefinitionMeta;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.ProcessInstanceMeta;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.CreateProcessInstancePayload;
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
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.model.impl.ProcessDefinitionMetaImpl;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.api.runtime.model.impl.ProcessInstanceMetaImpl;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.UnprocessableEntityException;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.core.common.spring.security.policies.ActivitiForbiddenException;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.core.common.spring.security.policies.SecurityPolicyAccess;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.model.impl.APIDeploymentConverter;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.runtime.api.query.impl.PageImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@PreAuthorize("hasRole('ACTIVITI_USER')")
public class ProcessRuntimeImpl implements ProcessRuntime {

    private final RepositoryService repositoryService;

    private final APIProcessDefinitionConverter processDefinitionConverter;

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final APIProcessInstanceConverter processInstanceConverter;

    private final APIVariableInstanceConverter variableInstanceConverter;

    private final APIDeploymentConverter deploymentConverter;

    private final ProcessRuntimeConfiguration configuration;

    private final ProcessSecurityPoliciesManager securityPoliciesManager;

    private final ApplicationEventPublisher eventPublisher;

    private final ProcessVariablesPayloadValidator processVariablesValidator;

    private final SecurityManager securityManager;

    public ProcessRuntimeImpl(RepositoryService repositoryService,
                              APIProcessDefinitionConverter processDefinitionConverter,
                              RuntimeService runtimeService,
                              TaskService taskService,
                              ProcessSecurityPoliciesManager securityPoliciesManager,
                              APIProcessInstanceConverter processInstanceConverter,
                              APIVariableInstanceConverter variableInstanceConverter,
                              APIDeploymentConverter deploymentConverter,
                              ProcessRuntimeConfiguration configuration,
                              ApplicationEventPublisher eventPublisher,
                              ProcessVariablesPayloadValidator processVariablesValidator,
                              SecurityManager securityManager) {
        this.repositoryService = repositoryService;
        this.processDefinitionConverter = processDefinitionConverter;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.securityPoliciesManager = securityPoliciesManager;
        this.processInstanceConverter = processInstanceConverter;
        this.variableInstanceConverter = variableInstanceConverter;
        this.deploymentConverter = deploymentConverter;
        this.configuration = configuration;
        this.eventPublisher = eventPublisher;
        this.processVariablesValidator = processVariablesValidator;
        this.securityManager = securityManager;
    }

    @Override
    public ProcessDefinition processDefinition(String processDefinitionId) {
        org.activiti.engine.repository.ProcessDefinition processDefinition;
        // try searching by Key if there is no matching by Id
        processDefinition = findLatestProcessDefinitionByKey(processDefinitionId)
            .orElseGet(() -> repositoryService.getProcessDefinition(processDefinitionId));

        checkProcessDefinitionBelongsToLatestDeployment(processDefinition);

        if (!securityPoliciesManager.canRead(processDefinition.getKey())) {
            throw new ActivitiObjectNotFoundException("Unable to find process definition for the given id:'" + processDefinitionId + "'");
        }
        return processDefinitionConverter.from(processDefinition);
    }

    private Optional<org.activiti.engine.repository.ProcessDefinition> findLatestProcessDefinitionByKey(String processDefinitionKey) {
        return repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .deploymentIds(latestDeploymentIds())
            .processDefinitionKey(processDefinitionKey)
            .orderByProcessDefinitionAppVersion()
            .desc()
            .list()
            .stream()
            .findFirst();
    }

    private Set<String> latestDeploymentIds() {
        return repositoryService.createDeploymentQuery()
                                .latestVersion()
                                .list()
                                .stream()
                                .map(org.activiti.engine.repository.Deployment::getId)
                                .collect(Collectors.toSet());
    }

    private void checkProcessDefinitionBelongsToLatestDeployment(org.activiti.engine.repository.ProcessDefinition processDefinition) {
        Integer appVersion = processDefinition.getAppVersion();

        if (appVersion != null && !selectLatestDeployment().getVersion().equals(appVersion)) {
            throw new UnprocessableEntityException("Process definition with the given id:'" + processDefinition.getId() + "' belongs to a different application version.");
        }
    }

    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable) {
        return processDefinitions(pageable, ProcessPayloadBuilder.processDefinitions().build(), List.of());
    }

    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable, List<String> include) {
        return processDefinitions(pageable, ProcessPayloadBuilder.processDefinitions().build(), include);
    }

    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable,
                                                      GetProcessDefinitionsPayload getProcessDefinitionsPayload) {
        return processDefinitions(pageable, getProcessDefinitionsPayload, List.of());
    }

    @Override
    public Page<ProcessDefinition> processDefinitions(Pageable pageable,
                                                      GetProcessDefinitionsPayload getProcessDefinitionsPayload,
                                                      List<String> include) {
        if (getProcessDefinitionsPayload == null) {
            throw new IllegalStateException("payload cannot be null");
        }
        GetProcessDefinitionsPayload securityKeysInPayload = securityPoliciesManager.restrictProcessDefQuery(SecurityPolicyAccess.READ);
        // If the security policies keys are not empty it means that I will need to use them to filter results,
        //   else ignore and use the user provided ones.
        if (!securityKeysInPayload.getProcessDefinitionKeys().isEmpty()) {
            getProcessDefinitionsPayload.setProcessDefinitionKeys(securityKeysInPayload.getProcessDefinitionKeys());
        }

        ProcessDefinitionQuery processDefinitionQuery = repositoryService
                .createProcessDefinitionQuery()
                .latestVersion()
                .deploymentIds(latestDeploymentIds());

        if (getProcessDefinitionsPayload.hasDefinitionKeys()) {
            processDefinitionQuery.processDefinitionKeys(getProcessDefinitionsPayload.getProcessDefinitionKeys());
        }

        return new PageImpl<>(processDefinitionConverter.from(processDefinitionQuery.list()),
                              Math.toIntExact(processDefinitionQuery.count()));
    }

    @Override
    public ProcessInstance processInstance(String processInstanceId) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = internalProcessInstance(processInstanceId);

        if (!canReadProcessInstance(internalProcessInstance)) {
            throw new ActivitiObjectNotFoundException("You cannot read the process instance with Id:'" + processInstanceId + "' due to security policies violation");
        }
        return processInstanceConverter.from(internalProcessInstance);
    }

    @Override
    public Page<ProcessInstance> processInstances(Pageable pageable) {
        return processInstances(pageable,
                ProcessPayloadBuilder.processInstances().build());
    }

    @Override
    public Page<ProcessInstance> processInstances(Pageable pageable,
                                                  GetProcessInstancesPayload getProcessInstancesPayload) {
        if (getProcessInstancesPayload == null) {
            throw new IllegalStateException("payload cannot be null");
        }
        GetProcessInstancesPayload securityKeysInPayload = securityPoliciesManager.restrictProcessInstQuery(SecurityPolicyAccess.READ);

        org.activiti.engine.runtime.ProcessInstanceQuery internalQuery = runtimeService.createProcessInstanceQuery();

        String currentUserId = securityManager.getAuthenticatedUserId();
        internalQuery.involvedUser(currentUserId);

        if (!securityKeysInPayload.getProcessDefinitionKeys().isEmpty()) {
            getProcessInstancesPayload.setProcessDefinitionKeys(securityKeysInPayload.getProcessDefinitionKeys());
        }
        if (getProcessInstancesPayload.getProcessDefinitionKeys() != null && !getProcessInstancesPayload.getProcessDefinitionKeys().isEmpty()) {
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

        return new PageImpl<>(processInstanceConverter.from(internalQuery.listPage(pageable.getStartIndex(),
                pageable.getMaxItems())),
                Math.toIntExact(internalQuery.count()));
    }

    @Override
    public ProcessRuntimeConfiguration configuration() {
        return configuration;
    }

    @Override
    public ProcessInstance start(StartProcessPayload startProcessPayload) {
        return processInstanceConverter.from(this.createProcessInstanceBuilder(startProcessPayload).start());
    }

    @Override
    public ProcessInstance startCreatedProcess(String processInstanceId, StartProcessPayload startProcessPayload) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = internalProcessInstance(processInstanceId);

        if (internalProcessInstance == null) {
            throw new NotFoundException("Unable to find process instance for the given id:'" + processInstanceId + "'");
        }

        if (!canWriteProcessInstance(internalProcessInstance)) {
            throw new ActivitiObjectNotFoundException("You cannot start the process instance with Id:'" + processInstanceId + "' due to security policies violation");
        }
       processVariablesValidator.checkStartProcessPayloadVariables(startProcessPayload, internalProcessInstance.getProcessDefinitionId());
       return processInstanceConverter.from(runtimeService.startCreatedProcessInstance(internalProcessInstance, startProcessPayload.getVariables()));
    }

    @Override
    public ProcessInstance create(CreateProcessInstancePayload createProcessInstancePayload) {
        return processInstanceConverter.from(createProcessInstanceBuilder(createProcessInstancePayload).create());
    }

    private ProcessInstanceBuilder createProcessInstanceBuilder(StartProcessPayload startProcessPayload) {
        ProcessDefinition processDefinition = getProcessDefinitionAndCheckUserHasRights(startProcessPayload.getProcessDefinitionId(),
            startProcessPayload.getProcessDefinitionKey());

        processVariablesValidator.checkStartProcessPayloadVariables(startProcessPayload, processDefinition.getId());

        return runtimeService
            .createProcessInstanceBuilder()
            .processDefinitionId(processDefinition.getId())
            .processDefinitionKey(processDefinition.getKey())
            .businessKey(startProcessPayload.getBusinessKey())
            .variables(startProcessPayload.getVariables())
            .name(startProcessPayload.getName());
    }

    private ProcessInstanceBuilder createProcessInstanceBuilder(CreateProcessInstancePayload createProcessPayload) {
        ProcessDefinition processDefinition = getProcessDefinitionAndCheckUserHasRights(createProcessPayload.getProcessDefinitionId(),
            createProcessPayload.getProcessDefinitionKey());

        return runtimeService
            .createProcessInstanceBuilder()
            .processDefinitionId(processDefinition.getId())
            .processDefinitionKey(processDefinition.getKey())
            .businessKey(createProcessPayload.getBusinessKey())
            .name(createProcessPayload.getName());
    }

    @Override
    public ProcessInstance suspend(SuspendProcessPayload suspendProcessPayload) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = internalProcessInstance(suspendProcessPayload.getProcessInstanceId());

        checkUserCanWritePermissionOnProcessInstance(internalProcessInstance);

        runtimeService.suspendProcessInstanceById(suspendProcessPayload.getProcessInstanceId());
        return processInstanceConverter.from(runtimeService.createProcessInstanceQuery().processInstanceId(suspendProcessPayload.getProcessInstanceId()).singleResult());
    }

    @Override
    public ProcessInstance resume(ResumeProcessPayload resumeProcessPayload) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = internalProcessInstance(resumeProcessPayload.getProcessInstanceId());

        checkUserCanWritePermissionOnProcessInstance(internalProcessInstance);

        runtimeService.activateProcessInstanceById(resumeProcessPayload.getProcessInstanceId());
        return processInstanceConverter.from(runtimeService.createProcessInstanceQuery()
                .processInstanceId(resumeProcessPayload.getProcessInstanceId()).singleResult());
    }

    @Override
    public ProcessInstance delete(DeleteProcessPayload deleteProcessPayload) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = internalProcessInstance(deleteProcessPayload.getProcessInstanceId());

        checkUserCanWritePermissionOnProcessInstance(internalProcessInstance);

        runtimeService.deleteProcessInstance(deleteProcessPayload.getProcessInstanceId(),
                deleteProcessPayload.getReason());

        ProcessInstanceImpl processInstance = (ProcessInstanceImpl) processInstanceConverter.from(internalProcessInstance);
        processInstance.setStatus(ProcessInstance.ProcessInstanceStatus.CANCELLED);
        return processInstance;
    }

    @Override
    public List<VariableInstance> variables(GetVariablesPayload getVariablesPayload) {
        //Process Instance will check security policies on read
        processInstance(getVariablesPayload.getProcessInstanceId());

        Map<String, org.activiti.engine.impl.persistence.entity.VariableInstance> variables;
        variables = runtimeService.getVariableInstances(getVariablesPayload.getProcessInstanceId());

        return variableInstanceConverter.from(variables.values());
    }

    @Override
    public void removeVariables(RemoveProcessVariablesPayload removeProcessVariablesPayload) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = internalProcessInstance(removeProcessVariablesPayload.getProcessInstanceId());

        checkUserCanWritePermissionOnProcessInstance(internalProcessInstance);

        runtimeService.removeVariables(removeProcessVariablesPayload.getProcessInstanceId(),
                removeProcessVariablesPayload.getVariableNames());

    }

    @Override
    public void setVariables(SetProcessVariablesPayload setProcessVariablesPayload) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = internalProcessInstance(setProcessVariablesPayload.getProcessInstanceId());

        checkUserCanWritePermissionOnProcessInstance(internalProcessInstance);

        processVariablesValidator.checkPayloadVariables(setProcessVariablesPayload,
            internalProcessInstance.getProcessDefinitionId());

        runtimeService.setVariables(setProcessVariablesPayload.getProcessInstanceId(),
                setProcessVariablesPayload.getVariables());

    }

    @Override
    @Transactional
    public void signal(SignalPayload signalPayload) {
        //@TODO: define security policies for signalling

        processVariablesValidator.checkSignalPayloadVariables(signalPayload,
                                                              null);

        eventPublisher.publishEvent(signalPayload);
    }

    @Override
    public ProcessDefinitionMeta processDefinitionMeta(String processDefinitionKey) {
        //Process Definition will check security policies on read
        processDefinition(processDefinitionKey);
        return new ProcessDefinitionMetaImpl(processDefinitionKey);
    }

    @Override
    public ProcessInstanceMeta processInstanceMeta(String processInstanceId) {
        //Process Instance will check security policies on read
        processInstance(processInstanceId);
        ProcessInstanceMetaImpl processInstanceMeta = new ProcessInstanceMetaImpl(processInstanceId);
        processInstanceMeta.setActiveActivitiesIds(runtimeService.getActiveActivityIds(processInstanceId));
        return processInstanceMeta;
    }

    @Override
    public ProcessInstance update(UpdateProcessPayload updateProcessPayload) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = internalProcessInstance(updateProcessPayload.getProcessInstanceId());

        checkUserCanWritePermissionOnProcessInstance(internalProcessInstance);

        if (updateProcessPayload.getBusinessKey() != null) {
            runtimeService.updateBusinessKey(updateProcessPayload.getProcessInstanceId(), updateProcessPayload.getBusinessKey());
        }
        if (updateProcessPayload.getName() != null) {
            runtimeService.setProcessInstanceName(updateProcessPayload.getProcessInstanceId(), updateProcessPayload.getName());
        }
        ProcessInstance updatedProcessInstance = processInstanceConverter.from(runtimeService.createProcessInstanceQuery()
                .processInstanceId(updateProcessPayload.getProcessInstanceId())
                .singleResult());

        return updatedProcessInstance;

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

    private void checkUserCanWritePermissionOnProcessDefinition(String processDefinitionKey) {

        if (!securityPoliciesManager.canWrite(processDefinitionKey)) {
            throw new ActivitiForbiddenException("Operation not permitted for " + processDefinitionKey + " due security policy violation");
        }
    }

    private void checkUserCanWritePermissionOnProcessInstance(org.activiti.engine.runtime.ProcessInstance processInstance) {

        if (!canWriteProcessInstance(processInstance)) {
            throw new ActivitiForbiddenException("Operation not permitted for on process instance " + processInstance.getProcessInstanceId() + " due security policy violation");
        }
    }

    protected ProcessDefinition getProcessDefinitionAndCheckUserHasRights(String processDefinitionId, String processDefinitionKey) {

        String checkId = processDefinitionKey != null ? processDefinitionKey : processDefinitionId;

        ProcessDefinition processDefinition = processDefinition(checkId);

        if (processDefinition == null) {
            throw new IllegalStateException("At least Process Definition Id or Key needs to be provided to start a process");
        }

        checkUserCanWritePermissionOnProcessDefinition(processDefinition.getKey());

        return processDefinition;
    }

    @Override
    public Deployment selectLatestDeployment(){
        return deploymentConverter.from(
                repositoryService
                    .createDeploymentQuery()
                    .deploymentName("SpringAutoDeployment")
                    .latestVersion()
                    .singleResult()
        );
    }

    public org.activiti.engine.runtime.ProcessInstance internalProcessInstance(String processInstanceId) {
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = runtimeService
            .createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        if (internalProcessInstance == null) {
            throw new NotFoundException("Unable to find process instance for the given id:'" + processInstanceId + "'");
        }
        return internalProcessInstance;
    }

    private boolean canReadProcessInstance(org.activiti.engine.runtime.ProcessInstance processInstance) {
        return securityPoliciesManager.canRead(processInstance.getProcessDefinitionKey()) &&
            (securityManager.getAuthenticatedUserId().equals(processInstance.getStartUserId()) ||
                isATaskAssigneeOrACandidate(processInstance.getProcessInstanceId()));
    }

    private boolean canWriteProcessInstance(org.activiti.engine.runtime.ProcessInstance processInstance) {
        return securityPoliciesManager.canWrite(processInstance.getProcessDefinitionKey()) &&
            securityManager.getAuthenticatedUserId().equals(processInstance.getStartUserId());
    }

    private boolean isATaskAssigneeOrACandidate(String processInstanceId) {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstanceId);
        taskQuery.or()
            .taskCandidateOrAssigned(securityManager.getAuthenticatedUserId(),
                securityManager.getAuthenticatedUserGroups())
            .taskOwner(authenticatedUserId)
            .endOr();

        return taskQuery.count() > 0;
    }

}
