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
package org.activiti.engine.impl.agenda;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.LinkEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TakeOutgoingSequenceFlowsOperationTest {

    private TakeOutgoingSequenceFlowsOperation operation;
    private ExecutionEntity execution;
    private CommandContext commandContext;

    @Before
    public void setUp() {
        commandContext = mock(CommandContext.class);
        execution = mock(ExecutionEntity.class);
        operation = new TakeOutgoingSequenceFlowsOperation(commandContext, execution, true);
    }

    @Test
    public void findRelatedIntermediateCatchEventForLinkEvent_shouldReturnCorrectFlowNode_when_targetEventIsGiven_AndMatchingCatchEventIsFound() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ThrowEvent throwEvent = mock(ThrowEvent.class);
        LinkEventDefinition linkEventDefinition = mock(LinkEventDefinition.class);
        when(linkEventDefinition.getTarget()).thenReturn("targetId");
        when(throwEvent.getEventDefinitions()).thenReturn(List.of(linkEventDefinition));

        when(throwEvent.getParentContainer()).thenReturn(mock(FlowElementsContainer.class));
        IntermediateCatchEvent catchEvent = mock(IntermediateCatchEvent.class);
        LinkEventDefinition catchEventDefinition = mock(LinkEventDefinition.class);
        when(catchEventDefinition.getId()).thenReturn("targetId");
        when(catchEvent.getEventDefinitions()).thenReturn(List.of(catchEventDefinition));
        when(((FlowNode) catchEvent).isLinkCatchEvent()).thenReturn(true);

        Collection<FlowElement> flowElements = new ArrayList<>();
        flowElements.add(catchEvent);
        when(throwEvent.getParentContainer().getFlowElements()).thenReturn(flowElements);

        Method method = TakeOutgoingSequenceFlowsOperation.class.getDeclaredMethod("findRelatedIntermediateCatchEventForLinkEvent", ThrowEvent.class);
        method.setAccessible(true);
        FlowNode result = (FlowNode) method.invoke(operation, throwEvent);

        assertEquals(catchEvent, result);
    }

    @Test
    public void findRelatedIntermediateCatchEventForLinkEvent_shouldReturnCorrectFlowNode_when_targetEventIsGiven_noMatchIsFound() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ThrowEvent throwEvent = mock(ThrowEvent.class);
        LinkEventDefinition linkEventDefinition = mock(LinkEventDefinition.class);
        when(linkEventDefinition.getTarget()).thenReturn("targetId");
        when(throwEvent.getEventDefinitions()).thenReturn(List.of(linkEventDefinition));

        when(throwEvent.getParentContainer()).thenReturn(mock(FlowElementsContainer.class));
        IntermediateCatchEvent catchEvent = mock(IntermediateCatchEvent.class);
        LinkEventDefinition catchEventDefinition = mock(LinkEventDefinition.class);
        when(catchEventDefinition.getId()).thenReturn("differentId");
        when(catchEvent.getEventDefinitions()).thenReturn(List.of(catchEventDefinition));
        when(((FlowNode) catchEvent).isLinkCatchEvent()).thenReturn(true);

        Collection<FlowElement> flowElements = new ArrayList<>();
        flowElements.add(catchEvent);
        when(throwEvent.getParentContainer().getFlowElements()).thenReturn(flowElements);


        Method method = TakeOutgoingSequenceFlowsOperation.class.getDeclaredMethod("findRelatedIntermediateCatchEventForLinkEvent", ThrowEvent.class);
        method.setAccessible(true);
        FlowNode result = (FlowNode) method.invoke(operation, throwEvent);

        assertEquals(null, result);
    }

}
