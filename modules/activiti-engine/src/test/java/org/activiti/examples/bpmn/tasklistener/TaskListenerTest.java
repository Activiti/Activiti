/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.examples.bpmn.tasklistener;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class TaskListenerTest extends PluggableActivitiTestCase {
  
  @Deployment(resources = {"org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskCreateListener() {
    runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Schedule meeting", task.getName());
    assertEquals("TaskCreateListener is listening!", task.getDescription());
  }
  
  @Deployment(resources = {"org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskAssignmentListener() {
    runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("TaskCreateListener is listening!", task.getDescription());
    
    // Set assignee and check if event is received
    taskService.setAssignee(task.getId(), "kermit");
    task = taskService.createTaskQuery().singleResult();
  assertEquals("TaskAssignmentListener is listening: kermit", task.getDescription());
  }
  
  @Deployment(resources = {"org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskCompleteListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "greeting"));
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "expressionValue"));
    
    // Completing first task will change the description 
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    
    assertEquals("Hello from The Process", runtimeService.getVariable(processInstance.getId(), "greeting"));
    assertEquals("Act", runtimeService.getVariable(processInstance.getId(), "shortName"));
  }
  
  @Deployment(resources = {"org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskListenerWithExpression() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "greeting2"));
    
    // Completing first task will change the description 
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    
    assertEquals("Write meeting notes", runtimeService.getVariable(processInstance.getId(), "greeting2"));
  }
  
  @Deployment(resources = {"org/activiti/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testAllEventsTaskListener() {
    runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    
    // Set assignee and complete task
    taskService.setAssignee(task.getId(), "kermit");
    taskService.complete(task.getId());
    
    // Verify the all-listener has received all events
    String eventsReceived = (String) runtimeService.getVariable(task.getProcessInstanceId(), "events");
    assertEquals("create - assignment - complete", eventsReceived);
  }
  
  @Deployment(resources = {"org/activiti/examples/bpmn/tasklistener/TaskListenerTest.testTaskListenersOnDelete.bpmn20.xml"})
  public void testTaskListenersOnDeleteByComplete() {
	  TaskDeleteListener.clear();
	  runtimeService.startProcessInstanceByKey("executionListenersOnDelete");
	  
	  List<Task> tasks = taskService.createTaskQuery().list();
	  assertNotNull(tasks);
	  assertEquals(1, tasks.size());
	  
	  Task task = taskService.createTaskQuery().taskName("User Task 1").singleResult();
	  assertNotNull(task);

	  assertEquals(0, TaskDeleteListener.getCurrentMessages().size());
	  assertEquals(0, TaskSimpleCompleteListener.getCurrentMessages().size());
	  
	  taskService.complete(task.getId());
	  
	  tasks = taskService.createTaskQuery().list();
	  
	  assertNotNull(tasks);
	  assertEquals(0, tasks.size());
	   
	  assertEquals(1, TaskDeleteListener.getCurrentMessages().size());
	  assertEquals("Delete Task Listener executed.", TaskDeleteListener.getCurrentMessages().get(0));
	  
	  assertEquals(1, TaskSimpleCompleteListener.getCurrentMessages().size());
    assertEquals("Complete Task Listener executed.", TaskSimpleCompleteListener.getCurrentMessages().get(0));
  }
  
  @Deployment(resources = {"org/activiti/examples/bpmn/tasklistener/TaskListenerTest.testTaskListenersOnDelete.bpmn20.xml"})
  public void testTaskListenersOnDeleteByDeleteProcessInstance() {
    TaskDeleteListener.clear();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersOnDelete");
    
    List<Task> tasks = taskService.createTaskQuery().list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());
    
    Task task = taskService.createTaskQuery().taskName("User Task 1").singleResult();
    assertNotNull(task);

    assertEquals(0, TaskDeleteListener.getCurrentMessages().size());
    assertEquals(0, TaskSimpleCompleteListener.getCurrentMessages().size());

    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");
    
    tasks = taskService.createTaskQuery().list();
    
    assertNotNull(tasks);
    assertEquals(0, tasks.size());
     
    assertEquals(1, TaskDeleteListener.getCurrentMessages().size());
    assertEquals("Delete Task Listener executed.", TaskDeleteListener.getCurrentMessages().get(0));
    
    assertEquals(0, TaskSimpleCompleteListener.getCurrentMessages().size());
  }
}
