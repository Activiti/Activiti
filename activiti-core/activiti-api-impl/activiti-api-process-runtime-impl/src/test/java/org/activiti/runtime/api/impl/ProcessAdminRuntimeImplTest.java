/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.DeleteProcessPayload;
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
import org.activiti.api.runtime.model.impl.ProcessDefinitionImpl;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ProcessAdminRuntimeImplTest {

    private ProcessAdminRuntimeImpl processAdminRuntime;

    @Mock
    private CommandExecutor commandExecutor;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private APIProcessInstanceConverter processInstanceConverter;

    @Mock
    private ProcessVariablesPayloadValidator processVariableValidator;

    @Mock
    private APIProcessDefinitionConverter processDefinitionConverter;

    @Mock
    private APIVariableInstanceConverter variableInstanceConverter;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private RepositoryServiceImpl repositoryService;

    @BeforeEach
    void setUp() {
        repositoryService = spy(new RepositoryServiceImpl());
        repositoryService.setCommandExecutor(commandExecutor);

        processAdminRuntime = spy(
            new ProcessAdminRuntimeImpl(
                repositoryService,
                processDefinitionConverter,
                runtimeService,
                processInstanceConverter,
                variableInstanceConverter,
                eventPublisher,
                processVariableValidator
            )
        );
    }

    @Test
    void should_applyPaginationParams_whenSearchingProcessDefinitions() {
        var processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);

        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(0, 2)).willReturn(Collections.emptyList());

        processAdminRuntime.processDefinitions(Pageable.of(0, 2));

        verify(processDefinitionQuery).listPage(0, 2);
    }

    @Test
    void should_returnProcessDefinitions_whenPaginationParamsProvided() {
        var processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        var internalProcessDef = mock(org.activiti.engine.repository.ProcessDefinition.class);
        var apiProcessDef = new ProcessDefinitionImpl();

        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(0, 10)).willReturn(List.of(internalProcessDef));
        given(processDefinitionQuery.count()).willReturn(1L);
        given(processDefinitionConverter.from(List.of(internalProcessDef))).willReturn(List.of(apiProcessDef));

        var result = processAdminRuntime.processDefinitions(Pageable.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).containsExactly(apiProcessDef);
        assertThat(result.getTotalItems()).isEqualTo(1);
    }

    @Test
    void should_applyLatestVersionFilter_whenSearchingProcessDefinitionsWithLatestVersionPayload() {
        var processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        var payload = ProcessPayloadBuilder.processDefinitions().withLatestVersionOnly(true).build();

        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processDefinitions(Pageable.of(0, 10), payload);

        verify(processDefinitionQuery).latestVersion();
    }

    @Test
    void should_applyProcessDefinitionKeysFilter_whenSearchingWithKeys() {
        var processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        var payload = ProcessPayloadBuilder.processDefinitions().withProcessDefinitionKeys(Set.of("key1", "key2")).build();

        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processDefinitions(Pageable.of(0, 10), payload);

        verify(processDefinitionQuery).processDefinitionKeys(payload.getProcessDefinitionKeys());
    }

    @Test
    void should_applyProcessDefinitionIdsFilter_whenSearchingWithIds() {
        var processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        var payload = ProcessPayloadBuilder.processDefinitions().withProcessDefinitionIds(Set.of("id1", "id2")).build();

        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processDefinitions(Pageable.of(0, 10), payload);

        verify(processDefinitionQuery).processDefinitionIds(payload.getProcessDefinitionIds());
    }

    @Test
    void should_throwIllegalState_whenProcessDefinitionsPayloadIsNull() {
        var pageable = Pageable.of(0, 10);
        assertThatThrownBy(() -> processAdminRuntime.processDefinitions(pageable, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("payload cannot be null");
    }

    @Test
    void should_returnProcessDefinition_whenFoundByIdWithLatestDeployment() {
        var processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        var deploymentQuery = mock(org.activiti.engine.repository.DeploymentQuery.class, Answers.RETURNS_SELF);
        var internalProcessDef = mock(org.activiti.engine.repository.ProcessDefinition.class);
        var deployment = mock(org.activiti.engine.repository.Deployment.class);
        var apiProcessDef = new ProcessDefinitionImpl();

        given(repositoryService.createDeploymentQuery()).willReturn(deploymentQuery);
        given(deploymentQuery.list()).willReturn(List.of(deployment));
        given(deployment.getId()).willReturn("deployId");
        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.list()).willReturn(List.of(internalProcessDef));
        given(processDefinitionConverter.from(internalProcessDef)).willReturn(apiProcessDef);

        var result = processAdminRuntime.processDefinition("procDefId");

        assertThat(result).isEqualTo(apiProcessDef);
    }

    @Test
    void should_throwActivitiObjectNotFound_whenProcessDefinitionNotFound() {
        var processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        var deploymentQuery = mock(org.activiti.engine.repository.DeploymentQuery.class, Answers.RETURNS_SELF);

        given(repositoryService.createDeploymentQuery()).willReturn(deploymentQuery);
        given(deploymentQuery.list()).willReturn(Collections.emptyList());
        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.list()).willReturn(Collections.emptyList());

        assertThatThrownBy(() -> processAdminRuntime.processDefinition("unknownId"))
            .isInstanceOf(ActivitiObjectNotFoundException.class);
    }

    @Test
    void should_startProcessInstance_withProcessDefinitionId() {
        var startPayload = new StartProcessPayload(
            "procDefId",
            null,
            "processName",
            "businessKey",
            Map.of("var1", "value1")
        );
        startPayload.setLinkedProcessInstanceId("linkedProcessId");
        startPayload.setLinkedProcessInstanceType("linkedProcessType");

        var processDefinitionImpl = new ProcessDefinitionImpl();
        processDefinitionImpl.setId("procDefId");
        processDefinitionImpl.setKey("procKey");

        var processInstanceBuilder = mock(ProcessInstanceBuilder.class);
        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        doReturn(processDefinitionImpl).when(processAdminRuntime).processDefinition("procDefId");
        given(runtimeService.createProcessInstanceBuilder()).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.processDefinitionId("procDefId")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.processDefinitionKey("procKey")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.businessKey("businessKey")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.variables(Map.of("var1", "value1"))).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.name("processName")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.linkedProcessInstanceId("linkedProcessId")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.linkedProcessInstanceType("linkedProcessType")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.start()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.start(startPayload);

        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_startProcessInstance_withProcessDefinitionKey() {
        var startPayload = new StartProcessPayload(null, "procKey", null, null, null);

        var processDefinitionImpl = new ProcessDefinitionImpl();
        processDefinitionImpl.setId("procDefId");
        processDefinitionImpl.setKey("procKey");

        var processInstanceBuilder = mock(ProcessInstanceBuilder.class);
        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        doReturn(processDefinitionImpl).when(processAdminRuntime).processDefinition("procKey");
        given(runtimeService.createProcessInstanceBuilder()).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.processDefinitionId("procDefId")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.processDefinitionKey("procKey")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.businessKey(null)).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.variables(null)).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.name(null)).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.linkedProcessInstanceId(null)).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.linkedProcessInstanceType(null)).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.start()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.start(startPayload);

        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_throwIllegalState_whenStartingProcessWithoutIdOrKey() {
        var startPayload = new StartProcessPayload();

        assertThatThrownBy(() -> processAdminRuntime.start(startPayload))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("At least Process Definition Id or Key needs to be provided");
    }

    @Test
    void should_returnProcessInstance_whenFoundById() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.processInstance("procInstId");

        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_throwNotFound_whenProcessInstanceNotFound() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class);

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("unknownId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(null);

        assertThatThrownBy(() -> processAdminRuntime.processInstance("unknownId"))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Unable to find process instance for the given id:'unknownId'");
    }

    @Test
    void should_returnPaginatedProcessInstances_withoutPayload() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(List.of(internalProcessInstance));
        given(processInstanceQuery.count()).willReturn(1L);
        given(processInstanceConverter.from(List.of(internalProcessInstance))).willReturn(List.of(apiProcessInstance));

        var result = processAdminRuntime.processInstances(Pageable.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).containsExactly(apiProcessInstance);
        assertThat(result.getTotalItems()).isEqualTo(1);
    }

    @Test
    void should_filterByProcessDefinitionKeys_whenSearchingProcessInstances() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class, Answers.RETURNS_SELF);
        var payload = new GetProcessInstancesPayload();
        payload.setProcessDefinitionKeys(Set.of("key1", "key2"));

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processInstances(Pageable.of(0, 10), payload);

        verify(processInstanceQuery).processDefinitionKeys(Set.of("key1", "key2"));
    }

    @Test
    void should_filterByBusinessKey_whenSearchingProcessInstances() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class, Answers.RETURNS_SELF);
        var payload = new GetProcessInstancesPayload();
        payload.setBusinessKey("businessKey123");

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processInstances(Pageable.of(0, 10), payload);

        verify(processInstanceQuery).processInstanceBusinessKey("businessKey123");
    }

    @Test
    void should_filterBySuspendedOnly_whenSearchingProcessInstances() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class, Answers.RETURNS_SELF);
        var payload = new GetProcessInstancesPayload();
        payload.setSuspendedOnly(true);

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processInstances(Pageable.of(0, 10), payload);

        verify(processInstanceQuery).suspended();
        verify(processInstanceQuery, never()).active();
    }

    @Test
    void should_filterByActiveOnly_whenSearchingProcessInstances() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class, Answers.RETURNS_SELF);
        var payload = new GetProcessInstancesPayload();
        payload.setActiveOnly(true);

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processInstances(Pageable.of(0, 10), payload);

        verify(processInstanceQuery).active();
        verify(processInstanceQuery, never()).suspended();
    }

    @Test
    void should_filterByParentProcessInstanceId_whenSearchingProcessInstances() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class, Answers.RETURNS_SELF);
        var payload = new GetProcessInstancesPayload();
        payload.setParentProcessInstanceId("parentId");

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processInstances(Pageable.of(0, 10), payload);

        verify(processInstanceQuery).superProcessInstanceId("parentId");
    }

    @Test
    void should_deleteProcessInstance_andReturnCancelledStatus() {
        var deletePayload = new DeleteProcessPayload();
        deletePayload.setProcessInstanceId("procInstId");
        deletePayload.setReason("test reason");

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var processInstanceImpl = new ProcessInstanceImpl();
        processInstanceImpl.setStatus(ProcessInstance.ProcessInstanceStatus.RUNNING);

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(processInstanceImpl);

        var result = processAdminRuntime.delete(deletePayload);

        verify(runtimeService).deleteProcessInstance("procInstId", "test reason");
        assertThat(result.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.CANCELLED);
    }

    @Test
    void should_throwNotFound_whenDeletingNonExistentProcessInstance() {
        var deletePayload = new DeleteProcessPayload();
        deletePayload.setProcessInstanceId("unknownId");

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("unknownId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(null);

        assertThatThrownBy(() -> processAdminRuntime.delete(deletePayload))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void should_suspendProcessInstance_andReturnUpdatedInstance() {
        var suspendPayload = new SuspendProcessPayload();
        suspendPayload.setProcessInstanceId("procInstId");

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.suspend(suspendPayload);

        verify(runtimeService).suspendProcessInstanceById("procInstId");
        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_resumeProcessInstance_andReturnUpdatedInstance() {
        var resumePayload = new ResumeProcessPayload();
        resumePayload.setProcessInstanceId("procInstId");

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.resume(resumePayload);

        verify(runtimeService).activateProcessInstanceById("procInstId");
        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_updateBusinessKeyAndName_whenProvided() {
        var updatePayload = new UpdateProcessPayload();
        updatePayload.setProcessInstanceId("procInstId");
        updatePayload.setBusinessKey("newBusinessKey");
        updatePayload.setName("newName");

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.update(updatePayload);

        InOrder inOrder = inOrder(runtimeService);
        inOrder.verify(runtimeService).updateBusinessKey("procInstId", "newBusinessKey");
        inOrder.verify(runtimeService).setProcessInstanceName("procInstId", "newName");
        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_updateOnlyBusinessKey_whenNameIsNull() {
        var updatePayload = new UpdateProcessPayload();
        updatePayload.setProcessInstanceId("procInstId");
        updatePayload.setBusinessKey("newBusinessKey");

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.update(updatePayload);

        verify(runtimeService).updateBusinessKey("procInstId", "newBusinessKey");
        verify(runtimeService, never()).setProcessInstanceName(anyString(), anyString());
        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_updateOnlyName_whenBusinessKeyIsNull() {
        var updatePayload = new UpdateProcessPayload();
        updatePayload.setProcessInstanceId("procInstId");
        updatePayload.setName("newName");

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.update(updatePayload);

        verify(runtimeService, never()).updateBusinessKey(anyString(), anyString());
        verify(runtimeService).setProcessInstanceName("procInstId", "newName");
        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_setProcessVariables() {
        var setVariablesPayload = new SetProcessVariablesPayload();
        setVariablesPayload.setProcessInstanceId("procInstId");
        Map<String, Object> variables = Map.of("var1", "value1", "var2", "value2");
        setVariablesPayload.setVariables(variables);

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var processInstanceImpl = new ProcessInstanceImpl();
        processInstanceImpl.setProcessDefinitionId("procDefId");

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(processInstanceImpl);

        processAdminRuntime.setVariables(setVariablesPayload);

        verify(processVariableValidator).checkPayloadVariables(setVariablesPayload, "procDefId");
        verify(runtimeService).setVariables("procInstId", variables);
    }

    @Test
    void should_getProcessVariables() {
        var getVariablesPayload = new GetVariablesPayload();
        getVariablesPayload.setProcessInstanceId("procInstId");

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var processInstanceImpl = new ProcessInstanceImpl();

        var internalVariable1 = mock(org.activiti.engine.impl.persistence.entity.VariableInstance.class);
        var internalVariable2 = mock(org.activiti.engine.impl.persistence.entity.VariableInstance.class);
        var variablesMap = Map.of("var1", internalVariable1, "var2", internalVariable2);

        var apiVariable1 = mock(org.activiti.api.model.shared.model.VariableInstance.class);
        var apiVariable2 = mock(org.activiti.api.model.shared.model.VariableInstance.class);

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(processInstanceImpl);
        given(runtimeService.getVariableInstances("procInstId")).willReturn(variablesMap);
        given(variableInstanceConverter.from(variablesMap.values())).willReturn(List.of(apiVariable1, apiVariable2));

        var result = processAdminRuntime.variables(getVariablesPayload);

        assertThat(result).containsExactly(apiVariable1, apiVariable2);
    }

    @Test
    void should_removeProcessVariables() {
        var removeVariablesPayload = new RemoveProcessVariablesPayload();
        removeVariablesPayload.setProcessInstanceId("procInstId");
        removeVariablesPayload.setVariableNames(List.of("var1", "var2"));

        processAdminRuntime.removeVariables(removeVariablesPayload);

        verify(runtimeService).removeVariables("procInstId", List.of("var1", "var2"));
    }

    @Test
    void should_publishSignalPayload_whenSignaling() {
        var signalPayload = new SignalPayload();

        processAdminRuntime.signal(signalPayload);

        verify(processVariableValidator).checkSignalPayloadVariables(signalPayload, null);
    }

    @Test
    void should_publishReceiveMessagePayload_whenReceivingMessage() {
        var messagePayload = new ReceiveMessagePayload();

        processAdminRuntime.receive(messagePayload);

        verify(processVariableValidator).checkReceiveMessagePayloadVariables(messagePayload, null);
    }

    @Test
    void should_startProcessInstanceByMessage() {
        var variables = Map.of("var1", (Object) "value1");
        var messagePayload = new StartMessagePayload("messageName", "businessKey", variables);

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        given(runtimeService.startProcessInstanceByMessage("messageName", "businessKey", variables))
            .willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.start(messagePayload);

        verify(processVariableValidator).checkStartMessagePayloadVariables(messagePayload, null);
        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_returnEmptyList_whenNoProcessInstancesFound() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class);

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());
        given(processInstanceQuery.count()).willReturn(0L);

        var result = processAdminRuntime.processInstances(Pageable.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalItems()).isZero();
    }

    @Test
    void should_notApplyFilters_whenPayloadIsNull() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class);

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());
        given(processInstanceQuery.count()).willReturn(0L);

        processAdminRuntime.processInstances(Pageable.of(0, 10), null);

        verify(processInstanceQuery, never()).processDefinitionKeys(any());
        verify(processInstanceQuery, never()).processInstanceBusinessKey(anyString());
        verify(processInstanceQuery, never()).suspended();
        verify(processInstanceQuery, never()).active();
    }

    @Test
    void should_applyBothActiveAndSuspendedFilters_whenBothAreSet() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class, Answers.RETURNS_SELF);
        var payload = new GetProcessInstancesPayload();
        payload.setActiveOnly(true);
        payload.setSuspendedOnly(true);

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processInstances(Pageable.of(0, 10), payload);

        verify(processInstanceQuery).active();
        verify(processInstanceQuery).suspended();
    }

    @Test
    void should_skipProcessDefinitionFilter_whenKeysAreEmpty() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class, Answers.RETURNS_SELF);
        var payload = new GetProcessInstancesPayload();
        payload.setProcessDefinitionKeys(Collections.emptySet());

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processInstances(Pageable.of(0, 10), payload);

        verify(processInstanceQuery, never()).processDefinitionKeys(any());
    }

    @Test
    void should_skipBusinessKeyFilter_whenBusinessKeyIsEmpty() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class, Answers.RETURNS_SELF);
        var payload = new GetProcessInstancesPayload();
        payload.setBusinessKey("");

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processInstances(Pageable.of(0, 10), payload);

        verify(processInstanceQuery, never()).processInstanceBusinessKey("");
    }

    @Test
    void should_returnMultipleProcessInstances_whenMultipleInstancesExist() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        var internalProcessInstance1 = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var internalProcessInstance2 = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance1 = new ProcessInstanceImpl();
        var apiProcessInstance2 = new ProcessInstanceImpl();

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(List.of(internalProcessInstance1, internalProcessInstance2));
        given(processInstanceQuery.count()).willReturn(2L);
        given(processInstanceConverter.from(List.of(internalProcessInstance1, internalProcessInstance2)))
            .willReturn(List.of(apiProcessInstance1, apiProcessInstance2));

        var result = processAdminRuntime.processInstances(Pageable.of(0, 10));

        assertThat(result.getContent()).containsExactly(apiProcessInstance1, apiProcessInstance2);
        assertThat(result.getTotalItems()).isEqualTo(2);
    }

    @Test
    void should_validateStartProcessPayloadVariables_beforeStarting() {
        var startPayload = new StartProcessPayload(
            "procDefId",
            null,
            "processName",
            "businessKey",
            Map.of("var1", "value1")
        );

        var processDefinitionImpl = new ProcessDefinitionImpl();
        processDefinitionImpl.setId("procDefId");
        processDefinitionImpl.setKey("procKey");

        var processInstanceBuilder = mock(ProcessInstanceBuilder.class);
        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        doReturn(processDefinitionImpl).when(processAdminRuntime).processDefinition("procDefId");
        given(runtimeService.createProcessInstanceBuilder()).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.processDefinitionId("procDefId")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.processDefinitionKey("procKey")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.businessKey("businessKey")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.variables(Map.of("var1", "value1"))).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.name("processName")).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.linkedProcessInstanceId(null)).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.linkedProcessInstanceType(null)).willReturn(processInstanceBuilder);
        given(processInstanceBuilder.start()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        processAdminRuntime.start(startPayload);

        verify(processVariableValidator).checkStartProcessPayloadVariables(startPayload, "procDefId");
    }

    @Test
    void should_returnProcessDefinitions_withMultipleResults() {
        var processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        var internalProcessDef1 = mock(org.activiti.engine.repository.ProcessDefinition.class);
        var internalProcessDef2 = mock(org.activiti.engine.repository.ProcessDefinition.class);
        var apiProcessDef1 = new ProcessDefinitionImpl();
        var apiProcessDef2 = new ProcessDefinitionImpl();

        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(0, 10)).willReturn(List.of(internalProcessDef1, internalProcessDef2));
        given(processDefinitionQuery.count()).willReturn(2L);
        given(processDefinitionConverter.from(List.of(internalProcessDef1, internalProcessDef2)))
            .willReturn(List.of(apiProcessDef1, apiProcessDef2));

        var result = processAdminRuntime.processDefinitions(Pageable.of(0, 10));

        assertThat(result.getContent()).containsExactly(apiProcessDef1, apiProcessDef2);
        assertThat(result.getTotalItems()).isEqualTo(2);
    }

    @Test
    void should_removeMultipleVariables() {
        var removeVariablesPayload = new RemoveProcessVariablesPayload();
        removeVariablesPayload.setProcessInstanceId("procInstId");
        var variableNames = List.of("var1", "var2", "var3");
        removeVariablesPayload.setVariableNames(variableNames);

        processAdminRuntime.removeVariables(removeVariablesPayload);

        verify(runtimeService).removeVariables("procInstId", variableNames);
    }

    @Test
    void should_notUpdateAnyField_whenBothBusinessKeyAndNameAreNull() {
        var updatePayload = new UpdateProcessPayload();
        updatePayload.setProcessInstanceId("procInstId");

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var apiProcessInstance = new ProcessInstanceImpl();

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        var result = processAdminRuntime.update(updatePayload);

        verify(runtimeService, never()).updateBusinessKey(anyString(), anyString());
        verify(runtimeService, never()).setProcessInstanceName(anyString(), anyString());
        assertThat(result).isEqualTo(apiProcessInstance);
    }

    @Test
    void should_returnEmptyProcessDefinitions_whenNoDefinitionsFound() {
        var processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);

        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(0, 10)).willReturn(Collections.emptyList());
        given(processDefinitionQuery.count()).willReturn(0L);

        var result = processAdminRuntime.processDefinitions(Pageable.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalItems()).isZero();
    }

    @Test
    void should_applyMultipleFilters_toProcessInstanceQuery() {
        var processInstanceQuery = mock(ProcessInstanceQuery.class, Answers.RETURNS_SELF);
        var payload = new GetProcessInstancesPayload();
        payload.setProcessDefinitionKeys(Set.of("key1"));
        payload.setBusinessKey("businessKey");
        payload.setParentProcessInstanceId("parentId");

        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.listPage(0, 10)).willReturn(Collections.emptyList());

        processAdminRuntime.processInstances(Pageable.of(0, 10), payload);

        verify(processInstanceQuery).processDefinitionKeys(Set.of("key1"));
        verify(processInstanceQuery).processInstanceBusinessKey("businessKey");
        verify(processInstanceQuery).superProcessInstanceId("parentId");
    }

    @Test
    void should_returnEmptyVariables_whenProcessInstanceHasNoVariables() {
        var getVariablesPayload = new GetVariablesPayload();
        getVariablesPayload.setProcessInstanceId("procInstId");

        var internalProcessInstance = mock(org.activiti.engine.runtime.ProcessInstance.class);
        var processInstanceImpl = new ProcessInstanceImpl();

        var processInstanceQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(processInstanceQuery);
        given(processInstanceQuery.processInstanceId("procInstId")).willReturn(processInstanceQuery);
        given(processInstanceQuery.singleResult()).willReturn(internalProcessInstance);
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(processInstanceImpl);
        given(runtimeService.getVariableInstances("procInstId")).willReturn(Collections.emptyMap());
        given(variableInstanceConverter.from(anySet())).willReturn(Collections.emptyList());

        var result = processAdminRuntime.variables(getVariablesPayload);

        assertThat(result).isEmpty();
    }
}
