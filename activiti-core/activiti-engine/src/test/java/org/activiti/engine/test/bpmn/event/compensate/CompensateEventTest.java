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


package org.activiti.engine.test.bpmn.event.compensate;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.EnableVerboseExecutionTreeLogging;
import org.activiti.engine.test.bpmn.event.compensate.helper.SetVariablesDelegate;

/**
 */
@EnableVerboseExecutionTreeLogging
public class CompensateEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testCompensateSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookHotel")).isEqualTo(5);

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testCompensateSubprocessWithoutActivityRef() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookHotel")).isEqualTo(5);

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testCompensateSubprocessWithUserTask() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task.getName()).isEqualTo("Manually undo book hotel");
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
    assertThat(task.getName()).isEqualTo("Manually undo book hotel");
    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testCompensateMiSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookHotel")).isEqualTo(5);

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateScope() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookHotel")).isEqualTo(5);
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookFlight")).isEqualTo(5);

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testCallActivityCompensationHandler.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/compensate/CompensationHandler.bpmn20.xml" })
  public void testCallActivityCompensationHandler() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count()).isEqualTo(5);
    }

    Execution execution = runtimeService.createExecutionQuery().activityId("beforeEnd").singleResult();
    runtimeService.trigger(execution.getId());
    assertProcessEnded(processInstance.getId());

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricProcessInstanceQuery().count()).isEqualTo(6);
    }

  }

  @Deployment
  public void testCompensateMiSubprocessVariableSnapshots() {

    // see referenced java delegates in the process definition.

    SetVariablesDelegate.variablesMap.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count()).isEqualTo(5);
    }

    assertProcessEnded(processInstance.getId());

  }

  public void testMultipleCompensationCatchEventsFails() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testMultipleCompensationCatchEventsFails.bpmn20.xml")
        .deploy());
  }

  public void testInvalidActivityRefFails() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testInvalidActivityRefFails.bpmn20.xml")
        .deploy())
      .withMessageContaining("Invalid attribute value for 'activityRef':");
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationStepEndRecorded.bpmn20.xml" })
  public void testCompensationStepEndTimeRecorded() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationStepEndRecordedProcess");
    assertProcessEnded(processInstance.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      final HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().activityId("compensationScriptTask");
      assertThat(query.count()).isEqualTo(1);
      final HistoricActivityInstance compensationScriptTask = query.singleResult();
      assertThat(compensationScriptTask).isNotNull();
      assertThat(compensationScriptTask.getEndTime()).isNotNull();
      assertThat(compensationScriptTask.getDurationInMillis()).isNotNull();
    }
  }

  @Deployment
  public void testCompensateWithSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId()).activityId("bookHotel").singleResult();
      assertThat(historicActivityInstance.getEndTime()).isNotNull();
    }

    // Triggering the task will trigger the compensation subprocess
    Task afterBookHotelTask = taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .taskDefinitionKey("afterBookHotel").singleResult();
    taskService.complete(afterBookHotelTask.getId());

    Task compensationTask1 = taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .taskDefinitionKey("compensateTask1").singleResult();
    assertThat(compensationTask1).isNotNull();

    Task compensationTask2 = taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .taskDefinitionKey("compensateTask2").singleResult();
    assertThat(compensationTask2).isNotNull();

    taskService.complete(compensationTask1.getId());
    taskService.complete(compensationTask2.getId());

    Task compensationTask3 = taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .taskDefinitionKey("compensateTask3").singleResult();
    assertThat(compensationTask3).isNotNull();
    taskService.complete(compensationTask3.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensateWithSubprocess.bpmn20.xml" })
  public void testCompensateWithSubprocess2() {

    // Same as testCompensateWithSubprocess, but without throwing the compensation event
    // As such, to verify that the extra compensation executions have no effect on the regular process execution

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess",
        singletonMap("doCompensation", false));

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
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    Task compensationTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("undoBookHotel").singleResult();
    assertThat(compensationTask).isNotNull();
    taskService.complete(compensationTask.getId());

    assertProcessEnded(processInstance.getId());

  }

}
