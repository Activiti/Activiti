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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.List;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.CreateProcessInstancePayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;
import org.activiti.api.runtime.model.impl.DeploymentImpl;
import org.activiti.api.runtime.model.impl.ProcessDefinitionImpl;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.UnprocessableEntityException;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.runtime.api.model.impl.APIDeploymentConverter;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;

public class ProcessRuntimeImplTest {

    private ProcessRuntimeImpl processRuntime;

    @Mock
    private ProcessSecurityPoliciesManager securityPoliciesManager;

    @Mock
    private CommandExecutor commandExecutor;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private APIProcessInstanceConverter processInstanceConverter;

    @Mock
    private APIDeploymentConverter deploymentConverter;

    @Mock
    private ProcessVariablesPayloadValidator processVariableValidator;

    @Mock
    APIProcessDefinitionConverter processDefinitionConverter;

    @BeforeEach
    public void setUp() {
        initMocks(this);

        RepositoryServiceImpl repositoryService = new RepositoryServiceImpl();
        repositoryService.setCommandExecutor(commandExecutor);

        processRuntime = spy(new ProcessRuntimeImpl(repositoryService,
                processDefinitionConverter,
                runtimeService,
                securityPoliciesManager,
                processInstanceConverter,
                null,
                deploymentConverter,
                null,
                null,
                processVariableValidator));

    }

    @Test
    public void updateShouldBeAbleToUpdateNameBusinessKey() {
        //given
        ProcessInstanceImpl process = new ProcessInstanceImpl();
        process.setId("processId");
        process.setProcessDefinitionKey("processDefinitionKey");

        doReturn(process).when(processRuntime).processInstance("processId");

        doReturn(true).when(securityPoliciesManager).canWrite("processDefinitionKey");

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
    public void should_getProcessDefinitionById_when_appVersionIsNull() {
        String processDefinitionId = "processDefinitionId";
        String processDefinitionKey = "processDefinitionKey";

        ProcessDefinitionEntityImpl processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId(processDefinitionId);
        processDefinition.setKey(processDefinitionKey);
        processDefinition.setAppVersion(null);
        List<ProcessDefinition> findProcessDefinitionResult = singletonList(processDefinition);

        given(commandExecutor.execute(any())).willReturn(findProcessDefinitionResult);
        given(securityPoliciesManager.canRead(processDefinitionKey)).willReturn(true);

        processRuntime.processDefinition(processDefinitionId);

        verify(processDefinitionConverter).from(processDefinition);
        verifyZeroInteractions(deploymentConverter);
    }

    @Test
    public void should_throwActivitiUnprocessableEntryException_when_processDefinitionAppVersionDiffersFromCurrentDeploymentVersion() {
        String processDefinitionId = "processDefinitionId";
        ProcessDefinitionEntityImpl processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId(processDefinitionId);
        processDefinition.setAppVersion(1);
        List<ProcessDefinition> findProcessDefinitionResult = singletonList(processDefinition);

        Deployment latestDeploymentEntity = new DeploymentEntityImpl();
        DeploymentImpl latestDeployment = new DeploymentImpl();
        latestDeployment.setVersion(2);

        given(deploymentConverter.from(latestDeploymentEntity)).willReturn(latestDeployment);
        given(commandExecutor.execute(any()))
            .willReturn(findProcessDefinitionResult)
            .willReturn(latestDeploymentEntity)
            .willReturn(latestDeployment);

        Throwable exception = catchThrowable(() -> processRuntime.processDefinition(processDefinitionId));

        assertThat(exception)
            .isInstanceOf(UnprocessableEntityException.class)
            .hasMessage("Process definition with the given id:'processDefinitionId' belongs to a different application version.");
    }

    @Test
    public void should_throwActivitiObjectNotFoundException_when_canReadFalse() {
        String processDefinitionId = "processDefinitionId";
        String processDefinitionKey = "processDefinitionKey";
        ProcessDefinitionEntityImpl processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId(processDefinitionId);
        processDefinition.setKey(processDefinitionKey);
        processDefinition.setAppVersion(1);
        List<ProcessDefinition> findProcessDefinitionResult = singletonList(processDefinition);

        Deployment latestDeploymentEntity = new DeploymentEntityImpl();
        DeploymentImpl deployment = new DeploymentImpl();
        deployment.setVersion(1);

        given(deploymentConverter.from(latestDeploymentEntity)).willReturn(deployment);
        given(commandExecutor.execute(any()))
            .willReturn(findProcessDefinitionResult)
            .willReturn(latestDeploymentEntity);

        given(securityPoliciesManager.canRead(processDefinitionKey)).willReturn(false);

        Throwable exception = catchThrowable(() -> processRuntime.processDefinition(processDefinitionId));

        assertThat(exception)
            .isInstanceOf(ActivitiObjectNotFoundException.class)
            .hasMessage("Unable to find process definition for the given id:'processDefinitionId'");
    }

    @Test
    public void should_createAProcessInstance_whenCreateIsCalled() {
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
    public void should_startAnAlreadyCreatedProcessInstance_whenCalled() {
        //given
        String processInstanceId = "process-instance-id";
        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        doReturn(processQuery).when(processQuery).processInstanceId(processInstanceId);
        doReturn(processQuery).when(runtimeService).createProcessInstanceQuery();
        org.activiti.engine.runtime.ProcessInstance internalProcess = new ExecutionEntityImpl();
        internalProcess.setAppVersion(1);
        doReturn(internalProcess).when(processQuery).singleResult();
        when(runtimeService.startCreatedProcessInstance(internalProcess, new HashMap<>())).thenReturn(internalProcess);
        ProcessInstanceImpl apiProcessInstance = new ProcessInstanceImpl();
        apiProcessInstance.setBusinessKey("business-result");
        apiProcessInstance.setId("999-999");
        given(processInstanceConverter.from(internalProcess)).willReturn(apiProcessInstance);
        given(securityPoliciesManager.canRead(any())).willReturn(true);

        //when
        StartProcessPayload payload = new StartProcessPayload();
        ProcessInstance createdProcessInstance = processRuntime.startCreatedProcess(processInstanceId, payload);

        //then
        assertThat(createdProcessInstance.getId()).isEqualTo("999-999");
        assertThat(createdProcessInstance.getBusinessKey()).isEqualTo("business-result");
    }

    @Test
    public void should_throwAndException_whenProcessIdDoesNotExists() {
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

}
