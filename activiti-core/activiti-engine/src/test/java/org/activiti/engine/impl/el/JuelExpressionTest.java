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
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.invocation.ExpressionGetInvocation;
import org.activiti.engine.impl.delegate.invocation.ExpressionSetInvocation;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JuelExpressionTest {

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

    @Before
    public void setUp() {
        Context.setProcessEngineConfiguration(processEngineConfiguration);
        when(processEngineConfiguration.getExpressionManager()).thenReturn(expressionManager);
        when(processEngineConfiguration.getDelegateInterceptor()).thenReturn(delegateInterceptor);
        when(expressionManager.getElContext(any(VariableScope.class))).thenReturn(elContext);

        juelExpression = new JuelExpression(valueExpression, "${myVar}");
    }

    @After
    public void tearDown() {
        Context.setProcessEngineConfiguration(null);
    }

    @Test
    public void should_returnExpressionValue() {
        String expectedValue = "testValue";
        doAnswer(invocation -> {
            ExpressionGetInvocation getInvocation = invocation.getArgument(0);
            when(valueExpression.getValue(elContext)).thenReturn(expectedValue);
            getInvocation.proceed();
            return null;
        }).when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        Object result = juelExpression.getValue(variableScope);

        assertThat(result).isEqualTo(expectedValue);
        verify(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));
    }

    @Test
    public void should_wrapPropertyNotFound_when_resolvingValue() {
        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown property used in expression: ${myVar}")
            .hasCauseInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    public void should_wrapMethodNotFound_when_resolvingValue() {
        doThrow(new MethodNotFoundException("Method not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown method used in expression: ${myVar}")
            .hasCauseInstanceOf(MethodNotFoundException.class);
    }

    @Test
    public void should_wrapGenericException_when_resolvingValue() {
        doThrow(new RuntimeException("Generic error"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Error while evaluating expression: ${myVar}")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_includeFlowElementContext_when_delegateExecutionPresent() {
        FlowNode flowNode = mock(FlowNode.class);
        when(flowNode.getId()).thenReturn("serviceTask1");
        when(flowNode.getOutgoingFlows()).thenReturn(Collections.emptyList());

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown property used in expression: ${myVar}")
            .hasMessageContaining("flowElementId: [serviceTask1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    public void should_includeFlowAndSequenceContext_when_sequenceFlowMatchesCondition() {
        String conditionExpression = "${myVar}";
        JuelExpression expressionWithCondition = new JuelExpression(valueExpression, conditionExpression);

        FlowNode flowNode = mock(FlowNode.class);
        when(flowNode.getId()).thenReturn("exclusiveGateway1");

        SequenceFlow sequenceFlow = mock(SequenceFlow.class);
        when(sequenceFlow.getId()).thenReturn("flow1");
        when(sequenceFlow.getConditionExpression()).thenReturn(conditionExpression);

        when(flowNode.getOutgoingFlows()).thenReturn(Collections.singletonList(sequenceFlow));
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> expressionWithCondition.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown property used in expression: " + conditionExpression)
            .hasMessageContaining("flowElementId: [exclusiveGateway1]")
            .hasMessageContaining("sequenceFlowId: [flow1]");
    }

    @Test
    public void should_showUnknownContext_when_noDelegateExecution() {
        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [unknown]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    public void should_setExpressionValue() {
        String valueToSet = "newValue";
        doAnswer(invocation -> {
            ExpressionSetInvocation setInvocation = invocation.getArgument(0);
            Object[] params = setInvocation.getInvocationParameters();
            assertThat(params).hasSize(1);
            assertThat(params[0]).isEqualTo(valueToSet);
            return null;
        }).when(delegateInterceptor).handleInvocation(any(ExpressionSetInvocation.class));

        juelExpression.setValue(valueToSet, variableScope);

        verify(delegateInterceptor).handleInvocation(any(ExpressionSetInvocation.class));
    }

    @Test
    public void should_wrapException_when_settingValue() {
        doThrow(new RuntimeException("Set value error"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionSetInvocation.class));

        assertThatThrownBy(() -> juelExpression.setValue("value", variableScope))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Error while evaluating expression: ${myVar}")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_returnExpressionString_when_valueExpressionPresent() {
        String expectedString = "${myVar}";
        when(valueExpression.getExpressionString()).thenReturn(expectedString);

        String result = juelExpression.toString();

        assertThat(result).isEqualTo(expectedString);
    }

    @Test
    public void should_returnDefaultToString_when_valueExpressionNull() {
        JuelExpression expressionWithNull = new JuelExpression(null, "${myVar}");

        String result = expressionWithNull.toString();

        assertThat(result).contains("JuelExpression");
    }

    @Test
    public void should_returnOriginalExpressionText() {
        String result = juelExpression.getExpressionText();

        assertThat(result).isEqualTo("${myVar}");
    }

    @Test
    public void should_evaluateExpression_withExpressionManagerAndVariablesMap() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("myVar", "testValue");

        when(expressionManager.getElContext(variables)).thenReturn(elContext);

        String expectedValue = "testValue";
        doAnswer(invocation -> {
            ExpressionGetInvocation getInvocation = invocation.getArgument(0);
            when(valueExpression.getValue(elContext)).thenReturn(expectedValue);
            getInvocation.proceed();
            return null;
        }).when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        Object result = juelExpression.getValue(expressionManager, delegateInterceptor, variables);

        assertThat(result).isEqualTo(expectedValue);
        verify(expressionManager).getElContext(variables);
    }

    @Test
    public void should_notIncludeFlowContext_when_expressionManagerThrows() {
        Map<String, Object> variables = new HashMap<>();
        when(expressionManager.getElContext(variables)).thenReturn(elContext);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(expressionManager, delegateInterceptor, variables))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Unknown property used in expression: ${myVar}")
            .hasMessageNotContaining("flowElementId")
            .hasMessageNotContaining("sequenceFlowId");
    }

    @Test
    public void should_showUnknown_when_flowElementIdEmpty() {
        FlowNode flowNode = mock(FlowNode.class);
        when(flowNode.getId()).thenReturn("");
        when(flowNode.getOutgoingFlows()).thenReturn(Collections.emptyList());

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [unknown]");
    }

    @Test
    public void should_showUnknown_when_flowElementIdNull() {
        FlowNode flowNode = mock(FlowNode.class);
        when(flowNode.getId()).thenReturn(null);
        when(flowNode.getOutgoingFlows()).thenReturn(Collections.emptyList());

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [unknown]");
    }

    @Test
    public void should_notExtractSequenceFlow_when_flowElementIsNotFlowNode() {
        FlowElement flowElement = mock(FlowElement.class);
        when(flowElement.getId()).thenReturn("startEvent1");

        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowElement);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> juelExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [startEvent1]")
            .hasMessageContaining("sequenceFlowId: [unknown]");
    }

    @Test
    public void should_matchSequenceFlowByConditionExpression_when_multipleOutgoingFlows() {
        String targetCondition = "${approved == true}";
        JuelExpression targetExpression = new JuelExpression(valueExpression, targetCondition);

        FlowNode flowNode = mock(FlowNode.class);
        when(flowNode.getId()).thenReturn("gateway1");

        SequenceFlow flow1 = mock(SequenceFlow.class);
        when(flow1.getConditionExpression()).thenReturn("${rejected}");

        SequenceFlow flow2 = mock(SequenceFlow.class);
        when(flow2.getId()).thenReturn("flow2");
        when(flow2.getConditionExpression()).thenReturn(targetCondition);

        SequenceFlow flow3 = mock(SequenceFlow.class);

        List<SequenceFlow> flows = List.of(flow1, flow2, flow3);
        when(flowNode.getOutgoingFlows()).thenReturn(flows);
        when(delegateExecution.getCurrentFlowElement()).thenReturn(flowNode);

        doThrow(new PropertyNotFoundException("Property not found"))
            .when(delegateInterceptor).handleInvocation(any(ExpressionGetInvocation.class));

        assertThatThrownBy(() -> targetExpression.getValue(delegateExecution))
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("flowElementId: [gateway1]")
            .hasMessageContaining("sequenceFlowId: [flow2]");
    }

}
