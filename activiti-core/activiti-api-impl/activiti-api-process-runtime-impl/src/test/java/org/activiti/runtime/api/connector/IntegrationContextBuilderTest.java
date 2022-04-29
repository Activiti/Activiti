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
package org.activiti.runtime.api.connector;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.runtime.api.impl.ExtensionsVariablesMappingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IntegrationContextBuilderTest {

    private static final int PROCESS_DEFINITION_VERSION = 1;
    private static final String PARENT_PROCESS_INSTANCE_ID = "parentProcessInstanceId";
    private static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    private static final String PROCESS_INSTANCE_BUSINESS_KEY = "processInstanceBusinessKey";
    private static final String CURRENT_ACTIVITY_ID = "currentActivityId";
    private static final String PROCESS_DEFINITION_ID = "processDefinitionId";
    private static final String PROCESS_INSTANCE_ID = "processInstanceId";
    private static final String ROOT_PROCESS_INSTANCE_ID = "rootProcessInstanceId";
    private static final String IMPLEMENTATION = "implementation";
    private static final String SERVICE_TASK_NAME = "serviceTaskName";

    @InjectMocks
    private IntegrationContextBuilder builder;

    @Mock
    private ExtensionsVariablesMappingProvider inboundVariablesProvider;

    @BeforeEach
    public void setUp() {
        ProcessEngineConfigurationImpl processEngineConfiguration = mock(ProcessEngineConfigurationImpl.class);
        Context.setProcessEngineConfiguration(processEngineConfiguration);

        DeploymentManager deploymentManager = mock(DeploymentManager.class);
        ProcessDefinition processDefinition = mock(ProcessDefinition.class);

        given(processEngineConfiguration.getDeploymentManager()).willReturn(deploymentManager);
        given(deploymentManager.findDeployedProcessDefinitionById(PROCESS_DEFINITION_ID)).willReturn(processDefinition);

        given(processDefinition.getKey()).willReturn(PROCESS_DEFINITION_KEY);
        given(processDefinition.getVersion()).willReturn(PROCESS_DEFINITION_VERSION);
    }

    @Test
    public void shouldBuildIntegrationContextFromExecution() {
        //given
        ExecutionEntity execution = mock(ExecutionEntity.class);
        ExecutionEntity processInstance = mock(ExecutionEntity.class);
        ServiceTask serviceTask = mock(ServiceTask.class);

        Map<String, Object> variables = singletonMap("key", "value");
        given(inboundVariablesProvider.calculateInputVariables(execution)).willReturn(variables);


        given(serviceTask.getImplementation()).willReturn(IMPLEMENTATION);
        given(serviceTask.getName()).willReturn(SERVICE_TASK_NAME);
        given(execution.getCurrentActivityId()).willReturn(CURRENT_ACTIVITY_ID);
        given(execution.getCurrentFlowElement()).willReturn(serviceTask);
        given(execution.getProcessInstanceId()).willReturn(PROCESS_INSTANCE_ID);
        given(execution.getRootProcessInstanceId()).willReturn(ROOT_PROCESS_INSTANCE_ID);
        given(execution.getProcessDefinitionId()).willReturn(PROCESS_DEFINITION_ID);
        given(execution.getCurrentActivityId()).willReturn(CURRENT_ACTIVITY_ID);
        given(execution.getProcessInstanceBusinessKey()).willReturn(PROCESS_INSTANCE_BUSINESS_KEY);
        given(execution.getProcessInstance()).willReturn(processInstance);
        given(processInstance.getParentProcessInstanceId()).willReturn(PARENT_PROCESS_INSTANCE_ID);

        //when
        IntegrationContext integrationContext = builder.from(execution);

        //then
        assertThat(integrationContext).isNotNull();
        assertThat(integrationContext.getConnectorType()).isEqualTo(IMPLEMENTATION);
        assertThat(integrationContext.getClientId()).isEqualTo(CURRENT_ACTIVITY_ID);
        assertThat(integrationContext.getClientName()).isEqualTo(SERVICE_TASK_NAME);
        assertThat(integrationContext.getClientType()).isEqualTo(ServiceTask.class.getSimpleName());
        assertThat(integrationContext.getBusinessKey()).isEqualTo(PROCESS_INSTANCE_BUSINESS_KEY);
        assertThat(integrationContext.getProcessDefinitionId()).isEqualTo(PROCESS_DEFINITION_ID);
        assertThat(integrationContext.getProcessInstanceId()).isEqualTo(PROCESS_INSTANCE_ID);
        assertThat(integrationContext.getRootProcessInstanceId()).isEqualTo(ROOT_PROCESS_INSTANCE_ID);
        assertThat(integrationContext.getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(integrationContext.getProcessDefinitionVersion()).isEqualTo(PROCESS_DEFINITION_VERSION);
        assertThat(integrationContext.getParentProcessInstanceId()).isEqualTo(PARENT_PROCESS_INSTANCE_ID);
        assertThat(integrationContext.getInBoundVariables()).containsAllEntriesOf(variables);
    }

    @Test
    public void shouldSetIdWhenIntegrationContextEntityIsProvided() {
        //given
        ExecutionEntity execution = mock(ExecutionEntity.class);
        ExecutionEntity processInstance = mock(ExecutionEntity.class);
        ServiceTask serviceTask = mock(ServiceTask.class);

        Map<String, Object> variables = singletonMap("key", "value");
        given(inboundVariablesProvider.calculateInputVariables(execution)).willReturn(variables);

        given(serviceTask.getImplementation()).willReturn(IMPLEMENTATION);
        given(serviceTask.getName()).willReturn(SERVICE_TASK_NAME);
        given(execution.getCurrentActivityId()).willReturn(CURRENT_ACTIVITY_ID);
        given(execution.getCurrentFlowElement()).willReturn(serviceTask);
        given(execution.getProcessInstanceId()).willReturn(PROCESS_INSTANCE_ID);
        given(execution.getProcessDefinitionId()).willReturn(PROCESS_DEFINITION_ID);
        given(execution.getCurrentActivityId()).willReturn(CURRENT_ACTIVITY_ID);
        given(execution.getProcessInstanceBusinessKey()).willReturn(PROCESS_INSTANCE_BUSINESS_KEY);
        given(execution.getProcessInstance()).willReturn(processInstance);
        given(processInstance.getParentProcessInstanceId()).willReturn(PARENT_PROCESS_INSTANCE_ID);

        IntegrationContextEntityImpl integrationContextEntity = new IntegrationContextEntityImpl();
        integrationContextEntity.setId("entityId");

        //when
        IntegrationContext integrationContext = builder.from(integrationContextEntity, execution);

        //then
        assertThat(integrationContext).isNotNull();
        assertThat(integrationContext.getId()).isEqualTo("entityId");
        assertThat(integrationContext.getConnectorType()).isEqualTo(IMPLEMENTATION);
        assertThat(integrationContext.getClientId()).isEqualTo(CURRENT_ACTIVITY_ID);
        assertThat(integrationContext.getClientName()).isEqualTo(SERVICE_TASK_NAME);
        assertThat(integrationContext.getClientType()).isEqualTo(ServiceTask.class.getSimpleName());
        assertThat(integrationContext.getBusinessKey()).isEqualTo(PROCESS_INSTANCE_BUSINESS_KEY);
        assertThat(integrationContext.getProcessDefinitionId()).isEqualTo(PROCESS_DEFINITION_ID);
        assertThat(integrationContext.getProcessInstanceId()).isEqualTo(PROCESS_INSTANCE_ID);
        assertThat(integrationContext.getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(integrationContext.getProcessDefinitionVersion()).isEqualTo(PROCESS_DEFINITION_VERSION);
        assertThat(integrationContext.getParentProcessInstanceId()).isEqualTo(PARENT_PROCESS_INSTANCE_ID);
        assertThat(integrationContext.getInBoundVariables()).containsAllEntriesOf(variables);
    }
}
