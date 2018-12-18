package org.activiti.runtime.api.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Map;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Test;


public class IntegrationContextBuilderTest {
    
    private static final int PROCESS_DEFINITION_VERSION = 1;
    private static final String PARENT_PROCESS_INSTANCE_ID = "parentProcessInstanceId";
    private static final String SUPER_EXECUTION_ID = "superExecutionId";
    private static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    private static final String PROCESS_INSTANCE_BUSINESS_KEY = "processInstanceBusinessKey";
    private static final String CURRENT_ACTIVITY_ID = "currentActivityId";
    private static final String PROCESS_DEFINITION_ID = "processDefinitionId";
    private static final String PROCESS_INSTANCE_ID = "processInstanceId";
    private static final String IMPLEMENTATION = "implementation";
    
    private IntegrationContextBuilder subject = new IntegrationContextBuilder(new VariablesMatchHelper());
    
    @Test
    public void shouldBuildIntegrationContextFromExecution() {
        //given
        ExecutionEntity execution = mock(ExecutionEntity.class);
        ExecutionEntity processInstance = mock(ExecutionEntity.class);
        ExecutionEntity superExecution = mock(ExecutionEntity.class);
        ServiceTask serviceTask = mock(ServiceTask.class);
        Map<String, Object> variables = Collections.singletonMap("key", "value");

        given(serviceTask.getImplementation()).willReturn(IMPLEMENTATION);
        given(execution.getVariables()).willReturn(variables);
        given(execution.getCurrentActivityId()).willReturn(CURRENT_ACTIVITY_ID);
        given(execution.getCurrentFlowElement()).willReturn(serviceTask);
        given(execution.getProcessInstanceId()).willReturn(PROCESS_INSTANCE_ID);
        given(execution.getProcessDefinitionId()).willReturn(PROCESS_DEFINITION_ID);
        given(execution.getCurrentActivityId()).willReturn(CURRENT_ACTIVITY_ID);
        given(execution.getProcessInstanceBusinessKey()).willReturn(PROCESS_INSTANCE_BUSINESS_KEY);
        given(execution.getProcessInstance()).willReturn(processInstance);
        given(processInstance.getProcessDefinitionKey()).willReturn(PROCESS_DEFINITION_KEY);
        given(processInstance.getProcessDefinitionVersion()).willReturn(PROCESS_DEFINITION_VERSION);
        given(processInstance.getSuperExecutionId()).willReturn(SUPER_EXECUTION_ID);
        given(processInstance.getSuperExecution()).willReturn(superExecution);
        given(superExecution.getProcessInstanceId()).willReturn(PARENT_PROCESS_INSTANCE_ID);
        
        //when
        IntegrationContext integrationContext = subject.from(execution, null);

        //then
        assertThat(integrationContext).isNotNull();
        assertThat(integrationContext.getConnectorType()).isEqualTo(IMPLEMENTATION);
        assertThat(integrationContext.getActivityElementId()).isEqualTo(CURRENT_ACTIVITY_ID);
        assertThat(integrationContext.getBusinessKey()).isEqualTo(PROCESS_INSTANCE_BUSINESS_KEY);
        assertThat(integrationContext.getProcessDefinitionId()).isEqualTo(PROCESS_DEFINITION_ID);
        assertThat(integrationContext.getProcessInstanceId()).isEqualTo(PROCESS_INSTANCE_ID);
        assertThat(integrationContext.getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(integrationContext.getProcessDefinitionVersion()).isEqualTo(PROCESS_DEFINITION_VERSION);
        assertThat(integrationContext.getParentProcessInstanceId()).isEqualTo(PARENT_PROCESS_INSTANCE_ID);
        assertThat(integrationContext.getInBoundVariables()).containsAllEntriesOf(variables);
    }    
    
    @Test(expected=ActivitiException.class)
    public void shouldThrowActivitiExceptionIfUsedOutsideCommandContext() {
        //given
        ExecutionEntity execution = mock(ExecutionEntity.class);
        ExecutionEntity processInstance = mock(ExecutionEntity.class);
        
        given(execution.getProcessInstance()).willReturn(processInstance);
        given(processInstance.getSuperExecutionId()).willReturn(SUPER_EXECUTION_ID);
        given(processInstance.getSuperExecution()).willThrow(NullPointerException.class);
        
        //when
        subject.from(execution, new ActionDefinition());

        //then
        fail("Excepted ActivitiException!");
    }
    
}
