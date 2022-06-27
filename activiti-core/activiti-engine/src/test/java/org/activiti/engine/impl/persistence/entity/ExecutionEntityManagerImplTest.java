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
package org.activiti.engine.impl.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.impl.cfg.PerformanceSettings;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.data.ExecutionDataManager;
import org.activiti.engine.runtime.Clock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.AdditionalAnswers.returnsFirstArg;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionEntityManagerImplTest {

    @InjectMocks
    private ExecutionEntityManagerImpl executionEntityManager;

    @Mock
    private ExecutionDataManager executionDataManager;

    @Mock
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @Mock
    private ActivitiEventDispatcher eventDispatcher;

    @Mock
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        given(processEngineConfiguration.getClock()).willReturn(clock);
        given(processEngineConfiguration.getEventDispatcher()).willReturn(eventDispatcher);
        Context.setProcessEngineConfiguration(processEngineConfiguration);
    }

    @Test
    public void should_createProcessInstanceExecution_when_initialVariableNameIsNull() {
        ProcessDefinitionEntity processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId("processDefinitionId");
        processDefinition.setKey("processDefinitionKey");
        processDefinition.setName("processDefinitionName");
        processDefinition.setVersion(1);
        processDefinition.setAppVersion(3);

        String businessKey = "businessKey";
        String tenantId = "tenantId";

        PerformanceSettings performanceSettings = mock(PerformanceSettings.class);
        given(processEngineConfiguration.getPerformanceSettings()).willReturn(performanceSettings);
        ExecutionEntity execution = new ExecutionEntityImpl();
        execution.setId("processInstanceId");
        given(executionDataManager.create()).willReturn(execution);

        ExecutionEntity processInstanceResult = executionEntityManager.createProcessInstanceExecution(processDefinition, businessKey, tenantId, null);

        assertThat(processInstanceResult.getProcessDefinitionId()).isEqualTo("processDefinitionId");
        assertThat(processInstanceResult.getProcessDefinitionKey()).isEqualTo("processDefinitionKey");
        assertThat(processInstanceResult.getProcessDefinitionName()).isEqualTo("processDefinitionName");
        assertThat(processInstanceResult.getProcessDefinitionVersion()).isEqualTo(1);
        assertThat(processInstanceResult.getAppVersion()).isEqualTo(3);
        assertThat(processInstanceResult.getBusinessKey()).isEqualTo(businessKey);
        assertThat(processInstanceResult.isScope()).isTrue();
        assertThat(processInstanceResult.getTenantId()).isEqualTo(tenantId);
        assertThat(processInstanceResult.getProcessInstanceId()).isEqualTo("processInstanceId");
        assertThat(processInstanceResult.getRootProcessInstanceId()).isEqualTo("processInstanceId");
        verify(executionDataManager).insert(processInstanceResult);
        verify(eventDispatcher).isEnabled();
    }

    @Test
    public void should_createChildExecution() {
        ExecutionEntityImpl parentExecution = new ExecutionEntityImpl();
        parentExecution.executions = new ArrayList<>();
        parentExecution.setRootProcessInstanceId("rootProcessInstanceId");
        parentExecution.setTenantId("tenantId");
        parentExecution.setProcessDefinitionId("processDefinitionId");
        parentExecution.setProcessDefinitionKey("processDefinitionKey");
        parentExecution.setProcessInstanceId("processInstanceId");
        parentExecution.setParentProcessInstanceId("parentProcessInstanceId");
        parentExecution.setAppVersion(4);

        ExecutionEntityImpl childExecution = new ExecutionEntityImpl();
        given(executionDataManager.create()).willReturn(childExecution);
        Date startTime = new Date();
        given(clock.getCurrentTime()).willReturn(startTime);

        ExecutionEntity childResult = executionEntityManager.createChildExecution(parentExecution);

        assertThat(childResult.getRootProcessInstanceId()).isEqualTo("rootProcessInstanceId");
        assertThat(childResult.isActive()).isTrue();
        assertThat(childResult.getStartTime()).isEqualTo(startTime);
        assertThat(childResult.getTenantId()).isEqualTo("tenantId");
        assertThat(childResult.getParent()).isEqualTo(parentExecution);
        assertThat(childResult.getProcessDefinitionId()).isEqualTo("processDefinitionId");
        assertThat(childResult.getProcessDefinitionKey()).isEqualTo("processDefinitionKey");
        assertThat(childResult.getProcessInstanceId()).isEqualTo("processInstanceId");
        assertThat(childResult.getParentProcessInstanceId()).isEqualTo("parentProcessInstanceId");
        assertThat(childResult.getAppVersion()).isEqualTo(4);
        assertThat(childResult.isScope()).isFalse();
        verify(executionDataManager).insert(childExecution);
        verify(eventDispatcher).isEnabled();
    }

    @Test
    public void should_createSubProcess() {
        ProcessDefinitionEntity processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId("processDefinitionId");
        processDefinition.setKey("processDefinitionKey");
        processDefinition.setName("processDefinitionName");
        processDefinition.setVersion(3);
        processDefinition.setAppVersion(5);

        ExecutionEntityImpl superExecution = new ExecutionEntityImpl();
        superExecution.executions = new ArrayList<>();
        superExecution.setSubProcessInstance(null);
        superExecution.setRootProcessInstanceId("rootProcessInstanceId");
        superExecution.setTenantId("tenantId");
        superExecution.setProcessInstanceId("superProcessInstanceId");
        ExecutionEntityImpl processInstance = new ExecutionEntityImpl();
        processInstance.setId("superProcessInstanceId");
        processInstance.setName("myNamedInstance");
        superExecution.setProcessInstance(processInstance);

        String businessKey = "businessKey";

        ExecutionEntity subProcessInstance = new ExecutionEntityImpl();
        subProcessInstance.setId("subProcessInstanceId");
        given(executionDataManager.create()).willReturn(subProcessInstance);
        Date startTime = new Date();
        given(clock.getCurrentTime()).willReturn(startTime);

        ExecutionEntity subProcessResult = executionEntityManager.createSubprocessInstance(processDefinition, superExecution, businessKey);

        assertThat(subProcessResult.isActive()).isTrue();
        assertThat(subProcessResult.getName()).isEqualTo("myNamedInstance");
        assertThat(subProcessResult.getRootProcessInstanceId()).isEqualTo("rootProcessInstanceId");
        assertThat(subProcessResult.getStartTime()).isEqualTo(startTime);
        assertThat(subProcessResult.getTenantId()).isEqualTo("tenantId");
        assertThat(subProcessResult.getSuperExecution()).isEqualTo(superExecution);
        assertThat(subProcessResult.getProcessDefinitionId()).isEqualTo("processDefinitionId");
        assertThat(subProcessResult.getProcessDefinitionKey()).isEqualTo("processDefinitionKey");
        assertThat(subProcessResult.getProcessDefinitionName()).isEqualTo("processDefinitionName");
        assertThat(subProcessResult.getProcessDefinitionVersion()).isEqualTo(3);
        assertThat(subProcessResult.getProcessInstanceId()).isEqualTo("subProcessInstanceId");
        assertThat(subProcessResult.getParentProcessInstanceId()).isEqualTo("superProcessInstanceId");
        assertThat(subProcessResult.isScope()).isTrue();
        assertThat(subProcessResult.getBusinessKey()).isEqualTo(businessKey);
        assertThat(subProcessResult.getAppVersion()).isEqualTo(5);
        verify(executionDataManager).insert(subProcessInstance);
        verify(eventDispatcher).isEnabled();
        assertThat(superExecution.getSubProcessInstance()).isEqualTo(subProcessInstance);
    }


    @Test
    public void should_updateStartDateOfProcessInstance() {
        ProcessDefinitionEntity processDefinition = new ProcessDefinitionEntityImpl();
        processDefinition.setId("processDefinitionId");
        processDefinition.setKey("processDefinitionKey");
        processDefinition.setName("processDefinitionName");
        processDefinition.setVersion(1);
        String businessKey = "businessKey";
        String tenantId = "tenantId";
        Date startTime = new Date();
        given(clock.getCurrentTime()).willReturn(startTime);
        PerformanceSettings performanceSettings = mock(PerformanceSettings.class);
        given(processEngineConfiguration.getPerformanceSettings()).willReturn(performanceSettings);
        ExecutionEntity execution = new ExecutionEntityImpl();
        execution.setId("processInstanceId");
        given(executionDataManager.create()).willReturn(execution);
        when(executionDataManager.update(any(ExecutionEntity.class))).then(returnsFirstArg());

        ExecutionEntity processInstanceResult = executionEntityManager.createProcessInstanceExecution(processDefinition, businessKey, tenantId, null);
        assertThat(processInstanceResult.getStartTime()).isNull();

        ExecutionEntity processInstanceUpdated = executionEntityManager.updateProcessInstanceStartDate(processInstanceResult);
        assertThat(processInstanceUpdated.getStartTime()).isEqualTo(startTime);
    }
}
