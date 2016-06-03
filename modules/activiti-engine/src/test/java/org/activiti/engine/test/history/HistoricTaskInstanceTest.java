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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricIdentityLink;
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
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("formKeyVar", "expressionFormKey");
    String processInstanceId = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest", varMap).getId();
    
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
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());
    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals(taskId, historicTaskInstance.getId());
    assertEquals(1234, historicTaskInstance.getPriority());
    assertEquals("Clean up", historicTaskInstance.getName());
    assertEquals("Schedule an engineering meeting for next week with the new hire.", historicTaskInstance.getDescription());
    assertEquals(dueDate, historicTaskInstance.getDueDate());
    assertEquals("kermit", historicTaskInstance.getAssignee());
    assertEquals(taskDefinitionKey, historicTaskInstance.getTaskDefinitionKey());
    assertEquals("expressionFormKey", historicTaskInstance.getFormKey());
    assertNull(historicTaskInstance.getEndTime());
    assertNull(historicTaskInstance.getDurationInMillis());
    assertNull(historicTaskInstance.getWorkTimeInMillis());
    
    runtimeService.setVariable(processInstanceId, "deadline", "yesterday");
    
    taskService.claim(taskId, "kermit");
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());
    historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertNotNull(historicTaskInstance.getClaimTime());
    assertNull(historicTaskInstance.getWorkTimeInMillis());
    assertEquals("expressionFormKey", historicTaskInstance.getFormKey());
    
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
    assertEquals("expressionFormKey", historicTaskInstance.getFormKey());
    assertNotNull(historicTaskInstance.getEndTime());
    assertNotNull(historicTaskInstance.getDurationInMillis());
    assertNotNull(historicTaskInstance.getClaimTime());
    assertNotNull(historicTaskInstance.getWorkTimeInMillis());
    
    historyService.deleteHistoricTaskInstance(taskId);

    assertEquals(0, historyService.createHistoricTaskInstanceQuery().count());
  }
  
  public void testDeleteHistoricTaskInstance() throws Exception {
    // deleting unexisting historic task instance should be silently ignored
    historyService.deleteHistoricTaskInstance("unexistingId");
  }
  
  @Deployment
  public void testHistoricTaskInstanceQuery() throws Exception {
    Calendar start = Calendar.getInstance();
    start.set(Calendar.MILLISECOND, 0);
    processEngineConfiguration.getClock().setCurrentTime(start.getTime());
    
    // First instance is finished
    ProcessInstance finishedInstance = runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest", "myBusinessKey");
    processEngineConfiguration.getClock().reset();
    
    // Set priority to non-default value
    Task task = taskService.createTaskQuery().processInstanceId(finishedInstance.getId()).singleResult();
    task.setPriority(1234);
    task.setOwner("fozzie");
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 04:05:06");
    task.setDueDate(dueDate);
    
    taskService.saveTask(task);
    taskService.addUserIdentityLink(task.getId(), "gonzo", "someType");
    
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
    
    // Process instance business key
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKey("myBusinessKey").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKey("unexistingKey").count());
    
    // Process definition id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionId(finishedInstance.getProcessDefinitionId()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionId("unexistingdefinitionid").count());
    
    // Process definition name
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionName("Historic task query test process").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionName("unexistingdefinitionname").count());
    
    // Process definition key
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionKey("HistoricTaskQueryTest").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionKey("unexistingdefinitionkey").count());
    
    // Process definition key in
    List<String> includeIds = new ArrayList<String>();
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionKeyIn(includeIds).count());
    includeIds.add("unexistingProcessDefinition");    
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionKeyIn(includeIds).count());    
    includeIds.add("HistoricTaskQueryTest");
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKeyIn(includeIds).count());
    
    // Form key
    HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery()
        .processInstanceId(finishedInstance.getId()).singleResult();
    assertEquals("testFormKey", historicTask.getFormKey());
    
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
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskMinPriority(1234).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskMinPriority(1000).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskMinPriority(1300).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskMaxPriority(1234).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskMaxPriority(1300).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskMaxPriority(1000).count());
    
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
    
    anHourAgo = new GregorianCalendar();
    anHourAgo.setTime(start.getTime());
    anHourAgo.add(Calendar.HOUR, -1);
    
    anHourLater = Calendar.getInstance();
    anHourLater.setTime(start.getTime());
    anHourLater.add(Calendar.HOUR, 1);
    
    // Start date
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskCreatedOn(start.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskCreatedOn(anHourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskCreatedAfter(anHourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskCreatedAfter(anHourLater.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskCreatedBefore(anHourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskCreatedBefore(anHourLater.getTime()).count());
    
    // Completed date
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskCompletedAfter(anHourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskCompletedAfter(anHourLater.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskCompletedBefore(anHourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskCompletedBefore(anHourLater.getTime()).count());
    
    // Filter based on identity-links
    // Assignee is involved
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("kermit").count());
    
    // Owner is involved
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("fozzie").count());
    
    // Manually involved person
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("gonzo").count());

    // Finished and Unfinished - Add anther other instance that has a running task (unfinished)
    runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().finished().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().unfinished().count());
  }
  
  @Deployment
  public void testHistoricTaskInstanceOrQuery() throws Exception {
    Calendar start = Calendar.getInstance();
    start.set(Calendar.MILLISECOND, 0);
    processEngineConfiguration.getClock().setCurrentTime(start.getTime());
    
    // First instance is finished
    ProcessInstance finishedInstance = runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest", "myBusinessKey");
    processEngineConfiguration.getClock().reset();
    
    // Set priority to non-default value
    Task task = taskService.createTaskQuery().processInstanceId(finishedInstance.getId()).singleResult();
    task.setPriority(1234);
    task.setOwner("fozzie");
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 04:05:06");
    task.setDueDate(dueDate);
    
    taskService.saveTask(task);
    taskService.addUserIdentityLink(task.getId(), "gonzo", "someType");
    
    // Complete the task
    String taskId = task.getId();
    taskService.complete(taskId);
    
    // Task id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskId(taskId).endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskId(taskId).or().taskId(taskId).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskId("unexistingtaskid").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskId("unexistingtaskid").endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskId(taskId).taskName("Clean up").endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskId("unexistingtaskid").taskName("Clean up").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskId("unexistingtaskid").taskName("unexistingname").endOr().count());
    
    // Name
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskName("Clean up").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskName("unexistingname").endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskNameLike("Clean u%").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskNameLike("%unexistingname%").endOr().count());
    final  List<String> taskNameList = new ArrayList<String>(1);
    taskNameList.add("Clean up");
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskNameIn(taskNameList).endOr().count());
    taskNameList.clear();
    taskNameList.add("unexistingname");
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskNameIn(taskNameList).endOr().count());
    taskNameList.clear();
    taskNameList.add("clean up");
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskNameInIgnoreCase(taskNameList).endOr().count());
    taskNameList.clear();
    taskNameList.add("unexistingname");
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskNameInIgnoreCase(taskNameList).endOr().count());
    
    taskNameList.clear();
    taskNameList.add("clean up");
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskName("Clean up").endOr().or().taskNameInIgnoreCase(taskNameList).endOr().count());
    taskNameList.clear();
    taskNameList.add("unexistingname");
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskName("Clean up").endOr().or().taskNameInIgnoreCase(taskNameList).endOr().count());

    // Description
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskDescription("Historic task description").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskDescription("unexistingdescription").endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLike("%task description").endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLike("%task description").taskDescription("unexistingdescription").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%unexistingdescripton%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLike("%unexistingdescripton%").taskDescription("unexistingdescription").endOr().count());
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskDescription("Historic task description").endOr().or().taskDescriptionLike("%task description").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskDescription("Historic task description").endOr().or().taskDescriptionLike("%task description2").endOr().count());
    
    // Execution id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().executionId(finishedInstance.getId()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().executionId("unexistingexecution").endOr().count());
    
    // Process instance id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().processInstanceId(finishedInstance.getId()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().processInstanceId("unexistingid").endOr().count());
    
    // Process instance business key
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().processInstanceBusinessKey("myBusinessKey").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().processInstanceBusinessKey("unexistingKey").endOr().count());
    
    // Process definition id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().processDefinitionId(finishedInstance.getProcessDefinitionId()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().processDefinitionId("unexistingdefinitionid").endOr().count());
    
    // Process definition name
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().processDefinitionName("Historic task query test process").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().processDefinitionName("unexistingdefinitionname").endOr().count());
    
    // Process definition key
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("HistoricTaskQueryTest").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("unexistingdefinitionkey").endOr().count());
    
    // Process definition key and ad hoc task
    Task adhocTask = taskService.newTask();
    taskService.saveTask(adhocTask);
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskId(adhocTask.getId()).processDefinitionKey("unexistingdefinitionkey").endOr().count());
    taskService.deleteTask(adhocTask.getId(), true);
	
    // Process definition key in
    List<String> includeIds = new ArrayList<String>();
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("unexistingdefinitionkey").processDefinitionKeyIn(includeIds).endOr().count());
    includeIds.add("unexistingProcessDefinition");
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("unexistingdefinitionkey").processDefinitionKeyIn(includeIds).endOr().count());    
    includeIds.add("unexistingProcessDefinition");    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("HistoricTaskQueryTest").processDefinitionKeyIn(includeIds).endOr().count());
    includeIds.add("HistoricTaskQueryTest");
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processDefinitionKey("unexistingdefinitionkey").processDefinitionKeyIn(includeIds).endOr().count());
    
    // Assignee
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskAssignee("kermit").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskAssignee("johndoe").endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskAssigneeLike("%ermit").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskAssigneeLike("%johndoe%").endOr().count());
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskAssignee("kermit").endOr().or().taskAssigneeLike("%ermit").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskAssignee("kermit").endOr().or().taskAssigneeLike("%johndoe%").endOr().count());
    
    // Delete reason
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskDeleteReason(TaskEntity.DELETE_REASON_COMPLETED).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskDeleteReason("deleted").endOr().count());
    
    // Task definition ID
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskDefinitionKey("task").endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskDefinitionKey("unexistingkey").endOr().count());
    
    // Task priority
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskPriority(1234).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskPriority(5678).endOr().count());
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskMinPriority(1234).endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskMinPriority(1000).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskMinPriority(1300).endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskMaxPriority(1234).endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskMaxPriority(1300).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskMaxPriority(1000).endOr().count());
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskPriority(1234).endOr().or().taskMinPriority(1234).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskPriority(1234).endOr().or().taskMinPriority(1300).endOr().count());
    
    // Due date
    Calendar anHourAgo = Calendar.getInstance();
    anHourAgo.setTime(dueDate);
    anHourAgo.add(Calendar.HOUR, -1);
    
    Calendar anHourLater = Calendar.getInstance();
    anHourLater.setTime(dueDate);
    anHourLater.add(Calendar.HOUR, 1);
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskDueDate(dueDate).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskDueDate(anHourAgo.getTime()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskDueDate(anHourLater.getTime()).endOr().count());
    
    // Due date before
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskDueBefore(anHourLater.getTime()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskDueBefore(anHourAgo.getTime()).endOr().count());
    
    // Due date after
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskDueAfter(anHourAgo.getTime()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskDueAfter(anHourLater.getTime()).endOr().count());
    
    anHourAgo = new GregorianCalendar();
    anHourAgo.setTime(start.getTime());
    anHourAgo.add(Calendar.HOUR, -1);
    
    anHourLater = Calendar.getInstance();
    anHourLater.setTime(start.getTime());
    anHourLater.add(Calendar.HOUR, 1);
    
    // Start date
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskCreatedOn(start.getTime()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskCreatedOn(anHourAgo.getTime()).endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskCreatedAfter(anHourAgo.getTime()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskCreatedAfter(anHourLater.getTime()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskCreatedBefore(anHourAgo.getTime()).endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskCreatedBefore(anHourLater.getTime()).endOr().count());
    
    // Completed date
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskCompletedAfter(anHourAgo.getTime()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskCompletedAfter(anHourLater.getTime()).endOr().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().or().taskCompletedBefore(anHourAgo.getTime()).endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskCompletedBefore(anHourLater.getTime()).endOr().count());
    
    // Filter based on identity-links
    // Assignee is involved
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskInvolvedUser("kermit").endOr().count());
    
    // Owner is involved
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskInvolvedUser("fozzie").endOr().count());
    
    // Manually involved person
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().taskInvolvedUser("gonzo").endOr().count());

    // Finished and Unfinished - Add anther other instance that has a running task (unfinished)
    runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().finished().endOr().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().or().unfinished().endOr().count());
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
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceStartTime().asc().count());    
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
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceStartTime().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskName().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().desc().count());    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskId().desc().count());    
  }
  
  @Deployment
  public void testHistoricIdentityLinksOnTask() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("historicIdentityLinks");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    
    // Set additional identity-link not coming from process
    taskService.addUserIdentityLink(task.getId(), "gonzo", "customUseridentityLink");
    assertEquals(4, taskService.getIdentityLinksForTask(task.getId()).size());
    
    // Check historic identity-links when task is still active
    List<HistoricIdentityLink> historicIdentityLinks = historyService.getHistoricIdentityLinksForTask(task.getId()); 
    assertEquals(4, historicIdentityLinks.size());
    
    // Validate all links
    boolean foundCandidateUser= false, foundCandidateGroup = false, foundAssignee = false, foundCustom = false;
    for(HistoricIdentityLink link : historicIdentityLinks) {
      assertEquals(task.getId(), link.getTaskId());
      if(link.getGroupId() != null) {
        assertEquals("sales", link.getGroupId());
        foundCandidateGroup = true;
      } else {
        if(link.getType().equals("candidate")) {
          assertEquals("fozzie", link.getUserId());
          foundCandidateUser = true;
        } else if(link.getType().equals("assignee")){
          assertEquals("kermit", link.getUserId());
          foundAssignee = true;
        } else if(link.getType().equals("customUseridentityLink")){
          assertEquals("gonzo", link.getUserId());
          foundCustom = true;
        }
      }
    }
    
    assertTrue(foundAssignee);
    assertTrue(foundCandidateGroup);
    assertTrue(foundCandidateUser);
    assertTrue(foundCustom);
    
    // Now complete the task and check if links are still there
    taskService.complete(task.getId());
    assertEquals(4, historyService.getHistoricIdentityLinksForTask(task.getId()).size());
    
    // After deleting historic task, exception should be thrown when trying to get links
    historyService.deleteHistoricTaskInstance(task.getId());
    
    try {
      historyService.getHistoricIdentityLinksForTask(task.getId()).size();
      fail("Exception expected");
    } catch(ActivitiObjectNotFoundException aonfe) {
      assertEquals(HistoricTaskInstance.class, aonfe.getObjectClass());
    }
  }
  
  public void testInvalidSorting() {
    try {
      historyService.createHistoricTaskInstanceQuery().asc();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricTaskInstanceQuery().desc();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
  }
  

  /**
   * Test to validate fix for ACT-1939: HistoryService loads invalid task local variables for completed task
   */
  @Deployment
  public void testVariableUpdateOrderHistoricTaskInstance() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("historicTask");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    
    // Update task and process-variable 10 times
    for(int i=0; i<10; i++) {
    	taskService.setVariableLocal(task.getId(), "taskVar", i);
    	runtimeService.setVariable(task.getExecutionId(), "procVar", i);
    }
    
    taskService.complete(task.getId());
    
    // Check if all variables have the value for the latest revision
    HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery()
    		.taskId(task.getId())
    		.includeProcessVariables()
    		.singleResult();
    
    Object varValue = taskInstance.getProcessVariables().get("procVar");
    assertEquals(9, varValue);
    
    taskInstance = historyService.createHistoricTaskInstanceQuery()
    		.taskId(task.getId())
    		.includeTaskLocalVariables()
    		.singleResult();
    
    varValue = taskInstance.getTaskLocalVariables().get("taskVar");
    assertEquals(9, varValue);
  }
}
