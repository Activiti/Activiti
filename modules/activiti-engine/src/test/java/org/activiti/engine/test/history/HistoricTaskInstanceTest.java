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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 * @author Frederik Heremans
 */
public class HistoricTaskInstanceTest extends PluggableActivitiTestCase {

  @Deployment
  public void testHistoricTaskInstance() throws Exception {
    String processInstanceId = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest").getId();
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    
    // Set priority to non-default value
    Task runtimeTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    runtimeTask.setPriority(1234);
    
    // Set due-date
    Date dueDate = sdf.parse("01/02/2003 04:05:06");
    runtimeTask.setDueDate(dueDate);
    taskService.saveTask(runtimeTask);
    
    String taskId = runtimeTask.getId();
    String taskDefinitionKey = runtimeTask.getTaskDefinitionKey();
    
    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals(taskId, historicTaskInstance.getId());
    assertEquals(1234, historicTaskInstance.getPriority());
    assertEquals("Clean up", historicTaskInstance.getName());
    assertEquals("Schedule an engineering meeting for next week with the new hire.", historicTaskInstance.getDescription());
    assertEquals(dueDate, historicTaskInstance.getDueDate());
    assertEquals("kermit", historicTaskInstance.getAssignee());
    assertEquals(taskDefinitionKey, historicTaskInstance.getTaskDefinitionKey());
    assertNull(historicTaskInstance.getEndTime());
    assertNull(historicTaskInstance.getDurationInMillis());
    
    runtimeService.setVariable(processInstanceId, "deadline", "yesterday");
    
    taskService.complete(taskId);
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());

    historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals(taskId, historicTaskInstance.getId());
    assertEquals(1234, historicTaskInstance.getPriority());
    assertEquals("Clean up", historicTaskInstance.getName());
    assertEquals("Schedule an engineering meeting for next week with the new hire.", historicTaskInstance.getDescription());
    assertEquals(dueDate, historicTaskInstance.getDueDate());
    assertEquals("kermit", historicTaskInstance.getAssignee());
    assertEquals(TaskEntity.DELETE_REASON_COMPLETED, historicTaskInstance.getDeleteReason());
    assertEquals(taskDefinitionKey, historicTaskInstance.getTaskDefinitionKey());
    assertNotNull(historicTaskInstance.getEndTime());
    assertNotNull(historicTaskInstance.getDurationInMillis());
    
    historyService.deleteHistoricTaskInstance(taskId);

    assertEquals(0, historyService.createHistoricTaskInstanceQuery().count());
  }
  
  public void testDeleteHistoricTaskInstance() throws Exception {
    // deleting unexisting historic task instance should be silently ignored
    historyService.deleteHistoricTaskInstance("unexistingId");
  }
  
  @Deployment
  public void testHistoricTaskInstanceQuery() throws Exception {
    // First instance is finished
    ProcessInstance finishedInstance = runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");
    
    // Set priority to non-default value
    Task task = taskService.createTaskQuery().processInstanceId(finishedInstance.getId()).singleResult();
    task.setPriority(1234);
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 04:05:06");
    task.setDueDate(dueDate);
    
    taskService.saveTask(task);
    
    // Complete the task
    String taskId = task.getId();
    taskService.complete(taskId);
    
    // Task id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskId(taskId).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskId("unexistingtaskid").count());
    
    // Name
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskName("Clean up").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskName("unexistingname").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskNameLike("Clean u%").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskNameLike("%lean up").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskNameLike("%lean u%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskNameLike("%unexistingname%").count());
    
    
    // Description
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDescription("Historic task description").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDescription("unexistingdescription").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%task description").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("Historic task %").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%task%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%unexistingdescripton%").count());
    
    // Execution id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().executionId(finishedInstance.getId()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().executionId("unexistingexecution").count());
    
    // Process instance id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processInstanceId(finishedInstance.getId()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processInstanceId("unexistingid").count());
    
    // Process definition id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionId(finishedInstance.getProcessDefinitionId()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionId("unexistingdefinitionid").count());
    
    // Process definition name
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionName("Historic task query test process").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionName("unexistingdefinitionname").count());
    
    // Process definition key
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionKey("HistoricTaskQueryTest").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionKey("unexistingdefinitionkey").count());
    
    
    // Assignee
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskAssignee("kermit").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskAssignee("johndoe").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%ermit").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("kermi%").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%ermi%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%johndoe%").count());
    
    // Delete reason
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDeleteReason(TaskEntity.DELETE_REASON_COMPLETED).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDeleteReason("deleted").count());
    
    // Task definition ID
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("task").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("unexistingkey").count());
    
    // Task priority
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskPriority(1234).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskPriority(5678).count());
    
    
    // Due date
    Calendar anHourAgo = Calendar.getInstance();
    anHourAgo.setTime(dueDate);
    anHourAgo.add(Calendar.HOUR, -1);
    
    Calendar anHourLater = Calendar.getInstance();
    anHourLater.setTime(dueDate);
    anHourLater.add(Calendar.HOUR, 1);
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDueDate(dueDate).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueDate(anHourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueDate(anHourLater.getTime()).count());
    
    // Due date before
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDueBefore(anHourLater.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueBefore(anHourAgo.getTime()).count());
    
    // Due date after
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDueAfter(anHourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueAfter(anHourLater.getTime()).count());
    
    // Finished and Unfinished - Add anther other instance that has a running task (unfinished)
    runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().finished().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().unfinished().count());
  }
  
  @Deployment
  public void testHistoricTaskInstanceQueryProcessFinished() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TwoTaskHistoricTaskQueryTest");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // Running task on running process should be available
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processUnfinished().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processFinished().count());
    
    // Finished and running task on running process should be available
    taskService.complete(task.getId());
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().processUnfinished().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processFinished().count());
    
    // 2 finished tasks are found for finished process after completing last task of process
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processUnfinished().count());
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().processFinished().count());
  }
  
  @Deployment
  public void testHistoricTaskInstanceQuerySorting() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");
    
    String taskId = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult().getId();
    taskService.complete(taskId);
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByDeleteReason().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByExecutionId().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceId().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskName().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().asc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskId().asc().count());    
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByDeleteReason().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByExecutionId().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceId().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskName().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskId().desc().count());    
  }
  
  public void testInvalidSorting() {
    try {
      historyService.createHistoricTaskInstanceQuery().asc();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricTaskInstanceQuery().desc();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().list();
      fail();
    } catch (ActivitiException e) {
      
    }
  }
}
