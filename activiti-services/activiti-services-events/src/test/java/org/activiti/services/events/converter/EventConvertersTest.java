/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.events.converter;

import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.engine.delegate.event.impl.ActivitiProcessCancelledEventImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.services.core.model.converter.ProcessInstanceConverter;
import org.activiti.services.core.model.events.ProcessEngineEvent;
import org.activiti.services.events.ActivityCancelledEventImpl;
import org.activiti.services.events.ActivityCompletedEventImpl;
import org.activiti.services.events.ProcessCancelledEventImpl;
import org.activiti.services.events.ProcessStartedEventImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class EventConvertersTest {



    @Test
    public void internalProcessStartedEventToExternalConvertion() throws Exception {
        //given
        ActivitiProcessStartedEvent activitiEvent = mock(ActivitiProcessStartedEvent.class);
        given(activitiEvent.getType()).willReturn(ActivitiEventType.PROCESS_STARTED);
        given(activitiEvent.getExecutionId()).willReturn("1");
        given(activitiEvent.getProcessInstanceId()).willReturn("1");
        given(activitiEvent.getProcessDefinitionId()).willReturn("myProcessDef");
        given(activitiEvent.getNestedProcessDefinitionId()).willReturn("myParentProcessDef");
        given(activitiEvent.getNestedProcessInstanceId()).willReturn("2");

        ProcessEngineEvent pee = new ProcessStartedEventConverter().from(activitiEvent);

        //then
        assertThat(pee).isInstanceOf(ProcessStartedEventImpl.class);
        assertThat(pee.getExecutionId()).isEqualTo("1");
        assertThat(pee.getProcessInstanceId()).isEqualTo("1");
        assertThat(pee.getProcessDefinitionId()).isEqualTo("myProcessDef");
        assertThat(((ProcessStartedEventImpl) pee).getNestedProcessDefinitionId()).isEqualTo("myParentProcessDef");
        assertThat(((ProcessStartedEventImpl) pee).getNestedProcessInstanceId()).isEqualTo("2");
    }

    @Test
    public void internalProcessCancelledEventToExternalConvertion() throws Exception {
        //given
        ActivitiProcessCancelledEventImpl activitiEvent = mock(ActivitiProcessCancelledEventImpl.class);
        given(activitiEvent.getType()).willReturn(ActivitiEventType.PROCESS_CANCELLED);
        given(activitiEvent.getExecutionId()).willReturn("1");
        given(activitiEvent.getProcessInstanceId()).willReturn("1");
        given(activitiEvent.getProcessDefinitionId()).willReturn("myProcessDef");
        given(activitiEvent.getCause()).willReturn("cause of the cancellation");

        ProcessEngineEvent pee = new ProcessCancelledEventConverter().from(activitiEvent);

        //then
        assertThat(pee).isInstanceOf(ProcessCancelledEventImpl.class);
        assertThat(pee.getExecutionId()).isEqualTo("1");
        assertThat(pee.getProcessInstanceId()).isEqualTo("1");
        assertThat(pee.getProcessDefinitionId()).isEqualTo("myProcessDef");
        assertThat(((ProcessCancelledEventImpl) pee).getCause()).isEqualTo("cause of the cancellation");
    }
    

    @Test
    public void internalActivityCancelledEventToExternalConvertion() throws Exception {
        //given
        ActivitiActivityCancelledEvent activitiEvent = mock(ActivitiActivityCancelledEvent.class);
        given(activitiEvent.getType()).willReturn(ActivitiEventType.ACTIVITY_CANCELLED);
        given(activitiEvent.getExecutionId()).willReturn("1");
        given(activitiEvent.getProcessInstanceId()).willReturn("1");
        given(activitiEvent.getProcessDefinitionId()).willReturn("myProcessDef");
        given(activitiEvent.getActivityId()).willReturn("ABC");
        given(activitiEvent.getActivityName()).willReturn("ActivityName");
        given(activitiEvent.getActivityType()).willReturn("ActivityType");
        given(activitiEvent.getCause()).willReturn("cause of the cancellation");

        ProcessEngineEvent pee = new ActivityCancelledEventConverter().from(activitiEvent);

        //then
        assertThat(pee).isInstanceOf(ActivityCancelledEventImpl.class);
        assertThat(pee.getExecutionId()).isEqualTo("1");
        assertThat(pee.getProcessInstanceId()).isEqualTo("1");
        assertThat(pee.getProcessDefinitionId()).isEqualTo("myProcessDef");
        assertThat(((ActivityCancelledEventImpl) pee).getActivityId()).isEqualTo("ABC");
        assertThat(((ActivityCancelledEventImpl) pee).getActivityName()).isEqualTo("ActivityName");
        assertThat(((ActivityCancelledEventImpl) pee).getActivityType()).isEqualTo("ActivityType");
        assertThat(((ActivityCancelledEventImpl) pee).getCause()).isEqualTo("cause of the cancellation");
    }

    @Test
    public void internalActivityCompletedEventToExternalConvertion() throws Exception {
        //given
        ActivitiActivityEvent activitiEvent = mock(ActivitiActivityEvent.class);
        given(activitiEvent.getType()).willReturn(ActivitiEventType.ACTIVITY_COMPLETED);
        given(activitiEvent.getExecutionId()).willReturn("1");
        given(activitiEvent.getProcessInstanceId()).willReturn("1");
        given(activitiEvent.getProcessDefinitionId()).willReturn("myProcessDef");
        given(activitiEvent.getActivityId()).willReturn("ABC");
        given(activitiEvent.getActivityName()).willReturn("ActivityName");
        given(activitiEvent.getActivityType()).willReturn("ActivityType");

        ProcessEngineEvent pee = new ActivityCompletedEventConverter().from(activitiEvent);

        //then
        assertThat(pee).isInstanceOf(ActivityCompletedEventImpl.class);
        assertThat(pee.getExecutionId()).isEqualTo("1");
        assertThat(pee.getProcessInstanceId()).isEqualTo("1");
        assertThat(pee.getProcessDefinitionId()).isEqualTo("myProcessDef");
        assertThat(((ActivityCompletedEventImpl) pee).getActivityId()).isEqualTo("ABC");
        assertThat(((ActivityCompletedEventImpl) pee).getActivityName()).isEqualTo("ActivityName");
        assertThat(((ActivityCompletedEventImpl) pee).getActivityType()).isEqualTo("ActivityType");
    }
}