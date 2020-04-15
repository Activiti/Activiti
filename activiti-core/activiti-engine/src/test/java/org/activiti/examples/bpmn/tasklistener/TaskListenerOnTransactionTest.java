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
package org.activiti.examples.bpmn.tasklistener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**

 */
public class TaskListenerOnTransactionTest extends PluggableActivitiTestCase {

  @Deployment
  public void testOnCompleteCommitted() {
    CurrentTaskTransactionDependentTaskListener.clear();

    Map<String, Object> variables = new HashMap<>();
    variables.put("serviceTask1", false);
    variables.put("serviceTask2", false);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenersOnCompleteCommitted", variables);

    // task 1 has committed listener
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // task 2 has rolled-back listener
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    List<CurrentTaskTransactionDependentTaskListener.CurrentTask> currentTasks = CurrentTaskTransactionDependentTaskListener.getCurrentTasks();
    assertThat(currentTasks).hasSize(1);

    assertThat(currentTasks.get(0).getTaskId()).isEqualTo("usertask1");
    assertThat(currentTasks.get(0).getTaskName()).isEqualTo("User Task 1");
    assertThat(currentTasks.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(currentTasks.get(0).getProcessInstanceId()).isNotNull();
  }

  @Deployment
  public void testOnCompleteRolledBack() {
    CurrentTaskTransactionDependentTaskListener.clear();

    Map<String, Object> variables = new HashMap<>();
    variables.put("serviceTask1", false);
    variables.put("serviceTask2", false);
    variables.put("serviceTask3", true);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenersOnCompleteCommitted", variables);

    // task 1 has before-commit listener
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // task 2 has rolled-back listener
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // task 3 has rolled-back listener
    task = taskService.createTaskQuery().singleResult();

    try {
      taskService.complete(task.getId());
    } catch (Exception ex) {

    }

    List<CurrentTaskTransactionDependentTaskListener.CurrentTask> currentTasks = CurrentTaskTransactionDependentTaskListener.getCurrentTasks();
    assertThat(currentTasks).hasSize(2);

    assertThat(currentTasks.get(0).getTaskId()).isEqualTo("usertask1");
    assertThat(currentTasks.get(0).getTaskName()).isEqualTo("User Task 1");
    assertThat(currentTasks.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(currentTasks.get(0).getProcessInstanceId()).isNotNull();

    assertThat(currentTasks.get(1).getTaskId()).isEqualTo("usertask3");
    assertThat(currentTasks.get(1).getTaskName()).isEqualTo("User Task 3");
    assertThat(currentTasks.get(1).getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(currentTasks.get(1).getProcessInstanceId()).isNotNull();
  }

  @Deployment
  public void testOnCompleteExecutionVariables() {

    CurrentTaskTransactionDependentTaskListener.clear();

    runtimeService.startProcessInstanceByKey("taskListenersOnCompleteExecutionVariables");

    // task 1 has committed listener
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // task 2 has committed listener
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    List<CurrentTaskTransactionDependentTaskListener.CurrentTask> currentTasks = CurrentTaskTransactionDependentTaskListener.getCurrentTasks();
    assertThat(currentTasks).hasSize(2);

    assertThat(currentTasks.get(0).getTaskId()).isEqualTo("usertask1");
    assertThat(currentTasks.get(0).getTaskName()).isEqualTo("User Task 1");
    assertThat(currentTasks.get(1).getExecutionVariables()).hasSize(1);
    assertThat(currentTasks.get(0).getExecutionVariables().get("injectedExecutionVariable")).isEqualTo("test1");

    assertThat(currentTasks.get(1).getTaskId()).isEqualTo("usertask2");
    assertThat(currentTasks.get(1).getTaskName()).isEqualTo("User Task 2");
    assertThat(currentTasks.get(1).getExecutionVariables()).hasSize(1);
    assertThat(currentTasks.get(1).getExecutionVariables().get("injectedExecutionVariable")).isEqualTo("test2");
  }

  @Deployment
  public void testOnCompleteTransactionalOperation() {
    CurrentTaskTransactionDependentTaskListener.clear();

    ProcessInstance firstProcessInstance = runtimeService.startProcessInstanceByKey("transactionDependentTaskListenerProcess");
    assertProcessEnded(firstProcessInstance.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
      assertThat(historicProcessInstances).hasSize(1);
      assertThat(historicProcessInstances.get(0).getProcessDefinitionKey()).isEqualTo("transactionDependentTaskListenerProcess");
    }

    ProcessInstance secondProcessInstance = runtimeService.startProcessInstanceByKey("secondTransactionDependentTaskListenerProcess");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(secondProcessInstance.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      // first historic process instance was deleted by task listener
      List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
      assertThat(historicProcessInstances).hasSize(1);
      assertThat(historicProcessInstances.get(0).getProcessDefinitionKey()).isEqualTo("secondTransactionDependentTaskListenerProcess");
    }

    List<MyTransactionalOperationTransactionDependentTaskListener.CurrentTask> currentTasks = MyTransactionalOperationTransactionDependentTaskListener.getCurrentTasks();
    assertThat(currentTasks).hasSize(1);

    assertThat(currentTasks.get(0).getTaskId()).isEqualTo("usertask1");
    assertThat(currentTasks.get(0).getTaskName()).isEqualTo("User Task 1");
  }

  @Deployment
  public void testOnCompleteCustomPropertiesResolver() {
    CurrentTaskTransactionDependentTaskListener.clear();

    runtimeService.startProcessInstanceByKey("transactionDependentTaskListenerProcess");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    List<CurrentTaskTransactionDependentTaskListener.CurrentTask> currentTasks = CurrentTaskTransactionDependentTaskListener.getCurrentTasks();
    assertThat(currentTasks).hasSize(1);

    assertThat(currentTasks.get(0).getTaskId()).isEqualTo("usertask1");
    assertThat(currentTasks.get(0).getTaskName()).isEqualTo("User Task 1");
    assertThat(currentTasks.get(0).getCustomPropertiesMap()).hasSize(1);
    assertThat(currentTasks.get(0).getCustomPropertiesMap().get("customProp1")).isEqualTo("usertask1");
  }

}
