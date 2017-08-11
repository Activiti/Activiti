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
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiProcessCancelledEventImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.task.Task;
import org.activiti.services.core.model.converter.ListConverter;
import org.activiti.services.core.model.converter.ProcessInstanceConverter;
import org.activiti.services.core.model.converter.TaskConverter;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.events.ActivityCancelledEvent;
import org.activiti.services.events.ActivityCompletedEventImpl;
import org.activiti.services.events.ProcessCancelledEvent;
import org.activiti.services.events.ProcessCompletedEvent;
import org.activiti.services.events.ProcessStartedEvent;
import org.activiti.services.events.TaskAssignedEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {ProcessInstanceConverter.class, TaskConverter.class, ListConverter.class})
public class EventConvertersTest {

    @Autowired
    private ProcessInstanceConverter processInstanceConverter;

    @Autowired
    private TaskConverter taskConverter;

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
        assertThat(pee).isInstanceOf(ProcessStartedEvent.class);
        assertThat(pee.getExecutionId()).isEqualTo("1");
        assertThat(pee.getProcessInstanceId()).isEqualTo("1");
        assertThat(pee.getProcessDefinitionId()).isEqualTo("myProcessDef");
        assertThat(((ProcessStartedEvent) pee).getNestedProcessDefinitionId()).isEqualTo("myParentProcessDef");
        assertThat(((ProcessStartedEvent) pee).getNestedProcessInstanceId()).isEqualTo("2");
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
        assertThat(pee).isInstanceOf(ProcessCancelledEvent.class);
        assertThat(pee.getExecutionId()).isEqualTo("1");
        assertThat(pee.getProcessInstanceId()).isEqualTo("1");
        assertThat(pee.getProcessDefinitionId()).isEqualTo("myProcessDef");
        assertThat(((ProcessCancelledEvent) pee).getCause()).isEqualTo("cause of the cancellation");
    }

    @Test
    public void internalProcessCompletedventToExternalConvertion() throws Exception {
        //given
        ActivitiEntityEvent activitiEvent = mock(ActivitiEntityEvent.class);
        given(activitiEvent.getType()).willReturn(ActivitiEventType.PROCESS_COMPLETED);
        given(activitiEvent.getExecutionId()).willReturn("1");
        given(activitiEvent.getProcessInstanceId()).willReturn("1");
        given(activitiEvent.getProcessDefinitionId()).willReturn("myProcessDef");
        ExecutionEntityImpl executionEntity = mock(ExecutionEntityImpl.class);
        ExecutionEntityImpl processInstance = mock(ExecutionEntityImpl.class);

        given(activitiEvent.getEntity()).willReturn(executionEntity);
        given(executionEntity.getProcessInstance()).willReturn(processInstance);

        given(processInstance.getId()).willReturn("1");

        ProcessEngineEvent pee = new ProcessCompletedEventConverter(processInstanceConverter).from(activitiEvent);

        //then
        assertThat(pee).isInstanceOf(ProcessCompletedEvent.class);
        assertThat(pee.getExecutionId()).isEqualTo("1");
        assertThat(pee.getProcessInstanceId()).isEqualTo("1");
        assertThat(pee.getProcessDefinitionId()).isEqualTo("myProcessDef");
        assertThat(((ProcessCompletedEvent) pee).getProcessInstance().getId()).isEqualTo(processInstance.getId());
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
        assertThat(pee).isInstanceOf(ActivityCancelledEvent.class);
        assertThat(pee.getExecutionId()).isEqualTo("1");
        assertThat(pee.getProcessInstanceId()).isEqualTo("1");
        assertThat(pee.getProcessDefinitionId()).isEqualTo("myProcessDef");
        assertThat(((ActivityCancelledEvent) pee).getActivityId()).isEqualTo("ABC");
        assertThat(((ActivityCancelledEvent) pee).getActivityName()).isEqualTo("ActivityName");
        assertThat(((ActivityCancelledEvent) pee).getActivityType()).isEqualTo("ActivityType");
        assertThat(((ActivityCancelledEvent) pee).getCause()).isEqualTo("cause of the cancellation");
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

    @Test
    public void internalTaskAssignedEventToExternalConvertion() throws Exception {
        //given
        ActivitiEntityEventImpl activitiEvent = mock(ActivitiEntityEventImpl.class);
        given(activitiEvent.getType()).willReturn(ActivitiEventType.TASK_ASSIGNED);
        given(activitiEvent.getExecutionId()).willReturn("1");
        given(activitiEvent.getProcessInstanceId()).willReturn("1");
        given(activitiEvent.getProcessDefinitionId()).willReturn("myProcessDef");
        Task task = mock(Task.class);
        given(task.getId()).willReturn("1");
        given(activitiEvent.getEntity()).willReturn(task);

        ProcessEngineEvent pee = new TaskAssignedEventConverter(taskConverter).from(activitiEvent);

        //then
        assertThat(pee).isInstanceOf(TaskAssignedEvent.class);
        assertThat(pee.getExecutionId()).isEqualTo("1");
        assertThat(pee.getProcessInstanceId()).isEqualTo("1");
        assertThat(pee.getProcessDefinitionId()).isEqualTo("myProcessDef");
        assertThat(((TaskAssignedEvent) pee).getTask().getId()).isEqualTo(task.getId());
    }
}