/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ProcessInstanceBuilderImplTest {

    @InjectMocks
    private ProcessInstanceBuilderImpl processInstanceBuilder;

    @Mock
    private RuntimeServiceImpl runtimeService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void hasProcessDefinitionIdOrKey_shouldReturnTrue_WhenProcessDefinitionIdIsSet() {
        //given
        processInstanceBuilder.processDefinitionId("procDefId");

        //when
        boolean hasProcessDefinitionIdOrKey = processInstanceBuilder.hasProcessDefinitionIdOrKey();

        //then
        assertThat(hasProcessDefinitionIdOrKey).isTrue();
    }

    @Test
    public void hasProcessDefinitionIdOrKey_shouldReturnTrue_WhenProcessDefinitionKeyIsSet() {
        //given
        processInstanceBuilder.processDefinitionKey("procDefKey");

        //when
        boolean hasProcessDefinitionIdOrKey = processInstanceBuilder.hasProcessDefinitionIdOrKey();

        //then
        assertThat(hasProcessDefinitionIdOrKey).isTrue();
    }

    @Test
    public void hasProcessDefinitionIdOrKey_shouldReturnFalse_WhenNoneOfProcessDefinitionIdOrKeyIsSet() {
        //given
        processInstanceBuilder.processDefinitionId(null);
        processInstanceBuilder.processDefinitionKey(null);

        //when
        boolean hasProcessDefinitionIdOrKey = processInstanceBuilder.hasProcessDefinitionIdOrKey();

        //then
        assertThat(hasProcessDefinitionIdOrKey).isFalse();
    }

    @Test
    public void create_shouldDelegateCreationToRuntimeService() {
        //given
        ProcessInstance processInstance = mock(
            ProcessInstance.class);
        given(runtimeService.createProcessInstance(processInstanceBuilder)).willReturn(processInstance);
        //when
        ProcessInstance createdProcess = processInstanceBuilder.create();
        //then
        assertThat(createdProcess).isEqualTo(processInstance);
    }

}
