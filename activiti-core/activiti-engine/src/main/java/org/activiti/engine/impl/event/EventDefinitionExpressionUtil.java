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
package org.activiti.engine.impl.event;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.NoExecutionVariableScope;

/**
 * Central place for resolving the runtime event name of a {@link SignalEventDefinition}.
 * <p>
 * Historically each signal behavior resolved the name slightly differently, which only
 * happened to work because models typically declare {@code <signal id="X" name="X"/>}.
 * This utility applies a single, documented precedence:
 * <ol>
 *   <li>if {@code signalRef} is set and resolves to a {@link Signal} in the model, the signal's
 *       <em>name</em> is used;</li>
 *   <li>otherwise the {@code signalRef} literal is used;</li>
 *   <li>if no {@code signalRef} is set at all, the {@code signalExpression} is used.</li>
 * </ol>
 * The resulting value is always run through the expression manager, so both a literal name and
 * an expression-based one are supported uniformly.
 * <p>
 * Modelled on Flowable's {@code EventDefinitionExpressionUtil} (Apache License 2.0).
 */
public final class EventDefinitionExpressionUtil {

    private EventDefinitionExpressionUtil() {}

    /**
     * Resolves the signal name, evaluating it against the given variable scope.
     *
     * @param signalEventDefinition the event definition to resolve; must not be {@code null}
     * @param bpmnModel the model used to look up a {@link Signal} by {@code signalRef}; may be {@code null}
     * @param variableScope scope used to evaluate the expression; when {@code null} no runtime
     *                      variables are available and only definition-time constructs can resolve
     * @return the resolved signal name, or {@code null} when neither a ref nor an expression is set
     */
    public static String determineSignalName(
        SignalEventDefinition signalEventDefinition,
        BpmnModel bpmnModel,
        VariableScope variableScope
    ) {
        String signalName = resolveRawSignalName(signalEventDefinition, bpmnModel);

        if (signalName == null || signalName.isEmpty()) {
            return signalName;
        }

        ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
        Expression expression = expressionManager.createExpression(signalName);
        Object value = expression.getValue(
            variableScope != null ? variableScope : NoExecutionVariableScope.getSharedInstance()
        );

        return value != null ? value.toString() : null;
    }

    /**
     * Resolves the {@link Signal} referenced by the given definition, if any.
     *
     * @return the referenced signal, or {@code null} when the definition has no resolvable {@code signalRef}
     */
    public static Signal determineSignal(SignalEventDefinition signalEventDefinition, BpmnModel bpmnModel) {
        if (bpmnModel == null || signalEventDefinition == null) {
            return null;
        }
        String signalRef = signalEventDefinition.getSignalRef();
        if (signalRef == null || signalRef.isEmpty()) {
            return null;
        }
        return bpmnModel.getSignal(signalRef);
    }

    private static String resolveRawSignalName(SignalEventDefinition signalEventDefinition, BpmnModel bpmnModel) {
        String signalRef = signalEventDefinition.getSignalRef();
        if (signalRef != null && !signalRef.isEmpty()) {
            Signal signal = determineSignal(signalEventDefinition, bpmnModel);
            return signal != null ? signal.getName() : signalRef;
        }
        return signalEventDefinition.getSignalExpression();
    }
}
