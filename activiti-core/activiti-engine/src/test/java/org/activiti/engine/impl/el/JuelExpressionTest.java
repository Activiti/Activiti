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
package org.activiti.engine.impl.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.el.ELContext;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.invocation.ExpressionGetInvocation;
import org.activiti.engine.impl.delegate.invocation.ExpressionSetInvocation;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionInfoCache;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionInfoCacheObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

class JuelExpressionTest {

    @Mock
    private ValueExpression valueExpression;

    @Mock
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @Mock
    private ExpressionManager expressionManager;

    @Mock
    private DelegateInterceptor delegateInterceptor;

    @Mock
    private ELContext elContext;

    @Mock
    private VariableScope variableScope;

    @Mock
    private DelegateExecution delegateExecution;

    private JuelExpression juelExpression;
    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        Context.setProcessEngineConfiguration(processEngineConfiguration);
        when(processEngineConfiguration.getExpressionManager()).thenReturn(expressionManager);
        when(processEngineConfiguration.getDelegateInterceptor()).thenReturn(delegateInterceptor);
        when(expressionManager.getElContext(any(VariableScope.class))).thenReturn(elContext);

        juelExpression = new JuelExpression(valueExpression, "${myVar}");
    }

    @AfterEach
    void tearDown() throws Exception {
        Context.removeBpmnOverrideContext();
        Context.removeProcessEngineConfiguration();
        mockitoCloseable.close();
    }

    @Test
    void should_returnExpressionValue() {
        String expectedValue = "testValue";
        doAnswer(invocation -> {
            ExpressionGetInvocation getInvocation = invocation.getArgument(0);
            when(valueExpression.getValue(elContext)).thenReturn(expectedValue);
            getInvocation.proceed();
            return null;
        })
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        Object result = juelExpression.getValue(variableScope);

        assertThat(result).isEqualTo(expectedValue);
        verify(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));
    }

    @Test
    void should_wrapPropertyNotFound_when_resolvingValue() {
        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown property used in expression: ${myVar}")
            .hasCauseInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void should_wrapMethodNotFound_when_resolvingValue() {
        doThrow(new MethodNotFoundException("Method not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown method used in expression: ${myVar}")
            .hasCauseInstanceOf(MethodNotFoundException.class);
    }

    @Test
    void should_wrapGenericException_when_resolvingValue() {
        doThrow(new RuntimeException("Generic error"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Error while evaluating expression: ${myVar}")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void should_includeFlowElementContext_when_delegateExecutionPresent() {
        FlowNode flowNode = mockFlowNodeWithoutOutgoingFlows("serviceTask1");

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown property used in expression: ${myVar}")
            .hasMessageContaining("flowElementId: [serviceTask1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_includeFlowAndSequenceContext_when_sequenceFlowMatchesCondition() {
        String conditionExpression = "${myVar}";
        JuelExpression expressionWithCondition = new JuelExpression(valueExpression, conditionExpression);

        SequenceFlow sequenceFlow = mock(SequenceFlow.class);
        when(sequenceFlow.getId()).thenReturn("flow1");
        when(sequenceFlow.getConditionExpression()).thenReturn(conditionExpression);

        FlowNode flowNode = mockFlowNode("exclusiveGateway1", Collections.singletonList(sequenceFlow));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        setupDefaultBpmnOverrideContext();

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expressionWithCondition.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown property used in expression: " + conditionExpression)
            .hasMessageContaining("flowElementId: [exclusiveGateway1]")
            .hasMessageContaining("sequenceFlowId: [flow1]");
    }

    @Test
    void should_showUnknownContext_when_noDelegateExecution() {
        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [unknown]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_setExpressionValue() {
        String valueToSet = "newValue";
        doAnswer(invocation -> {
            ExpressionSetInvocation setInvocation = invocation.getArgument(0);
            Object[] params = setInvocation.getInvocationParameters();
            assertThat(params).containsExactly(valueToSet);
            return null;
        })
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionSetInvocation.class));

        juelExpression.setValue(valueToSet, variableScope);

        verify(delegateInterceptor).handleInvocation(any(ExpressionSetInvocation.class));
    }

    @Test
    void should_wrapException_when_settingValue() {
        doThrow(new RuntimeException("Set value error"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionSetInvocation.class));

        assertThatThrownBy(() -> juelExpression.setValue("value", variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Error while evaluating expression: ${myVar}")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void should_returnExpressionString_when_valueExpressionPresent() {
        String expectedString = "${myVar}";
        when(valueExpression.getExpressionString()).thenReturn(expectedString);

        String result = juelExpression.toString();

        assertThat(result).isEqualTo(expectedString);
    }

    @Test
    void should_returnDefaultToString_when_valueExpressionNull() {
        JuelExpression expressionWithNull = new JuelExpression(null, "${myVar}");

        String result = expressionWithNull.toString();

        assertThat(result).contains("JuelExpression");
    }

    @Test
    void should_returnOriginalExpressionText() {
        String result = juelExpression.getExpressionText();

        assertThat(result).isEqualTo("${myVar}");
    }

    @Test
    void should_evaluateExpression_withExpressionManagerAndVariablesMap() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("myVar", "testValue");

        when(expressionManager.getElContext(variables)).thenReturn(elContext);

        String expectedValue = "testValue";
        doAnswer(invocation -> {
            ExpressionGetInvocation getInvocation = invocation.getArgument(0);
            when(valueExpression.getValue(elContext)).thenReturn(expectedValue);
            getInvocation.proceed();
            return null;
        })
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        Object result = juelExpression.getValue(expressionManager, delegateInterceptor, variables);

        assertThat(result).isEqualTo(expectedValue);
        verify(expressionManager).getElContext(variables);
    }

    @Test
    void should_notIncludeFlowContext_when_expressionManagerThrows() {
        Map<String, Object> variables = new HashMap<>();
        when(expressionManager.getElContext(variables)).thenReturn(elContext);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(expressionManager, delegateInterceptor, variables))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown property used in expression: ${myVar}")
            .hasMessageNotContaining("flowElementId")
            .hasMessageNotContaining("sequenceFlowId");
    }

    @Test
    void should_showUnknown_when_flowElementIdEmpty() {
        FlowNode flowNode = mockFlowNodeWithoutOutgoingFlows("");

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [unknown]");
    }

    @Test
    void should_showUnknown_when_flowElementIdNull() {
        FlowNode flowNode = mockFlowNodeWithoutOutgoingFlows(null);

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [unknown]");
    }

    @Test
    void should_notExtractSequenceFlow_when_flowElementIsNotFlowNode() {
        FlowElement flowElement = mock(FlowElement.class);
        when(flowElement.getId()).thenReturn("startEvent1");

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowElement);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [startEvent1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_showUnknownFlowElement_when_currentFlowElementIsNull() {
        when(delegateExecution.getCurrentFlowElement()).thenReturn(null);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [unknown]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_showUnknownSequenceFlow_when_conditionExpressionIsNull() {
        SequenceFlow flowWithNullCondition = mock(SequenceFlow.class);
        when(flowWithNullCondition.getConditionExpression()).thenReturn(null);

        FlowNode flowNode = mockFlowNode("gateway1", List.of(flowWithNullCondition));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        setupDefaultBpmnOverrideContext();

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_showUnknownSequenceFlow_when_conditionExpressionIsEmpty() {
        SequenceFlow flowWithEmptyCondition = mock(SequenceFlow.class);
        when(flowWithEmptyCondition.getConditionExpression()).thenReturn("");

        FlowNode flowNode = mockFlowNode("gateway1", List.of(flowWithEmptyCondition));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        setupDefaultBpmnOverrideContext();

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_showUnknownSequenceFlow_when_conditionExpressionIsWhitespaceOnly() {
        SequenceFlow flowWithWhitespaceCondition = mock(SequenceFlow.class);
        when(flowWithWhitespaceCondition.getConditionExpression()).thenReturn("   ");

        FlowNode flowNode = mockFlowNode("gateway1", List.of(flowWithWhitespaceCondition));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        setupDefaultBpmnOverrideContext();

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_showUnknownSequenceFlow_when_sequenceFlowIdIsNull() {
        String conditionExpression = "${myVar}";
        SequenceFlow flowWithNullId = mock(SequenceFlow.class);
        when(flowWithNullId.getConditionExpression()).thenReturn(conditionExpression);
        when(flowWithNullId.getId()).thenReturn(null);

        FlowNode flowNode = mockFlowNode("gateway1", List.of(flowWithNullId));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        setupDefaultBpmnOverrideContext();

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_showUnknownSequenceFlow_when_sequenceFlowIdIsEmpty() {
        String conditionExpression = "${myVar}";
        SequenceFlow flowWithEmptyId = mock(SequenceFlow.class);
        when(flowWithEmptyId.getConditionExpression()).thenReturn(conditionExpression);
        when(flowWithEmptyId.getId()).thenReturn("");

        FlowNode flowNode = mockFlowNode("gateway1", List.of(flowWithEmptyId));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        setupDefaultBpmnOverrideContext();

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_matchSequenceFlowByConditionExpression_when_multipleOutgoingFlows() {
        String targetCondition = "${approved == true}";
        JuelExpression targetExpression = new JuelExpression(valueExpression, targetCondition);

        SequenceFlow flow1 = mock(SequenceFlow.class);
        when(flow1.getConditionExpression()).thenReturn("${rejected}");

        SequenceFlow flow2 = mock(SequenceFlow.class);
        when(flow2.getId()).thenReturn("flow2");
        when(flow2.getConditionExpression()).thenReturn(targetCondition);

        SequenceFlow flow3 = mock(SequenceFlow.class);

        FlowNode flowNode = mockFlowNode("gateway1", List.of(flow1, flow2, flow3));

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        setupDefaultBpmnOverrideContext();

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> targetExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [flow2]");
    }

    @Test
    void should_includeFlowElementContext_when_methodNotFoundWithDelegateExecution() {
        FlowNode flowNode = mockFlowNodeWithoutOutgoingFlows("serviceTask1");
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new MethodNotFoundException("Method not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown method used in expression: ${myVar}")
            .hasMessageContaining("flowElementId: [serviceTask1]")
            .hasMessageContaining("sequenceFlowId: [unknown]")
            .hasCauseInstanceOf(MethodNotFoundException.class);
    }

    @Test
    void should_includeFlowElementContext_when_genericExceptionWithDelegateExecution() {
        FlowNode flowNode = mockFlowNodeWithoutOutgoingFlows("serviceTask1");
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new RuntimeException("Generic error"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Error while evaluating expression: ${myVar}")
            .hasMessageContaining("flowElementId: [serviceTask1]")
            .hasMessageContaining("sequenceFlowId: [unknown]")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void should_matchSequenceFlowByOverriddenCondition_when_bpmnOverrideExists() {
        String originalCondition = "${original}";
        String overriddenCondition = "${overridden}";
        String processDefinitionId = "process-def-1";
        String sequenceFlowId = "flow1";

        JuelExpression expressionWithOverriddenCondition = new JuelExpression(valueExpression, overriddenCondition);

        SequenceFlow sequenceFlow = mock(SequenceFlow.class);
        when(sequenceFlow.getId()).thenReturn(sequenceFlowId);
        when(sequenceFlow.getConditionExpression()).thenReturn(originalCondition);

        FlowNode flowNode = mockFlowNode("gateway1", List.of(sequenceFlow));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);
        when(delegateExecution.getProcessDefinitionId()).thenReturn(processDefinitionId);

        ObjectNode elementProperties = mock(ObjectNode.class);
        JsonNode conditionNode = mock(JsonNode.class);
        when(conditionNode.isNull()).thenReturn(false);
        when(conditionNode.asText()).thenReturn(overriddenCondition);
        when(elementProperties.get(DynamicBpmnConstants.SEQUENCE_FLOW_CONDITION)).thenReturn(conditionNode);

        setupBpmnOverrideContext(processDefinitionId, sequenceFlowId, elementProperties);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expressionWithOverriddenCondition.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [flow1]");
    }

    @Test
    void should_fallbackToOriginalCondition_when_bpmnOverrideReturnsNullObjectNode() {
        String conditionExpression = "${myVar}";
        String processDefinitionId = "process-def-1";
        String sequenceFlowId = "flow1";

        JuelExpression expressionWithCondition = new JuelExpression(valueExpression, conditionExpression);

        SequenceFlow sequenceFlow = mock(SequenceFlow.class);
        when(sequenceFlow.getId()).thenReturn(sequenceFlowId);
        when(sequenceFlow.getConditionExpression()).thenReturn(conditionExpression);

        FlowNode flowNode = mockFlowNode("gateway1", List.of(sequenceFlow));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);
        when(delegateExecution.getProcessDefinitionId()).thenReturn(processDefinitionId);

        setupBpmnOverrideContext(processDefinitionId, sequenceFlowId, null);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expressionWithCondition.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [flow1]");
    }

    @Test
    void should_fallbackToOriginalCondition_when_sequenceFlowConditionPropertyIsNull() {
        String conditionExpression = "${myVar}";
        String processDefinitionId = "process-def-1";
        String sequenceFlowId = "flow1";

        JuelExpression expressionWithCondition = new JuelExpression(valueExpression, conditionExpression);

        SequenceFlow sequenceFlow = mock(SequenceFlow.class);
        when(sequenceFlow.getId()).thenReturn(sequenceFlowId);
        when(sequenceFlow.getConditionExpression()).thenReturn(conditionExpression);

        FlowNode flowNode = mockFlowNode("gateway1", List.of(sequenceFlow));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);
        when(delegateExecution.getProcessDefinitionId()).thenReturn(processDefinitionId);

        ObjectNode elementProperties = mock(ObjectNode.class);
        when(elementProperties.get(DynamicBpmnConstants.SEQUENCE_FLOW_CONDITION)).thenReturn(null);

        setupBpmnOverrideContext(processDefinitionId, sequenceFlowId, elementProperties);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expressionWithCondition.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [flow1]");
    }

    @Test
    void should_returnNullActiveCondition_when_overriddenValueIsJsonNull() {
        String originalCondition = "${original}";
        String processDefinitionId = "process-def-1";
        String sequenceFlowId = "flow1";

        JuelExpression expressionWithNull = new JuelExpression(valueExpression, null);

        SequenceFlow sequenceFlow = mock(SequenceFlow.class);
        when(sequenceFlow.getId()).thenReturn(sequenceFlowId);
        when(sequenceFlow.getConditionExpression()).thenReturn(originalCondition);

        FlowNode flowNode = mockFlowNode("gateway1", List.of(sequenceFlow));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);
        when(delegateExecution.getProcessDefinitionId()).thenReturn(processDefinitionId);

        ObjectNode elementProperties = mock(ObjectNode.class);
        JsonNode conditionNode = mock(JsonNode.class);
        when(conditionNode.isNull()).thenReturn(true);
        when(elementProperties.get(DynamicBpmnConstants.SEQUENCE_FLOW_CONDITION)).thenReturn(conditionNode);

        setupBpmnOverrideContext(processDefinitionId, sequenceFlowId, elementProperties);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expressionWithNull.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [flow1]");
    }

    @Test
    void should_notMatchSequenceFlow_when_overriddenConditionDoesNotMatchExpression() {
        String expressionText = "${different}";
        String originalCondition = "${original}";
        String overriddenCondition = "${overridden}";
        String processDefinitionId = "process-def-1";
        String sequenceFlowId = "flow1";

        JuelExpression expression = new JuelExpression(valueExpression, expressionText);

        SequenceFlow sequenceFlow = mock(SequenceFlow.class);
        when(sequenceFlow.getId()).thenReturn(sequenceFlowId);
        when(sequenceFlow.getConditionExpression()).thenReturn(originalCondition);

        FlowNode flowNode = mockFlowNode("gateway1", List.of(sequenceFlow));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);
        when(delegateExecution.getProcessDefinitionId()).thenReturn(processDefinitionId);

        ObjectNode elementProperties = mock(ObjectNode.class);
        JsonNode conditionNode = mock(JsonNode.class);
        when(conditionNode.isNull()).thenReturn(false);
        when(conditionNode.asText()).thenReturn(overriddenCondition);
        when(elementProperties.get(DynamicBpmnConstants.SEQUENCE_FLOW_CONDITION)).thenReturn(conditionNode);

        setupBpmnOverrideContext(processDefinitionId, sequenceFlowId, elementProperties);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_useOriginalCondition_when_variableScopeIsNotDelegateExecution() {
        String conditionExpression = "${myVar}";

        JuelExpression expressionWithCondition = new JuelExpression(valueExpression, conditionExpression);

        SequenceFlow sequenceFlow = mock(SequenceFlow.class);
        when(sequenceFlow.getId()).thenReturn("flow1");
        when(sequenceFlow.getConditionExpression()).thenReturn(conditionExpression);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expressionWithCondition.getValue(variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [unknown]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    void should_returnUnknownSequenceFlowId_when_extractSequenceFlowThrowsException() {
        String conditionExpression = "${myVar}";
        JuelExpression expressionWithCondition = new JuelExpression(valueExpression, conditionExpression);

        FlowNode flowNode = mock(FlowNode.class);
        when(flowNode.getId()).thenReturn("gateway1");
        when(flowNode.getOutgoingFlows()).thenThrow(new RuntimeException("Unexpected error during flow extraction"));

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expressionWithCondition.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [unknown]")
            .hasMessageNotContaining("Unexpected error during flow extraction");
    }

    @Test
    void should_returnUnknownSequenceFlowId_when_bpmnOverrideContextThrowsException() {
        String conditionExpression = "${myVar}";
        String processDefinitionId = "process-def-1";

        JuelExpression expressionWithCondition = new JuelExpression(valueExpression, conditionExpression);

        SequenceFlow sequenceFlow = mock(SequenceFlow.class);
        when(sequenceFlow.getId()).thenReturn("flow1");
        when(sequenceFlow.getConditionExpression()).thenReturn(conditionExpression);

        FlowNode flowNode = mockFlowNode("gateway1", List.of(sequenceFlow));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);
        when(delegateExecution.getProcessDefinitionId()).thenReturn(processDefinitionId);

        DeploymentManager deploymentManager = mock(DeploymentManager.class);
        when(processEngineConfiguration.getDeploymentManager()).thenReturn(deploymentManager);
        when(deploymentManager.getProcessDefinitionInfoCache()).thenThrow(new RuntimeException("Cache access failed"));

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor)
            .handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expressionWithCondition.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [unknown]")
            .hasMessageNotContaining("Cache access failed");
    }

    private void setupBpmnOverrideContext(
        String processDefinitionId,
        String flowElementId,
        ObjectNode elementProperties
    ) {
        DynamicBpmnService dynamicBpmnService = mock(DynamicBpmnService.class);
        DeploymentManager deploymentManager = mock(DeploymentManager.class);
        ProcessDefinitionInfoCache processDefinitionInfoCache = mock(ProcessDefinitionInfoCache.class);
        ProcessDefinitionInfoCacheObject cacheObject = mock(ProcessDefinitionInfoCacheObject.class);
        ObjectNode infoNode = mock(ObjectNode.class);

        when(processEngineConfiguration.getDynamicBpmnService()).thenReturn(dynamicBpmnService);
        when(processEngineConfiguration.getDeploymentManager()).thenReturn(deploymentManager);
        when(deploymentManager.getProcessDefinitionInfoCache()).thenReturn(processDefinitionInfoCache);
        when(processDefinitionInfoCache.get(processDefinitionId)).thenReturn(cacheObject);
        when(cacheObject.getInfoNode()).thenReturn(infoNode);
        when(dynamicBpmnService.getBpmnElementProperties(eq(flowElementId), any(ObjectNode.class))).thenReturn(
            elementProperties
        );
    }

    private void setupDefaultBpmnOverrideContext() {
        DynamicBpmnService dynamicBpmnService = mock(DynamicBpmnService.class);
        DeploymentManager deploymentManager = mock(DeploymentManager.class);
        ProcessDefinitionInfoCache processDefinitionInfoCache = mock(ProcessDefinitionInfoCache.class);
        ProcessDefinitionInfoCacheObject cacheObject = mock(ProcessDefinitionInfoCacheObject.class);

        when(processEngineConfiguration.getDynamicBpmnService()).thenReturn(dynamicBpmnService);
        when(processEngineConfiguration.getDeploymentManager()).thenReturn(deploymentManager);
        when(deploymentManager.getProcessDefinitionInfoCache()).thenReturn(processDefinitionInfoCache);
        when(processDefinitionInfoCache.get(any())).thenReturn(cacheObject);
        when(cacheObject.getInfoNode()).thenReturn(null);
    }

    private FlowNode mockFlowNodeWithoutOutgoingFlows(String flowNodeId) {
        return mockFlowNode(flowNodeId, Collections.emptyList());
    }

    private FlowNode mockFlowNode(String flowNodeId, List<SequenceFlow> outgoingFlows) {
        FlowNode flowNode = mock(FlowNode.class);
        when(flowNode.getId()).thenReturn(flowNodeId);
        when(flowNode.getOutgoingFlows()).thenReturn(outgoingFlows);
        return flowNode;
    }
}
