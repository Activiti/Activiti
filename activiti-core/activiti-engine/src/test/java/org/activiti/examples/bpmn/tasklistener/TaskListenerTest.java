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

package org.activiti.examples.bpmn.tasklistener;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import java.util.List;


public class TaskListenerTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
  public void testTaskCreateListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Schedule meeting");
    assertThat(task.getDescription()).isEqualTo("TaskCreateListener is listening!");

    // Manually cleanup the process instance. If we don't do this, the
    // following actions will occur:
    // 1. The cleanup rule will delete the process
    // 2. The process deletion will fire a DELETE event to the TaskAllEventsListener
    // 3. The TaskAllEventsListener will set a variable on the Task
    // 4. Setting that variable will result in an entry in the ACT_HI_DETAIL table
    // 5. The AbstractActivitiTestCase will fail the test because the DB is not clean
    // By triggering the DELETE event from within the test, we ensure that
    // all of the records are written before the test cleanup begins
    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");
  }

  @Deployment(resources = {"org/activiti/examples/bpmn/tasklistener/TaskListenerInSubProcessTest.bpmn20.xml"})
  public void testTaskCreateListenerInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerInSubProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Schedule meeting");
    assertThat(task.getDescription()).isEqualTo("TaskCreateListener is listening!");

    // Manually cleanup the process instance. If we don't do this, the
    // following actions will occur:
    // 1. The cleanup rule will delete the process
    // 2. The process deletion will fire a DELETE event to the TaskAllEventsListener
    // 3. The TaskAllEventsListener will set a variable on the Task
    // 4. Setting that variable will result in an entry in the ACT_HI_DETAIL table
    // 5. The AbstractActivitiTestCase will fail the test because the DB is not clean
    // By triggering the DELETE event from within the test, we ensure that
    // all of the records are written before the test cleanup begins
    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");
  }


  @Deployment(resources = { "org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
  public void testTaskAssignmentListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getDescription()).isEqualTo("TaskCreateListener is listening!");

    // Set assignee and check if event is received
    taskService.setAssignee(task.getId(), "kermit");
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: kermit");

    // Manually cleanup the process instance. If we don't do this, the
    // following actions will occur:
    // 1. The cleanup rule will delete the process
    // 2. The process deletion will fire a DELETE event to the
    // TaskAllEventsListener
    // 3. The TaskAllEventsListener will set a variable on the Task
    // 4. Setting that variable will result in an entry in the ACT_HI_DETAIL
    // table
    // 5. The AbstractActivitiTestCase will fail the test because the DB is
    // not clean
    // By triggering the DELETE event from within the test, we ensure that
    // all of the records
    // are written before the test cleanup begins
    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");
  }

  /**
   * Validate fix for ACT-1627: Not throwing assignment event on every update
   */
  @Deployment(resources = { "org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
  public void testTaskAssignmentListenerNotCalledWhenAssigneeNotUpdated() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getDescription()).isEqualTo("TaskCreateListener is listening!");

    // Set assignee and check if event is received
    taskService.setAssignee(task.getId(), "kermit");
    task = taskService.createTaskQuery().singleResult();

    assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: kermit");

    // Reset description and assign to same person. This should NOT trigger
    // an assignment
    task.setDescription("Clear");
    taskService.saveTask(task);
    taskService.setAssignee(task.getId(), "kermit");
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getDescription()).isEqualTo("Clear");

    // Set assignee through task-update
    task.setAssignee("kermit");
    taskService.saveTask(task);

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getDescription()).isEqualTo("Clear");

    // Update another property should not trigger assignment
    task.setName("test");
    taskService.saveTask(task);

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getDescription()).isEqualTo("Clear");

    // Update to different
    task.setAssignee("john");
    taskService.saveTask(task);

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: john");

    // Manually cleanup the process instance.
    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
  public void testTaskCompleteListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertThat(runtimeService.getVariable(processInstance.getId(), "greeting")).isEqualTo(null);
    assertThat(runtimeService.getVariable(processInstance.getId(), "expressionValue")).isEqualTo(null);

    // Completing first task will change the description
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertThat(runtimeService.getVariable(processInstance.getId(), "greeting")).isEqualTo("Hello from The Process");
    assertThat(runtimeService.getVariable(processInstance.getId(), "shortName")).isEqualTo("Act");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
  public void testTaskListenerWithExpression() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertThat(runtimeService.getVariable(processInstance.getId(), "greeting2")).isEqualTo(null);

    // Completing first task will change the description
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertThat(runtimeService.getVariable(processInstance.getId(), "greeting2")).isEqualTo("Write meeting notes");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
  public void testAllEventsTaskListener() {
    runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();

    // Set assignee and complete task
    taskService.setAssignee(task.getId(), "kermit");
    taskService.complete(task.getId());

    // Verify the all-listener has received all events
    String eventsReceived = (String) runtimeService.getVariable(task.getProcessInstanceId(), "events");
    assertThat(eventsReceived).isEqualTo("create - assignment - complete - delete");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/tasklistener/TaskListenerTest.testTaskListenersOnDelete.bpmn20.xml" })
  public void testTaskListenersOnDeleteByComplete() {
    TaskDeleteListener.clear();
    runtimeService.startProcessInstanceByKey("executionListenersOnDelete");

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(1);

    Task task = taskService.createTaskQuery().taskName("User Task 1").singleResult();
    assertThat(task).isNotNull();

    assertThat(TaskDeleteListener.getCurrentMessages()).hasSize(0);
    assertThat(TaskSimpleCompleteListener.getCurrentMessages()).hasSize(0);

    taskService.complete(task.getId());

    tasks = taskService.createTaskQuery().list();

    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(0);

    assertThat(TaskDeleteListener.getCurrentMessages()).hasSize(1);
    assertThat(TaskDeleteListener.getCurrentMessages().get(0)).isEqualTo("Delete Task Listener executed.");

    assertThat(TaskSimpleCompleteListener.getCurrentMessages()).hasSize(1);
    assertThat(TaskSimpleCompleteListener.getCurrentMessages().get(0)).isEqualTo("Complete Task Listener executed.");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/tasklistener/TaskListenerTest.testTaskListenersOnDelete.bpmn20.xml" })
  public void testTaskListenersOnDeleteByDeleteProcessInstance() {
    TaskDeleteListener.clear();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersOnDelete");

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(1);

    Task task = taskService.createTaskQuery().taskName("User Task 1").singleResult();
    assertThat(task).isNotNull();

    assertThat(TaskDeleteListener.getCurrentMessages()).hasSize(0);
    assertThat(TaskSimpleCompleteListener.getCurrentMessages()).hasSize(0);

    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");

    tasks = taskService.createTaskQuery().list();

    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(0);

    assertThat(TaskDeleteListener.getCurrentMessages()).hasSize(1);
    assertThat(TaskDeleteListener.getCurrentMessages().get(0)).isEqualTo("Delete Task Listener executed.");

    assertThat(TaskSimpleCompleteListener.getCurrentMessages()).hasSize(0);
  }
}
