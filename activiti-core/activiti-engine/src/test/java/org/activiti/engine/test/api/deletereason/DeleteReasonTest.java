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
package org.activiti.engine.test.api.deletereason;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 */
public class DeleteReasonTest extends PluggableActivitiTestCase {

  @Deployment
  public void testDeleteProcessInstance() {
    ProcessInstance  processInstance = runtimeService.startProcessInstanceByKey("deleteReasonProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("A");
    taskService.complete(task.getId());
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult().getDeleteReason())
        .isEqualTo(DeleteReason.PROCESS_INSTANCE_DELETED);

      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
          .processInstanceId(processInstance.getId()).list();
      assertThat(historicTaskInstances).hasSize(4);

      // Task A is completed normally, the others are deleted
      for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
        if (historicTaskInstance.getName().equals("A")) {
          assertThat(historicTaskInstance.getDeleteReason()).isNull();
        } else {
          assertThat(historicTaskInstance.getDeleteReason()).isEqualTo(DeleteReason.PROCESS_INSTANCE_DELETED);
        }
      }

      assertHistoricActivitiesDeleteReason(processInstance, null, "A");
      assertHistoricActivitiesDeleteReason(processInstance, DeleteReason.PROCESS_INSTANCE_DELETED, "B", "C", "D");
    }
  }

  @Deployment
  public void testDeleteProcessInstanceWithCustomDeleteReason() {
    ProcessInstance  processInstance = runtimeService.startProcessInstanceByKey("deleteReasonProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("A");
    taskService.complete(task.getId());

    // Delete process instance with custom delete reason
    String customDeleteReason = "custom delete reason";
    runtimeService.deleteProcessInstance(processInstance.getId(), customDeleteReason);
    assertThat(runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0L);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult().getDeleteReason())
        .isEqualTo(customDeleteReason);

      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
          .processInstanceId(processInstance.getId()).list();
      assertThat(historicTaskInstances).hasSize(4);

      // Task A is completed normally, the others are deleted
      for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
        if (historicTaskInstance.getName().equals("A")) {
          assertThat(historicTaskInstance.getDeleteReason()).isNull();
        } else {
          assertThat(historicTaskInstance.getDeleteReason()).isEqualTo(customDeleteReason);
        }
      }

      assertHistoricActivitiesDeleteReason(processInstance, null, "A");
      assertHistoricActivitiesDeleteReason(processInstance, customDeleteReason, "B", "C", "D");
    }
  }

  @Deployment
  public void testRegularProcessInstanceEnd() {
    ProcessInstance  processInstance = runtimeService.startProcessInstanceByKey("deleteReasonProcess");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    while (!tasks.isEmpty()) {
      for (Task task : tasks) {
        taskService.complete(task.getId());
      }
      tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    }
    assertThat(runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0L);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      assertThat(historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstance.getId()).singleResult().getDeleteReason()).isNull();

      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
          .processInstanceId(processInstance.getId()).list();
      assertThat(historicTaskInstances).hasSize(5);

      for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
        assertThat(historicTaskInstance.getDeleteReason()).isNull();
      }

      assertHistoricActivitiesDeleteReason(processInstance, null, "A", "B", "C", "D", "E");
    }
  }

  @Deployment
  public void testDeleteProcessInstanceWithReceiveTask() {
    // First case: one receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("deleteReasonReceiveTask");
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("A").singleResult();
    assertThat(execution).isNotNull();
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      assertThat(historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstance.getId()).singleResult().getDeleteReason())
          .isEqualTo(DeleteReason.PROCESS_INSTANCE_DELETED);

      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
          .activityId("A").processInstanceId(processInstance.getId()).list();
      assertThat(historicActivityInstances).hasSize(1);

      for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
        assertThat(historicActivityInstance.getDeleteReason()).isEqualTo(DeleteReason.PROCESS_INSTANCE_DELETED);
      }
    }

    // Second case: two receive tasks in embedded subprocess
    processInstance = runtimeService.startProcessInstanceByKey("deleteReasonReceiveTask");
    Execution executionA = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("A").singleResult();
    assertThat(executionA).isNotNull();
    runtimeService.trigger(executionA.getId());

    Execution executionB = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("B").singleResult();
    assertThat(executionB).isNotNull();
    Execution executionC = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("C").singleResult();
    assertThat(executionC).isNotNull();

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      assertThat(historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstance.getId()).singleResult().getDeleteReason())
          .isEqualTo(DeleteReason.PROCESS_INSTANCE_DELETED);

      assertHistoricActivitiesDeleteReason(processInstance, null, "A");
      assertHistoricActivitiesDeleteReason(processInstance, DeleteReason.PROCESS_INSTANCE_DELETED, "B", "C");
    }
  }

  @Deployment
  public void testInterruptingBoundaryEvent() {
    ProcessInstance  processInstance = runtimeService.startProcessInstanceByKey("deleteReasonProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("A");
    taskService.complete(task.getId());

    // Timer firing should delete all tasks
    Job timerJob = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timerJob.getId());
    managementService.executeJob(timerJob.getId());

    assertHistoricTasksDeleteReason(processInstance, null, "A");
    assertHistoricTasksDeleteReason(processInstance, DeleteReason.BOUNDARY_EVENT_INTERRUPTING, "B", "C", "D");
    assertHistoricActivitiesDeleteReason(processInstance, null, "A");
    assertHistoricActivitiesDeleteReason(processInstance, DeleteReason.BOUNDARY_EVENT_INTERRUPTING, "B", "C", "D", "theSubprocess");
  }

  @Deployment
  public void testInterruptingBoundaryEvent2() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("deleteReasonReceiveTask");
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("A").singleResult();
    assertThat(execution).isNotNull();
    runtimeService.trigger(execution.getId());

    // Timer firing should delete all tasks
    Job timerJob = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timerJob.getId());
    managementService.executeJob(timerJob.getId());

    assertHistoricActivitiesDeleteReason(processInstance, null, "A");
    assertHistoricActivitiesDeleteReason(processInstance, DeleteReason.BOUNDARY_EVENT_INTERRUPTING, "B", "C", "theSubprocess");
  }

}
