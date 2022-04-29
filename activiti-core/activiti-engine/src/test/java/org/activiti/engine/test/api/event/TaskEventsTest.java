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

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to tasks.
 *
 */
public class TaskEventsTest extends PluggableActivitiTestCase {

  private TestActivitiEntityEventListener listener;

  /**
   * Check create, update and delete events for a task.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskEventsInProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processInstance).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Check create event
    assertThat(listener.getEventsReceived()).hasSize(3);
    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(event.getEntity()).isInstanceOf(Task.class);
    Task taskFromEvent = (Task) event.getEntity();
    assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
    assertExecutionDetails(event, processInstance);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.TASK_CREATED);
    assertThat(event.getEntity()).isInstanceOf(Task.class);
    taskFromEvent = (Task) event.getEntity();
    assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
    assertExecutionDetails(event, processInstance);

    listener.clearEventsReceived();

    // Update duedate, owner and priority should trigger update-event
    taskService.setDueDate(task.getId(), new Date());
    assertThat(listener.getEventsReceived()).hasSize(1);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertExecutionDetails(event, processInstance);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    listener.clearEventsReceived();

    taskService.setPriority(task.getId(), 12);
    assertThat(listener.getEventsReceived()).hasSize(1);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    assertExecutionDetails(event, processInstance);
    listener.clearEventsReceived();

    taskService.setOwner(task.getId(), "kermit");
    assertThat(listener.getEventsReceived()).hasSize(1);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    assertExecutionDetails(event, processInstance);
    listener.clearEventsReceived();

    // Updating detached task and calling saveTask should trigger a single update-event
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    task.setDueDate(new Date());
    task.setOwner("john");
    taskService.saveTask(task);

    assertThat(listener.getEventsReceived()).hasSize(1);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    assertExecutionDetails(event, processInstance);
    listener.clearEventsReceived();

    // Check delete-event on complete
    taskService.complete(task.getId());
    assertThat(listener.getEventsReceived()).hasSize(2);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.TASK_COMPLETED);
    assertExecutionDetails(event, processInstance);
    TaskEntity taskEntity = (TaskEntity) event.getEntity();
    assertThat(taskEntity.getDueDate()).isNotNull();
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    assertExecutionDetails(event, processInstance);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskAssignmentEventInProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processInstance).isNotNull();
    listener.clearEventsReceived();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Set assignee through API
    taskService.setAssignee(task.getId(), "kermit");
    assertThat(listener.getEventsReceived()).hasSize(2);
    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.TASK_ASSIGNED);
    assertThat(event.getEntity()).isInstanceOf(Task.class);
    Task taskFromEvent = (Task) event.getEntity();
    assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
    assertThat(taskFromEvent.getAssignee()).isEqualTo("kermit");
    assertExecutionDetails(event, processInstance);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    assertThat(event.getEntity()).isInstanceOf(Task.class);
    assertExecutionDetails(event, processInstance);
    listener.clearEventsReceived();

    // Set assignee through updateTask
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setAssignee("newAssignee");
    taskService.saveTask(task);

    assertThat(listener.getEventsReceived()).hasSize(2);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.TASK_ASSIGNED);
    assertThat(event.getEntity()).isInstanceOf(Task.class);
    taskFromEvent = (Task) event.getEntity();
    assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
    assertThat(taskFromEvent.getAssignee()).isEqualTo("newAssignee");
    assertExecutionDetails(event, processInstance);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    assertThat(event.getEntity()).isInstanceOf(Task.class);
    assertExecutionDetails(event, processInstance);
    listener.clearEventsReceived();

    // Unclaim
    taskService.unclaim(task.getId());
    assertThat(listener.getEventsReceived()).hasSize(2);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.TASK_ASSIGNED);
    assertThat(event.getEntity()).isInstanceOf(Task.class);
    taskFromEvent = (Task) event.getEntity();
    assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
    assertThat(taskFromEvent.getAssignee()).isEqualTo(null);
    assertExecutionDetails(event, processInstance);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    assertThat(event.getEntity()).isInstanceOf(Task.class);
    assertExecutionDetails(event, processInstance);
    listener.clearEventsReceived();
  }

  /**
   * Check events related to process instance delete and standalone task delete.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testDeleteEventDoesNotDispathComplete() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processInstance).isNotNull();
    listener.clearEventsReceived();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Delete process, should delete task as well, but not complete
    runtimeService.deleteProcessInstance(processInstance.getId(), "testing task delete events");

    assertThat(listener.getEventsReceived()).hasSize(1);
    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    assertThat(event.getEntity()).isInstanceOf(Task.class);
    Task taskFromEvent = (Task) event.getEntity();
    assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
    assertExecutionDetails(event, processInstance);

    try {
      task = taskService.newTask();
      task.setCategory("123");
      task.setDescription("Description");
      taskService.saveTask(task);
      listener.clearEventsReceived();

      // Delete standalone task, only a delete-event should be dispatched
      taskService.deleteTask(task.getId());

      assertThat(listener.getEventsReceived()).hasSize(1);
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);
      taskFromEvent = (Task) event.getEntity();
      assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
      assertThat(event.getProcessDefinitionId()).isNull();
      assertThat(event.getProcessInstanceId()).isNull();
      assertThat(event.getExecutionId()).isNull();

    } finally {
      if (task != null) {
        String taskId = task.getId();
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
          // If task still exists, delete it to have a clean DB after
          // test
          taskService.deleteTask(taskId);
        }
        historyService.deleteHistoricTaskInstance(taskId);
      }
    }
  }

  /**
   * This method checks to ensure that the task.fireEvent(TaskListener.EVENTNAME_CREATE), fires before
   * the dispatchEvent ActivitiEventType.TASK_CREATED.  A ScriptTaskListener updates the priority and
   * assignee before the dispatchEvent() takes place.
     */
  @Deployment(resources= {"org/activiti/engine/test/api/event/TaskEventsTest.testEventFiring.bpmn20.xml"})
  public void testEventFiringOrdering() {
    //We need to add a special listener that copies the Task values - to record its state when the event fires,
    //otherwise the in-memory task instances is changed after the event fires.
    TestActivitiEntityEventTaskListener tlistener = new TestActivitiEntityEventTaskListener(Task.class);
    processEngineConfiguration.getEventDispatcher().addEventListener(tlistener);

    try {

      runtimeService.startProcessInstanceByKey("testTaskLocalVars");

      // Fetch first task
      Task task = taskService.createTaskQuery().singleResult();

      // Complete first task
      taskService.complete(task.getId(), emptyMap(), true);

      ActivitiEntityEvent event = (ActivitiEntityEvent) tlistener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);

      event = (ActivitiEntityEvent) tlistener.getEventsReceived().get(1);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);

      event = (ActivitiEntityEvent) tlistener.getEventsReceived().get(2);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.TASK_CREATED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);
      Task taskFromEvent = tlistener.getTasks().get(2);
      assertThat(taskFromEvent.getId()).isEqualTo(task.getId());

      // verify script listener has done its job, on create before ActivitiEntityEvent was fired
      assertThat(taskFromEvent.getAssignee())
        .as("The ScriptTaskListener must set this value before the dispatchEvent fires.")
        .isEqualTo("scriptedAssignee");
      assertThat(taskFromEvent.getPriority())
        .as("The ScriptTaskListener must set this value before the dispatchEvent fires.")
        .isEqualTo(877);

      // Fetch second task
      taskService.createTaskQuery().singleResult();
    } finally {
      processEngineConfiguration.getEventDispatcher().removeEventListener(tlistener);
    }
  }

  /**
   * Check all events for tasks not related to a process-instance
   */
  public void testStandaloneTaskEvents() throws Exception {

    Task task = null;
    try {
      task = taskService.newTask();
      task.setCategory("123");
      task.setDescription("Description");
      taskService.saveTask(task);

      assertThat(listener.getEventsReceived()).hasSize(3);

      ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);
      Task taskFromEvent = (Task) event.getEntity();
      assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
      assertThat(event.getProcessDefinitionId()).isNull();
      assertThat(event.getProcessInstanceId()).isNull();
      assertThat(event.getExecutionId()).isNull();

      event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);

      event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.TASK_CREATED);
      listener.clearEventsReceived();

      // Update task
      taskService.setOwner(task.getId(), "owner");
      assertThat(listener.getEventsReceived()).hasSize(1);
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);
      taskFromEvent = (Task) event.getEntity();
      assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
      assertThat(taskFromEvent.getOwner()).isEqualTo("owner");
      assertThat(event.getProcessDefinitionId()).isNull();
      assertThat(event.getProcessInstanceId()).isNull();
      assertThat(event.getExecutionId()).isNull();
      listener.clearEventsReceived();

      // Assign task
      taskService.setAssignee(task.getId(), "kermit");
      assertThat(listener.getEventsReceived()).hasSize(2);
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.TASK_ASSIGNED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);
      taskFromEvent = (Task) event.getEntity();
      assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
      assertThat(taskFromEvent.getAssignee()).isEqualTo("kermit");
      assertThat(event.getProcessDefinitionId()).isNull();
      assertThat(event.getProcessInstanceId()).isNull();
      assertThat(event.getExecutionId()).isNull();
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);
      taskFromEvent = (Task) event.getEntity();
      assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
      assertThat(event.getProcessDefinitionId()).isNull();
      assertThat(event.getProcessInstanceId()).isNull();
      assertThat(event.getExecutionId()).isNull();
      listener.clearEventsReceived();

      // Complete task
      taskService.complete(task.getId());
      assertThat(listener.getEventsReceived()).hasSize(2);
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.TASK_COMPLETED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);
      taskFromEvent = (Task) event.getEntity();
      assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
      assertThat(event.getProcessDefinitionId()).isNull();
      assertThat(event.getProcessInstanceId()).isNull();
      assertThat(event.getExecutionId()).isNull();

      event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
      assertThat(event.getEntity()).isInstanceOf(Task.class);
      taskFromEvent = (Task) event.getEntity();
      assertThat(taskFromEvent.getId()).isEqualTo(task.getId());
      assertThat(event.getProcessDefinitionId()).isNull();
      assertThat(event.getProcessInstanceId()).isNull();
      assertThat(event.getExecutionId()).isNull();

    } finally {
      if (task != null) {
        String taskId = task.getId();
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
          // If task still exists, delete it to have a clean DB after
          // test
          taskService.deleteTask(taskId);
        }
        historyService.deleteHistoricTaskInstance(taskId);
      }
    }
  }

  protected void assertExecutionDetails(ActivitiEvent event, ProcessInstance processInstance) {
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isNotNull();
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    listener = new TestActivitiEntityEventListener(Task.class);
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    if (listener != null) {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }
}
