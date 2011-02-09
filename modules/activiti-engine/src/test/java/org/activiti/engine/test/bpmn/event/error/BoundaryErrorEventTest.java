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
package org.activiti.engine.test.bpmn.event.error;

import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class BoundaryErrorEventTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testCatchErrorOnEmbeddedSubprocess() {
    runtimeService.startProcessInstanceByKey("boundaryErrorOnEmbeddedSubprocess");
    
    // After process start, usertask in subprocess should exist
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("subprocessTask", task.getName());
    
    // After task completion, error end event is reached and catched
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task after catching the error", task.getName());
  }
  
  @Deployment
  public void testCatchErrorOfInnerSubprocessOnOuterSubprocess() {
    runtimeService.startProcessInstanceByKey("boundaryErrorTest");

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("Inner subprocess task 1", tasks.get(0).getName());
    assertEquals("Inner subprocess task 2", tasks.get(1).getName());
    
    // Completing task 2, will cause the end error event to throw error with code 123
    taskService.complete(tasks.get(1).getId());
    tasks = taskService.createTaskQuery().list();
    Task taskAfterError = taskService.createTaskQuery().singleResult();
    assertEquals("task outside subprocess", taskAfterError.getName());
  }
  
  @Deployment
  public void testCatchErrorInConcurrentEmbeddedSubprocesses() {
    
    // Completing task A will lead to task D
    String procId = runtimeService.startProcessInstanceByKey("boundaryEventTestConcurrentSubprocesses").getId();
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task A", tasks.get(0).getName());
    assertEquals("task B", tasks.get(1).getName());
    taskService.complete(tasks.get(0).getId());
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task D", task.getName());
    taskService.complete(task.getId());
    assertProcessEnded(procId);
    
    // Completing task B will lead to task C
    procId = runtimeService.startProcessInstanceByKey("boundaryEventTestConcurrentSubprocesses").getId();
    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task A", tasks.get(0).getName());
    assertEquals("task B", tasks.get(1).getName());
    taskService.complete(tasks.get(1).getId());
    
    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task A", tasks.get(0).getName());
    assertEquals("task C", tasks.get(1).getName());
    taskService.complete(tasks.get(1).getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task A", task.getName());
    
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task D", task.getName());
  }
  
  @Deployment
  public void testDeeplyNestedErrorThrown() {
    
    // Input = 1 -> error1 will be thrown, which will destroy ALL BUT ONE 
    // subprocess, which leads to an end event, which ultimately leads to ending the process instance
    String procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Nested task", task.getName());
    taskService.complete(task.getId(), CollectionUtil.singletonMap("input", 1));
    assertProcessEnded(procId);
    
    // Input == 2 -> error2 will be thrown, leading to a userTask outside all subprocesses
    procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown").getId();
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Nested task", task.getName());
    taskService.complete(task.getId(), CollectionUtil.singletonMap("input", 2));
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task after catch", task.getName());
    taskService.complete(task.getId());
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testDeeplyNestedErrorThrownOnlyAutomaticSteps() {
    // input == 1 -> error2 is thrown -> catched on subprocess2 -> end event in subprocess -> proc inst end 1
    String procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown", 
            CollectionUtil.singletonMap("input", 1)).getId();
    assertProcessEnded(procId);
    
    HistoricProcessInstance hip;
    int historyLevel = processEngineConfiguration.getHistoryLevel();
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      hip = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
      assertEquals("processEnd1", hip.getEndActivityId());
    }
    // input == 2 -> error2 is thrown -> catched on subprocess1 -> proc inst end 2
    procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown", 
            CollectionUtil.singletonMap("input", 1)).getId();
    assertProcessEnded(procId);
    
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      hip = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
      assertEquals("processEnd1", hip.getEndActivityId());
    }
  }
  
  @Deployment(resources = {
          "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorOnCallActivity-parent.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml"
  })
  public void testCatchErrorOnCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnCallActivity").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task in subprocess", task.getName());
    
    // Completing the task will reach the end error event,
    // which is catched on the call activity boundary
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Escalated Task", task.getName());
    
    // Completing the task will end the process instance
    taskService.complete(task.getId());
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = {
          "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByCallActivityOnSubprocess.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml"
  })
  public void testCatchErrorThrownByCallActivityOnSubprocess() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnSubprocess").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task in subprocess", task.getName());
    
    // Completing the task will reach the end error event,
    // which is catched on the call activity boundary
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Escalated Task", task.getName());
    
    // Completing the task will end the process instance
    taskService.complete(task.getId());
    assertProcessEnded(procId);
  }

}
