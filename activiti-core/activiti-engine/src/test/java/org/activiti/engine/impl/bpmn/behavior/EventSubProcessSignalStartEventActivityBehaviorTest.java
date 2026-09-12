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
package org.activiti.engine.impl.bpmn.behavior;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.IntegerDataObject;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.StringDataObject;
import org.activiti.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EventSubProcessSignalStartEventActivityBehavior#execute(DelegateExecution)}.
 * The integration paths around this behavior are exercised by
 * {@code SignalEventSubprocessTest}; here we pin down the per-instance contract
 * of {@code execute()} in isolation, without spinning up a process engine.
 */
class EventSubProcessSignalStartEventActivityBehaviorTest {

    private static final String SIGNAL_NAME = "newSignal";

    @Test
    void execute_marksTheExecutionAsScopeAndCopiesDataObjectsToLocalVariables() {
        // given a start event whose enclosing event sub-process declares two data objects
        StringDataObject stringDataObject = new StringDataObject();
        stringDataObject.setName("greeting");
        stringDataObject.setValue("hello");

        IntegerDataObject integerDataObject = new IntegerDataObject();
        integerDataObject.setName("attempts");
        integerDataObject.setValue(3);

        EventSubProcess eventSubProcess = new EventSubProcess();
        eventSubProcess.setDataObjects(List.of(stringDataObject, integerDataObject));

        StartEvent startEvent = newStartEventInside(eventSubProcess);

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getCurrentFlowElement()).thenReturn(startEvent);
        when(execution.hasVariable(anyString())).thenReturn(false);

        EventSubProcessSignalStartEventActivityBehavior behavior = newBehavior();

        // when
        behavior.execute(execution);

        // then the scope flag is set on the execution
        verify(execution).setScope(true);

        // and each data object is seeded as a local variable on that scope
        verify(execution).setVariableLocal("greeting", "hello");
        verify(execution).setVariableLocal("attempts", 3);
    }

    @Test
    void execute_doesNotClobberDataObjectsThatAlreadyExistInScope() {
        // given a data object whose name collides with a variable already present in scope
        StringDataObject stringDataObject = new StringDataObject();
        stringDataObject.setName("greeting");
        stringDataObject.setValue("hello");

        EventSubProcess eventSubProcess = new EventSubProcess();
        eventSubProcess.setDataObjects(List.of(stringDataObject));

        StartEvent startEvent = newStartEventInside(eventSubProcess);

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getCurrentFlowElement()).thenReturn(startEvent);
        when(execution.hasVariable("greeting")).thenReturn(true);

        // when
        newBehavior().execute(execution);

        // then the scope flag is still set, but the pre-existing variable is left untouched
        verify(execution).setScope(true);
        verify(execution, never()).setVariableLocal(eq("greeting"), any());
    }

    @Test
    void execute_setsScopeEvenWhenNoDataObjectsAreDeclared() {
        // given an event sub-process with no <dataObject> children
        EventSubProcess eventSubProcess = new EventSubProcess();
        StartEvent startEvent = newStartEventInside(eventSubProcess);

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getCurrentFlowElement()).thenReturn(startEvent);

        // when
        newBehavior().execute(execution);

        // then the scope flag is still set ...
        verify(execution).setScope(true);

        // ... and no variables are seeded
        verify(execution, never()).setVariableLocal(anyString(), any());
    }

    @Test
    void execute_doesNotSeedVariablesWhenDataObjectsCollectionIsNull() {
        // given an event sub-process whose data-objects collection is explicitly null
        EventSubProcess eventSubProcess = new EventSubProcess();
        eventSubProcess.setDataObjects(null);
        StartEvent startEvent = newStartEventInside(eventSubProcess);

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getCurrentFlowElement()).thenReturn(startEvent);

        // when
        newBehavior().execute(execution);

        // then we still mark the execution as a scope but we never seed any variable
        verify(execution).setScope(true);
        verify(execution, never()).setVariableLocal(anyString(), any());
    }

    private static StartEvent newStartEventInside(EventSubProcess eventSubProcess) {
        StartEvent startEvent = new StartEvent();
        // EventSubProcess implements FlowElementsContainer; this is how the parser wires the
        // back-reference that getSubProcess() reads from.
        startEvent.setParentContainer(eventSubProcess);
        return startEvent;
    }

    private static EventSubProcessSignalStartEventActivityBehavior newBehavior() {
        SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
        signalEventDefinition.setSignalRef(SIGNAL_NAME);
        Signal signal = new Signal();
        signal.setName(SIGNAL_NAME);
        return new EventSubProcessSignalStartEventActivityBehavior(signalEventDefinition, signal);
    }
}
