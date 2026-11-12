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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Unit tests for {@link EventDefinitionExpressionUtil}.
 * <p>
 * These pin down the documented precedence between {@code signalRef} and
 * {@code activiti:signalExpression}, and the fact that <em>both</em> are evaluated through the
 * expression manager, so a signal name may be dynamic regardless of how it is modelled.
 * <p>
 * A real {@link ExpressionManager} is used (rather than a mock) so that the JUEL evaluation
 * itself is covered; only the engine configuration lookup is stubbed, since
 * {@code JuelExpression} resolves it from the static {@link Context}.
 */
class EventDefinitionExpressionUtilTest {

    private static final String SIGNAL_REF = "signalId";
    private static final String SIGNAL_NAME = "newSignal";

    @BeforeEach
    void setUp() {
        ProcessEngineConfigurationImpl configuration = mock(ProcessEngineConfigurationImpl.class);
        given(configuration.getExpressionManager()).willReturn(new ExpressionManager());
        given(configuration.getDelegateInterceptor()).willReturn(new DefaultDelegateInterceptor());
        given(configuration.getObjectMapper()).willReturn(new JsonMapper());
        Context.setProcessEngineConfiguration(configuration);
    }

    @AfterEach
    void tearDown() {
        Context.removeProcessEngineConfiguration();
    }

    @Test
    void determineSignalName_should_evaluateSignalExpressionAgainstTheVariableScope() {
        // given a definition that only declares activiti:signalExpression="${signalName}"
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalExpression("${signalName}");

        VariableScope variableScope = variableScopeWith("signalName", "order-cancelled");

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            new BpmnModel(),
            variableScope
        );

        // then the runtime variable value is used as the signal name
        assertThat(resolved).isEqualTo("order-cancelled");
    }

    @Test
    void determineSignalName_should_returnSignalExpressionAsIsWhenItIsALiteral() {
        // given a signalExpression that contains no EL at all
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalExpression(SIGNAL_NAME);

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            new BpmnModel(),
            mock(VariableScope.class)
        );

        // then it is passed through unchanged
        assertThat(resolved).isEqualTo(SIGNAL_NAME);
    }

    @Test
    void determineSignalName_should_evaluateSignalExpressionWhenNoVariableScopeIsAvailable() {
        // given a literal expression and no scope (definition-time resolution)
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalExpression(SIGNAL_NAME);

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            new BpmnModel(),
            null
        );

        // then no NPE is thrown and the literal resolves
        assertThat(resolved).isEqualTo(SIGNAL_NAME);
    }

    @Test
    void determineSignalName_should_convertNonStringExpressionResultsToString() {
        // given an expression resolving to a non-String value
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalExpression("${signalName}");

        VariableScope variableScope = variableScopeWith("signalName", 42);

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            new BpmnModel(),
            variableScope
        );

        // then
        assertThat(resolved).isEqualTo("42");
    }

    @Test
    void determineSignalName_should_returnNullWhenTheExpressionResolvesToNull() {
        // given an expression pointing at a variable whose value is null
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalExpression("${signalName}");

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            new BpmnModel(),
            variableScopeWith("signalName", null)
        );

        // then
        assertThat(resolved).isNull();
    }

    @Test
    void determineSignalName_should_preferTheReferencedSignalNameOverTheExpression() {
        // given a definition that declares both a signalRef resolvable in the model and an expression
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalRef(SIGNAL_REF);
        signalEventDefinition.setSignalExpression("${signalName}");

        BpmnModel bpmnModel = modelWithSignal(SIGNAL_REF, SIGNAL_NAME);

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            bpmnModel,
            variableScopeWith("signalName", "should-not-be-used")
        );

        // then signalRef wins: the <signal> name is used and the expression is ignored
        assertThat(resolved).isEqualTo(SIGNAL_NAME);
    }

    @Test
    void determineSignalName_should_evaluateTheReferencedSignalNameAsAnExpression() {
        // given a <signal> whose name is itself an expression
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalRef(SIGNAL_REF);

        BpmnModel bpmnModel = modelWithSignal(SIGNAL_REF, "${signalName}");

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            bpmnModel,
            variableScopeWith("signalName", "order-cancelled")
        );

        // then the signal name is resolved uniformly through the expression manager
        assertThat(resolved).isEqualTo("order-cancelled");
    }

    @Test
    void determineSignalName_should_fallBackToTheSignalRefLiteralWhenItIsNotDeclaredInTheModel() {
        // given a signalRef that does not match any <signal> in the model
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalRef(SIGNAL_NAME);

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            new BpmnModel(),
            mock(VariableScope.class)
        );

        // then the ref itself is treated as the signal name
        assertThat(resolved).isEqualTo(SIGNAL_NAME);
    }

    @Test
    void determineSignalName_should_returnNullWhenNeitherRefNorExpressionIsSet() {
        // given an empty definition
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            new BpmnModel(),
            mock(VariableScope.class)
        );

        // then nothing can be resolved, and no expression evaluation is attempted
        assertThat(resolved).isNull();
    }

    @Test
    void determineSignalName_should_tolerateANullBpmnModel() {
        // given a definition with only an expression and no model available
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalExpression("${signalName}");

        // when
        String resolved = EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            null,
            variableScopeWith("signalName", "order-cancelled")
        );

        // then
        assertThat(resolved).isEqualTo("order-cancelled");
    }

    @Test
    void determineSignal_should_returnTheReferencedSignal() {
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalRef(SIGNAL_REF);

        Signal signal = EventDefinitionExpressionUtil.determineSignal(
            signalEventDefinition,
            modelWithSignal(SIGNAL_REF, SIGNAL_NAME)
        );

        assertThat(signal).isNotNull();
        assertThat(signal.getName()).isEqualTo(SIGNAL_NAME);
    }

    @Test
    void determineSignal_should_returnNullWhenNothingCanBeResolved() {
        SignalEventDefinition withoutRef = new SignalEventDefinition();
        withoutRef.setSignalExpression("${signalName}");

        SignalEventDefinition withUnknownRef = new SignalEventDefinition();
        withUnknownRef.setSignalRef("unknown");

        assertThat(EventDefinitionExpressionUtil.determineSignal(withoutRef, new BpmnModel())).isNull();
        assertThat(EventDefinitionExpressionUtil.determineSignal(withUnknownRef, new BpmnModel())).isNull();
        assertThat(EventDefinitionExpressionUtil.determineSignal(withUnknownRef, null)).isNull();
        assertThat(EventDefinitionExpressionUtil.determineSignal(null, new BpmnModel())).isNull();
    }

    private static BpmnModel modelWithSignal(String id, String name) {
        BpmnModel bpmnModel = new BpmnModel();
        Signal signal = new Signal();
        signal.setId(id);
        signal.setName(name);
        bpmnModel.addSignal(signal);
        return bpmnModel;
    }

    private static VariableScope variableScopeWith(String name, Object value) {
        VariableInstance variableInstance = mock(VariableInstance.class);
        given(variableInstance.getValue()).willReturn(value);

        VariableScope variableScope = mock(VariableScope.class);
        given(variableScope.hasVariable(name)).willReturn(true);
        given(variableScope.getVariableInstance(name)).willReturn(variableInstance);
        return variableScope;
    }
}
