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
package org.activiti.spring.test.taskListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Yvo Swillens
 */
@ContextConfiguration("classpath:org/activiti/spring/test/executionListener/TransactionDependentListenerTest-context.xml")
public class TaskListenerOnTransactionTest extends SpringActivitiTestCase {

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
    assertEquals(1, currentTasks.size());

    assertEquals("usertask1", currentTasks.get(0).getTaskId());
    assertEquals("User Task 1", currentTasks.get(0).getTaskName());
    assertEquals(processInstance.getId(), currentTasks.get(0).getProcessInstanceId());
    assertNotNull(currentTasks.get(0).getProcessInstanceId());
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
    assertEquals(2, currentTasks.size());

    assertEquals("usertask1", currentTasks.get(0).getTaskId());
    assertEquals("User Task 1", currentTasks.get(0).getTaskName());
    assertEquals(processInstance.getId(), currentTasks.get(0).getProcessInstanceId());
    assertNotNull(currentTasks.get(0).getProcessInstanceId());

    assertEquals("usertask3", currentTasks.get(1).getTaskId());
    assertEquals("User Task 3", currentTasks.get(1).getTaskName());
    assertEquals(processInstance.getId(), currentTasks.get(1).getProcessInstanceId());
    assertNotNull(currentTasks.get(1).getProcessInstanceId());
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
    assertEquals(2, currentTasks.size());

    assertEquals("usertask1", currentTasks.get(0).getTaskId());
    assertEquals("User Task 1", currentTasks.get(0).getTaskName());
    assertEquals(1, currentTasks.get(1).getExecutionVariables().size());
    assertEquals("test1", currentTasks.get(0).getExecutionVariables().get("injectedExecutionVariable"));

    assertEquals("usertask2", currentTasks.get(1).getTaskId());
    assertEquals("User Task 2", currentTasks.get(1).getTaskName());
    assertEquals(1, currentTasks.get(1).getExecutionVariables().size());
    assertEquals("test2", currentTasks.get(1).getExecutionVariables().get("injectedExecutionVariable"));
  }

  @Deployment
  public void testOnCompleteTransactionalOperation() {
    CurrentTaskTransactionDependentTaskListener.clear();

    ProcessInstance firstProcessInstance = runtimeService.startProcessInstanceByKey("transactionDependentTaskListenerProcess");
    assertProcessEnded(firstProcessInstance.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
      assertEquals(1, historicProcessInstances.size());
      assertEquals("transactionDependentTaskListenerProcess", historicProcessInstances.get(0).getProcessDefinitionKey());
    }

    ProcessInstance secondProcessInstance = runtimeService.startProcessInstanceByKey("secondTransactionDependentTaskListenerProcess");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(secondProcessInstance.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      // first historic process instance was deleted by task listener
      List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
      assertEquals(1, historicProcessInstances.size());
      assertEquals("secondTransactionDependentTaskListenerProcess", historicProcessInstances.get(0).getProcessDefinitionKey());
    }

    List<MyTransactionalOperationTransactionDependentTaskListener.CurrentTask> currentTasks = MyTransactionalOperationTransactionDependentTaskListener.getCurrentTasks();
    assertEquals(1, currentTasks.size());

    assertEquals("usertask1", currentTasks.get(0).getTaskId());
    assertEquals("User Task 1", currentTasks.get(0).getTaskName());
  }

  @Deployment
  public void testOnCompleteCustomPropertiesResolver() {
    CurrentTaskTransactionDependentTaskListener.clear();

    runtimeService.startProcessInstanceByKey("transactionDependentTaskListenerProcess");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    List<CurrentTaskTransactionDependentTaskListener.CurrentTask> currentTasks = CurrentTaskTransactionDependentTaskListener.getCurrentTasks();
    assertEquals(1, currentTasks.size());

    assertEquals("usertask1", currentTasks.get(0).getTaskId());
    assertEquals("User Task 1", currentTasks.get(0).getTaskName());
    assertEquals(1, currentTasks.get(0).getCustomPropertiesMap().size());
    assertEquals("usertask1", currentTasks.get(0).getCustomPropertiesMap().get("customProp1"));
  }

}
