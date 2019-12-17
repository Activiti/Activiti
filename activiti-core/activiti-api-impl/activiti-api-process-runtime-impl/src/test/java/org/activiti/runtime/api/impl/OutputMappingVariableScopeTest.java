package org.activiti.runtime.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.junit.Before;
import org.junit.Test;

public class OutputMappingVariableScopeTest {

    private OutputMappingVariableScope outputMappingVariableScope;
    private Map<String, Object> variables = new HashMap<>();
    private List<String> requestedVariables = new ArrayList<String>();

    @Before
    public void setUp() {
        variables.put("one", 1);
        variables.put("two", "2");
        variables.put("three", true);
        variables.put("four", new HashMap<>());
        outputMappingVariableScope = new OutputMappingVariableScope(variables);

        requestedVariables.add("one");
        requestedVariables.add("two");
    }

    @Test
    public void should_returnEmptyCollection_when_noArgumentsConstructorIsUsedAndVariablesRetrieved() {
        outputMappingVariableScope = new OutputMappingVariableScope();

        assertThat(outputMappingVariableScope.hasVariable("one")).isFalse();
        assertThat(outputMappingVariableScope.hasVariableLocal("one")).isFalse();

        assertThat(outputMappingVariableScope.hasVariables()).isFalse();
        assertThat(outputMappingVariableScope.hasVariablesLocal()).isFalse();

        assertThat(outputMappingVariableScope.getVariableNames()).isEmpty();
        assertThat(outputMappingVariableScope.getVariableNamesLocal()).isEmpty();

        assertThat(outputMappingVariableScope.getVariable("one")).isNull();
        assertThat(outputMappingVariableScope.getVariable("one", true)).isNull();
        assertThat(outputMappingVariableScope.getVariableLocal("one")).isNull();
        assertThat(outputMappingVariableScope.getVariableLocal("one", true)).isNull();
        assertThat(outputMappingVariableScope.getVariable("one", Integer.class)).isNull();
        assertThat(outputMappingVariableScope.getVariableLocal("one", Integer.class)).isNull();

        assertThat(outputMappingVariableScope.getVariables()).isEmpty();
        assertThat(outputMappingVariableScope.getVariables(requestedVariables)).isEmpty();
        assertThat(outputMappingVariableScope.getVariables(requestedVariables, true)).isEmpty();
        assertThat(outputMappingVariableScope.getVariablesLocal()).isEmpty();
        assertThat(outputMappingVariableScope.getVariablesLocal(requestedVariables)).isEmpty();
        assertThat(outputMappingVariableScope.getVariablesLocal(requestedVariables, true)).isEmpty();

        assertThat(outputMappingVariableScope.getVariableInstance("one")).isNull();
        assertThat(outputMappingVariableScope.getVariableInstance("one", true)).isNull();
        assertThat(outputMappingVariableScope.getVariableInstanceLocal("one")).isNull();
        assertThat(outputMappingVariableScope.getVariableInstanceLocal("one", true)).isNull();

        assertThat(outputMappingVariableScope.getVariableInstances()).isEmpty();
        assertThat(outputMappingVariableScope.getVariableInstances(requestedVariables)).isEmpty();
        assertThat(outputMappingVariableScope.getVariableInstances(requestedVariables, true)).isEmpty();
        assertThat(outputMappingVariableScope.getVariableInstancesLocal()).isEmpty();
        assertThat(outputMappingVariableScope.getVariableInstancesLocal(requestedVariables)).isEmpty();
        assertThat(outputMappingVariableScope.getVariableInstancesLocal(requestedVariables, true)).isEmpty();
    }

    @Test
    public void should_returnValues_when_argumentsConstructorIsUsedAndVariablesRetrieved() {
        assertThat(outputMappingVariableScope.hasVariable("one")).isTrue();
        assertThat(outputMappingVariableScope.hasVariableLocal("one")).isTrue();

        assertThat(outputMappingVariableScope.hasVariables()).isTrue();
        assertThat(outputMappingVariableScope.hasVariablesLocal()).isTrue();

        assertThat(outputMappingVariableScope.getVariableNames().size()).isEqualTo(variables.size());
        assertThat(outputMappingVariableScope.getVariableNamesLocal().size()).isEqualTo(variables.size());

        assertThat(outputMappingVariableScope.getVariable("one")).isEqualTo(1);
        assertThat(outputMappingVariableScope.getVariable("one", true)).isEqualTo(1);
        assertThat(outputMappingVariableScope.getVariableLocal("one")).isEqualTo(1);
        assertThat(outputMappingVariableScope.getVariableLocal("one", true)).isEqualTo(1);
        assertThat(outputMappingVariableScope.getVariable("one", Integer.class)).isEqualTo(1);
        assertThat(outputMappingVariableScope.getVariableLocal("one", Integer.class)).isEqualTo(1);

        assertThat(outputMappingVariableScope.getVariables().size()).isEqualTo(variables.size());
        assertThat(outputMappingVariableScope.getVariables(requestedVariables).size()).isEqualTo(requestedVariables
                                                                                                                   .size());
        assertThat(outputMappingVariableScope.getVariables(requestedVariables, true).size()).isEqualTo(
                                                                                                       requestedVariables
                                                                                                                         .size());
        assertThat(outputMappingVariableScope.getVariablesLocal().size()).isEqualTo(variables.size());
        assertThat(outputMappingVariableScope.getVariablesLocal(requestedVariables).size()).isEqualTo(requestedVariables
                                                                                                                        .size());
        assertThat(outputMappingVariableScope.getVariablesLocal(requestedVariables, true).size()).isEqualTo(
                                                                                                            requestedVariables
                                                                                                                              .size());

        assertThat(outputMappingVariableScope.getVariableInstance("one").getTextValue()).isEqualTo("1");
        assertThat(outputMappingVariableScope.getVariableInstance("one", true).getTextValue()).isEqualTo("1");
        assertThat(outputMappingVariableScope.getVariableInstanceLocal("one").getTextValue()).isEqualTo("1");
        assertThat(outputMappingVariableScope.getVariableInstanceLocal("one", true).getTextValue()).isEqualTo("1");

        assertThat(outputMappingVariableScope.getVariableInstances().size()).isEqualTo(variables.size());
        assertThat(outputMappingVariableScope.getVariableInstances(requestedVariables).size()).isEqualTo(
                                                                                                         requestedVariables
                                                                                                                           .size());
        assertThat(outputMappingVariableScope.getVariableInstances(requestedVariables, true).size()).isEqualTo(
                                                                                                               requestedVariables
                                                                                                                                 .size());
        assertThat(outputMappingVariableScope.getVariableInstancesLocal().size()).isEqualTo(variables.size());
        assertThat(outputMappingVariableScope.getVariableInstancesLocal(requestedVariables).size()).isEqualTo(
                                                                                                              requestedVariables
                                                                                                                                .size());
        assertThat(outputMappingVariableScope.getVariableInstancesLocal(requestedVariables, true).size()).isEqualTo(
                                                                                                                    requestedVariables.size());
    }

    @Test
    public void should_throwException_when_variableInstanceSetMethodsAreCalled() {
        VariableInstance instance = outputMappingVariableScope.getVariableInstance("one");
        assertThatThrownBy(() -> instance.setBytes(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setCachedValue(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setDeleted(false)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setDoubleValue(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setExecutionId(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setId(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setInserted(false)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setLongValue(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setName(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setProcessInstanceId(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setRevision(0)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setTaskId(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setTextValue(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setTextValue2(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setTypeName(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setUpdated(false)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> instance.setValue(null)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void should_returnValue_when_variableInstanceGetMethodsAreCalled() {
        VariableInstance instance = outputMappingVariableScope.getVariableInstance("one");
        assertThat(instance.getBytes()).isNotNull();
        assertThat(instance.getCachedValue()).isNotNull();
        assertThat(instance.isDeleted()).isFalse();
        assertThat(instance.getDoubleValue()).isNull();
        assertThat(instance.getExecutionId()).isNull();
        assertThat(instance.getId()).isNotNull();
        assertThat(instance.isInserted()).isTrue();
        assertThat(instance.getLongValue()).isNull();
        assertThat(instance.getName()).isNotNull();
        assertThat(instance.getPersistentState()).isNull();
        assertThat(instance.getProcessInstanceId()).isNull();
        assertThat(instance.getRevision()).isNotNull();
        assertThat(instance.getRevisionNext()).isNotNull();
        assertThat(instance.getTaskId()).isNull();
        assertThat(instance.getTextValue()).isNotNull();
        assertThat(instance.getTextValue2()).isNotNull();
        assertThat(instance.isUpdated()).isTrue();
        assertThat(instance.getValue()).isNotNull();
    }

    @Test
    public void should_returnTheCorrectType_when_variableInstanceTypeIsRetrieved() {
        assertThat(outputMappingVariableScope.getVariableInstance("one").getTypeName()).isEqualTo("integer");
        assertThat(outputMappingVariableScope.getVariableInstance("two").getTypeName()).isEqualTo("string");
        assertThat(outputMappingVariableScope.getVariableInstance("three").getTypeName()).isEqualTo("boolean");
        assertThat(outputMappingVariableScope.getVariableInstance("four").getTypeName()).isEqualTo("json");
    }
}
