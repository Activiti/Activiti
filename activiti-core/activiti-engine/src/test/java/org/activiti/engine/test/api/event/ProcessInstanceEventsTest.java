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

package org.activiti.engine.test.api.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.engine.delegate.event.impl.ActivitiActivityCancelledEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiProcessCancelledEventImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to process instances.
 *

 */
public class ProcessInstanceEventsTest extends PluggableActivitiTestCase {

  private TestInitializedEntityEventListener listener;

  /**
   * Test create, update and delete events of process instances.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testProcessInstanceEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertThat(processInstance).isNotNull();

    // Check create-event
    assertThat(listener.getEventsReceived()).hasSize(6);
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiEntityEvent.class);

    // process instance create event
    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(((ProcessInstance) event.getEntity()).getId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    // start event create event
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isNotEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    // start event create initialized
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isNotEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(4);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(event).isInstanceOf(ActivitiEntityEventImpl.class);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(5);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.PROCESS_STARTED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(event).isInstanceOf(ActivitiProcessStartedEvent.class);
    assertThat(((ActivitiProcessStartedEvent)event).getNestedProcessDefinitionId()).isNull();
    assertThat(((ActivitiProcessStartedEvent)event).getNestedProcessInstanceId()).isNull();

    listener.clearEventsReceived();

    // Check update event when suspended/activated
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    runtimeService.activateProcessInstanceById(processInstance.getId());

    assertThat(listener.getEventsReceived()).hasSize(4);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(((ProcessInstance) event.getEntity()).getId()).isEqualTo(processInstance.getId());
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_SUSPENDED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_SUSPENDED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isNotEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_ACTIVATED);
    assertThat(((ProcessInstance) event.getEntity()).getId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_ACTIVATED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isNotEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    listener.clearEventsReceived();

    // Check update event when process-definition is suspended (should
    // cascade suspend/activate all process instances)
    repositoryService.suspendProcessDefinitionById(processInstance.getProcessDefinitionId(), true, null);
    repositoryService.activateProcessDefinitionById(processInstance.getProcessDefinitionId(), true, null);

    assertThat(listener.getEventsReceived()).hasSize(4);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(((ProcessInstance) event.getEntity()).getId()).isEqualTo(processInstance.getId());
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_SUSPENDED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_SUSPENDED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isNotEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_ACTIVATED);
    assertThat(((ProcessInstance) event.getEntity()).getId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_ACTIVATED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isNotEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    listener.clearEventsReceived();

    // Check update-event when business-key is updated
    runtimeService.updateBusinessKey(processInstance.getId(), "thekey");
    assertThat(listener.getEventsReceived()).hasSize(1);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(((ProcessInstance) event.getEntity()).getId()).isEqualTo(processInstance.getId());
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    listener.clearEventsReceived();

    runtimeService.deleteProcessInstance(processInstance.getId(), "Testing events");

    List<ActivitiEvent> processCancelledEvents = listener.filterEvents(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(processCancelledEvents).hasSize(1);
    ActivitiCancelledEvent cancelledEvent = (ActivitiCancelledEvent) processCancelledEvents.get(0);
    assertThat(cancelledEvent.getType()).isEqualTo(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(cancelledEvent.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(cancelledEvent.getExecutionId()).isEqualTo(processInstance.getId());
    listener.clearEventsReceived();
  }

  /**
   * Test create, update and delete events of process instances.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testSubProcessInstanceEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");
    assertThat(processInstance).isNotNull();
    String processDefinitionId = processInstance.getProcessDefinitionId();

    // Check create-event one main process the second one Scope execution, and the third one subprocess
    assertThat(listener.getEventsReceived()).hasSize(10);
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiEntityEvent.class);

    // process instance created event
    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(((ProcessInstance) event.getEntity()).getId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processDefinitionId);

    // start event created event
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    String startChildExecutionId = event.getExecutionId();
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(startChildExecutionId).isNotEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processDefinitionId);

    // start event initialized event
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(((ExecutionEntity) event.getEntity()).getId()).isNotEqualTo(processInstance.getId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
    String mainProcessExecutionId = event.getExecutionId();
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(mainProcessExecutionId).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processDefinitionId);

    // Process Start Date
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(4);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    // Process start
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(5);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.PROCESS_STARTED);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(event).isInstanceOf(ActivitiProcessStartedEvent.class);
    assertThat(((ActivitiProcessStartedEvent)event).getNestedProcessDefinitionId()).isNull();
    assertThat(((ActivitiProcessStartedEvent)event).getNestedProcessInstanceId()).isNull();

    // sub process instance created event
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(6);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    ExecutionEntity subProcessEntity = (ExecutionEntity) event.getEntity();
    assertThat(subProcessEntity.getSuperExecutionId()).isEqualTo(startChildExecutionId);
    String subProcessInstanceId = subProcessEntity.getProcessInstanceId();

    // sub process instance start created event
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(7);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(event.getProcessInstanceId()).isEqualTo(subProcessInstanceId);
    assertThat(event.getExecutionId()).isNotEqualTo(subProcessInstanceId);
    String subProcessDefinitionId = ((ExecutionEntity) event.getEntity()).getProcessDefinitionId();
    assertThat(subProcessDefinitionId).isNotNull();
    ProcessDefinition subProcessDefinition = repositoryService.getProcessDefinition(subProcessDefinitionId);
    assertThat(subProcessDefinition.getKey()).isEqualTo("simpleSubProcess");

    // sub process instance start initialized event
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(8);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    assertThat(event.getProcessInstanceId()).isEqualTo(subProcessInstanceId);
    assertThat(event.getExecutionId()).isNotEqualTo(subProcessInstanceId);
    subProcessDefinitionId = ((ExecutionEntity) event.getEntity()).getProcessDefinitionId();
    assertThat(subProcessDefinitionId).isNotNull();

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(9);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.PROCESS_STARTED);
    assertThat(event.getProcessInstanceId()).isEqualTo(subProcessInstanceId);
    assertThat(event.getProcessDefinitionId()).isEqualTo(subProcessDefinitionId);
    assertThat(event).isInstanceOf(ActivitiProcessStartedEvent.class);
    assertThat(((ActivitiProcessStartedEvent) event).getNestedProcessDefinitionId()).isEqualTo(processDefinitionId);
    assertThat(((ActivitiProcessStartedEvent) event).getNestedProcessInstanceId()).isEqualTo(processInstance.getId());

    listener.clearEventsReceived();
  }

  /**
   * Test process with signals start.
   */
  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalWithGlobalScope.bpmn20.xml" })
  public void testSignalProcessInstanceStart() throws Exception {
    this.runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    listener.clearEventsReceived();

    runtimeService.startProcessInstanceByKey("processWithSignalThrow");
    listener.clearEventsReceived();
  }

  /**
   * Test Start->End process on PROCESS_COMPLETED event
   */
  @Deployment(resources = { "org/activiti/engine/test/api/event/ProcessInstanceEventsTest.noneTaskProcess.bpmn20.xml" })
  public void testProcessCompleted_StartEnd() throws Exception {
    this.runtimeService.startProcessInstanceByKey("noneTaskProcess");

    assertThat(listener.filterEvents(ActivitiEventType.PROCESS_COMPLETED)).as("ActivitiEventType.PROCESS_COMPLETED was expected 1 time.").hasSize(1);
  }

  /**
   * Test Start->User Task process on PROCESS_COMPLETED event
   */
  @Deployment(resources = { "org/activiti/engine/test/api/event/ProcessInstanceEventsTest.noEndProcess.bpmn20.xml" })
  public void testProcessCompleted_NoEnd() throws Exception {
    ProcessInstance noEndProcess = this.runtimeService.startProcessInstanceByKey("noEndProcess");
    Task task = taskService.createTaskQuery().processInstanceId(noEndProcess.getId()).singleResult();
    taskService.complete(task.getId());

    assertThat(listener.filterEvents(ActivitiEventType.PROCESS_COMPLETED)).as("ActivitiEventType.PROCESS_COMPLETED was expected 1 time.").hasSize(1);
  }

  /**
   * Test +-->Task1 Start-<> +-->Task1
   *
   * process on PROCESS_COMPLETED event
   */
  @Deployment(resources = { "org/activiti/engine/test/api/event/ProcessInstanceEventsTest.parallelGatewayNoEndProcess.bpmn20.xml" })
  public void testProcessCompleted_ParallelGatewayNoEnd() throws Exception {
    this.runtimeService.startProcessInstanceByKey("noEndProcess");

    assertThat(listener.filterEvents(ActivitiEventType.PROCESS_COMPLETED)).as("ActivitiEventType.PROCESS_COMPLETED was expected 1 time.").hasSize(1);
  }

  /**
   * Test +-->End1 Start-<> +-->End2
   * <p/>
   * process on PROCESS_COMPLETED event
   */
  @Deployment(resources = { "org/activiti/engine/test/api/event/ProcessInstanceEventsTest.parallelGatewayTwoEndsProcess.bpmn20.xml" })
  public void testProcessCompleted_ParallelGatewayTwoEnds() throws Exception {
    this.runtimeService.startProcessInstanceByKey("noEndProcess");

    List<ActivitiEvent> events = listener.filterEvents(ActivitiEventType.PROCESS_COMPLETED);
    assertThat(events).as("ActivitiEventType.PROCESS_COMPLETED was expected 1 time.").hasSize(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testProcessInstanceCancelledEvents_cancel() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processInstance).isNotNull();
    listener.clearEventsReceived();

    runtimeService.deleteProcessInstance(processInstance.getId(), "delete_test");

    List<ActivitiEvent> processCancelledEvents = listener.filterEvents(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(processCancelledEvents).as("ActivitiEventType.PROCESS_CANCELLED was expected 1 time.").hasSize(1);
    ActivitiCancelledEvent processCancelledEvent = (ActivitiCancelledEvent) processCancelledEvents.get(0);
    assertThat(ActivitiCancelledEvent.class.isAssignableFrom(processCancelledEvent.getClass())).as("The cause has to be the same as deleteProcessInstance method call").isTrue();
    assertThat(processCancelledEvent.getProcessInstanceId()).as("The process instance has to be the same as in deleteProcessInstance method call").isEqualTo(processInstance.getId());
    assertThat(processCancelledEvent.getExecutionId()).as("The execution instance has to be the same as in deleteProcessInstance method call").isEqualTo(processInstance.getId());
    assertThat(processCancelledEvent.getCause()).as("The cause has to be the same as in deleteProcessInstance method call").isEqualTo("delete_test");

    List<ActivitiEvent> taskCancelledEvents = listener.filterEvents(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(taskCancelledEvents).as("ActivitiEventType.ACTIVITY_CANCELLED was expected 1 time.").hasSize(1);
    ActivitiActivityCancelledEvent activityCancelledEvent = (ActivitiActivityCancelledEvent) taskCancelledEvents.get(0);
    assertThat(ActivitiActivityCancelledEvent.class.isAssignableFrom(activityCancelledEvent.getClass())).as("The cause has to be the same as deleteProcessInstance method call").isTrue();
    assertThat(activityCancelledEvent.getProcessInstanceId()).as("The process instance has to be the same as in deleteProcessInstance method call").isEqualTo(processInstance.getId());
    assertThat(activityCancelledEvent.getCause()).as("The cause has to be the same as in deleteProcessInstance method call").isEqualTo("delete_test");

    listener.clearEventsReceived();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testProcessInstanceCancelledEvents_cancelProcessHierarchy() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");
    ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
    assertThat(processInstance).isNotNull();
    listener.clearEventsReceived();

    runtimeService.deleteProcessInstance(processInstance.getId(), "delete_test");

    List<ActivitiEvent> processCancelledEvents = listener.filterEvents(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(processCancelledEvents).as("ActivitiEventType.PROCESS_CANCELLED was expected 2 times.").hasSize(2);
    ActivitiCancelledEvent processCancelledEvent = (ActivitiCancelledEvent) processCancelledEvents.get(0);
    assertThat(ActivitiCancelledEvent.class.isAssignableFrom(processCancelledEvent.getClass())).as("The cause has to be the same as deleteProcessInstance method call").isTrue();
    assertThat(processCancelledEvent.getProcessInstanceId()).as("The process instance has to be the same as in deleteProcessInstance method call").isEqualTo(subProcess.getId());
    assertThat(processCancelledEvent.getExecutionId()).as("The execution instance has to be the same as in deleteProcessInstance method call").isEqualTo(subProcess.getId());
    assertThat(processCancelledEvent.getCause()).as("The cause has to be the same as in deleteProcessInstance method call").isEqualTo("delete_test");

    processCancelledEvent = (ActivitiCancelledEvent) processCancelledEvents.get(1);
    assertThat(ActivitiCancelledEvent.class.isAssignableFrom(processCancelledEvent.getClass())).as("The cause has to be the same as deleteProcessInstance method call").isTrue();
    assertThat(processCancelledEvent.getProcessInstanceId()).as("The process instance has to be the same as in deleteProcessInstance method call").isEqualTo(processInstance.getId());
    assertThat(processCancelledEvent.getExecutionId()).as("The execution instance has to be the same as in deleteProcessInstance method call").isEqualTo(processInstance.getId());
    assertThat(processCancelledEvent.getCause()).as("The cause has to be the same as in deleteProcessInstance method call").isEqualTo("delete_test");

    assertThat(this.taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).as("No task can be active for deleted process.").isEqualTo(0);

    List<ActivitiEvent> taskCancelledEvents = listener.filterEvents(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(taskCancelledEvents).as("ActivitiEventType.ACTIVITY_CANCELLED was expected 1 time.").hasSize(1);
    ActivitiActivityCancelledEvent activityCancelledEvent = (ActivitiActivityCancelledEvent) taskCancelledEvents.get(0);
    assertThat(ActivitiActivityCancelledEvent.class.isAssignableFrom(activityCancelledEvent.getClass())).as("The cause has to be the same as deleteProcessInstance method call").isTrue();
    assertThat(activityCancelledEvent.getProcessInstanceId()).as("The process instance has to point to the subprocess").isEqualTo(subProcess.getId());
    assertThat(activityCancelledEvent.getCause()).as("The cause has to be the same as in deleteProcessInstance method call").isEqualTo("delete_test");

    listener.clearEventsReceived();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testProcessInstanceCancelledEvents_complete() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processInstance).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    List<ActivitiEvent> processCancelledEvents = listener.filterEvents(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(processCancelledEvents).as("There should be no ActivitiEventType.PROCESS_CANCELLED event after process complete.").hasSize(0);
    List<ActivitiEvent> taskCancelledEvents = listener.filterEvents(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(taskCancelledEvents).as("There should be no ActivitiEventType.ACTIVITY_CANCELLED event.").hasSize(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testProcessInstanceTerminatedEvents_complete() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processInstance).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    List<ActivitiEvent> processTerminatedEvents = listener.filterEvents(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(processTerminatedEvents).as("There should be no ActivitiEventType.PROCESS_TERMINATED event after process complete.").hasSize(0);
  }

  @Deployment(resources = "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testProcessTerminate.bpmn")
  public void testProcessInstanceTerminatedEvents() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
    assertThat(executionEntities).isEqualTo(3);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateTask").singleResult();
    taskService.complete(task.getId());

    List<ActivitiEvent> processTerminatedEvents = listener.filterEvents(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(processTerminatedEvents).as("There should be exactly one ActivitiEventType.PROCESS_CANCELLED event after the task complete.").hasSize(1);
    ActivitiProcessCancelledEventImpl activitiEvent = (ActivitiProcessCancelledEventImpl) processTerminatedEvents.get(0);
    assertThat(activitiEvent.getProcessInstanceId()).isEqualTo(pi.getProcessInstanceId());

    List<ActivitiEvent> activityTerminatedEvents = listener.filterEvents(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(activityTerminatedEvents).as("There should be exactly two ActivitiEventType.ACTIVITY_CANCELLED event after the task complete.").hasSize(2);

    for (ActivitiEvent event : activityTerminatedEvents) {

      ActivitiActivityCancelledEventImpl activityEvent = (ActivitiActivityCancelledEventImpl) event;
      if (activityEvent.getActivityId().equals("preNormalTerminateTask")) {
        assertThat(activityEvent.getActivityId()).as("The user task must be terminated").isEqualTo("preNormalTerminateTask");
        assertThat(activityEvent.getCause()).as("The cause must be terminate end event").isEqualTo("Terminated by end event: EndEvent_2");
      } else if (activityEvent.getActivityId().equals("EndEvent_2")) {
        assertThat(activityEvent.getActivityId()).as("The end event must be terminated").isEqualTo("EndEvent_2");
        assertThat(activityEvent.getCause()).as("The cause must be terminate end event").isEqualTo("Terminated by end event: EndEvent_2");
      }

    }

  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivity.bpmn",
      "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessTerminate.bpmn" })
  public void testProcessInstanceTerminatedEvents_callActivity() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    List<ActivitiEvent> processTerminatedEvents = listener.filterEvents(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(processTerminatedEvents).as("There should be exactly one ActivitiEventType.PROCESS_CANCELLED event after the task complete.").hasSize(1);
    ActivitiProcessCancelledEventImpl processCancelledEvent = (ActivitiProcessCancelledEventImpl) processTerminatedEvents.get(0);
    assertThat(processCancelledEvent.getProcessInstanceId()).isNotEqualTo(pi.getProcessInstanceId());
    assertThat(processCancelledEvent.getProcessDefinitionId()).contains("terminateEndEventSubprocessExample");

  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInParentProcess.bpmn", "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testProcessInstanceTerminatedEvents_terminateInParentProcess() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateParentProcess");

    // should terminate the called process and continue the parent
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    List<ActivitiEvent> processTerminatedEvents = listener.filterEvents(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(processTerminatedEvents).as("There should be exactly one ActivitiEventType.PROCESS_TERMINATED event after the task complete.").hasSize(1);
    ActivitiProcessCancelledEventImpl processCancelledEvent = (ActivitiProcessCancelledEventImpl) processTerminatedEvents.get(0);
    assertThat(processCancelledEvent.getProcessInstanceId()).isEqualTo(pi.getProcessInstanceId());
    assertThat(processCancelledEvent.getProcessDefinitionId()).contains("terminateParentProcess");

    List<ActivitiEvent> activityTerminatedEvents = listener.filterEvents(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(activityTerminatedEvents).as("3 activities must be cancelled.").hasSize(3);

    for (ActivitiEvent event : activityTerminatedEvents) {

      ActivitiActivityCancelledEventImpl activityEvent = (ActivitiActivityCancelledEventImpl) event;

      if (activityEvent.getActivityId().equals("theTask")) {

        assertThat(activityEvent.getActivityId()).as("The user task must be terminated in the called sub process.").isEqualTo("theTask");
        assertThat(activityEvent.getCause()).as("The cause must be terminate end event").isEqualTo("Terminated by end event: EndEvent_3");

      } else if (activityEvent.getActivityId().equals("CallActivity_1")) {

        assertThat(activityEvent.getActivityId()).as("The call activity must be terminated").isEqualTo("CallActivity_1");
        assertThat(activityEvent.getCause()).as("The cause must be terminate end event").isEqualTo("Terminated by end event: EndEvent_3");

      } else if (activityEvent.getActivityId().equals("EndEvent_3")) {

        assertThat(activityEvent.getActivityId()).as("The end event must be terminated").isEqualTo("EndEvent_3");
        assertThat(activityEvent.getCause()).as("The cause must be terminate end event").isEqualTo("Terminated by end event: EndEvent_3");

      }

    }

  }

  @Deployment(resources = {
          "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorOnCallActivity-parent.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml"
  })
  public void testProcessCompletedEvents_callActivityErrorEndEvent() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("catchErrorOnCallActivity");

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task in subprocess");
    List<ProcessInstance> subProcesses = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).list();
    assertThat(subProcesses).hasSize(1);

    // Completing the task will reach the end error event,
    // which is caught on the call activity boundary
    taskService.complete(task.getId());

    List<ActivitiEvent> processCompletedEvents = listener.filterEvents(ActivitiEventType.PROCESS_COMPLETED_WITH_ERROR_END_EVENT);
    assertThat(processCompletedEvents).as("There should be exactly an ActivitiEventType.PROCESS_COMPLETED_WITH_ERROR_END_EVENT event after the task complete.").hasSize(1);
    ActivitiEntityEvent processCompletedEvent = (ActivitiEntityEvent) processCompletedEvents.get(0);
    assertThat(processCompletedEvent.getExecutionId()).isEqualTo(subProcesses.get(0).getId());

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Escalated Task");

    // Completing the task will end the process instance
    taskService.complete(task.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment(resources = {
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml"})
  public void testDeleteMultiInstanceCallActivityProcessInstance() {
    assertThat(taskService.createTaskQuery().count()).isEqualTo(0);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelCallActivity");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(7);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(12);
    this.listener.clearEventsReceived();

    runtimeService.deleteProcessInstance(processInstance.getId(), "testing instance deletion");

    assertThat(this.listener.getEventsReceived().get(0).getType()).as("Task cancelled event has to be fired.").isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(this.listener.getEventsReceived().get(2).getType()).as("SubProcess cancelled event has to be fired.").isEqualTo(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(0);
  }

  @Override
  protected void initializeServices() {
    super.initializeServices();
    this.listener = new TestInitializedEntityEventListener();
    processEngineConfiguration.getEventDispatcher().addEventListener(this.listener);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    if (listener != null) {
      listener.clearEventsReceived();
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }

  private class TestInitializedEntityEventListener implements ActivitiEventListener {

    private List<ActivitiEvent> eventsReceived;

    public TestInitializedEntityEventListener() {

      eventsReceived = new ArrayList<ActivitiEvent>();
    }

    public List<ActivitiEvent> getEventsReceived() {
      return eventsReceived;
    }

    public void clearEventsReceived() {
      eventsReceived.clear();
    }

    @Override
    public void onEvent(ActivitiEvent event) {
      if (event instanceof ActivitiEntityEvent && ProcessInstance.class.isAssignableFrom(((ActivitiEntityEvent) event).getEntity().getClass())) {
        // check whether entity in the event is initialized before adding to the list.
        assertThat(((ExecutionEntity) ((ActivitiEntityEvent) event).getEntity()).getId()).isNotNull();
        eventsReceived.add(event);
      } else if (ActivitiEventType.PROCESS_CANCELLED.equals(event.getType()) || ActivitiEventType.ACTIVITY_CANCELLED.equals(event.getType())) {
        eventsReceived.add(event);
      }
    }

    @Override
    public boolean isFailOnException() {
      return true;
    }

    public List<ActivitiEvent> filterEvents(ActivitiEventType eventType) {// count timer cancelled events
      List<ActivitiEvent> filteredEvents = new ArrayList<ActivitiEvent>();
      List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
      for (ActivitiEvent eventReceived : eventsReceived) {
        if (eventType.equals(eventReceived.getType())) {
          filteredEvents.add(eventReceived);
        }
      }
      return filteredEvents;
    }

  }
}
