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

package org.activiti.engine.test.history;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 */
public class HistoricProcessInstanceTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void testHistoricDataCreatedForProcessExecution() {

    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2010);
    calendar.set(Calendar.MONTH, 8);
    calendar.set(Calendar.DAY_OF_MONTH, 30);
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date noon = calendar.getTime();

    processEngineConfiguration.getClock().setCurrentTime(noon);
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "myBusinessKey");

    assertThat(historyService.createHistoricProcessInstanceQuery().unfinished().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().finished().count()).isEqualTo(0);
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertThat(historicProcessInstance).isNotNull();
    assertThat(historicProcessInstance.getId()).isEqualTo(processInstance.getId());
    assertThat(historicProcessInstance.getBusinessKey()).isEqualTo(processInstance.getBusinessKey());
    assertThat(historicProcessInstance.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(historicProcessInstance.getStartTime()).isEqualTo(noon);
    assertThat(historicProcessInstance.getEndTime()).isNull();
    assertThat(historicProcessInstance.getDurationInMillis()).isNull();

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

    assertThat(tasks).hasSize(1);

    // in this test scenario we assume that 25 seconds after the process
    // start, the
    // user completes the task (yes! he must be almost as fast as me)
    Date twentyFiveSecsAfterNoon = new Date(noon.getTime() + 25 * 1000);
    processEngineConfiguration.getClock().setCurrentTime(twentyFiveSecsAfterNoon);
    taskService.complete(tasks.get(0).getId());

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertThat(historicProcessInstance).isNotNull();
    assertThat(historicProcessInstance.getId()).isEqualTo(processInstance.getId());
    assertThat(historicProcessInstance.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(historicProcessInstance.getStartTime()).isEqualTo(noon);
    assertThat(historicProcessInstance.getEndTime()).isEqualTo(twentyFiveSecsAfterNoon);
    assertThat(historicProcessInstance.getDurationInMillis()).isEqualTo(new Long(25 * 1000));

    assertThat(historyService.createHistoricProcessInstanceQuery().unfinished().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().finished().count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void testDeleteProcessInstanceHistoryCreated() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processInstance).isNotNull();

    // delete process instance should not delete the history
    runtimeService.deleteProcessInstance(processInstance.getId(), "cancel");
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(historicProcessInstance.getEndTime()).isNotNull();
  }

  /*
   * @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"}) public void testHistoricProcessInstanceVariables() { Map<String,Object> vars = new
   * HashMap<String,Object>(); vars.put("foo", "bar"); vars.put("baz", "boo");
   *
   * runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
   *
   * assertThat(historyService.createHistoricProcessInstanceQuery().processVariableEquals ("foo", "bar").count()).isEqualTo(1); assertThat(1, historyService.createHistoricProcessInstanceQuery
   * ().processVariableEquals("baz", "boo").count()); assertThat(1, historyService .createHistoricProcessInstanceQuery().processVariableEquals("foo", "bar").processVariableEquals("baz",
   * "boo").count()); }
   */

  @Deployment(resources = { "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQuery() {
    Calendar startTime = Calendar.getInstance();

    processEngineConfiguration.getClock().setCurrentTime(startTime.getTime());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "businessKey123");
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "someType");
    runtimeService.setProcessInstanceName(processInstance.getId(), "The name");
    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    // Name and name like
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceName("The name").singleResult().getName()).isEqualTo("The name");
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceName("The name").count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceName("Other name").count()).isEqualTo(0);

    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("% name").count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("%nope").count()).isEqualTo(0);

    // Query after update name
    runtimeService.setProcessInstanceName(processInstance.getId(), "New name");
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceName("New name").singleResult().getName()).isEqualTo("New name");
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceName("New name").count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceName("The name").count()).isEqualTo(0);

    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("New %").count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("The %").count()).isEqualTo(0);

    // Start/end dates
    assertThat(historyService.createHistoricProcessInstanceQuery().finishedBefore(hourAgo.getTime()).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().finishedBefore(hourFromNow.getTime()).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().finishedAfter(hourAgo.getTime()).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().finishedAfter(hourFromNow.getTime()).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().startedBefore(hourFromNow.getTime()).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().startedBefore(hourAgo.getTime()).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().startedAfter(hourAgo.getTime()).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().startedAfter(hourFromNow.getTime()).count()).isEqualTo(0);

    // General fields
    assertThat(historyService.createHistoricProcessInstanceQuery().finished().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKeyIn(singletonList("oneTaskProcess")).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKeyIn(asList("undefined", "oneTaskProcess")).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKeyIn(asList("undefined1", "undefined2")).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey("businessKey123").count()).isEqualTo(1);

    List<String> excludeIds = new ArrayList<String>();
    excludeIds.add("unexistingProcessDefinition");

    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(excludeIds).count()).isEqualTo(1);

    excludeIds.add("oneTaskProcess");
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(excludeIds).count()).isEqualTo(0);

    // After finishing process
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertThat(historyService.createHistoricProcessInstanceQuery().finished().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().finishedBefore(hourAgo.getTime()).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().finishedBefore(hourFromNow.getTime()).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().finishedAfter(hourAgo.getTime()).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().finishedAfter(hourFromNow.getTime()).count()).isEqualTo(0);

    // Check identity links
    assertThat(historyService.createHistoricProcessInstanceQuery().involvedUser("kermit").count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().involvedUser("gonzo").count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceOrQuery() {
    Calendar startTime = Calendar.getInstance();

    processEngineConfiguration.getClock().setCurrentTime(startTime.getTime());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "businessKey123");
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "someType");
    runtimeService.setProcessInstanceName(processInstance.getId(), "The name");
    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    // Name and name like
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceName("The name").processDefinitionId("undefined").endOr().singleResult().getName()).isEqualTo("The name");
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceName("The name").processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceName("Other name").processDefinitionId("undefined").endOr().count()).isEqualTo(0);

    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("% name").processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("%nope").processDefinitionId("undefined").endOr().count()).isEqualTo(0);

    assertThat(historyService.createHistoricProcessInstanceQuery()
        .or()
          .processInstanceName("The name")
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processInstanceNameLike("% name")
          .processDefinitionId("undefined")
        .endOr()
        .count()).isEqualTo(1);

    assertThat(historyService.createHistoricProcessInstanceQuery()
        .or()
          .processInstanceName("The name")
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processInstanceNameLike("undefined")
          .processDefinitionId("undefined")
        .endOr()
        .count()).isEqualTo(0);

    // Query after update name
    runtimeService.setProcessInstanceName(processInstance.getId(), "New name");
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceName("New name").processDefinitionId("undefined").endOr().singleResult().getName()).isEqualTo("New name");
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceName("New name").processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceName("The name").processDefinitionId("undefined").endOr().count()).isEqualTo(0);

    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("New %").processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("The %").processDefinitionId("undefined").endOr().count()).isEqualTo(0);

    // Start/end dates
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finishedBefore(hourAgo.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finishedBefore(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finishedAfter(hourAgo.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finishedAfter(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().startedBefore(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().startedBefore(hourAgo.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().startedAfter(hourAgo.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().startedAfter(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(0);

    // General fields
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finished().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceId(processInstance.getId()).processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionId(processInstance.getProcessDefinitionId()).processDefinitionKey("undefined").count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionId("undefined").processDefinitionKeyIn(asList("undefined", "oneTaskProcess")).endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionId("undefined").processDefinitionKeyIn(asList("undefined1", "undefined2")).endOr().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionKey("oneTaskProcess").processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceBusinessKey("businessKey123").processDefinitionId("undefined").endOr().count()).isEqualTo(1);

    List<String> excludeIds = new ArrayList<String>();
    excludeIds.add("unexistingProcessDefinition");

    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionKeyNotIn(excludeIds).processDefinitionId("undefined").endOr().count()).isEqualTo(1);

    excludeIds.add("oneTaskProcess");
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionKeyNotIn(excludeIds).processDefinitionId("undefined").endOr().count()).isEqualTo(0);

    // After finishing process
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finished().processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finishedBefore(hourAgo.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finishedBefore(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finishedAfter(hourAgo.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().finishedAfter(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count()).isEqualTo(0);

    // Check identity links
    assertThat(historyService.createHistoricProcessInstanceQuery().or().involvedUser("kermit").processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().involvedUser("gonzo").processDefinitionId("undefined").endOr().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceSorting() {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().list()).hasSize(1);

    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().list()).hasSize(1);

    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().count()).isEqualTo(1);

    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().count()).isEqualTo(1);

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // First complete process instance 2
    for (Task task : taskService.createTaskQuery().processInstanceId(processInstance2.getId()).list()) {
      taskService.complete(task.getId());
    }

    // Then process instance 1
    for (Task task : taskService.createTaskQuery().processInstanceId(processInstance1.getId()).list()) {
      taskService.complete(task.getId());
    }

    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().list()).hasSize(2);

    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().list()).hasSize(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().list()).hasSize(2);

    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().count()).isEqualTo(2);

    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().count()).isEqualTo(2);
    assertThat(historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().count()).isEqualTo(2);

    // Verify orderByProcessInstanceEndTime
    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().list();
    List<String> processInstanceIds = new ArrayList<String>(2);
    processInstanceIds.add(historicProcessInstances.get(0).getId());
    processInstanceIds.add(historicProcessInstances.get(1).getId());
    assertThat(processInstanceIds.contains(processInstance1.getId())).isTrue();
    assertThat(processInstanceIds.contains(processInstance2.getId())).isTrue();

    // Verify again, with variables included (bug reported on that)
    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().includeProcessVariables().list();
    processInstanceIds = new ArrayList<String>(2);
    processInstanceIds.add(historicProcessInstances.get(0).getId());
    processInstanceIds.add(historicProcessInstances.get(1).getId());
    assertThat(processInstanceIds.contains(processInstance1.getId())).isTrue();
    assertThat(processInstanceIds.contains(processInstance2.getId())).isTrue();
  }

  public void testInvalidSorting() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> historyService.createHistoricProcessInstanceQuery().asc());

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> historyService.createHistoricProcessInstanceQuery().desc());

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().list());
  }

  @Deployment(resources = { "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml" })
  // ACT-1098
  public void testDeleteReason() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      final String deleteReason = "some delete reason";
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.deleteProcessInstance(pi.getId(), deleteReason);
      HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(pi.getId()).singleResult();
      assertThat(hpi.getDeleteReason()).isEqualTo(deleteReason);
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void testHistoricIdentityLinksOnProcessInstance() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.addUserIdentityLink(pi.getId(), "kermit", "myType");

      // Check historic links
      List<HistoricIdentityLink> historicLinks = historyService.getHistoricIdentityLinksForProcessInstance(pi.getId());
      assertThat(historicLinks).hasSize(1);

      assertThat(historicLinks.get(0).getType()).isEqualTo("myType");
      assertThat(historicLinks.get(0).getUserId()).isEqualTo("kermit");
      assertThat(historicLinks.get(0).getGroupId()).isNull();
      assertThat(historicLinks.get(0).getProcessInstanceId()).isEqualTo(pi.getId());

      // When process is ended, link should remain
      taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
      assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();

      assertThat(historyService.getHistoricIdentityLinksForProcessInstance(pi.getId())).hasSize(1);

      // When process is deleted, identitylinks shouldn't exist anymore
      historyService.deleteHistoricProcessInstance(pi.getId());
      assertThat(historyService.getHistoricIdentityLinksForProcessInstance(pi.getId())).hasSize(0);
    }
  }

  /**
   * Validation for ACT-821
   */
  @Deployment(resources = { "org/activiti/engine/test/history/HistoricProcessInstanceTest.testDeleteHistoricProcessInstanceWithCallActivity.bpmn20.xml",
      "org/activiti/engine/test/history/HistoricProcessInstanceTest.testDeleteHistoricProcessInstanceWithCallActivity-subprocess.bpmn20.xml" })
  public void testDeleteHistoricProcessInstanceWithCallActivity() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

      runtimeService.deleteProcessInstance(pi.getId(), "testing");

      // The parent and child process should be present in history
      assertThat(historyService.createHistoricProcessInstanceQuery().count()).isEqualTo(2L);

      // Deleting the parent process should cascade the child-process
      historyService.deleteHistoricProcessInstance(pi.getId());
      assertThat(historyService.createHistoricProcessInstanceQuery().count()).isEqualTo(0L);
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceName() {
    String piName = "Customized Process Instance Name";
    ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder();
    builder.processDefinitionKey("oneTaskProcess");
    builder.name(piName);
    ProcessInstance processInstance1 = builder.start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance1.getProcessInstanceId()).singleResult();
    assertThat(historicProcessInstance.getName()).isEqualTo(piName);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceName(piName).list()).hasSize(1);
  }

  /**
   * Validation for https://jira.codehaus.org/browse/ACT-2182
   */
  public void testNameAndTenantIdSetWhenFetchingVariables() {

    String tenantId = "testTenantId";
    String processInstanceName = "myProcessInstance";

    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml").tenantId(tenantId).deploy().getId();

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("name", "Kermit");
    vars.put("age", 60);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, tenantId);
    runtimeService.setProcessInstanceName(processInstance.getId(), processInstanceName);

    // Verify name and tenant id (didn't work on mssql and db2) on process
    // instance
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().includeProcessVariables().list();
    assertThat(processInstances).hasSize(1);
    processInstance = processInstances.get(0);

    assertThat(processInstance.getName()).isEqualTo(processInstanceName);
    assertThat(processInstance.getTenantId()).isEqualTo(tenantId);

    Map<String, Object> processInstanceVars = processInstance.getProcessVariables();
    assertThat(processInstanceVars).hasSize(2);
    assertThat(processInstanceVars.get("name")).isEqualTo("Kermit");
    assertThat(processInstanceVars.get("age")).isEqualTo(60);

    // Verify name and tenant id (didn't work on mssql and db2) on historic
    // process instance
    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().list();
    assertThat(historicProcessInstances).hasSize(1);
    HistoricProcessInstance historicProcessInstance = historicProcessInstances.get(0);

    // Verify name and tenant id (didn't work on mssql and db2) on process
    // instance
    assertThat(historicProcessInstance.getName()).isEqualTo(processInstanceName);
    assertThat(historicProcessInstance.getTenantId()).isEqualTo(tenantId);

    Map<String, Object> historicProcessInstanceVars = historicProcessInstance.getProcessVariables();
    assertThat(historicProcessInstanceVars).hasSize(2);
    assertThat(historicProcessInstanceVars.get("name")).isEqualTo("Kermit");
    assertThat(historicProcessInstanceVars.get("age")).isEqualTo(60);

    // cleanup
    repositoryService.deleteDeployment(deploymentId, true);
  }

}
