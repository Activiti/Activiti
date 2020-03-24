/*
 * Copyright 2020 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Optional;
import java.util.stream.Collectors;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.Deployment;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessDefinitionMeta;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.ProcessInstanceMeta;
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
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.model.impl.ProcessDefinitionMetaImpl;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.api.runtime.model.impl.ProcessInstanceMetaImpl;
import org.activiti.api.runtime.shared.UnprocessableEntityException;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.core.common.spring.security.policies.ActivitiForbiddenException;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.core.common.spring.security.policies.SecurityPolicyAccess;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
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

    private final APIProcessInstanceConverter processInstanceConverter;

    private final APIVariableInstanceConverter variableInstanceConverter;

    private final APIDeploymentConverter deploymentConverter;

    private final ProcessRuntimeConfiguration configuration;

    private final ProcessSecurityPoliciesManager securityPoliciesManager;

    private final ApplicationEventPublisher eventPublisher;

    private final ProcessVariablesPayloadValidator processVariablesValidator;

    public ProcessRuntimeImpl(RepositoryService repositoryService,
                              APIProcessDefinitionConverter processDefinitionConverter,
                              RuntimeService runtimeService,
                              ProcessSecurityPoliciesManager securityPoliciesManager,
                              APIProcessInstanceConverter processInstanceConverter,
                              APIVariableInstanceConverter variableInstanceConverter,
                              APIDeploymentConverter deploymentConverter,
                              ProcessRuntimeConfiguration configuration,
                              ApplicationEventPublisher eventPublisher,
                              ProcessVariablesPayloadValidator processVariablesValidator) {
        this.repositoryService = repositoryService;
        this.processDefinitionConverter = processDefinitionConverter;
        this.runtimeService = runtimeService;
        this.securityPoliciesManager = securityPoliciesManager;
        this.processInstanceConverter = processInstanceConverter;
        this.variableInstanceConverter = variableInstanceConverter;
        this.deploymentConverter = deploymentConverter;
        this.configuration = configuration;
        this.eventPublisher = eventPublisher;
        this.processVariablesValidator = processVariablesValidator;
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
            .processDefinitionKey(processDefinitionKey)
            .orderByProcessDefinitionAppVersion()
            .desc()
            .list()
            .stream()
            .findFirst();
    }

    private void checkProcessDefinitionBelongsToLatestDeployment(org.activiti.engine.repository.ProcessDefinition processDefinition){
        if (!selectLatestDeployment().getVersion().equals(processDefinition.getAppVersion())) {
            throw new UnprocessableEntityException("Process definition with the given id:'" + processDefinition.getId() + "' belongs to a different application version.");
        }
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
        GetProcessDefinitionsPayload securityKeysInPayload = securityPoliciesManager.restrictProcessDefQuery(SecurityPolicyAccess.READ);
        // If the security policies keys are not empty it means that I will need to use them to filter results,
        //   else ignore and use the user provided ones.
        if (!securityKeysInPayload.getProcessDefinitionKeys().isEmpty()) {
            getProcessDefinitionsPayload.setProcessDefinitionKeys(securityKeysInPayload.getProcessDefinitionKeys());
        }
        ProcessDefinitionQuery processDefinitionQuery = repositoryService
                .createProcessDefinitionQuery();
        if (getProcessDefinitionsPayload.hasDefinitionKeys()) {
            processDefinitionQuery.processDefinitionKeys(getProcessDefinitionsPayload.getProcessDefinitionKeys());
        }

        List<org.activiti.engine.repository.ProcessDefinition> currentVersionDefinitions = filterCurrentVersionDefinitions(processDefinitionQuery.list());

        return new PageImpl<>(processDefinitionConverter.from(currentVersionDefinitions),
                              Math.toIntExact(processDefinitionQuery.count()));
    }

    private List<org.activiti.engine.repository.ProcessDefinition> filterCurrentVersionDefinitions (List<org.activiti.engine.repository.ProcessDefinition> allDefinitions){
        int currentVersion = selectLatestDeployment().getVersion();
        return allDefinitions
                .stream()
                .filter(processDefinition -> processDefinition.getAppVersion() == null ||
                                             processDefinition.getAppVersion().equals(currentVersion))
                //we fetch possible unversioned definitions from different types of deployments
                .collect(Collectors.toList());
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

        ProcessDefinition processDefinition = getProcessDefinitionAndCheckUserHasRights(startProcessPayload.getProcessDefinitionId(),
                                                                                        startProcessPayload.getProcessDefinitionKey());

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
    public ProcessInstance create(StartProcessPayload startProcessPayload) {

        ProcessDefinition processDefinition = getProcessDefinitionAndCheckUserHasRights(startProcessPayload.getProcessDefinitionId(),
            startProcessPayload.getProcessDefinitionKey());

        processVariablesValidator.checkStartProcessPayloadVariables(startProcessPayload, processDefinition.getId());

        return processInstanceConverter.from(runtimeService
            .createProcessInstanceBuilder()
            .processDefinitionId(processDefinition.getId())
            .processDefinitionKey(processDefinition.getKey())
            .businessKey(startProcessPayload.getBusinessKey())
            .variables(startProcessPayload.getVariables())
            .name(startProcessPayload.getName())
            .create());
    }

    @Override
    public ProcessInstance suspend(SuspendProcessPayload suspendProcessPayload) {
        ProcessInstance processInstance = processInstance(suspendProcessPayload.getProcessInstanceId());

        checkUserCanWrite(processInstance.getProcessDefinitionKey());

        runtimeService.suspendProcessInstanceById(suspendProcessPayload.getProcessInstanceId());
        return processInstanceConverter.from(runtimeService.createProcessInstanceQuery().processInstanceId(suspendProcessPayload.getProcessInstanceId()).singleResult());
    }

    @Override
    public ProcessInstance resume(ResumeProcessPayload resumeProcessPayload) {
        ProcessInstance processInstance = processInstance(resumeProcessPayload.getProcessInstanceId());

        checkUserCanWrite(processInstance.getProcessDefinitionKey());

        runtimeService.activateProcessInstanceById(resumeProcessPayload.getProcessInstanceId());
        return processInstanceConverter.from(runtimeService.createProcessInstanceQuery()
                .processInstanceId(resumeProcessPayload.getProcessInstanceId()).singleResult());
    }

    @Override
    public ProcessInstance delete(DeleteProcessPayload deleteProcessPayload) {
        ProcessInstanceImpl processInstance = (ProcessInstanceImpl) processInstance(deleteProcessPayload.getProcessInstanceId());

        checkUserCanWrite(processInstance.getProcessDefinitionKey());

        runtimeService.deleteProcessInstance(deleteProcessPayload.getProcessInstanceId(),
                deleteProcessPayload.getReason());
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
        ProcessInstanceImpl processInstance = (ProcessInstanceImpl) processInstance(removeProcessVariablesPayload.getProcessInstanceId());

        checkUserCanWrite(processInstance.getProcessDefinitionKey());

        runtimeService.removeVariables(removeProcessVariablesPayload.getProcessInstanceId(),
                removeProcessVariablesPayload.getVariableNames());

    }

    @Override
    public void setVariables(SetProcessVariablesPayload setProcessVariablesPayload) {
        ProcessInstanceImpl processInstance = (ProcessInstanceImpl) processInstance(setProcessVariablesPayload.getProcessInstanceId());

        checkUserCanWrite(processInstance.getProcessDefinitionKey());

        processVariablesValidator.checkPayloadVariables(setProcessVariablesPayload,
                processInstance.getProcessDefinitionId());

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
        ProcessInstance processInstance = processInstance(updateProcessPayload.getProcessInstanceId());

        checkUserCanWrite(processInstance.getProcessDefinitionKey());

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

    private void checkUserCanWrite(String processDefinitionKey) {

        if (!securityPoliciesManager.canWrite(processDefinitionKey)) {
            throw new ActivitiForbiddenException("Operation not permitted for " + processDefinitionKey + " due security policy violation");
        }
    }

    private ProcessDefinition getProcessDefinitionAndCheckUserHasRights(String processDefinitionId, String processDefinitionKey) {

        String checkId = processDefinitionKey != null ? processDefinitionKey : processDefinitionId;

        ProcessDefinition processDefinition = processDefinition(checkId);

        if (processDefinition == null) {
            throw new IllegalStateException("At least Process Definition Id or Key needs to be provided to start a process");
        }

        checkUserCanWrite(processDefinition.getKey());

        return processDefinition;
    }

    @Override
    public Deployment selectLatestDeployment(){
        return deploymentConverter.from(
                repositoryService
                        .createDeploymentQuery()
                        .singleResult()
        );
    }

}
