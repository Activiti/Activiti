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


package org.activiti.engine.test.history;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;


public class HistoricVariableInstanceTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml", "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml" })
  public void testOrderProcessWithCallActivity() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      // After the process has started, the 'verify credit history' task should be active
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
      TaskQuery taskQuery = taskService.createTaskQuery();
      Task verifyCreditTask = taskQuery.singleResult();
      assertThat(verifyCreditTask.getName()).isEqualTo("Verify credit history");

      // Verify with Query API
      ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
      assertThat(subProcessInstance).isNotNull();
      assertThat(runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId()).isEqualTo(pi.getId());

      // Completing the task with approval, will end the subprocess and continue the original process
      taskService.complete(verifyCreditTask.getId(), singletonMap("creditApproved", true));
      Task prepareAndShipTask = taskQuery.singleResult();
      assertThat(prepareAndShipTask.getName()).isEqualTo("Prepare and Ship");
    }
  }

  @Deployment
  public void testSimple() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
      TaskQuery taskQuery = taskService.createTaskQuery();
      Task userTask = taskQuery.singleResult();
      assertThat(userTask.getName()).isEqualTo("userTask1");

      taskService.complete(userTask.getId(), singletonMap("myVar", "test789"));

      assertProcessEnded(processInstance.getId());

      List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
      assertThat(variables).hasSize(1);

      HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
      assertThat(historicVariable.getTextValue()).isEqualTo("test456");

      assertThat(historyService.createHistoricActivityInstanceQuery().count()).isEqualTo(5);
      assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(3);
    }
  }

  @Deployment
  public void testSimpleNoWaitState() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
      assertProcessEnded(processInstance.getId());

      List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
      assertThat(variables).hasSize(1);

      HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
      assertThat(historicVariable.getTextValue()).isEqualTo("test456");

      assertThat(historyService.createHistoricActivityInstanceQuery().count()).isEqualTo(4);
      assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2);
    }
  }

  @Deployment
  public void testParallel() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
      TaskQuery taskQuery = taskService.createTaskQuery();
      Task userTask = taskQuery.singleResult();
      assertThat(userTask.getName()).isEqualTo("userTask1");

      taskService.complete(userTask.getId(), singletonMap("myVar", "test789"));

      assertProcessEnded(processInstance.getId());

      List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list();
      assertThat(variables).hasSize(2);

      HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
      assertThat(historicVariable.getName()).isEqualTo("myVar");
      assertThat(historicVariable.getTextValue()).isEqualTo("test789");

      HistoricVariableInstanceEntity historicVariable1 = (HistoricVariableInstanceEntity) variables.get(1);
      assertThat(historicVariable1.getName()).isEqualTo("myVar1");
      assertThat(historicVariable1.getTextValue()).isEqualTo("test456");

      assertThat(historyService.createHistoricActivityInstanceQuery().count()).isEqualTo(8);
      assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(5);
    }
  }

  @Deployment
  public void testParallelNoWaitState() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
      assertProcessEnded(processInstance.getId());

      List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
      assertThat(variables).hasSize(1);

      HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
      assertThat(historicVariable.getTextValue()).isEqualTo("test456");

      assertThat(historyService.createHistoricActivityInstanceQuery().count()).isEqualTo(7);
      assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2);
    }
  }

  @Deployment
  public void testTwoSubProcessInParallelWithinSubProcess() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoSubProcessInParallelWithinSubProcess");
      assertProcessEnded(processInstance.getId());

      List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list();
      assertThat(variables).hasSize(2);

      HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
      assertThat(historicVariable.getName()).isEqualTo("myVar");
      assertThat(historicVariable.getTextValue()).isEqualTo("test101112");

      HistoricVariableInstanceEntity historicVariable1 = (HistoricVariableInstanceEntity) variables.get(1);
      assertThat(historicVariable1.getName()).isEqualTo("myVar1");
      assertThat(historicVariable1.getTextValue()).isEqualTo("test789");

      assertThat(historyService.createHistoricActivityInstanceQuery().count()).isEqualTo(18);
      assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(7);
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/history/HistoricVariableInstanceTest.testCallSimpleSubProcess.bpmn20.xml", "org/activiti/engine/test/history/simpleSubProcess.bpmn20.xml" })
  public void testHistoricVariableInstanceQuery() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");
      assertProcessEnded(processInstance.getId());

      assertThat(historyService.createHistoricVariableInstanceQuery().count()).isEqualTo(4);
      assertThat(historyService.createHistoricVariableInstanceQuery().list()).hasSize(4);
      assertThat(historyService.createHistoricVariableInstanceQuery().orderByProcessInstanceId().asc().count()).isEqualTo(4);
      assertThat(historyService.createHistoricVariableInstanceQuery().orderByProcessInstanceId().asc().list()).hasSize(4);
      assertThat(historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().count()).isEqualTo(4);
      assertThat(historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list()).hasSize(4);

      assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(2);
      assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(2);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableName("myVar").count()).isEqualTo(2);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableName("myVar").list()).hasSize(2);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableNameLike("myVar1").count()).isEqualTo(2);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableNameLike("myVar1").list()).hasSize(2);

      List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
      assertThat(variables).hasSize(4);

      assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test123").count()).isEqualTo(1);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test123").list()).hasSize(1);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test456").count()).isEqualTo(1);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test456").list()).hasSize(1);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test666").count()).isEqualTo(1);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test666").list()).hasSize(1);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test666").count()).isEqualTo(1);
      assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test666").list()).hasSize(1);

      assertThat(historyService.createHistoricActivityInstanceQuery().count()).isEqualTo(8);
      assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(5);
    }
  }

  public void testHistoricVariableQuery2() {
    deployTwoTasksTestProcess();

    // Generate data
    Map<String, Object> startVars = new HashMap<String, Object>();
    startVars.put("startVar", "hello");
    String processInstanceId = runtimeService.startProcessInstanceByKey("twoTasksProcess", startVars).getId();
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    for (int i=0; i<tasks.size(); i++) {
      runtimeService.setVariableLocal(tasks.get(i).getExecutionId(), "executionVar" + i, i);
      taskService.setVariableLocal(tasks.get(i).getId(), "taskVar" + i, i);
    }

    // Verify historic variable instance queries
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstanceId).orderByVariableName().asc().list();
    assertThat(historicVariableInstances).hasSize(5);

   List<String> expectedVariableNames =  asList("executionVar0", "executionVar1", "startVar", "taskVar0", "taskVar1");
   for (int i=0; i<expectedVariableNames.size(); i++) {
     assertThat(historicVariableInstances.get(i).getVariableName()).isEqualTo(expectedVariableNames.get(i));
   }

   // by execution id
   historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
       .executionId(tasks.get(0).getExecutionId()).orderByVariableName().asc().list();
   assertThat(historicVariableInstances).hasSize(2);
   assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("executionVar0");
   assertThat(historicVariableInstances.get(1).getVariableName()).isEqualTo("taskVar0");
   historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
       .executionId(tasks.get(1).getExecutionId()).orderByVariableName().asc().list();
   assertThat(historicVariableInstances).hasSize(2);
   assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("executionVar1");
   assertThat(historicVariableInstances.get(1).getVariableName()).isEqualTo("taskVar1");

   // By process instance id and execution id
   historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
       .processInstanceId(processInstanceId).executionId(tasks.get(0).getExecutionId()).orderByVariableName().asc().list();
   assertThat(historicVariableInstances).hasSize(2);
   assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("executionVar0");
   assertThat(historicVariableInstances.get(1).getVariableName()).isEqualTo("taskVar0");
   historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
      .processInstanceId(processInstanceId).executionId(tasks.get(1).getExecutionId()).orderByVariableName().asc().list();
   assertThat(historicVariableInstances).hasSize(2);
   assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("executionVar1");
   assertThat(historicVariableInstances.get(1).getVariableName()).isEqualTo("taskVar1");

   // By task id
   historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
       .taskId(tasks.get(0).getId()).list();
   assertThat(historicVariableInstances).hasSize(1);
   assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("taskVar0");
   historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
       .taskId(tasks.get(1).getId()).list();
   assertThat(historicVariableInstances).hasSize(1);
   assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("taskVar1");

   // By task id and process instance id
   historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
       .processInstanceId(processInstanceId).taskId(tasks.get(0).getId()).list();
   assertThat(historicVariableInstances).hasSize(1);
   assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("taskVar0");
   historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
      .processInstanceId(processInstanceId).taskId(tasks.get(1).getId()).list();
   assertThat(historicVariableInstances).hasSize(1);
   assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("taskVar1");

  }

  public void testHistoricVariableQueryByExecutionIds() {
    deployTwoTasksTestProcess();

    Set<String> processInstanceIds = new HashSet<String>();
    Set<String> testProcessInstanceIds = new HashSet<String>();
    for (int i = 0; i < 3; i++){
      // Generate data
      Map<String, Object> startVars = new HashMap<String, Object>();
      if (i == 1) {
        startVars.put("startVar2", "hello2");
      } else {
        startVars.put("startVar", "hello");
      }
      String processInstanceId = runtimeService.startProcessInstanceByKey("twoTasksProcess", startVars).getId();
      processInstanceIds.add(processInstanceId);
      if (i != 1) {
        testProcessInstanceIds.add(processInstanceId);
      }
    }

    assertThat(historyService.createHistoricVariableInstanceQuery().executionIds(testProcessInstanceIds).list().size())
            .isEqualTo((int) historyService.createHistoricVariableInstanceQuery().executionIds(testProcessInstanceIds).count())
            .isEqualTo(2);

    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().executionIds(testProcessInstanceIds).list();
    assertThat(historicVariableInstances).hasSize(2)
            .extracting("name", "value")
            .containsExactly(
                    tuple("startVar", "hello"),
                    tuple("startVar", "hello"));

    historicVariableInstances = historyService.createHistoricVariableInstanceQuery().executionIds(processInstanceIds).list();

    assertThat(historicVariableInstances).hasSize(3)
            .extracting("name", "value")
            .containsExactlyInAnyOrder(
                    tuple("startVar", "hello"),
                    tuple("startVar", "hello"),
                    tuple("startVar2", "hello2"));
  }

  @Deployment(resources={
    "org/activiti/engine/test/api/runtime/variableScope.bpmn20.xml"
  })
  public void testHistoricVariableQueryByExecutionIdsForScope(){
    Map<String, Object> processVars = new HashMap<String, Object>();
    processVars.put("processVar", "processVar");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("variableScopeProcess", processVars);

    Set<String> executionIds = new HashSet<String>();
    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions){
      if (!processInstance.getId().equals(execution.getId())){
        executionIds.add(execution.getId());
        runtimeService.setVariableLocal(execution.getId(), "executionVar", "executionVar");
      }
    }

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks){
      taskService.setVariableLocal(task.getId(), "taskVar", "taskVar");
    }

    Set<String> processInstanceIds = new HashSet<String>();
    processInstanceIds.add(processInstance.getId());
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().executionIds(processInstanceIds).list();
    assertThat(1).isEqualTo(historicVariableInstances.size());
    assertThat("processVar").isEqualTo(historicVariableInstances.get(0).getVariableName());
    assertThat("processVar").isEqualTo(historicVariableInstances.get(0).getValue() );

    historicVariableInstances = historyService.createHistoricVariableInstanceQuery().executionIds(executionIds).excludeTaskVariables().list();
    assertThat(2).isEqualTo(historicVariableInstances.size());
    assertThat("executionVar").isEqualTo(historicVariableInstances.get(0).getVariableName());
    assertThat("executionVar").isEqualTo(historicVariableInstances.get(0).getValue() );
    assertThat("executionVar").isEqualTo(historicVariableInstances.get(1).getVariableName());
    assertThat("executionVar").isEqualTo(historicVariableInstances.get(1).getValue() );
  }

  public void testHistoricVariableQueryByTaskIds() {
    deployTwoTasksTestProcess();
    // Generate data
    String processInstanceId = runtimeService.startProcessInstanceByKey("twoTasksProcess").getId();
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    taskService.setVariableLocal(tasks.get(0).getId(), "taskVar1", "hello1");
    taskService.setVariableLocal(tasks.get(1).getId(), "taskVar2", "hello2");

    Set<String> taskIds = new HashSet<String>();
    taskIds.add(tasks.get(0).getId());
    taskIds.add(tasks.get(1).getId());
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().taskIds(taskIds).list();
    assertThat(historyService.createHistoricVariableInstanceQuery().taskIds(taskIds).count()).isEqualTo(2);
    assertThat(historicVariableInstances).hasSize(2);
    assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("taskVar1");
    assertThat(historicVariableInstances.get(0).getValue()).isEqualTo("hello1");
    assertThat(historicVariableInstances.get(1).getVariableName()).isEqualTo("taskVar2");
    assertThat(historicVariableInstances.get(1).getValue()).isEqualTo("hello2");

    taskIds = new HashSet<String>();
    taskIds.add(tasks.get(0).getId());
    historicVariableInstances = historyService.createHistoricVariableInstanceQuery().taskIds(taskIds).list();
    assertThat(historyService.createHistoricVariableInstanceQuery().taskIds(taskIds).count()).isEqualTo(1);
    assertThat(historicVariableInstances).hasSize(1);
    assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo("taskVar1");
    assertThat(historicVariableInstances.get(0).getValue()).isEqualTo("hello1");
  }

  @Deployment(resources={
    "org/activiti/engine/test/api/runtime/variableScope.bpmn20.xml"
  })
  public void testHistoricVariableQueryByTaskIdsForScope() {
    Map<String, Object> processVars = new HashMap<String, Object>();
    processVars.put("processVar", "processVar");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("variableScopeProcess", processVars);

    Set<String> executionIds = new HashSet<String>();
    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions){
      if (!processInstance.getId().equals(execution.getId())){
        executionIds.add(execution.getId());
        runtimeService.setVariableLocal(execution.getId(), "executionVar", "executionVar");
      }
    }

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    Set<String> taskIds = new HashSet<String>();
    for (Task task : tasks){
      taskService.setVariableLocal(task.getId(), "taskVar", "taskVar");
      taskIds.add(task.getId());
    }

    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().taskIds(taskIds).list();
    assertThat(2).isEqualTo(historicVariableInstances.size());
    assertThat("taskVar").isEqualTo(historicVariableInstances.get(0).getVariableName());
    assertThat("taskVar").isEqualTo(historicVariableInstances.get(0).getValue() );
    assertThat("taskVar").isEqualTo(historicVariableInstances.get(1).getVariableName());
    assertThat("taskVar").isEqualTo(historicVariableInstances.get(1).getValue() );
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessVariableOnDeletion() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      HashMap<String, Object> variables = new HashMap<String, Object>();
      variables.put("testVar", "Hallo Christian");
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
      runtimeService.deleteProcessInstance(processInstance.getId(), "deleted");
      assertProcessEnded(processInstance.getId());

      // check that process variable is set even if the process is canceled and not ended normally
      assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableValueEquals("testVar", "Hallo Christian").count()).isEqualTo(1);
    }
  }

  @Deployment(resources = { "org/activiti/standalone/history/FullHistoryTest.testVariableUpdatesAreLinkedToActivity.bpmn20.xml" })
  public void testVariableUpdatesLinkedToActivity() throws Exception {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("ProcessWithSubProcess");

      Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("test", "1");
      taskService.complete(task.getId(), variables);

      // now we are in the subprocess
      task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
      variables.clear();
      variables.put("test", "2");
      taskService.complete(task.getId(), variables);

      // now we are ended
      assertProcessEnded(pi.getId());

      // check history
      List<HistoricDetail> updates = historyService.createHistoricDetailQuery().variableUpdates().list();
      assertThat(updates).hasSize(2);

      Map<String, HistoricVariableUpdate> updatesMap = new HashMap<String, HistoricVariableUpdate>();
      HistoricVariableUpdate update = (HistoricVariableUpdate) updates.get(0);
      updatesMap.put((String) update.getValue(), update);
      update = (HistoricVariableUpdate) updates.get(1);
      updatesMap.put((String) update.getValue(), update);

      HistoricVariableUpdate update1 = updatesMap.get("1");
      HistoricVariableUpdate update2 = updatesMap.get("2");

      assertThat(update1.getActivityInstanceId()).isNotNull();
      assertThat(update1.getExecutionId()).isNotNull();
      HistoricActivityInstance historicActivityInstance1 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update1.getActivityInstanceId()).singleResult();
      assertThat(historicActivityInstance1.getActivityId()).isEqualTo("usertask1");

      // TODO https://activiti.atlassian.net/browse/ACT-1083
      assertThat(update2.getActivityInstanceId()).isNotNull();
      HistoricActivityInstance historicActivityInstance2 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update2.getActivityInstanceId()).singleResult();
      assertThat(historicActivityInstance2.getActivityId()).isEqualTo("usertask2");

      /*
       * This is OK! The variable is set on the root execution, on a execution never run through the activity, where the process instances stands when calling the set Variable. But the ActivityId of
       * this flow node is used. So the execution id's doesn't have to be equal.
       *
       * execution id: On which execution it was set activity id: in which activity was the process instance when setting the variable
       */
      assertThat(historicActivityInstance2.getExecutionId().equals(update2.getExecutionId())).isFalse();
    }
  }

  // Test for ACT-1528, which (correctly) reported that deleting any
  // historic process instance would remove ALL historic variables.
  // Yes. Real serious bug.
  @Deployment
  public void testHistoricProcessInstanceDeleteCascadesCorrectly() {

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {

      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("var1", "value1");
      variables.put("var2", "value2");
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess", variables);
      assertThat(processInstance).isNotNull();

      variables = new HashMap<String, Object>();
      variables.put("var3", "value3");
      variables.put("var4", "value4");
      ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("myProcess", variables);
      assertThat(processInstance2).isNotNull();

      // check variables
      long count = historyService.createHistoricVariableInstanceQuery().count();
      assertThat(count).isEqualTo(4);

      // delete runtime execution of ONE process instance
      runtimeService.deleteProcessInstance(processInstance.getId(), "reason 1");
      historyService.deleteHistoricProcessInstance(processInstance.getId());

      // recheck variables
      // this is a bug: all variables was deleted after delete a history processinstance
      count = historyService.createHistoricVariableInstanceQuery().count();
      assertThat(count).isEqualTo(2);
    }

  }

  @Deployment(resources = "org/activiti/engine/test/history/HistoricVariableInstanceTest.testSimple.bpmn20.xml")
  public void testNativeHistoricVariableInstanceQuery() {

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {

      assertThat(managementService.getTableName(HistoricVariableInstance.class)).isEqualTo("ACT_HI_VARINST");
      assertThat(managementService.getTableName(HistoricVariableInstanceEntity.class)).isEqualTo("ACT_HI_VARINST");

      String tableName = managementService.getTableName(HistoricVariableInstance.class);
      String baseQuerySql = "SELECT * FROM " + tableName;

      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("var1", "value1");
      variables.put("var2", "value2");
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc", variables);
      assertThat(processInstance).isNotNull();

      assertThat(historyService.createNativeHistoricVariableInstanceQuery().sql(baseQuerySql).list()).hasSize(3);

      String sqlWithConditions = baseQuerySql + " where NAME_ = #{name}";
      assertThat(historyService.createNativeHistoricVariableInstanceQuery().sql(sqlWithConditions).parameter("name", "myVar").singleResult().getValue()).isEqualTo("test123");

      sqlWithConditions = baseQuerySql + " where NAME_ like #{name}";
      assertThat(historyService.createNativeHistoricVariableInstanceQuery().sql(sqlWithConditions).parameter("name", "var%").list()).hasSize(2);

      // paging
      assertThat(historyService.createNativeHistoricVariableInstanceQuery().sql(baseQuerySql).listPage(0, 3)).hasSize(3);
      assertThat(historyService.createNativeHistoricVariableInstanceQuery().sql(baseQuerySql).listPage(1, 3)).hasSize(2);
      assertThat(historyService.createNativeHistoricVariableInstanceQuery().sql(sqlWithConditions).parameter("name", "var%").listPage(0, 2)).hasSize(2);
    }

  }

  @Deployment(resources = "org/activiti/engine/test/history/HistoricVariableInstanceTest.testSimple.bpmn20.xml")
  public void testNativeHistoricDetailQuery() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      assertThat(managementService.getTableName(HistoricDetail.class)).isEqualTo("ACT_HI_DETAIL");
      assertThat(managementService.getTableName(HistoricVariableUpdate.class)).isEqualTo("ACT_HI_DETAIL");

      String tableName = managementService.getTableName(HistoricDetail.class);
      String baseQuerySql = "SELECT * FROM " + tableName;

      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("var1", "value1");
      variables.put("var2", "value2");
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc", variables);
      assertThat(processInstance).isNotNull();

      assertThat(historyService.createNativeHistoricDetailQuery().sql(baseQuerySql).list()).hasSize(3);

      String sqlWithConditions = baseQuerySql + " where NAME_ = #{name} and TYPE_ = #{type}";
      assertThat(historyService.createNativeHistoricDetailQuery().sql(sqlWithConditions).parameter("name", "myVar").parameter("type", "VariableUpdate").singleResult()).isNotNull();

      sqlWithConditions = baseQuerySql + " where NAME_ like #{name}";
      assertThat(historyService.createNativeHistoricDetailQuery().sql(sqlWithConditions).parameter("name", "var%").list()).hasSize(2);

      // paging
      assertThat(historyService.createNativeHistoricDetailQuery().sql(baseQuerySql).listPage(0, 3)).hasSize(3);
      assertThat(historyService.createNativeHistoricDetailQuery().sql(baseQuerySql).listPage(1, 3)).hasSize(2);
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void testChangeType() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      TaskQuery taskQuery = taskService.createTaskQuery();
      Task task = taskQuery.singleResult();
      assertThat(task.getName()).isEqualTo("my task");

      // no type change
      runtimeService.setVariable(processInstance.getId(), "firstVar", "123");
      assertThat(getHistoricVariable("firstVar").getValue()).isEqualTo("123");
      runtimeService.setVariable(processInstance.getId(), "firstVar", "456");
      assertThat(getHistoricVariable("firstVar").getValue()).isEqualTo("456");
      runtimeService.setVariable(processInstance.getId(), "firstVar", "789");
      assertThat(getHistoricVariable("firstVar").getValue()).isEqualTo("789");

      // type is changed from text to integer and back again. same result expected(?)
      runtimeService.setVariable(processInstance.getId(), "secondVar", "123");
      assertThat(getHistoricVariable("secondVar").getValue()).isEqualTo("123");
      runtimeService.setVariable(processInstance.getId(), "secondVar", 456);
      // there are now 2 historic variables, so the following does not work
      assertThat(getHistoricVariable("secondVar").getValue()).isEqualTo(456);
      runtimeService.setVariable(processInstance.getId(), "secondVar", "789");
      // there are now 3 historic variables, so the following does not work
      assertThat(getHistoricVariable("secondVar").getValue()).isEqualTo("789");

      taskService.complete(task.getId());

      assertProcessEnded(processInstance.getId());
    }
  }

  private HistoricVariableInstance getHistoricVariable(String variableName) {
    return historyService.createHistoricVariableInstanceQuery().variableName(variableName).singleResult();
  }

  @Deployment
  public void testRestrictByExecutionId() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
      TaskQuery taskQuery = taskService.createTaskQuery();
      Task userTask = taskQuery.singleResult();
      assertThat(userTask.getName()).isEqualTo("userTask1");

      taskService.complete(userTask.getId(), singletonMap("myVar", "test789"));

      assertProcessEnded(processInstance.getId());

      List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().executionId(processInstance.getId()).list();
      assertThat(variables).hasSize(1);

      HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
      assertThat(historicVariable.getTextValue()).isEqualTo("test456");

      assertThat(historyService.createHistoricActivityInstanceQuery().count()).isEqualTo(5);
      assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(3);
    }
  }
}
