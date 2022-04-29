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
package org.activiti.engine.impl.el.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.junit.Test;

public class VariableElResolverTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private VariableElResolver resolver = new VariableElResolver(objectMapper);

    @Test
    public void canResolve_should_returnTrueWhenVariableScopeHasVariableForProperty() {
        //given
        VariableScope variableScope = mock(VariableScope.class);
        given(variableScope.hasVariable("myVar")).willReturn(true);

        //when
        boolean canResolve = resolver.canResolve("myVar", variableScope);

        //then
        assertThat(canResolve).isTrue();
    }

    @Test
    public void canResolve_should_returnFalseWhenVariableScopeDoesNotHaveVariableForProperty() {
        //given
        VariableScope variableScope = mock(VariableScope.class);
        given(variableScope.hasVariable("myVar")).willReturn(false);

        //when
        boolean canResolve = resolver.canResolve("myVar", variableScope);

        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void resolve_should_returnVariableInstanceValueWhenItsNotJsonArray() {
        //given
        VariableScope variableScope = buildVariableScope("myVar", "myValue", "string");

        //when
        Object result = resolver.resolve("myVar", variableScope);

        //then
        assertThat(result).isEqualTo("myValue");
    }

    @Test
    public void resolve_should_returnVariableInstanceValueConvertedToListWhenItsJsonArray() throws Exception {
        //given
        JsonNode jsonNode = objectMapper.readTree("[\"green\", \"blue\", \"red\"]");
        VariableScope variableScope = buildVariableScope("colors", jsonNode, "json");

        //when
        Object result = resolver.resolve("colors", variableScope);

        //then
        assertThat(result).isEqualTo(Arrays.asList("green", "blue", "red"));
    }

    private VariableScope buildVariableScope(String variableName, Object variableValue, String type) {
        VariableScope variableScope = mock(VariableScope.class);
        VariableInstance variableInstance = mock(VariableInstance.class);
        given(variableInstance.getValue()).willReturn(variableValue);
        given(variableInstance.getTypeName()).willReturn(type);
        given(variableScope.getVariableInstance(variableName)).willReturn(variableInstance);
        return variableScope;
    }

}
