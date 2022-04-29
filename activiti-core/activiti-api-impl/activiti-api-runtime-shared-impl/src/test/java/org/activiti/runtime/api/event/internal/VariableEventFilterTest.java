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

package org.activiti.runtime.api.event.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiVariableEventImpl;
import org.junit.jupiter.api.Test;

public class VariableEventFilterTest {

    private VariableEventFilter variableEventFilter = new VariableEventFilter();

    @Test
    public void should_emmitEvent_when_executionIdIsEqualsToProcessInstanceId() {
        //given
        ActivitiVariableEventImpl event = new ActivitiVariableEventImpl(
            ActivitiEventType.VARIABLE_CREATED);
        event.setExecutionId("id");
        event.setProcessInstanceId("id");

        //when
        boolean shouldEmmitEvent = variableEventFilter.shouldEmmitEvent(
            event);

        //then
        assertThat(shouldEmmitEvent).isTrue();
    }

    @Test
    public void shouldNot_emmitEvent_when_executionIdIsNotEqualsToProcessInstanceIdAndTaskIdIsNotSet() {
        //given
        ActivitiVariableEventImpl event = new ActivitiVariableEventImpl(
            ActivitiEventType.VARIABLE_CREATED);
        event.setExecutionId("id");
        event.setProcessInstanceId("anotherId");

        //when
        boolean shouldEmmitEvent = variableEventFilter.shouldEmmitEvent(
            event);

        //then
        assertThat(shouldEmmitEvent).isFalse();
    }

    @Test
    public void should_EmmitEvent_when_executionIdIsNotEqualsToProcessInstanceIdAndTaskIdIsSet() {
        //given
        ActivitiVariableEventImpl event = new ActivitiVariableEventImpl(
            ActivitiEventType.VARIABLE_CREATED);
        event.setExecutionId("id");
        event.setProcessInstanceId("anotherId");
        event.setTaskId("taskId");

        //when
        boolean shouldEmmitEvent = variableEventFilter.shouldEmmitEvent(event);

        //then
        assertThat(shouldEmmitEvent).isTrue();
    }

    @Test
    public void should_EmmitEvent_when_executionIdAndProcessInstanceIdAreNotSetAndTaskIdIsSet() {
        //given
        ActivitiVariableEventImpl event = new ActivitiVariableEventImpl(
            ActivitiEventType.VARIABLE_CREATED);
        event.setExecutionId(null);
        event.setProcessInstanceId(null);
        event.setTaskId("taskId");

        //when
        boolean shouldEmmitEvent = variableEventFilter.shouldEmmitEvent(event);

        //then
        assertThat(shouldEmmitEvent).isTrue();
    }

}
