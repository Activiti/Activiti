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

package org.activiti.engine.test.bpmn.subprocess.adhoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Tijs Rademakers
 */
public class AdhocSubProcessTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSimpleAdhocSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertNotNull(execution);
    
    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(2, enabledActivities.size());
    
    Execution newTaskExecution = runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    assertNotNull(newTaskExecution);
    assertNotNull(newTaskExecution.getId());
    
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("subProcessTask").singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());

    taskService.complete(subProcessTask.getId());
    
    enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(2, enabledActivities.size());
    
    runtimeService.completeAdhocSubProcess(execution.getId());
    
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("After task", afterTask.getName());
    
    taskService.complete(afterTask.getId());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }
  
  @Deployment
  public void testSimpleCompletionCondition() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertNotNull(execution);
    
    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(2, enabledActivities.size());
    
    Execution newTaskExecution = runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    assertNotNull(newTaskExecution);
    assertNotNull(newTaskExecution.getId());
    
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("subProcessTask").singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());

    taskService.complete(subProcessTask.getId());
    
    enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(2, enabledActivities.size());
    
    newTaskExecution = runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    
    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task2 in subprocess", subProcessTask.getName());
    
    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);
    
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("After task", afterTask.getName());
    
    taskService.complete(afterTask.getId());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
    
      List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
          .processInstanceId(pi.getId())
          .orderByHistoricTaskInstanceEndTime()
          .asc()
          .list();
      
      assertEquals(3, historicTasks.size());
      List<String> taskDefinitionKeys = new ArrayList<String>(3);
      taskDefinitionKeys.add(historicTasks.get(0).getTaskDefinitionKey());
      taskDefinitionKeys.add(historicTasks.get(1).getTaskDefinitionKey());
      taskDefinitionKeys.add(historicTasks.get(2).getTaskDefinitionKey());
      assertTrue(taskDefinitionKeys.contains("subProcessTask"));
      assertTrue(taskDefinitionKeys.contains("subProcessTask2"));
      assertTrue(taskDefinitionKeys.contains("afterTask"));
      
    }
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }
  
  @Deployment
  public void testParallelAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertNotNull(execution);
    
    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(2, enabledActivities.size());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(2, tasks.size());

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);
    
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("After task", afterTask.getName());
    
    taskService.complete(afterTask.getId());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }
  
  @Deployment
  public void testSequentialAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertNotNull(execution);
    
    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(2, enabledActivities.size());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    try {
      runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
      fail("exception expected because can only enable one activity in a sequential ad-hoc sub process");
    } catch (ActivitiException e) {
      // expected
    }
    
    taskService.complete(subProcessTask.getId());
    
    // now we can enable the activity
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    
    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task2 in subprocess", subProcessTask.getName());

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);
    
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("After task", afterTask.getName());
    
    taskService.complete(afterTask.getId());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }
  
  @Deployment
  public void testFlowsInAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertNotNull(execution);
    
    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(2, enabledActivities.size());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    taskService.complete(subProcessTask.getId());
    
    try {
      runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
      fail("exception expected because can only enable one activity in a sequential ad-hoc sub process");
    } catch (ActivitiException e) {
      // expected
    }
    
    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("The next task", subProcessTask.getName());
    
    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);
    
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("After task", afterTask.getName());
    
    taskService.complete(afterTask.getId());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }
  
  @Deployment(resources="org/activiti/engine/test/bpmn/subprocess/adhoc/AdhocSubProcessTest.testFlowsInAdhocSubProcess.bpmn20.xml")
  public void testCompleteFlowBeforeEndInAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertNotNull(execution);
    
    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(2, enabledActivities.size());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);
    
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("After task", afterTask.getName());
    
    taskService.complete(afterTask.getId());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }
  
  @Deployment
  public void testParallelFlowsInAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertNotNull(execution);
    
    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(3, enabledActivities.size());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask3");
    
    Task subProcessTask2 = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("subProcessTask2").singleResult();
    assertEquals("Task2 in subprocess", subProcessTask2.getName());
    taskService.complete(subProcessTask2.getId());
    
    subProcessTask2 = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("sequentialTask2").singleResult();
    assertEquals("The next task2", subProcessTask2.getName());
    
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(3, tasks.size());
    
    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);
    
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("After task", afterTask.getName());
    
    taskService.complete(afterTask.getId());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }
  
  @Deployment
  public void testKeepRemainingInstancesAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertNotNull(execution);
    
    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(2, enabledActivities.size());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(2, tasks.size());

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);
    
    // ad-hoc sub process is not completed because of cancelRemainingInstances is set to false
    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task2 in subprocess", subProcessTask.getName());
    
    taskService.complete(subProcessTask.getId());
    
    // with no remaining executions the ad-hoc sub process will be completed
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("After task", afterTask.getName());
    
    taskService.complete(afterTask.getId());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }
  
  @Deployment
  public void testParallelFlowsWithKeepRemainingInstancesAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertNotNull(execution);
    
    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertEquals(3, enabledActivities.size());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask3");
    
    Task subProcessTask2 = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("subProcessTask2").singleResult();
    assertEquals("Task2 in subprocess", subProcessTask2.getName());
    taskService.complete(subProcessTask2.getId());
    
    subProcessTask2 = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("sequentialTask2").singleResult();
    assertEquals("The next task2", subProcessTask2.getName());
    
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(3, tasks.size());
    
    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);
    
    // ad-hoc sub process is not completed because of cancelRemainingInstances is set to false
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(3, tasks.size());
    
    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("sequentialTask").singleResult();
    assertEquals("The next task", subProcessTask.getName());
    
    taskService.complete(subProcessTask.getId(), variableMap);
    
    // ad-hoc sub process is not completed because of cancelRemainingInstances is set to false
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(2, tasks.size());
    
    taskService.complete(subProcessTask2.getId(), variableMap);
    
    // ad-hoc sub process is not completed because of cancelRemainingInstances is set to false
    Task subProcessTask3 = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task3 in subprocess", subProcessTask3.getName());
    
    taskService.complete(subProcessTask3.getId(), variableMap);
    
    // with no remaining executions the ad-hoc sub process will be completed
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("After task", afterTask.getName());
    
    taskService.complete(afterTask.getId());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }
}
