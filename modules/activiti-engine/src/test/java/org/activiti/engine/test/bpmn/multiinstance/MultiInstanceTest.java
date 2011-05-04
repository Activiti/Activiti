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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
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
  public void testSequentialUserTasksHistory() {
    runtimeService.startProcessInstanceByKey("miSequentialUserTasks", 
            CollectionUtil.singletonMap("nrOfLoops", 4)).getId();
    for (int i=0; i<4; i++) {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
    }
    
    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
    
      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();
      assertEquals(4, historicTaskInstances.size());
      for (HistoricTaskInstance ht : historicTaskInstances) {
        assertNotNull(ht.getAssignee());
        assertNotNull(ht.getStartTime());
        assertNotNull(ht.getEndTime());
      }
      
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
      assertEquals(4, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getActivityId());
        assertNotNull(hai.getActivityName());
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
        assertNotNull(hai.getAssignee());
      }
      
    }
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
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelUserTasks.bpmn20.xml"})
  public void testParallelUserTasksHistory() {
    runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }
    
    // Validate history
    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().asc().list();
      for (int i=0; i<historicTaskInstances.size(); i++) {
        HistoricTaskInstance hi = historicTaskInstances.get(i);
        assertNotNull(hi.getStartTime());
        assertNotNull(hi.getEndTime());
        assertEquals("kermit_"+i, hi.getAssignee());
      }
      
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
      assertEquals(3, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
        assertNotNull(hai.getAssignee());
        assertEquals("userTask", hai.getActivityType());
      }
    }
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
  public void testParallelUserTasksBasedOnCollection() {
    List<String> assigneeList = Arrays.asList("kermit", "gonzo", "mispiggy", "fozzie", "bubba");
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksBasedOnCollection",
          CollectionUtil.singletonMap("assigneeList", assigneeList)).getId();
    
    List<Task> tasks = taskService.createTaskQuery().orderByTaskAssignee().asc().list();
    assertEquals(5, tasks.size());
    assertEquals("bubba", tasks.get(0).getAssignee());
    assertEquals("fozzie", tasks.get(1).getAssignee());
    assertEquals("gonzo", tasks.get(2).getAssignee());
    assertEquals("kermit", tasks.get(3).getAssignee());
    assertEquals("mispiggy", tasks.get(4).getAssignee());
    
    // Completing 3 tasks will trigger completioncondition
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    taskService.complete(tasks.get(2).getId());
    assertEquals(0, taskService.createTaskQuery().count());
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testParallelUserTasksCustomExtensions() {
    Map<String, Object> vars = new HashMap<String, Object>();
    List<String> assigneeList = Arrays.asList("kermit", "gonzo", "fozzie");
    vars.put("assigneeList", assigneeList);
    runtimeService.startProcessInstanceByKey("miSequentialUserTasks", vars);
    
    for (String assignee : assigneeList) {
      Task task = taskService.createTaskQuery().singleResult();
      assertEquals(assignee, task.getAssignee());
      taskService.complete(task.getId());
    }
  }
  
  @Deployment
  public void testParallelUserTasksExecutionAndTaskListeners() {
    runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    
    Execution waitState = runtimeService.createExecutionQuery().singleResult();
    assertEquals(3, runtimeService.getVariable(waitState.getId(), "taskListenerCounter"));
    assertEquals(3, runtimeService.getVariable(waitState.getId(), "executionListenerCounter"));
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
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialScriptTasks.bpmn20.xml"})
  public void testSequentialScriptTasksHistory() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put("nrOfLoops", 7);
    runtimeService.startProcessInstanceByKey("miSequentialScriptTask", vars);
    
    // Validate history
    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicInstances = historyService.createHistoricActivityInstanceQuery().activityType("scriptTask").orderByActivityId().asc().list();
      assertEquals(7, historicInstances.size());
      for (int i=0; i<7; i++) {
        HistoricActivityInstance hai = historicInstances.get(i);
        assertEquals("scriptTask", hai.getActivityType());
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
      }
    }
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
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelScriptTasks.bpmn20.xml"})
  public void testParallelScriptTasksHistory() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put("nrOfLoops", 4);
    runtimeService.startProcessInstanceByKey("miParallelScriptTask", vars);
    
    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("scriptTask").list();
      assertEquals(4, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getStartTime());
      }
    }
  }
  
  @Deployment
  public void testParallelScriptTasksCompletionCondition() {
    runtimeService.startProcessInstanceByKey("miParallelScriptTaskCompletionCondition");
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(2, sum);
  }
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelScriptTasksCompletionCondition.bpmn20.xml"})
  public void testParallelScriptTasksCompletionConditionHistory() {
    runtimeService.startProcessInstanceByKey("miParallelScriptTaskCompletionCondition");
    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("scriptTask").list();
      assertEquals(2, historicActivityInstances.size());
    }
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

  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialSubProcess.bpmn20.xml"})
  public void testSequentialSubProcessHistory() {
    runtimeService.startProcessInstanceByKey("miSequentialSubprocess");
    for (int i=0; i<4; i++) {
      List<Task> tasks = taskService.createTaskQuery().list();
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }
    
    // Validate history
    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> onlySubProcessInstances = historyService.createHistoricActivityInstanceQuery().activityType("subProcess").list();
      assertEquals(4, onlySubProcessInstances.size());
      
      List<HistoricActivityInstance> historicInstances = historyService.createHistoricActivityInstanceQuery().activityType("subProcess").list();
      assertEquals(4, historicInstances.size()); 
      for (HistoricActivityInstance hai : historicInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
      }
      
      historicInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
      assertEquals(8, historicInstances.size()); 
      for (HistoricActivityInstance hai : historicInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
      }
    }
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
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelSubProcess.bpmn20.xml"})
  public void testParallelSubProcessHistory() {
    runtimeService.startProcessInstanceByKey("miParallelSubprocess");
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }
    
    // Validate history
    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityId("miSubProcess").list();
      assertEquals(2, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
      }
    }
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
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivity.bpmn20.xml",
  "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testParallelCallActivityHistory() {
    runtimeService.startProcessInstanceByKey("miParallelCallActivity");
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(12, tasks.size());
    for (int i = 0; i < tasks.size(); i++) {
      taskService.complete(tasks.get(i).getId());
    }
    
    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      // Validate historic processes
      List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
      assertEquals(7, historicProcessInstances.size()); // 6 subprocesses + main process
      for (HistoricProcessInstance hpi : historicProcessInstances) {
        assertNotNull(hpi.getStartTime());
        assertNotNull(hpi.getEndTime());
      }
      
      // Validate historic tasks
      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();
      assertEquals(12, historicTaskInstances.size());
      for (HistoricTaskInstance hti : historicTaskInstances) {
        assertNotNull(hti.getStartTime());
        assertNotNull(hti.getEndTime());
        assertNotNull(hti.getAssignee());
        assertEquals("completed", hti.getDeleteReason());
      }
      
      // Validate historic activities
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("callActivity").list();
      assertEquals(6, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
      }
    }
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
  
  // ACT-764
  @Deployment
  public void testSequentialServiceTaskWithClass() {
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("multiInstanceServiceTask", CollectionUtil.singletonMap("result", 5));
    Integer result = (Integer) runtimeService.getVariable(procInst.getId(), "result");
    assertEquals(160, result.intValue());
    
    runtimeService.signal(procInst.getId());
    assertProcessEnded(procInst.getId());
  }
  
  @Deployment
  public void testSequentialServiceTaskWithClassAndCollection() {
    Collection<Integer> items = Arrays.asList(1,2,3,4,5,6);
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("result", 1);
    vars.put("items", items);
    
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("multiInstanceServiceTask", vars);
    Integer result = (Integer) runtimeService.getVariable(procInst.getId(), "result");
    assertEquals(720, result.intValue());
    
    runtimeService.signal(procInst.getId());
    assertProcessEnded(procInst.getId());
  }
  
}
