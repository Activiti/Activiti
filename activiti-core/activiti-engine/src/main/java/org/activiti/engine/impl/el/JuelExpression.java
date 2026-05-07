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

import tools.jackson.databind.JsonNode;
import jakarta.el.ELContext;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.invocation.ExpressionGetInvocation;
import org.activiti.engine.impl.delegate.invocation.ExpressionSetInvocation;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.springframework.util.StringUtils;

/**
 * Expression implementation backed by a JUEL {@link ValueExpression}.
 *
 */
public class JuelExpression implements Expression {

    private String expressionText;
    private ValueExpression valueExpression;

    public JuelExpression(ValueExpression valueExpression, String expressionText) {
        this.valueExpression = valueExpression;
        this.expressionText = expressionText;
    }

    @Override
    public Object getValue(VariableScope variableScope) {
        ELContext elContext = Context.getProcessEngineConfiguration()
            .getExpressionManager()
            .getElContext(variableScope);
        return getValueFromContextWithScope(elContext, Context.getProcessEngineConfiguration().getDelegateInterceptor(), variableScope);
    }

    @Override
    public void setValue(Object value, VariableScope variableScope) {
        ELContext elContext = Context.getProcessEngineConfiguration()
            .getExpressionManager()
            .getElContext(variableScope);
        try {
            ExpressionSetInvocation invocation = new ExpressionSetInvocation(valueExpression, elContext, value);
            Context.getProcessEngineConfiguration().getDelegateInterceptor().handleInvocation(invocation);
        } catch (Exception e) {
            throw new ActivitiException("Error while evaluating expression: " + expressionText, e);
        }
    }

    @Override
    public String toString() {
        if (valueExpression != null) {
            return valueExpression.getExpressionString();
        }
        return super.toString();
    }

    @Override
    public String getExpressionText() {
        return expressionText;
    }

    @Override
    public Object getValue(
        ExpressionManager expressionManager,
        DelegateInterceptor delegateInterceptor,
        Map<String, Object> availableVariables
    ) {
        ELContext elContext = expressionManager.getElContext(availableVariables);
        return getValueFromContext(elContext, delegateInterceptor);
    }

    private Object getValueFromContext(ELContext elContext, DelegateInterceptor delegateInterceptor) {
        try {
            return this.evaluateExpression(elContext, delegateInterceptor);
        } catch (PropertyNotFoundException pnfe) {
            throw new ActivitiException("Unknown property used in expression: " + expressionText, pnfe);
        } catch (MethodNotFoundException mnfe) {
            throw new ActivitiException("Unknown method used in expression: " + expressionText, mnfe);
        } catch (Exception ele) {
            throw new ActivitiException("Error while evaluating expression: " + expressionText, ele);
        }
    }

    private Object getValueFromContextWithScope(ELContext elContext, DelegateInterceptor delegateInterceptor, VariableScope variableScope) {
        try {
            return this.evaluateExpression(elContext, delegateInterceptor);
        } catch (Exception e) {
            String contextInfo = ExpressionContext.from(variableScope, expressionText).formatContextInfo();
            if (e instanceof PropertyNotFoundException) {
                throw new ActivitiException("Unknown property used in expression: " + expressionText + contextInfo, e);
            } else if (e instanceof MethodNotFoundException) {
                throw new ActivitiException("Unknown method used in expression: " + expressionText + contextInfo, e);
            } else {
                throw new ActivitiException("Error while evaluating expression: " + expressionText + contextInfo, e);
            }
        }
    }

    private Object evaluateExpression(ELContext elContext, DelegateInterceptor delegateInterceptor) {
        ExpressionGetInvocation invocation = new ExpressionGetInvocation(valueExpression, elContext);
        delegateInterceptor.handleInvocation(invocation);
        return invocation.getInvocationResult();
    }

    private record ExpressionContext(String flowElementId, String sequenceFlowId) {

        private static final String UNKNOWN_ID = "unknown";

        static ExpressionContext from(VariableScope variableScope, String expressionText) {
            Optional<FlowElement> flowElementOptional = extractFlowElement(variableScope);
            String flowElementId = flowElementOptional.map(FlowElement::getId).filter(StringUtils::hasText).orElse(UNKNOWN_ID);
            String sequenceFlowId = safeGet(
                () -> extractSequenceFlow(variableScope, flowElementOptional, expressionText).map(SequenceFlow::getId).filter(StringUtils::hasText).orElse(UNKNOWN_ID),
                UNKNOWN_ID
            );
            return new ExpressionContext(flowElementId, sequenceFlowId);
        }

        private static <T> T safeGet(Supplier<T> supplier, T defaultValue) {
            return Optional.ofNullable(supplier).map(s -> {
                try {
                    return s.get();
                } catch (Exception _) {
                    return defaultValue;
                }
            }).orElse(defaultValue);
        }

        private static Optional<FlowElement> extractFlowElement(VariableScope variableScope) {
            return Optional.ofNullable(
                (variableScope instanceof DelegateExecution execution)
                    ? execution.getCurrentFlowElement()
                    : null
            );
        }

        private static Optional<SequenceFlow> extractSequenceFlow(VariableScope variableScope, Optional<FlowElement> flowElementOpt, String expressionText) {

            return flowElementOpt
                .filter(FlowNode.class::isInstance)
                .map(FlowNode.class::cast)
                .flatMap(flowNode -> flowNode.getOutgoingFlows().stream()
                    .filter(flow -> findActiveConditionExpression(variableScope, expressionText).test(flow))
                    .findFirst()
                );
        }

        private static Predicate<SequenceFlow> findActiveConditionExpression(VariableScope variableScope, String expressionText) {
            return (sequenceFlow -> {
                var activeCondition = getActiveConditionExpression(sequenceFlow.getConditionExpression(), sequenceFlow, variableScope);
                return Objects.equals(activeCondition, expressionText);
            });
        }

        private static String getActiveConditionExpression(String originalExpression, SequenceFlow sequenceFlow, VariableScope variableScope) {
            if (variableScope instanceof DelegateExecution execution) {
                var processDefinitionId = execution.getProcessDefinitionId();
                var flowElementId = sequenceFlow.getId();
                var objectNode = Context.getBpmnOverrideElementProperties(flowElementId, processDefinitionId);
                JsonNode newValue;
                if (objectNode != null && (newValue = objectNode.get(DynamicBpmnConstants.SEQUENCE_FLOW_CONDITION)) != null) {
                    if (newValue.isNull()) return null;
                    return newValue.asText();
                }
            }
            return originalExpression;
        }

        String formatContextInfo() {
            return " flowElementId: [" + flowElementId + "], sequenceFlowId: [" + sequenceFlowId + "]";
        }
    }
}
