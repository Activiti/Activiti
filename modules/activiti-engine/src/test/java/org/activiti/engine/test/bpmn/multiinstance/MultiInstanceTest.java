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

package org.activiti.engine.test.bpmn.multiinstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class MultiInstanceTest extends PluggableActivitiTestCase {
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml"})
  public void testSequentialUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasks", 
            CollectionUtil.singletonMap("nrOfLoops", 3)).getId();
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    assertEquals("kermit_0", task.getAssignee());
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    assertEquals("kermit_1", task.getAssignee());
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    assertEquals("kermit_2", task.getAssignee());
    taskService.complete(task.getId());
    
    assertNull(taskService.createTaskQuery().singleResult());
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml"})
  public void testSequentialUserTasksWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasks",
            CollectionUtil.singletonMap("nrOfLoops", 3)).getId();
    
    // Complete 1 tasks
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    
    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml"})
  public void testSequentialUserTasksCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasks",
            CollectionUtil.singletonMap("nrOfLoops", 10)).getId();
    
    // 10 tasks are to be created, but completionCondition stops them at 5
    for (int i=0; i<5; i++) {
      Task task = taskService.createTaskQuery().singleResult();
      taskService.complete(task.getId());
    }
    assertNull(taskService.createTaskQuery().singleResult());
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testNestedSequentialUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialUserTasks").getId();
    
    for (int i=0; i<3; i++) {
      Task task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
      assertEquals("My Task", task.getName());
      taskService.complete(task.getId());
    }
    
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testParallelUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasks").getId();
    
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(3, tasks.size());
    assertEquals("My Task 0", tasks.get(0).getName());
    assertEquals("My Task 1", tasks.get(1).getName());
    assertEquals("My Task 2", tasks.get(2).getName());
    
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    taskService.complete(tasks.get(2).getId());
    assertProcessEnded(procId); 
  }
  
  @Deployment
  public void testParallelUserTasksWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksWithTimer").getId();
    
    List<Task> tasks = taskService.createTaskQuery().list();
    taskService.complete(tasks.get(0).getId());
    
    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testParallelUserTasksCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksCompletionCondition").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(5, tasks.size());
    
    // Completing 3 tasks gives 50% of tasks completed, which triggers completionCondition
    for (int i=0; i<3; i++) {
      assertEquals(5-i, taskService.createTaskQuery().count());
      taskService.complete(tasks.get(i).getId());
    }
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testNestedParallelUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelUserTasks").getId();
    
    List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
    for (Task task : tasks) {
      assertEquals("My Task", task.getName());
      taskService.complete(task.getId());
    }
    
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testSequentialScriptTasks() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put("nrOfLoops", 5);
    runtimeService.startProcessInstanceByKey("miSequentialScriptTask", vars);
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(10, sum);
  }
  
  @Deployment
  public void testSequentialScriptTasksCompletionCondition() {
    runtimeService.startProcessInstanceByKey("miSequentialScriptTaskCompletionCondition").getId();
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(5, sum);
  }
  
  @Deployment
  public void testParallelScriptTasks() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put("nrOfLoops", 10);
    runtimeService.startProcessInstanceByKey("miParallelScriptTask", vars);
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(45, sum);
  }
  
  @Deployment
  public void testParallelScriptTasksCompletionCondition() {
    runtimeService.startProcessInstanceByKey("miParallelScriptTaskCompletionCondition");
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(2, sum);
  }
  
  @Deployment
  public void testSequentialSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocess").getId();
    
    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    for (int i=0; i<4; i++) {
      List<Task> tasks = query.list();
      assertEquals(2, tasks.size());
      
      assertEquals("task one", tasks.get(0).getName());
      assertEquals("task two", tasks.get(1).getName());
      
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }
    
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testSequentialSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocessWithTimer").getId();
    
    // Complete one subprocess
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testSequentialSubProcessCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocessCompletionCondition").getId();
    
    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    for (int i=0; i<3; i++) {
      List<Task> tasks = query.list();
      assertEquals(2, tasks.size());
      
      assertEquals("task one", tasks.get(0).getName());
      assertEquals("task two", tasks.get(1).getName());
      
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }
    
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testNestedSequentialSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialSubProcess").getId();
    
    for (int i=0; i<3; i++) {
      List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }
    
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testNestedSequentialSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialSubProcessWithTimer").getId();
    
    for (int i=0; i<2; i++) {
      List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }
    
    // Complete one task, to make it a bit more trickier
    List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
    taskService.complete(tasks.get(0).getId());
    
    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    
    assertProcessEnded(procId);
  }

  @Deployment
  public void testParallelSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocess").getId();
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(4, tasks.size());
    
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testParallelSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessWithTimer").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(6, tasks.size());
    
    // Complete two tasks
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    
    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testParallelSubProcessCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessCompletionCondition").getId();
    List<Task> tasks = taskService.createTaskQuery().orderByTaskId().asc().list();
    assertEquals(4, tasks.size());
    
    for (int i=0; i<2; i++) {
      taskService.complete(tasks.get(i).getId());
    }
    
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testParallelSubProcessAllAutomatic() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessAllAutomatics",
            CollectionUtil.singletonMap("nrOfLoops", 5)).getId();
    Execution waitState = runtimeService.createExecutionQuery().singleResult();
    assertEquals(10, runtimeService.getVariable(waitState.getId(), "sum"));
    
    runtimeService.signal(waitState.getId());
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelSubProcessAllAutomatic.bpmn20.xml"})
  public void testParallelSubProcessAllAutomaticCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessAllAutomatics", 
            CollectionUtil.singletonMap("nrOfLoops", 10)).getId();
    Execution waitState = runtimeService.createExecutionQuery().singleResult();
    assertEquals(12, runtimeService.getVariable(waitState.getId(), "sum"));
    
    runtimeService.signal(waitState.getId());
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testNestedParallelSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelSubProcess").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(8, tasks.size());
    
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testNestedParallelSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelSubProcess").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(12, tasks.size());
    
    for (int i=0; i<3; i++) {
      taskService.complete(tasks.get(i).getId());
    }
    
    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialCallActivity.bpmn20.xml",
          "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml"})
  public void testSequentialCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialCallActivity").getId();
    
    for (int i=0; i<3; i++) {
      List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
      assertEquals(2, tasks.size());
      assertEquals("task one", tasks.get(0).getName());
      assertEquals("task two", tasks.get(1).getName());
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }
    
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialCallActivityWithTimer.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testSequentialCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialCallActivityWithTimer").getId();

    // Complete first subprocess
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task one", tasks.get(0).getName());
    assertEquals("task two", tasks.get(1).getName());
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    
    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testParallelCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelCallActivity").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(12, tasks.size());
    for (int i = 0; i < tasks.size(); i++) {
      taskService.complete(tasks.get(i).getId());
    }

    assertProcessEnded(procId);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivityWithTimer.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testParallelCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelCallActivity").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(6, tasks.size());
    for (int i = 0; i < 2; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedSequentialCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedSequentialCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialCallActivity").getId();
    
    for (int i=0; i<4; i++) {
      List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
      assertEquals(2, tasks.size());
      assertEquals("task one", tasks.get(0).getName());
      assertEquals("task two", tasks.get(1).getName());
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    assertProcessEnded(procId);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedSequentialCallActivityWithTimer.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedSequentialCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialCallActivityWithTimer").getId();

    // first instance
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task one", tasks.get(0).getName());
    assertEquals("task two", tasks.get(1).getName());
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    
    // one task of second instance
    tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());
    taskService.complete(tasks.get(0).getId());

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedParallelCallActivity.bpmn20.xml",
  "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedParallelCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelCallActivity").getId();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(14, tasks.size());
    for (int i = 0; i < 14; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    assertProcessEnded(procId);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedParallelCallActivityWithTimer.bpmn20.xml",
  "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedParallelCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelCallActivityWithTimer").getId();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(4, tasks.size());
    for (int i = 0; i < 3; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    
    assertProcessEnded(procId);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedParallelCallActivityCompletionCondition.bpmn20.xml",
  "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedParallelCallActivityCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelCallActivityCompletionCondition").getId();

    List<Task> tasks = taskService.createTaskQuery().orderByTaskId().asc().list();
    assertEquals(8, tasks.size());
    for (int i = 0; i < 4; i++) {
      taskService.complete(tasks.get(i).getId());
    }
    
    assertProcessEnded(procId);
  }
  
}
