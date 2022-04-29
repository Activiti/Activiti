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

package org.activiti.engine.impl.bpmn.behavior;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.activiti.bpmn.model.Activity;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.cmd.CompleteTaskCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

public class ParallelMultiInstanceBehaviorTest {

    @Spy
    @InjectMocks
    private ParallelMultiInstanceBehavior multiInstanceBehavior;

    @Mock
    private Activity activity;

    @Mock
    private AbstractBpmnActivityBehavior innerActivityBehavior;

    @Mock
    private CommandContext commandContext;

    private AutoCloseable autoCloseable;

    @Before
    public void setUp() {
        autoCloseable = openMocks(this);
        doReturn(commandContext).when(multiInstanceBehavior).getCommandContext();
    }

    @After
    public void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void hasOutputDataItem_should_returnTrue_when_outputDataItemIsSet() {
        //given
        multiInstanceBehavior.setOutputDataItem("outItem");

        //when
        boolean hasOutputDataItem = multiInstanceBehavior.hasOutputDataItem();

        //then
        assertThat(hasOutputDataItem).isTrue();
    }

    @Test
    public void hasOutputDataItem_should_returnFalse_when_outputDataItemIsNull() {
        //given
        multiInstanceBehavior.setOutputDataItem(null);

        //when
        boolean hasOutputDataItem = multiInstanceBehavior.hasOutputDataItem();

        //then
        assertThat(hasOutputDataItem).isFalse();
    }

    @Test
    public void hasOutputDataItem_should_returnFalse_when_outputDataItemIsEmpty() {
        //given
        multiInstanceBehavior.setOutputDataItem("");

        //when
        boolean hasOutputDataItem = multiInstanceBehavior.hasOutputDataItem();

        //then
        assertThat(hasOutputDataItem).isFalse();
    }
    @Test
    public void hasLoopDataOutputRef_should_returnTrue_when_dataOutputRefIsSet() {
        //given
        multiInstanceBehavior.setLoopDataOutputRef("DataOutputRef");

        //when
        boolean hasLoopDataOutputRef = multiInstanceBehavior.hasLoopDataOutputRef();

        //then
        assertThat(hasLoopDataOutputRef).isTrue();
    }

    @Test
    public void hasLoopDataOutputRef_should_returnFalse_when_dataOutputRefIsNull() {
        //given
        multiInstanceBehavior.setLoopDataOutputRef(null);

        //when
        boolean hasLoopDataOutputRef = multiInstanceBehavior.hasLoopDataOutputRef();

        //then
        assertThat(hasLoopDataOutputRef).isFalse();
    }

    @Test
    public void hasLoopDataOutputRef_should_returnFalse_when_dataOutputRefIsEmpty() {
        //given
        multiInstanceBehavior.setLoopDataOutputRef("");

        //when
        boolean hasLoopDataOutputRef = multiInstanceBehavior.hasLoopDataOutputRef();

        //then
        assertThat(hasLoopDataOutputRef).isFalse();
    }

    @Test
    public void getResultItemElement_should_useExecutionVariablesLocal() {
        //given
        Map<String, Object> variablesLocal = Collections.singletonMap("var", "value");
        DelegateExecution childExecution = mock(DelegateExecution.class);
        given(childExecution.getVariablesLocal()).willReturn(variablesLocal);
        given(multiInstanceBehavior.getResultElementItem(variablesLocal)).willReturn("result");

        //when
        Object resultElementItem = multiInstanceBehavior
            .getResultElementItem(childExecution);

        //then
        assertThat(resultElementItem).isEqualTo("result");
    }

    @Test
    public void getResultItemElement_should_returnOutputDataItem_when_outputDataItemIsSet() {
        //given
        Map<String, Object> variables = Map.of("name", "John", "city", "London");
        multiInstanceBehavior.setOutputDataItem("city");

        //when
        Object resultElementItem = multiInstanceBehavior.getResultElementItem(variables);

        //then
        assertThat(resultElementItem).isEqualTo("London");
    }

    @Test
    public void getResultItemElement_should_returnAllVariablesExcludingControlVariables_when_noOutputDataItem() {
        //given
        multiInstanceBehavior.setOutputDataItem(null);
        multiInstanceBehavior.setCollectionElementIndexVariable("counter");
        Map<String, Object> variables = Map.of(
            "name", "John",
            "city", "London",
            MultiInstanceActivityBehavior.NUMBER_OF_COMPLETED_INSTANCES, 3,
            MultiInstanceActivityBehavior.NUMBER_OF_ACTIVE_INSTANCES, 2,
            MultiInstanceActivityBehavior.NUMBER_OF_INSTANCES, 5,
            multiInstanceBehavior.getCollectionElementIndexVariable(), 1
            );

        //when
        Object resultElementItem = multiInstanceBehavior.getResultElementItem(variables);

        //then
        assertThat(resultElementItem).isEqualTo(Map.of(
            "name", "John",
            "city", "London"));
    }

    @Test
    public void updateResultCollection_should_doNothing_when_noOutputDataRef() {
        //given
        multiInstanceBehavior.setLoopDataOutputRef(null);
        DelegateExecution childExecution = mock(DelegateExecution.class);
        DelegateExecution miRootExecution = mock(DelegateExecution.class);

        //when
        multiInstanceBehavior.updateResultCollection(childExecution, miRootExecution);

        //then
        verifyNoInteractions(childExecution, miRootExecution);
    }

    @Test
    public void updateResultCollection_should_createNewList_when_OutputDataRefIsSetButDoesNotExistYet() {
        //given
        multiInstanceBehavior.setLoopDataOutputRef("miResult");
        DelegateExecution childExecution = mock(DelegateExecution.class);
        DelegateExecution miRootExecution = mock(DelegateExecution.class);
        doReturn("currentItem").when(multiInstanceBehavior).getResultElementItem(childExecution);

        //when
        multiInstanceBehavior.updateResultCollection(childExecution, miRootExecution);

        //then
        verify(miRootExecution).setVariableLocal("miResult", Collections.singletonList("currentItem"));
    }

    @Test
    public void updateResultCollection_should_updateList_when_OutputDataRefIsSetAndExistsAlready() {
        //given
        String loopDataOutputRef = "miResult";
        multiInstanceBehavior.setLoopDataOutputRef(loopDataOutputRef);
        DelegateExecution childExecution = mock(DelegateExecution.class);
        DelegateExecution miRootExecution = mock(DelegateExecution.class);
        given(miRootExecution.getVariableLocal(loopDataOutputRef)).willReturn(new ArrayList<>(Collections.singleton("previousItem")));
        doReturn("currentItem").when(multiInstanceBehavior).getResultElementItem(childExecution);

        //when
        multiInstanceBehavior.updateResultCollection(childExecution, miRootExecution);

        //then
        verify(miRootExecution).setVariableLocal(loopDataOutputRef, Arrays.asList("previousItem", "currentItem"));
    }

}
