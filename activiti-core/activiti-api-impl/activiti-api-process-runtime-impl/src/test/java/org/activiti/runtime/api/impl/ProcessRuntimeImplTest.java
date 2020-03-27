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

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;
import org.activiti.api.runtime.model.impl.DeploymentImpl;
import org.activiti.api.runtime.model.impl.ProcessDefinitionImpl;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.api.runtime.shared.UnprocessableEntityException;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.impl.runtime.ProcessInstanceBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.runtime.api.model.impl.APIDeploymentConverter;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

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
    private RepositoryServiceImpl repositoryService;

    @Mock
    APIProcessDefinitionConverter processDefinitionConverter;

    @Mock
    ProcessDefinitionQuery processDefinitionQuery;

    @Before
    public void setUp() {
        initMocks(this);

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
        ProcessInstance updatedProcess = processRuntime.update(updateProcessPayload);

        //then
        verify(runtimeService).updateBusinessKey("processId", "businessKey");
        verifyNoMoreInteractions(internalProcess);
    }

    @Test
    public void should_throwActivitiUnprocessableEntryException_when_processDefinitionAppVersionDiffersFromCurrentDeploymentVersion() {
        String processDefinitionId = "processDefinitionId";
        ProcessDefinitionEntityImpl processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId(processDefinitionId);
        processDefinition.setAppVersion(1);
        List<ProcessDefinition> findProcessDefinitionResult = Collections.singletonList(processDefinition);

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
        List<ProcessDefinition> findProcessDefinitionResult = Collections.singletonList(processDefinition);

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
    public void should_createAprocessInstance_whenCreateIsCalled() {
        String processDefinitionId = "processDefinitionId";
        ProcessDefinitionEntityImpl processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId(processDefinitionId);
        processDefinition.setKey("key");
        processDefinition.setAppVersion(1);
        ProcessDefinitionImpl processDefinitionResult = new ProcessDefinitionImpl();
        processDefinitionResult.setKey("key");
        processDefinitionResult.setId("999-999");
        List<ProcessDefinition> findProcessDefinitionResult = Collections.singletonList(processDefinition);
        Deployment latestDeploymentEntity = new DeploymentEntityImpl();
        DeploymentImpl deployment = new DeploymentImpl();
        deployment.setVersion(1);
        given(deploymentConverter.from(latestDeploymentEntity)).willReturn(deployment);
        DeploymentQuery deploymentQuery = spy(DeploymentQuery.class);
        org.activiti.engine.runtime.ProcessInstance internalProcess = mock(org.activiti.engine.runtime.ProcessInstance.class);
        when(internalProcess.getName()).thenReturn("PROCESS CREATED");

        given(deploymentQuery.singleResult()).willReturn(latestDeploymentEntity);
        when(processDefinitionQuery.processDefinitionKey(anyString())).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.orderByProcessDefinitionAppVersion()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.desc()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.list()).thenReturn(findProcessDefinitionResult);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
        when(repositoryService.createDeploymentQuery()).thenReturn(deploymentQuery);
        given(deploymentConverter.from(latestDeploymentEntity)).willReturn(deployment);
        given(securityPoliciesManager.canRead("key")).willReturn(true);
        when(processDefinitionConverter.from(any(ProcessDefinition.class))).thenReturn(processDefinitionResult);
        given(securityPoliciesManager.canWrite("key")).willReturn(true);
        RuntimeServiceImpl runtimeServiceImpl = mock(RuntimeServiceImpl.class);
        when(runtimeServiceImpl.createProcessInstance(any())).thenReturn(internalProcess);
        ProcessInstanceBuilderImpl processInstanceBuilder = mock(ProcessInstanceBuilderImpl.class);
        when(runtimeService.createProcessInstanceBuilder()).thenReturn(processInstanceBuilder);


        StartProcessPayload startPayload = new StartProcessPayload(processDefinitionId,
            "processDefinitionKey",
            "test-create",
            "business-key",
            new HashMap<String, Object>());

        ProcessInstance createdProcessInstance = processRuntime.create(startPayload);
        assertThat(createdProcessInstance).isNot(null);
        assertThat(createdProcessInstance.getName()).isEqualTo("PROCESS CREATED");
    }

}
