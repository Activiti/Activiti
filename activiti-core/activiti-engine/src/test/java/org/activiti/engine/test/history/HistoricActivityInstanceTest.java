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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**


 */
public class HistoricActivityInstanceTest extends PluggableActivitiTestCase {

  @Deployment
  public void testHistoricActivityInstanceNoop() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("noopProcess");

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("noop").singleResult();

    assertThat(historicActivityInstance.getActivityId()).isEqualTo("noop");
    assertThat(historicActivityInstance.getActivityType()).isEqualTo("serviceTask");
    assertThat(historicActivityInstance.getProcessDefinitionId()).isNotNull();
    assertThat(historicActivityInstance.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(historicActivityInstance.getStartTime()).isNotNull();
    assertThat(historicActivityInstance.getEndTime()).isNotNull();
    assertThat(historicActivityInstance.getDurationInMillis() >= 0).isTrue();
  }

  @Deployment
  public void testHistoricActivityInstanceReceive() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveProcess");

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("receive").singleResult();

    assertThat(historicActivityInstance.getActivityId()).isEqualTo("receive");
    assertThat(historicActivityInstance.getActivityType()).isEqualTo("receiveTask");
    assertThat(historicActivityInstance.getEndTime()).isNull();
    assertThat(historicActivityInstance.getDurationInMillis()).isNull();
    assertThat(historicActivityInstance.getProcessDefinitionId()).isNotNull();
    assertThat(historicActivityInstance.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(historicActivityInstance.getStartTime()).isNotNull();

    Execution execution = runtimeService.createExecutionQuery().onlyChildExecutions().processInstanceId(processInstance.getId()).singleResult();
    runtimeService.trigger(execution.getId());

    historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("receive").singleResult();

    assertThat(historicActivityInstance.getActivityId()).isEqualTo("receive");
    assertThat(historicActivityInstance.getActivityType()).isEqualTo("receiveTask");
    assertThat(historicActivityInstance.getEndTime()).isNotNull();
    assertThat(historicActivityInstance.getDurationInMillis() >= 0).isTrue();
    assertThat(historicActivityInstance.getProcessDefinitionId()).isNotNull();
    assertThat(historicActivityInstance.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(historicActivityInstance.getStartTime()).isNotNull();
  }

  @Deployment
  public void testHistoricActivityInstanceUnfinished() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processInstance).isNotNull();

    HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery();

    long finishedActivityInstanceCount = historicActivityInstanceQuery.finished().count();
    assertThat(finishedActivityInstanceCount).as("The Start event is completed").isEqualTo(1);

    long unfinishedActivityInstanceCount = historicActivityInstanceQuery.unfinished().count();
    assertThat(unfinishedActivityInstanceCount).as("One active (unfinished) User Task").isEqualTo(1);

  }

  @Deployment
  public void testHistoricActivityInstanceQuery() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("noopProcess");

    assertThat(historyService.createHistoricActivityInstanceQuery().activityId("nonExistingActivityId").list()).hasSize(0);
    assertThat(historyService.createHistoricActivityInstanceQuery().activityId("noop").list()).hasSize(1);

    assertThat(historyService.createHistoricActivityInstanceQuery().activityType("nonExistingActivityType").list()).hasSize(0);
    assertThat(historyService.createHistoricActivityInstanceQuery().activityType("serviceTask").list()).hasSize(1);

    assertThat(historyService.createHistoricActivityInstanceQuery().activityName("nonExistingActivityName").list()).hasSize(0);
    assertThat(historyService.createHistoricActivityInstanceQuery().activityName("No operation").list()).hasSize(1);

    assertThat(historyService.createHistoricActivityInstanceQuery().taskAssignee("nonExistingAssignee").list()).hasSize(0);

    assertThat(historyService.createHistoricActivityInstanceQuery().executionId("nonExistingExecutionId").list()).hasSize(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(3);
    } else {
      assertThat(historyService.createHistoricActivityInstanceQuery().executionId(processInstance.getId()).list()).hasSize(0);
    }

    assertThat(historyService.createHistoricActivityInstanceQuery().processInstanceId("nonExistingProcessInstanceId").list()).hasSize(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(3);
    } else {
      assertThat(historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(0);
    }

    assertThat(historyService.createHistoricActivityInstanceQuery().processDefinitionId("nonExistingProcessDefinitionId").list()).hasSize(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricActivityInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list()).hasSize(3);
    } else {
      assertThat(historyService.createHistoricActivityInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list()).hasSize(0);
    }

    assertThat(historyService.createHistoricActivityInstanceQuery().unfinished().list()).hasSize(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricActivityInstanceQuery().finished().list()).hasSize(3);
    } else {
      assertThat(historyService.createHistoricActivityInstanceQuery().finished().list()).hasSize(0);
    }

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().list().get(0);
      assertThat(historyService.createHistoricActivityInstanceQuery().activityInstanceId(historicActivityInstance.getId()).list()).hasSize(1);
    }
  }

  @Deployment
  public void testHistoricActivityInstanceForEventsQuery() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("eventProcess");
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
    runtimeService.signalEventReceived("signal");
    assertProcessEnded(pi.getId());

    assertThat(historyService.createHistoricActivityInstanceQuery().activityId("noop").list()).hasSize(1);
    assertThat(historyService.createHistoricActivityInstanceQuery().activityId("userTask").list()).hasSize(1);
    assertThat(historyService.createHistoricActivityInstanceQuery().activityId("intermediate-event").list()).hasSize(1);
    assertThat(historyService.createHistoricActivityInstanceQuery().activityId("start").list()).hasSize(1);
    assertThat(historyService.createHistoricActivityInstanceQuery().activityId("end").list()).hasSize(1);

    // TODO: Discuss if boundary events will occur in the log!
    // assertThat(1,
    // historyService.createHistoricActivityInstanceQuery().activityId("boundaryEvent").list().size());

    HistoricActivityInstance intermediateEvent = historyService.createHistoricActivityInstanceQuery().activityId("intermediate-event").singleResult();
    assertThat(intermediateEvent.getStartTime()).isNotNull();
    assertThat(intermediateEvent.getEndTime()).isNotNull();

    HistoricActivityInstance startEvent = historyService.createHistoricActivityInstanceQuery().activityId("start").singleResult();
    assertThat(startEvent.getStartTime()).isNotNull();
    assertThat(startEvent.getEndTime()).isNotNull();

    HistoricActivityInstance endEvent = historyService.createHistoricActivityInstanceQuery().activityId("end").singleResult();
    assertThat(endEvent.getStartTime()).isNotNull();
    assertThat(endEvent.getEndTime()).isNotNull();
  }

  @Deployment
  public void testHistoricActivityInstanceProperties() {
    // Start process instance
    runtimeService.startProcessInstanceByKey("taskAssigneeProcess");

    // Get task list
    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("theTask").singleResult();

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(historicActivityInstance.getTaskId()).isEqualTo(task.getId());
    assertThat(historicActivityInstance.getAssignee()).isEqualTo("kermit");
  }

  @Deployment(resources = { "org/activiti/engine/test/history/calledProcess.bpmn20.xml", "org/activiti/engine/test/history/HistoricActivityInstanceTest.testCallSimpleSubProcess.bpmn20.xml" })
  public void testHistoricActivityInstanceCalledProcessId() {
    runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("callSubProcess").singleResult();

    HistoricProcessInstance oldInstance = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("calledProcess").singleResult();

    assertThat(historicActivityInstance.getCalledProcessInstanceId()).isEqualTo(oldInstance.getId());
  }

  @Deployment
  public void testSorting() {
    runtimeService.startProcessInstanceByKey("process");

    int expectedActivityInstances;
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      expectedActivityInstances = 2;
    } else {
      expectedActivityInstances = 0;
    }

    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().asc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().asc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().asc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByExecutionId().asc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().asc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().asc().list()).hasSize(expectedActivityInstances);

    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().desc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().desc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().desc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByExecutionId().desc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().desc().list()).hasSize(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().desc().list()).hasSize(expectedActivityInstances);

    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().asc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().asc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().asc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByExecutionId().asc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().asc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().asc().count()).isEqualTo(expectedActivityInstances);

    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().desc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().desc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().desc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByExecutionId().desc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().desc().count()).isEqualTo(expectedActivityInstances);
    assertThat(historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().desc().count()).isEqualTo(expectedActivityInstances);
  }

  public void testInvalidSorting() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> historyService.createHistoricActivityInstanceQuery().asc().list());

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> historyService.createHistoricActivityInstanceQuery().desc().list());

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().list());
  }

  /**
   * Test to validate fix for ACT-1399: Boundary-event and event-based auditing
   */
  @Deployment
  public void testBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("boundaryEventProcess");
    // Complete the task with the boundary-event on it
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0L);

    // Check if there is NO historic activity instance for a boundary-event
    // that has not triggered
    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("boundary").processInstanceId(processInstance.getId()).singleResult();

    assertThat(historicActivityInstance).isNull();

    // Now check the history when the boundary-event is fired
    processInstance = runtimeService.startProcessInstanceByKey("boundaryEventProcess");

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    Execution signalExecution = runtimeService.createExecutionQuery().signalEventSubscriptionName("alert").singleResult();
    runtimeService.signalEventReceived("alert", signalExecution.getId());
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0L);

    historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("boundary").processInstanceId(processInstance.getId()).singleResult();

    assertThat(historicActivityInstance).isNotNull();
    assertThat(historicActivityInstance.getStartTime()).isNotNull();
    assertThat(historicActivityInstance.getEndTime()).isNotNull();
  }

  /**
   * Test to validate fix for ACT-1399: Boundary-event and event-based auditing
   */
  @Deployment
  public void testEventBasedGateway() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("catchSignal");
    Execution waitingExecution = runtimeService.createExecutionQuery().signalEventSubscriptionName("alert").singleResult();
    assertThat(waitingExecution).isNotNull();
    runtimeService.signalEventReceived("alert", waitingExecution.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0L);

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("eventBasedgateway").processInstanceId(processInstance.getId()).singleResult();

    assertThat(historicActivityInstance).isNotNull();
  }

  /**
   * Test to validate fix for ACT-1549: endTime of joining parallel gateway is not set
   */
  @Deployment
  public void testParallelJoinEndTime() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("forkJoin");

    List<Task> tasksToComplete = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(tasksToComplete).hasSize(2);

    // Complete both tasks, second task-complete should end the fork-gateway
    // and set time
    taskService.complete(tasksToComplete.get(0).getId());
    taskService.complete(tasksToComplete.get(1).getId());

    List<HistoricActivityInstance> historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("join").processInstanceId(processInstance.getId()).list();

    assertThat(historicActivityInstance).isNotNull();

    // History contains 2 entries for parallel join (one for each path
    // arriving in the join), should contain end-time
    assertThat(historicActivityInstance).hasSize(2);
    assertThat(historicActivityInstance.get(0).getEndTime()).isNotNull();
    assertThat(historicActivityInstance.get(1).getEndTime()).isNotNull();
  }

  @Deployment
  public void testLoop() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("historic-activity-loops", singletonMap("input", 0));

    // completing 10 user tasks
    // 15 service tasks should have passed

    for (int i=0; i<10; i++) {
      Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      Number inputNumber = (Number) taskService.getVariable(task.getId(), "input");
      int input = inputNumber.intValue();
      assertThat(input).isEqualTo(i);
      taskService.complete(task.getId(), singletonMap("input", input + 1));
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    }

    // Verify history
    List<HistoricActivityInstance> taskActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
    assertThat(taskActivityInstances).hasSize(10);
    for (HistoricActivityInstance historicActivityInstance : taskActivityInstances) {
      assertThat(historicActivityInstance.getStartTime()).isNotNull();
      assertThat(historicActivityInstance.getEndTime()).isNotNull();
    }

    List<HistoricActivityInstance> serviceTaskInstances = historyService.createHistoricActivityInstanceQuery().activityType("serviceTask").list();
    assertThat(serviceTaskInstances).hasSize(15);
    for (HistoricActivityInstance historicActivityInstance : serviceTaskInstances) {
      assertThat(historicActivityInstance.getStartTime()).isNotNull();
      assertThat(historicActivityInstance.getEndTime()).isNotNull();
    }
  }

}
