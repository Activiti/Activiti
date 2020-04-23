/*
 * Copyright 2020 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import javax.el.ELContext;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.variable.AuthenticatedUserELResolver;
import org.activiti.engine.impl.el.variable.ExecutionElResolver;
import org.activiti.engine.impl.el.variable.ProcessInitiatorELResolver;
import org.activiti.engine.impl.el.variable.TaskElResolver;
import org.activiti.engine.impl.el.variable.VariableElResolver;
import org.activiti.engine.impl.el.variable.VariableScopeItemELResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

public class VariableScopeElResolverTest {

    @Spy
    @InjectMocks
    private VariableScopeElResolver variableScopeElResolver;

    @Mock
    private VariableScope variableScope;

    @Mock
    private VariableScopeItemELResolver firstItemResolver;

    @Mock
    private VariableScopeItemELResolver secondItemResolver;

    @Mock
    private VariableScopeItemELResolver thirdItemResolver;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        doReturn(Arrays.asList(firstItemResolver, secondItemResolver, thirdItemResolver)).when(
            variableScopeElResolver).getVariableScopeItemELResolvers();
    }


    @Test
    public void getValue_should_returnResolvedValueAndMarkContextAsResolved() {
        //given
        String property = "myVar";
        ELContext elContext = mock(ELContext.class);

        given(firstItemResolver.canResolve(property, variableScope)).willReturn(false);
        given(secondItemResolver.canResolve(property, variableScope)).willReturn(true);
        given(secondItemResolver.resolve(property, variableScope)).willReturn("myValue");

        //when
        Object result = variableScopeElResolver.getValue(elContext, null, property);

        //then
        assertThat(result).isEqualTo("myValue");
        verify(elContext).setPropertyResolved(true);
        verifyZeroInteractions(thirdItemResolver);
    }

    @Test
    public void getValue_should_returnNullWhenNoneOfItemResolversCanResolveTheProperty() {
        //given
        String property = "myVar";
        ELContext elContext = mock(ELContext.class);

        given(firstItemResolver.canResolve(property, variableScope)).willReturn(false);
        given(secondItemResolver.canResolve(property, variableScope)).willReturn(false);
        given(secondItemResolver.canResolve(property, variableScope)).willReturn(false);

        //when
        Object result = variableScopeElResolver.getValue(elContext, null, property);

        //then
        assertThat(result).isNull();
        verifyZeroInteractions(elContext);
    }


    @Test
    public void getVariableScopeItemELResolvers_should_return_defaultItemResolvers() {
        //given
        ProcessEngineConfigurationImpl processEngineConfiguration = mock(ProcessEngineConfigurationImpl.class);
        given(processEngineConfiguration.getObjectMapper()).willReturn(new ObjectMapper());
        Context.setProcessEngineConfiguration(processEngineConfiguration);
        doCallRealMethod().when(variableScopeElResolver).getVariableScopeItemELResolvers();

        //when
        List<VariableScopeItemELResolver> variableScopeItemELResolvers = variableScopeElResolver
            .getVariableScopeItemELResolvers();
        //then
        assertThat(variableScopeItemELResolvers)
            .extracting(itemResolver -> itemResolver.getClass().getName())
            .containsExactly(
                ExecutionElResolver.class.getName(),
                TaskElResolver.class.getName(),
                AuthenticatedUserELResolver.class.getName(),
                ProcessInitiatorELResolver.class.getName(),
                VariableElResolver.class.getName()
            );

    }
}
