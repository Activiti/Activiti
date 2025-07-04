/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.CreateProcessInstancePayload;
import org.activiti.api.process.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;
import org.activiti.api.runtime.model.impl.DeploymentImpl;
import org.activiti.api.runtime.model.impl.ProcessDefinitionImpl;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.UnprocessableEntityException;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.runtime.api.model.impl.APIDeploymentConverter;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProcessRuntimeImplTest {

    private ProcessRuntimeImpl processRuntime;

    @Mock
    private ProcessSecurityPoliciesManager securityPoliciesManager;

    @Mock
    private CommandExecutor commandExecutor;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService taskService;

    @Mock
    private APIProcessInstanceConverter processInstanceConverter;

    @Mock
    private APIDeploymentConverter deploymentConverter;

    @Mock
    private ProcessVariablesPayloadValidator processVariableValidator;

    @Mock
    private APIProcessDefinitionConverter processDefinitionConverter;

    @Mock
    private SecurityManager securityManager;

    private RepositoryServiceImpl repositoryService;

    @BeforeEach
    void setUp() {
        repositoryService = spy(new RepositoryServiceImpl());
        repositoryService.setCommandExecutor(commandExecutor);

        processRuntime = spy(new ProcessRuntimeImpl(repositoryService,
            processDefinitionConverter,
            runtimeService,
            taskService,
            securityPoliciesManager,
            processInstanceConverter,
            null,
            deploymentConverter,
            null,
            null,
            processVariableValidator,
            securityManager));

    }

    @Test
    void updateShouldBeAbleToUpdateNameBusinessKey() {
        //given
        ExecutionEntityImpl internalProcessInstance = new ExecutionEntityImpl();
        internalProcessInstance.setId("processId");
        internalProcessInstance.setProcessDefinitionKey("processDefinitionKey");
        internalProcessInstance.setStartUserId("testuser");

        doReturn(internalProcessInstance).when(processRuntime).internalProcessInstance("processId");

        doReturn(true).when(securityPoliciesManager).canWrite("processDefinitionKey");
        doReturn("testuser").when(securityManager).getAuthenticatedUserId();

        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        doReturn(processQuery).when(processQuery).processInstanceId("processId");
        doReturn(processQuery).when(runtimeService).createProcessInstanceQuery();

        org.activiti.engine.runtime.ProcessInstance internalProcess = mock(org.activiti.engine.runtime.ProcessInstance.class);

        doReturn(internalProcess).when(processQuery).singleResult();


        UpdateProcessPayload updateProcessPayload = ProcessPayloadBuilder.update()
                .withProcessInstanceId("processId")
                .withBusinessKey("businessKey")
                .withName("name")
                .build();

        //when
        processRuntime.update(updateProcessPayload);

        //then
        verify(runtimeService).updateBusinessKey("processId", "businessKey");
        verifyNoMoreInteractions(internalProcess);
    }

    @Test
    void should_getProcessDefinitionById_when_appVersionIsNull() {
        doReturn("user").when(securityManager).getAuthenticatedUserId();

        String processDefinitionId = "processDefinitionId";
        String processDefinitionKey = "processDefinitionKey";

        ProcessDefinitionEntityImpl processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId(processDefinitionId);
        processDefinition.setKey(processDefinitionKey);
        processDefinition.setAppVersion(null);
        List<ProcessDefinition> findProcessDefinitionResult = singletonList(processDefinition);

        DeploymentEntityImpl deploymentEntity = new DeploymentEntityImpl();
        deploymentEntity.setId("deploymentId");

        given(commandExecutor.execute(any())).willReturn(Arrays.asList(deploymentEntity))
                                             .willReturn(findProcessDefinitionResult);
        given(securityPoliciesManager.canRead(processDefinitionKey)).willReturn(true);

        processRuntime.processDefinition(processDefinitionId);

        verify(processDefinitionConverter).from(processDefinition);
        verifyNoInteractions(deploymentConverter);
    }

    @Test
    void should_throwActivitiUnprocessableEntryException_when_processDefinitionAppVersionDiffersFromCurrentDeploymentVersion() {
        doReturn("user").when(securityManager).getAuthenticatedUserId();

        String processDefinitionId = "processDefinitionId";
        ProcessDefinitionEntityImpl processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId(processDefinitionId);
        processDefinition.setAppVersion(1);
        List<ProcessDefinition> findProcessDefinitionResult = singletonList(processDefinition);

        DeploymentEntityImpl latestDeploymentEntity = new DeploymentEntityImpl();
        latestDeploymentEntity.setId("deploymentId");

        DeploymentImpl latestDeployment = new DeploymentImpl();
        latestDeployment.setVersion(2);

        given(deploymentConverter.from(latestDeploymentEntity)).willReturn(latestDeployment);
        given(commandExecutor.execute(any()))
            .willReturn(Arrays.asList(latestDeploymentEntity))
            .willReturn(findProcessDefinitionResult)
            .willReturn(latestDeploymentEntity)
            .willReturn(latestDeployment);

        Throwable exception = catchThrowable(() -> processRuntime.processDefinition(processDefinitionId));

        assertThat(exception)
            .isInstanceOf(UnprocessableEntityException.class)
            .hasMessage("Process definition with the given id:'processDefinitionId' belongs to a different application version.");
    }

    @Test
    void should_throwActivitiObjectNotFoundException_when_canReadFalse() {
        doReturn("user").when(securityManager).getAuthenticatedUserId();

        String processDefinitionId = "processDefinitionId";
        String processDefinitionKey = "processDefinitionKey";
        ProcessDefinitionEntityImpl processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId(processDefinitionId);
        processDefinition.setKey(processDefinitionKey);
        processDefinition.setAppVersion(1);
        List<ProcessDefinition> findProcessDefinitionResult = singletonList(processDefinition);

        DeploymentEntityImpl latestDeploymentEntity = new DeploymentEntityImpl();
        latestDeploymentEntity.setId("deploymentId");
        DeploymentImpl deployment = new DeploymentImpl();
        deployment.setVersion(1);

        given(deploymentConverter.from(latestDeploymentEntity)).willReturn(deployment);
        given(commandExecutor.execute(any()))
            .willReturn(Arrays.asList(latestDeploymentEntity))
            .willReturn(findProcessDefinitionResult)
            .willReturn(latestDeploymentEntity);

        given(securityPoliciesManager.canRead(processDefinitionKey)).willReturn(false);

        Throwable exception = catchThrowable(() -> processRuntime.processDefinition(processDefinitionId));

        assertThat(exception)
            .isInstanceOf(ActivitiObjectNotFoundException.class)
            .hasMessage("Unable to find process definition for the given id or key:'processDefinitionId'");
    }

    @Test
    void should_createAProcessInstance_whenCreateIsCalled() {
        //given
        String processDefinitionId = "processDefinitionId";
        ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl();
        processDefinition.setId(processDefinitionId);
        processDefinition.setKey("key");

        CreateProcessInstancePayload createPayload = ProcessPayloadBuilder.create()
        .withProcessDefinitionId(processDefinitionId)
            .withProcessDefinitionKey("key")
            .withName("test-create")
            .build();

        doReturn(processDefinition)
            .when(processRuntime)
                .getProcessDefinitionAndCheckUserHasRights(createPayload.getProcessDefinitionId(),
                    createPayload.getProcessDefinitionKey());

        ProcessInstanceBuilder processInstanceBuilder = mock(ProcessInstanceBuilder.class, Answers.RETURNS_SELF);
        given(runtimeService.createProcessInstanceBuilder()).willReturn(processInstanceBuilder);
        org.activiti.engine.runtime.ProcessInstance internalProcessInstance = mock(
            org.activiti.engine.runtime.ProcessInstance.class);
        given(processInstanceBuilder.create()).willReturn(internalProcessInstance);

        ProcessInstanceImpl apiProcessInstance = new ProcessInstanceImpl();
        given(processInstanceConverter.from(internalProcessInstance)).willReturn(apiProcessInstance);

        //when
        ProcessInstance createdProcessInstance = processRuntime.create(createPayload);

        //then
        assertThat(createdProcessInstance).isEqualTo(apiProcessInstance);
        verify(processInstanceBuilder).processDefinitionId(processDefinition.getId());
        verify(processInstanceBuilder).processDefinitionKey(processDefinition.getKey());
        verify(processInstanceBuilder).name(createPayload.getName());
    }

    @Test
    void should_startAnAlreadyCreatedProcessInstance_whenCalled() {
        //given
        String processInstanceId = "process-instance-id";
        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        doReturn(processQuery).when(processQuery).processInstanceId(processInstanceId);
        doReturn(processQuery).when(runtimeService).createProcessInstanceQuery();
        ExecutionEntityImpl internalProcess = new ExecutionEntityImpl();
        internalProcess.setStartUserId("testuser");
        internalProcess.setAppVersion(1);
        doReturn(internalProcess).when(processQuery).singleResult();
        when(runtimeService.startCreatedProcessInstance(internalProcess, new HashMap<>())).thenReturn(internalProcess);
        ProcessInstanceImpl apiProcessInstance = new ProcessInstanceImpl();
        apiProcessInstance.setBusinessKey("business-result");
        apiProcessInstance.setId("999-999");
        given(processInstanceConverter.from(internalProcess)).willReturn(apiProcessInstance);
        given(securityPoliciesManager.canWrite(any())).willReturn(true);
        doReturn("testuser").when(securityManager).getAuthenticatedUserId();

        //when
        StartProcessPayload payload = new StartProcessPayload();
        ProcessInstance createdProcessInstance = processRuntime.startCreatedProcess(processInstanceId, payload);

        //then
        assertThat(createdProcessInstance.getId()).isEqualTo("999-999");
        assertThat(createdProcessInstance.getBusinessKey()).isEqualTo("business-result");
    }

    @Test
    void should_throwAndException_whenProcessIdDoesNotExists() {
        //given
        String processInstanceId = "process-instance-id";
        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        doReturn(processQuery).when(processQuery).processInstanceId(processInstanceId);
        doReturn(processQuery).when(runtimeService).createProcessInstanceQuery();
        doReturn(null).when(processQuery).singleResult();
        StartProcessPayload payload = new StartProcessPayload();

        Throwable exception = catchThrowable(() -> processRuntime.startCreatedProcess(processInstanceId, payload));

        assertThat(exception)
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Unable to find process instance for the given id:'process-instance-id'");
    }

    @Test
     void should_applyPaginationParams_whenSearchingProcessDefinitions() {
        doReturn("testUser").when(securityManager).getAuthenticatedUserId();

        given(securityPoliciesManager.restrictProcessDefQuery(any())).willReturn(ProcessPayloadBuilder.processDefinitions().build());

        ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        given(processDefinitionQuery.deploymentIds(any())).willReturn(processDefinitionQuery);
        given(repositoryService.createDeploymentQuery()).willReturn(mock(DeploymentQuery.class, Answers.RETURNS_SELF));
        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(0, 2)).willReturn(Collections.emptyList());

        processRuntime.processDefinitions(Pageable.of(0, 2), List.of());

        verify(processDefinitionQuery).listPage(0, 2);
    }

    @Test
    void should_setCategoryNotEquals_when_excludedCategoryIsSet() {
        doReturn("user").when(securityManager).getAuthenticatedUserId();
        given(securityPoliciesManager.restrictProcessDefQuery(any()))
            .willReturn(ProcessPayloadBuilder.processDefinitions().build());

        String processCategory = "#triggerableByForm";

        ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        DeploymentQuery deploymentQuery = mock(DeploymentQuery.class, Answers.RETURNS_SELF);

        when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
        when(repositoryService.createDeploymentQuery()).thenReturn(deploymentQuery);
        when(deploymentQuery.latestVersion()).thenReturn(deploymentQuery);
        when(deploymentQuery.list()).thenReturn(Collections.emptyList());
        when(processDefinitionQuery.deploymentIds(any())).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.listPage(anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(processDefinitionQuery.count()).thenReturn(0L);

        Pageable pageable = Pageable.of(0, 10);
        GetProcessDefinitionsPayload payload = ProcessPayloadBuilder.processDefinitions()
            .withProcessCategoryToExclude(processCategory)
            .build();

        processRuntime.processDefinitions(pageable, payload);

        verify(processDefinitionQuery).processDefinitionCategoryNotEquals(processCategory);
    }

}
