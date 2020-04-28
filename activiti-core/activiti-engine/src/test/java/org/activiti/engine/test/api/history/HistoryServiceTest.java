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

package org.activiti.engine.test.api.history;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.api.runtime.ProcessInstanceQueryTest;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 */
public class HistoryServiceTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQuery() {
    // With a clean ProcessEngine, no instances should be available
    assertThat(historyService.createHistoricProcessInstanceQuery().count() == 0).isTrue();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(historyService.createHistoricProcessInstanceQuery().count() == 1).isTrue();

    // Complete the task and check if the size is count 1
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(tasks).hasSize(1);
    taskService.complete(tasks.get(0).getId());
    assertThat(historyService.createHistoricProcessInstanceQuery().count() == 1).isTrue();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryOrderBy() {
    // With a clean ProcessEngine, no instances should be available
    assertThat(historyService.createHistoricProcessInstanceQuery().count() == 0).isTrue();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(tasks).hasSize(1);
    taskService.complete(tasks.get(0).getId());

    historyService.createHistoricTaskInstanceQuery().orderByDeleteReason().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByExecutionId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskCreateTime().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceDuration().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceEndTime().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskName().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskOwner().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().asc().list();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceUserIdAndActivityId() {
    Authentication.setAuthenticatedUserId("johndoe");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertThat(historicProcessInstance.getStartUserId()).isEqualTo("johndoe");
    assertThat(historicProcessInstance.getStartActivityId()).isEqualTo("theStart");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(tasks).hasSize(1);
    taskService.complete(tasks.get(0).getId());

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertThat(historicProcessInstance.getEndActivityId()).isEqualTo("theEnd");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml", "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml" })
  public void testOrderProcessWithCallActivity() {
    // After the process has started, the 'verify credit history' task
    // should be
    // active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();

    // Completing the task with approval, will end the subprocess and
    // continue
    // the original process
    taskService.complete(verifyCreditTask.getId(), singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertThat(prepareAndShipTask.getName()).isEqualTo("Prepare and Ship");

    // verify
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertThat(historicProcessInstance).isNotNull();
    assertThat(historicProcessInstance.getProcessDefinitionId().contains("checkCreditProcess")).isTrue();
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml", "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml" })
  public void testExcludeSubprocesses() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().excludeSubprocesses(true).singleResult();
    assertThat(historicProcessInstance).isNotNull();
    assertThat(historicProcessInstance.getId()).isEqualTo(pi.getId());

    List<HistoricProcessInstance> instanceList = historyService.createHistoricProcessInstanceQuery().excludeSubprocesses(false).list();
    assertThat(instanceList).hasSize(2);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml",
      "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryByProcessDefinitionKey() {

    String processDefinitionKey = "oneTaskProcess";
    runtimeService.startProcessInstanceByKey(processDefinitionKey);
    runtimeService.startProcessInstanceByKey("orderProcess");
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(processDefinitionKey).singleResult();
    assertThat(historicProcessInstance).isNotNull();
    assertThat(historicProcessInstance.getProcessDefinitionKey().equals(processDefinitionKey)).isTrue();
    assertThat(historicProcessInstance.getStartActivityId()).isEqualTo("theStart");

    // now complete the task to end the process instance
    Task task = taskService.createTaskQuery().processDefinitionKey("checkCreditProcess").singleResult();
    taskService.complete(task.getId(), singletonMap("creditApproved", true));

    // and make sure the super process instance is set correctly on the
    // HistoricProcessInstance
    HistoricProcessInstance historicProcessInstanceSub = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("checkCreditProcess").singleResult();
    HistoricProcessInstance historicProcessInstanceSuper = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("orderProcess").singleResult();
    assertThat(historicProcessInstanceSub.getSuperProcessInstanceId()).isEqualTo(historicProcessInstanceSuper.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryByProcessInstanceIds() {
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess", i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess2", "1").getId());

    // start an instance that will not be part of the query
    runtimeService.startProcessInstanceByKey("oneTaskProcess2", "2");

    HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceIds(processInstanceIds);
    assertThat(processInstanceQuery.count()).isEqualTo(5);

    List<HistoricProcessInstance> processInstances = processInstanceQuery.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(5);

    for (HistoricProcessInstance historicProcessInstance : processInstances) {
      assertThat(processInstanceIds.contains(historicProcessInstance.getId())).isTrue();
    }
  }

  public void testHistoricProcessInstanceQueryByProcessInstanceIdsEmpty() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .as("ActivitiException expected")
      .isThrownBy(() -> historyService.createHistoricProcessInstanceQuery().processInstanceIds(new HashSet<>()))
      .withMessageContaining("Set of process instance ids is empty");
  }

  public void testHistoricProcessInstanceQueryByProcessInstanceIdsNull() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .as("ActivitiException expected")
      .isThrownBy(() -> historyService.createHistoricProcessInstanceQuery().processInstanceIds(null))
      .withMessageContaining("Set of process instance ids is null");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryForDelete() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    runtimeService.deleteProcessInstance(processInstanceId, null);

    HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId);
    assertThat(processInstanceQuery.count()).isEqualTo(1);
    HistoricProcessInstance processInstance = processInstanceQuery.singleResult();
    assertThat(processInstance.getId()).isEqualTo(processInstanceId);
    assertThat(processInstance.getDeleteReason()).isEqualTo(DeleteReason.PROCESS_INSTANCE_DELETED);

    processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).deleted();
    assertThat(processInstanceQuery.count()).isEqualTo(1);
    processInstance = processInstanceQuery.singleResult();
    assertThat(processInstance.getId()).isEqualTo(processInstanceId);
    assertThat(processInstance.getDeleteReason()).isEqualTo(DeleteReason.PROCESS_INSTANCE_DELETED);

    processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).notDeleted();
    assertThat(processInstanceQuery.count()).isEqualTo(0);

    historyService.deleteHistoricProcessInstance(processInstanceId);
    processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId);
    assertThat(processInstanceQuery.count()).isEqualTo(0);

    processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    runtimeService.deleteProcessInstance(processInstanceId, "custom message");

    processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId);
    assertThat(processInstanceQuery.count()).isEqualTo(1);
    processInstance = processInstanceQuery.singleResult();
    assertThat(processInstance.getId()).isEqualTo(processInstanceId);
    assertThat(processInstance.getDeleteReason()).isEqualTo("custom message");

    processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).deleted();
    assertThat(processInstanceQuery.count()).isEqualTo(1);
    processInstance = processInstanceQuery.singleResult();
    assertThat(processInstance.getId()).isEqualTo(processInstanceId);
    assertThat(processInstance.getDeleteReason()).isEqualTo("custom message");

    processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).notDeleted();
    assertThat(processInstanceQuery.count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryByDeploymentId() {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess", i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess2", "1").getId());

    HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery().deploymentId(deployment.getId());
    assertThat(processInstanceQuery.count()).isEqualTo(5);
    assertThat(processInstanceQuery.list().get(0).getDeploymentId()).isEqualTo(deployment.getId());

    List<HistoricProcessInstance> processInstances = processInstanceQuery.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(5);

    processInstanceQuery = historyService.createHistoricProcessInstanceQuery().deploymentId("invalid");
    assertThat(processInstanceQuery.count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryByDeploymentIdIn() {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess", i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess2", "1").getId());

    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployment.getId());
    deploymentIds.add("invalid");
    HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery().deploymentIdIn(deploymentIds);
    assertThat(processInstanceQuery.count()).isEqualTo(5);

    List<HistoricProcessInstance> processInstances = processInstanceQuery.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(5);

    deploymentIds = new ArrayList<String>();
    deploymentIds.add("invalid");
    processInstanceQuery = historyService.createHistoricProcessInstanceQuery().deploymentIdIn(deploymentIds);
    assertThat(processInstanceQuery.count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml" })
  public void testHistoricTaskInstanceQueryByDeploymentId() {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess", i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess2", "1").getId());

    HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().deploymentId(deployment.getId());
    assertThat(taskInstanceQuery.count()).isEqualTo(5);

    List<HistoricTaskInstance> taskInstances = taskInstanceQuery.list();
    assertThat(taskInstances).isNotNull();
    assertThat(taskInstances).hasSize(5);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().deploymentId("invalid");
    assertThat(taskInstanceQuery.count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml" })
  public void testHistoricTaskInstanceQueryByDeploymentIdIn() {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess", i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess2", "1").getId());

    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployment.getId());
    HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().deploymentIdIn(deploymentIds);
    assertThat(taskInstanceQuery.count()).isEqualTo(5);

    List<HistoricTaskInstance> taskInstances = taskInstanceQuery.list();
    assertThat(taskInstances).isNotNull();
    assertThat(taskInstances).hasSize(5);

    deploymentIds.add("invalid");
    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().deploymentIdIn(deploymentIds);
    assertThat(taskInstanceQuery.count()).isEqualTo(5);

    deploymentIds = new ArrayList<String>();
    deploymentIds.add("invalid");
    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().deploymentIdIn(deploymentIds);
    assertThat(taskInstanceQuery.count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml" })
  public void testHistoricTaskInstanceOrQueryByDeploymentId() {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess", i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess2", "1").getId());

    HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().or().deploymentId(deployment.getId()).endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(5);

    List<HistoricTaskInstance> taskInstances = taskInstanceQuery.list();
    assertThat(taskInstances).isNotNull();
    assertThat(taskInstances).hasSize(5);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().or().deploymentId("invalid").endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(0);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().or().taskDefinitionKey("theTask").deploymentId("invalid").endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(5);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("theTask").or().deploymentId("invalid").endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(0);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDefinitionKey("theTask")
          .deploymentId("invalid")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess")
          .processDefinitionId("invalid")
        .endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(4);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDefinitionKey("theTask")
          .deploymentId("invalid")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess2")
          .processDefinitionId("invalid")
        .endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(1);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDefinitionKey("theTask")
          .deploymentId("invalid")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess")
          .processDefinitionId("invalid")
        .endOr()
        .processInstanceBusinessKey("1");
    assertThat(taskInstanceQuery.count()).isEqualTo(1);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDefinitionKey("theTask")
          .deploymentId("invalid")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess2")
          .processDefinitionId("invalid")
        .endOr()
        .processInstanceBusinessKey("1");
    assertThat(taskInstanceQuery.count()).isEqualTo(1);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDefinitionKey("theTask")
          .deploymentId("invalid")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess2")
          .processDefinitionId("invalid")
        .endOr()
        .processInstanceBusinessKey("2");
    assertThat(taskInstanceQuery.count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml" })
  public void testHistoricTaskInstanceOrQueryByDeploymentIdIn() {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess", i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess2", "1").getId());

    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployment.getId());
    HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().or().deploymentIdIn(deploymentIds).processDefinitionId("invalid").endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(5);

    List<HistoricTaskInstance> taskInstances = taskInstanceQuery.list();
    assertThat(taskInstances).isNotNull();
    assertThat(taskInstances).hasSize(5);

    deploymentIds.add("invalid");
    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().or().deploymentIdIn(deploymentIds).processDefinitionId("invalid").endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(5);

    deploymentIds = new ArrayList<String>();
    deploymentIds.add("invalid");
    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().or().deploymentIdIn(deploymentIds).processDefinitionId("invalid").endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(0);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().or().taskDefinitionKey("theTask").deploymentIdIn(deploymentIds).endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(5);

    taskInstanceQuery = historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("theTask").or().deploymentIdIn(deploymentIds).endOr();
    assertThat(taskInstanceQuery.count()).isEqualTo(0);
  }

  @Deployment(resources={"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testLocalizeTasks() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("my task");
    assertThat(tasks.get(0).getDescription()).isNull();

    ObjectNode infoNode = dynamicBpmnService.changeLocalizationName("en-GB", "theTask", "My localized name");
    dynamicBpmnService.changeLocalizationDescription("en-GB".toString(), "theTask", "My localized description", infoNode);
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

    tasks = historyService.createHistoricTaskInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("my task");
    assertThat(tasks.get(0).getDescription()).isNull();

    tasks = historyService.createHistoricTaskInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("en-GB").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("My localized name");
    assertThat(tasks.get(0).getDescription()).isEqualTo("My localized description");

    tasks = historyService.createHistoricTaskInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).listPage(0, 10);
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("my task");
    assertThat(tasks.get(0).getDescription()).isNull();

    tasks = historyService.createHistoricTaskInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("en-GB").listPage(0, 10);
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("My localized name");
    assertThat(tasks.get(0).getDescription()).isEqualTo("My localized description");

    HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    assertThat(task.getName()).isEqualTo("my task");
    assertThat(task.getDescription()).isNull();

    task = historyService.createHistoricTaskInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("en-GB").singleResult();
    assertThat(task.getName()).isEqualTo("My localized name");
    assertThat(task.getDescription()).isEqualTo("My localized description");

    task = historyService.createHistoricTaskInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    assertThat(task.getName()).isEqualTo("my task");
    assertThat(task.getDescription()).isNull();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/concurrentExecution.bpmn20.xml" })
  public void testHistoricVariableInstancesOnParallelExecution() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("concurrent", singletonMap("rootValue", "test"));

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    for (Task task : tasks) {
      // set token local variable
      log.debug("setting variables on task {}, execution {}", task.getId(), task.getExecutionId());
      runtimeService.setVariableLocal(task.getExecutionId(), "parallelValue1", task.getName());
      runtimeService.setVariableLocal(task.getExecutionId(), "parallelValue2", "test");
      taskService.complete(task.getId(), emptyMap());
    }
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());

    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("rootValue", "test").count()).isEqualTo(1);

    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("parallelValue1", "Receive Payment").count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("parallelValue1", "Ship Order").count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("parallelValue2", "test").count()).isEqualTo(1);
  }

  /**
   * basically copied from {@link ProcessInstanceQueryTest}
   */
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryStringVariable() {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("stringVar", "abcdef"));
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult().getId());

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", map(
      "stringVar", "abcdef",
      "stringVar2", "ghijkl"
    ));
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult().getId());

    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("stringVar", "azerty"));
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance3.getId()).singleResult().getId());

    // Test EQUAL on single string variable, should result in 2 matches
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar", "abcdef");
    List<HistoricProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    // Test EQUAL on two string variables, should result in single match
    query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar", "abcdef").variableValueEquals("stringVar2", "ghijkl");
    HistoricProcessInstance resultInstance = query.singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance2.getId());

    // Test NOT_EQUAL, should return only 1 resultInstance
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar", "abcdef").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    // Test GREATER_THAN, should return only matching 'azerty'
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("stringVar", "abcdef").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("stringVar", "z").singleResult();
    assertThat(resultInstance).isNull();

    // Test GREATER_THAN_OR_EQUAL, should return 3 results
    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "abcdef").count()).isEqualTo(3);
    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "z").count()).isEqualTo(0);

    // Test LESS_THAN, should return 2 results
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThan("stringVar", "abcdeg").list();
    assertThat(processInstances).hasSize(2);
    List<String> expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLessThan("stringVar", "abcdef").count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count()).isEqualTo(3);

    // Test LESS_THAN_OR_EQUAL
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "abcdef").list();
    assertThat(processInstances).hasSize(2);
    expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count()).isEqualTo(3);
    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "aa").count()).isEqualTo(0);

    // Test LIKE
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "azert%").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "%y").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "%zer%").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "a%").count()).isEqualTo(3);
    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "%x%").count()).isEqualTo(0);

    // Test value-only matching
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueEquals("azerty").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueEquals("abcdef").list();
    assertThat(processInstances).hasSize(2);
    expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueEquals("notmatchinganyvalues").singleResult();
    assertThat(resultInstance).isNull();

    historyService.deleteHistoricProcessInstance(processInstance1.getId());
    historyService.deleteHistoricProcessInstance(processInstance2.getId());
    historyService.deleteHistoricProcessInstance(processInstance3.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryEqualsIgnoreCase() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("mixed", "AbCdEfG");
    vars.put("lower", "ABCDEFG");
    vars.put("upper", "abcdefg");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().variableValueEqualsIgnoreCase("mixed", "abcdefg").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    instance = historyService.createHistoricProcessInstanceQuery().variableValueEqualsIgnoreCase("lower", "abcdefg").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    instance = historyService.createHistoricProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", "abcdefg").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    // Pass in non-lower-case string
    instance = historyService.createHistoricProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", "ABCdefg").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    // Pass in null-value, should cause exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .as("Exception expected")
      .isThrownBy(() -> historyService.createHistoricProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", null).singleResult())
      .withMessage("value is null");

    // Pass in null name, should cause exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .as("Exception expected")
      .isThrownBy(() -> historyService.createHistoricProcessInstanceQuery().variableValueEqualsIgnoreCase(null, "abcdefg").singleResult())
      .withMessage("name is null");
  }

  /**
   * Only do one second type, as the logic is same as in {@link ProcessInstanceQueryTest} and I do not want to duplicate all test case logic here. Basically copied from
   * {@link ProcessInstanceQueryTest}
   */
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryDateVariable() throws Exception {
    Date date1 = Calendar.getInstance().getTime();

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("dateVar", date1));
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult().getId());

    Date date2 = Calendar.getInstance().getTime();
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", map(
      "dateVar", date1,
      "dateVar2", date2
    ));
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult().getId());

    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("dateVar", nextYear.getTime()));
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance3.getId()).singleResult().getId());

    Calendar nextMonth = Calendar.getInstance();
    nextMonth.add(Calendar.MONTH, 1);

    Calendar twoYearsLater = Calendar.getInstance();
    twoYearsLater.add(Calendar.YEAR, 2);

    Calendar oneYearAgo = Calendar.getInstance();
    oneYearAgo.add(Calendar.YEAR, -1);

    // Query on single short variable, should result in 2 matches
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", date1);
    List<HistoricProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    // Query on two short variables, should result in single value
    query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", date1).variableValueEquals("dateVar2", date2);
    HistoricProcessInstance resultInstance = query.singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance2.getId());

    // Query with unexisting variable value
    Date unexistingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/01/1989 12:00:00");
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", unexistingDate).singleResult();
    assertThat(resultInstance).isNull();

    // Test NOT_EQUALS
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar", date1).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    // Test GREATER_THAN
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("dateVar", nextMonth.getTime()).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("dateVar", nextYear.getTime()).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("dateVar", oneYearAgo.getTime()).count()).isEqualTo(3);

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextMonth.getTime()).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextYear.getTime()).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", oneYearAgo.getTime()).count()).isEqualTo(3);

    // Test LESS_THAN
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThan("dateVar", nextYear.getTime()).list();
    assertThat(processInstances).hasSize(2);

    List<String> expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLessThan("dateVar", date1).count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLessThan("dateVar", twoYearsLater.getTime()).count()).isEqualTo(3);

    // Test LESS_THAN_OR_EQUAL
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", nextYear.getTime()).list();
    assertThat(processInstances).hasSize(3);

    assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", oneYearAgo.getTime()).count()).isEqualTo(0);

    // Test value-only matching
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueEquals(nextYear.getTime()).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueEquals(date1).list();
    assertThat(processInstances).hasSize(2);
    expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueEquals(twoYearsLater.getTime()).singleResult();
    assertThat(resultInstance).isNull();

    historyService.deleteHistoricProcessInstance(processInstance1.getId());
    historyService.deleteHistoricProcessInstance(processInstance2.getId());
    historyService.deleteHistoricProcessInstance(processInstance3.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNativeHistoricProcessInstanceTest() {
    // just test that the query will be constructed and executed, details
    // are tested in the TaskQueryTest
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(historyService.createNativeHistoricProcessInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricProcessInstance.class)).count()).isEqualTo(1);
    assertThat(historyService.createNativeHistoricProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).list()).hasSize(1);
    // assertThat(1,
    // historyService.createNativeHistoricProcessInstanceQuery().sql("SELECT * FROM "
    // +
    // managementService.getTableName(HistoricProcessInstance.class)).listPage(0,
    // 1).size());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNativeHistoricTaskInstanceTest() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(historyService.createNativeHistoricTaskInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricProcessInstance.class)).count()).isEqualTo(1);
    assertThat(historyService.createNativeHistoricTaskInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).list()).hasSize(1);
    assertThat(historyService.createNativeHistoricTaskInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).listPage(0, 1)).hasSize(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNativeHistoricActivityInstanceTest() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(historyService.createNativeHistoricActivityInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricProcessInstance.class)).count()).isEqualTo(1);
    assertThat(historyService.createNativeHistoricActivityInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).list()).hasSize(1);
    assertThat(historyService.createNativeHistoricActivityInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).listPage(0, 1)).hasSize(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryByProcessDefinitionName() {

    String processDefinitionKey = "oneTaskProcess";
    String processDefinitionName = "The One Task Process";
    runtimeService.startProcessInstanceByKey(processDefinitionKey);

    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionName(processDefinitionName).list().get(0).getProcessDefinitionName()).isEqualTo(processDefinitionName);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionName(processDefinitionName).list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionName(processDefinitionName).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionName("invalid").list()).hasSize(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionName("invalid").count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionName(processDefinitionName).processDefinitionId("invalid").endOr().list().get(0).getProcessDefinitionName()).isEqualTo(processDefinitionName);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionName(processDefinitionName).processDefinitionId("invalid").endOr().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionName(processDefinitionName).processDefinitionId("invalid").endOr().count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryByProcessDefinitionCategory() {
    String processDefinitionKey = "oneTaskProcess";
    String processDefinitionCategory = "ExamplesCategory";
    runtimeService.startProcessInstanceByKey(processDefinitionKey);

    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionCategory(processDefinitionCategory).list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionCategory(processDefinitionCategory).count()).isEqualTo(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionCategory("invalid").list()).hasSize(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionCategory("invalid").count()).isEqualTo(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionCategory(processDefinitionCategory).processDefinitionId("invalid").endOr().list()).hasSize(1);
    assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionCategory(processDefinitionCategory).processDefinitionId("invalid").endOr().count()).isEqualTo(1);
  }

}
