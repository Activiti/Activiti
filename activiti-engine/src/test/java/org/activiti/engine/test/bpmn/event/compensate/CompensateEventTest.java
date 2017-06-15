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

package org.activiti.engine.test.bpmn.event.compensate;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.EnableVerboseExecutionTreeLogging;
import org.activiti.engine.test.bpmn.event.compensate.helper.SetVariablesDelegate;

/**
 * @author Tijs Rademakers
 */
@EnableVerboseExecutionTreeLogging
public class CompensateEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testCompensateSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment
  public void testCompensateSubprocessWithoutActivityRef() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment
  public void testCompensateSubprocessWithUserTask() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("Manually undo book hotel", task.getName());
    taskService.complete(task.getId());

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment
  public void testCompensateSubprocessWithUserTask2() {
    
    // Same process as testCompensateSubprocessWithUserTask, but now the end event is reached first
    // (giving an exception before)

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("Manually undo book hotel", task.getName());
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment
  public void testCompensateMiSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateScope() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testCallActivityCompensationHandler.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/compensate/CompensationHandler.bpmn20.xml" })
  public void testCallActivityCompensationHandler() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());
    }

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(6, historyService.createHistoricProcessInstanceQuery().count());
    }

  }

  @Deployment
  public void testCompensateMiSubprocessVariableSnapshots() {

    // see referenced java delegates in the process definition.

    SetVariablesDelegate.variablesMap.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());
    }

    assertProcessEnded(processInstance.getId());

  }

  public void testMultipleCompensationCatchEventsFails() {
    try {
      repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testMultipleCompensationCatchEventsFails.bpmn20.xml").deploy();
      fail("exception expected");
    } catch (Exception e) {
    }
  }

  public void testInvalidActivityRefFails() {
    try {
      repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testInvalidActivityRefFails.bpmn20.xml").deploy();
      fail("exception expected");
    } catch (Exception e) {
      if (!e.getMessage().contains("Invalid attribute value for 'activityRef':")) {
        fail("different exception expected");
      }
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationStepEndRecorded.bpmn20.xml" })
  public void testCompensationStepEndTimeRecorded() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationStepEndRecordedProcess");
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      final HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().activityId("compensationScriptTask");
      assertEquals(1, query.count());
      final HistoricActivityInstance compensationScriptTask = query.singleResult();
      assertNotNull(compensationScriptTask);
      assertNotNull(compensationScriptTask.getEndTime());
      assertNotNull(compensationScriptTask.getDurationInMillis());
    }
  }
  
  @Deployment
  public void testCompensateWithSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId()).activityId("bookHotel").singleResult();
      assertNotNull(historicActivityInstance.getEndTime());
    }
    
    // Triggering the task will trigger the compensation subprocess
    Task afterBookHotelTask = taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .taskDefinitionKey("afterBookHotel").singleResult();
    taskService.complete(afterBookHotelTask.getId());
    
    Task compensationTask1 = taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .taskDefinitionKey("compensateTask1").singleResult();
    assertNotNull(compensationTask1);
    
    Task compensationTask2 = taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .taskDefinitionKey("compensateTask2").singleResult();
    assertNotNull(compensationTask2);
    
    taskService.complete(compensationTask1.getId());
    taskService.complete(compensationTask2.getId());
    
    Task compensationTask3 = taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .taskDefinitionKey("compensateTask3").singleResult();
    assertNotNull(compensationTask3);
    taskService.complete(compensationTask3.getId());
    
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensateWithSubprocess.bpmn20.xml" })
  public void testCompensateWithSubprocess2() {
    
    // Same as testCompensateWithSubprocess, but without throwing the compensation event
    // As such, to verify that the extra compensation executions have no effect on the regular process execution
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess", 
        CollectionUtil.singletonMap("doCompensation", false));
    
    Task afterBookHotelTask = taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .taskDefinitionKey("afterBookHotel").singleResult();
    taskService.complete(afterBookHotelTask.getId());
    
    assertProcessEnded(processInstance.getId());
  }
  
  
  @Deployment
  public void testCompensateNestedSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    
    // Completing should trigger the compensations
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("afterNestedSubProcess").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    Task compensationTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("undoBookHotel").singleResult();
    assertNotNull(compensationTask);
    taskService.complete(compensationTask.getId());
    
    assertProcessEnded(processInstance.getId());
    
  }
  
}
