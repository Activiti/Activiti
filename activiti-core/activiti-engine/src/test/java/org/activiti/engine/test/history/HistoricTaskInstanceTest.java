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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class HistoricTaskInstanceTest extends PluggableActivitiTestCase {

    @Deployment
    public void testHistoricTaskInstance() throws Exception {
        Map<String, Object> varMap = new HashMap<String, Object>();
        varMap.put("formKeyVar",
                   "expressionFormKey");
        String processInstanceId = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest",
                                                                            varMap).getId();

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

        assertThat(historyService.createHistoricTaskInstanceQuery().count()).isEqualTo(1);
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
        assertThat(historicTaskInstance.getId()).isEqualTo(taskId);
        assertThat(historicTaskInstance.getPriority()).isEqualTo(1234);
        assertThat(historicTaskInstance.getName()).isEqualTo("Clean up");
        assertThat(historicTaskInstance.getDescription()).isEqualTo("Schedule an engineering meeting for next week with the new hire.");
        assertThat(historicTaskInstance.getDueDate()).isEqualTo(dueDate);
        assertThat(historicTaskInstance.getAssignee()).isEqualTo("kermit");
        assertThat(historicTaskInstance.getTaskDefinitionKey()).isEqualTo(taskDefinitionKey);
        assertThat(historicTaskInstance.getFormKey()).isEqualTo("expressionFormKey");
        assertThat(historicTaskInstance.getEndTime()).isNull();
        assertThat(historicTaskInstance.getDurationInMillis()).isNull();
        assertThat(historicTaskInstance.getWorkTimeInMillis()).isNull();

        runtimeService.setVariable(processInstanceId,
                                   "deadline",
                                   "yesterday");

        taskService.claim(taskId,
                          "kermit");

        assertThat(historyService.createHistoricTaskInstanceQuery().count()).isEqualTo(1);
        historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
        assertThat(historicTaskInstance.getClaimTime()).isNotNull();
        assertThat(historicTaskInstance.getWorkTimeInMillis()).isNull();
        assertThat(historicTaskInstance.getFormKey()).isEqualTo("expressionFormKey");

        taskService.complete(taskId);

        assertThat(historyService.createHistoricTaskInstanceQuery().count()).isEqualTo(1);

        historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
        assertThat(historicTaskInstance.getId()).isEqualTo(taskId);
        assertThat(historicTaskInstance.getPriority()).isEqualTo(1234);
        assertThat(historicTaskInstance.getName()).isEqualTo("Clean up");
        assertThat(historicTaskInstance.getDescription()).isEqualTo("Schedule an engineering meeting for next week with the new hire.");
        assertThat(historicTaskInstance.getDueDate()).isEqualTo(dueDate);
        assertThat(historicTaskInstance.getAssignee()).isEqualTo("kermit");
        assertThat(historicTaskInstance.getDeleteReason()).isNull();
        assertThat(historicTaskInstance.getTaskDefinitionKey()).isEqualTo(taskDefinitionKey);
        assertThat(historicTaskInstance.getFormKey()).isEqualTo("expressionFormKey");
        assertThat(historicTaskInstance.getEndTime()).isNotNull();
        assertThat(historicTaskInstance.getDurationInMillis()).isNotNull();
        assertThat(historicTaskInstance.getClaimTime()).isNotNull();
        assertThat(historicTaskInstance.getWorkTimeInMillis()).isNotNull();

        historyService.deleteHistoricTaskInstance(taskId);

        assertThat(historyService.createHistoricTaskInstanceQuery().count()).isEqualTo(0);
    }

    public void testDeleteHistoricTaskInstance() throws Exception {
        // deleting unexisting historic task instance should be silently ignored
        historyService.deleteHistoricTaskInstance("unexistingId");
    }

    @Deployment
    public void testHistoricTaskInstanceQuery() throws Exception {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.MILLISECOND,
                  0);
        processEngineConfiguration.getClock().setCurrentTime(start.getTime());

        // First instance is finished
        ProcessInstance finishedInstance = runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest",
                                                                                    "myBusinessKey");
        processEngineConfiguration.getClock().reset();

        // Set priority to non-default value
        Task task = taskService.createTaskQuery().processInstanceId(finishedInstance.getId()).singleResult();
        task.setPriority(1234);
        task.setOwner("fozzie");
        Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 04:05:06");
        task.setDueDate(dueDate);

        taskService.saveTask(task);
        taskService.addUserIdentityLink(task.getId(),
                                        "gonzo",
                                        "someType");

        // Complete the task
        String taskId = task.getId();
        taskService.complete(taskId);

        // Task id
        assertThat(historyService.createHistoricTaskInstanceQuery().taskId(taskId).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskId("unexistingtaskid").count()).isEqualTo(0);

        // Name
        assertThat(historyService.createHistoricTaskInstanceQuery().taskName("Clean up").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskName("unexistingname").count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLike("Clean u%").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLike("%lean up").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLike("%lean u%").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLike("%unexistingname%").count()).isEqualTo(0);

        // Description
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDescription("Historic task description").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDescription("unexistingdescription").count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%task description").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("Historic task %").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%task%").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%unexistingdescripton%").count()).isEqualTo(0);

        // Execution id
        assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceId(finishedInstance.getId()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().executionId("unexistingexecution").count()).isEqualTo(0);

        // Process instance id
        assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceId(finishedInstance.getId()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceId("unexistingid").count()).isEqualTo(0);

        // Process instance business key
        assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKey("myBusinessKey").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKey("unexistingKey").count()).isEqualTo(0);

        // Process definition id
        assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionId(finishedInstance.getProcessDefinitionId()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionId("unexistingdefinitionid").count()).isEqualTo(0);

        // Process definition name
        assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionName("Historic task query test process").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionName("unexistingdefinitionname").count()).isEqualTo(0);

        // Process definition key
        assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionKey("HistoricTaskQueryTest").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionKey("unexistingdefinitionkey").count()).isEqualTo(0);

        // Process definition key in
        List<String> includeIds = new ArrayList<String>();
        assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionKeyIn(includeIds).count()).isEqualTo(1);
        includeIds.add("unexistingProcessDefinition");
        assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionKeyIn(includeIds).count()).isEqualTo(0);
        includeIds.add("HistoricTaskQueryTest");
        assertThat(historyService.createHistoricProcessInstanceQuery().processDefinitionKeyIn(includeIds).count()).isEqualTo(1);

        // Form key
        HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(finishedInstance.getId()).singleResult();
        assertThat(historicTask.getFormKey()).isEqualTo("testFormKey");

        // Assignee
        assertThat(historyService.createHistoricTaskInstanceQuery().taskAssignee("kermit").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskAssignee("johndoe").count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%ermit").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("kermi%").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%ermi%").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%johndoe%").count()).isEqualTo(0);

        // Delete reason
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDeleteReason("deleted").count()).isEqualTo(0);

        // Task definition ID
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("task").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("unexistingkey").count()).isEqualTo(0);

        // Task priority
        assertThat(historyService.createHistoricTaskInstanceQuery().taskPriority(1234).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskPriority(5678).count()).isEqualTo(0);

        assertThat(historyService.createHistoricTaskInstanceQuery().taskMinPriority(1234).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskMinPriority(1000).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskMinPriority(1300).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskMaxPriority(1234).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskMaxPriority(1300).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskMaxPriority(1000).count()).isEqualTo(0);

        // Due date
        Calendar anHourAgo = Calendar.getInstance();
        anHourAgo.setTime(dueDate);
        anHourAgo.add(Calendar.HOUR,
                      -1);

        Calendar anHourLater = Calendar.getInstance();
        anHourLater.setTime(dueDate);
        anHourLater.add(Calendar.HOUR,
                        1);

        assertThat(historyService.createHistoricTaskInstanceQuery().taskDueDate(dueDate).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDueDate(anHourAgo.getTime()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDueDate(anHourLater.getTime()).count()).isEqualTo(0);

        // Due date before
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDueBefore(anHourLater.getTime()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDueBefore(anHourAgo.getTime()).count()).isEqualTo(0);

        // Due date after
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDueAfter(anHourAgo.getTime()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDueAfter(anHourLater.getTime()).count()).isEqualTo(0);

        anHourAgo = new GregorianCalendar();
        anHourAgo.setTime(start.getTime());
        anHourAgo.add(Calendar.HOUR,
                      -1);

        anHourLater = Calendar.getInstance();
        anHourLater.setTime(start.getTime());
        anHourLater.add(Calendar.HOUR,
                        1);

        // Start date
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCreatedOn(start.getTime()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCreatedOn(anHourAgo.getTime()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCreatedAfter(anHourAgo.getTime()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCreatedAfter(anHourLater.getTime()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCreatedBefore(anHourAgo.getTime()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCreatedBefore(anHourLater.getTime()).count()).isEqualTo(1);

        // Completed date
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCompletedAfter(anHourAgo.getTime()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCompletedAfter(anHourLater.getTime()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCompletedBefore(anHourAgo.getTime()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskCompletedBefore(anHourLater.getTime()).count()).isEqualTo(1);

        // Filter based on identity-links Assignee is involved
        assertThat(historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("kermit").count()).isEqualTo(1);

        // Owner is involved
        assertThat(historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("fozzie").count()).isEqualTo(1);

        // Manually involved person
        assertThat(historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("gonzo").count()).isEqualTo(1);

        // Finished and Unfinished - Add anther other instance that has a running task (unfinished)
        runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");

        assertThat(historyService.createHistoricTaskInstanceQuery().finished().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().unfinished().count()).isEqualTo(1);
    }

    @Deployment
    public void testHistoricTaskInstanceOrQuery() throws Exception {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.MILLISECOND,
                  0);
        processEngineConfiguration.getClock().setCurrentTime(start.getTime());

        // First instance is finished
        ProcessInstance finishedInstance = runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest",
                                                                                    "myBusinessKey");
        processEngineConfiguration.getClock().reset();

        // Set priority to non-default value
        Task task = taskService.createTaskQuery().processInstanceId(finishedInstance.getId()).singleResult();
        task.setPriority(1234);
        task.setOwner("fozzie");
        Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 04:05:06");
        task.setDueDate(dueDate);

        taskService.saveTask(task);
        taskService.addUserIdentityLink(task.getId(),
                                        "gonzo",
                                        "someType");

        // Complete the task
        String taskId = task.getId();
        taskService.complete(taskId);

        // Task id
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskId(taskId).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskId(taskId).or().taskId(taskId).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskId("unexistingtaskid").count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskId("unexistingtaskid").endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskId(taskId).taskName("Clean up").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskId("unexistingtaskid").taskName("Clean up").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskId("unexistingtaskid").taskName("unexistingname").endOr().count()).isEqualTo(0);

        // Name
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskName("Clean up").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskName("unexistingname").endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskNameLike("Clean u%").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskNameLike("%unexistingname%").endOr().count()).isEqualTo(0);
        final List<String> taskNameList = new ArrayList<String>(1);
        taskNameList.add("Clean up");
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskNameIn(taskNameList).endOr().count()).isEqualTo(1);
        taskNameList.clear();
        taskNameList.add("unexistingname");
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskNameIn(taskNameList).endOr().count()).isEqualTo(0);
        taskNameList.clear();
        taskNameList.add("clean up");
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskNameInIgnoreCase(taskNameList).endOr().count()).isEqualTo(1);
        taskNameList.clear();
        taskNameList.add("unexistingname");
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskNameInIgnoreCase(taskNameList).endOr().count()).isEqualTo(0);

        taskNameList.clear();
        taskNameList.add("clean up");
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskName("Clean up").endOr().or().taskNameInIgnoreCase(taskNameList).endOr().count()).isEqualTo(1);
        taskNameList.clear();
        taskNameList.add("unexistingname");
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskName("Clean up").endOr().or().taskNameInIgnoreCase(taskNameList).endOr().count()).isEqualTo(0);

        // Description
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDescription("Historic task description").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDescription("unexistingdescription").endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLike("%task description").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLike("%task description").taskDescription("unexistingdescription").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%unexistingdescripton%").count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLike("%unexistingdescripton%").taskDescription("unexistingdescription").endOr().count()).isEqualTo(0);

        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDescription("Historic task description").endOr().or().taskDescriptionLike("%task description").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDescription("Historic task description").endOr().or().taskDescriptionLike("%task description2").endOr().count()).isEqualTo(0);

        // Execution id
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processInstanceId(finishedInstance.getId()).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().executionId("unexistingexecution").endOr().count()).isEqualTo(0);

        // Process instance id
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processInstanceId(finishedInstance.getId()).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processInstanceId("unexistingid").endOr().count()).isEqualTo(0);

        // Process instance business key
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processInstanceBusinessKey("myBusinessKey").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processInstanceBusinessKey("unexistingKey").endOr().count()).isEqualTo(0);

        // Process definition id
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processDefinitionId(finishedInstance.getProcessDefinitionId()).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processDefinitionId("unexistingdefinitionid").endOr().count()).isEqualTo(0);

        // Process definition name
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processDefinitionName("Historic task query test process").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processDefinitionName("unexistingdefinitionname").endOr().count()).isEqualTo(0);

        // Process definition key
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("HistoricTaskQueryTest").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("unexistingdefinitionkey").endOr().count()).isEqualTo(0);

        // Process definition key and ad hoc task
        Task adhocTask = taskService.newTask();
        taskService.saveTask(adhocTask);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskId(adhocTask.getId()).processDefinitionKey("unexistingdefinitionkey").endOr().count()).isEqualTo(1);
        taskService.deleteTask(adhocTask.getId(), true);

        // Process definition key in
        List<String> includeIds = new ArrayList<String>();
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("unexistingdefinitionkey").processDefinitionKeyIn(includeIds).endOr().count()).isEqualTo(0);
        includeIds.add("unexistingProcessDefinition");
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("unexistingdefinitionkey").processDefinitionKeyIn(includeIds).endOr().count()).isEqualTo(0);
        includeIds.add("unexistingProcessDefinition");
        assertThat(historyService.createHistoricTaskInstanceQuery().or().processDefinitionKey("HistoricTaskQueryTest").processDefinitionKeyIn(includeIds).endOr().count()).isEqualTo(1);
        includeIds.add("HistoricTaskQueryTest");
        assertThat(historyService.createHistoricProcessInstanceQuery().or().processDefinitionKey("unexistingdefinitionkey").processDefinitionKeyIn(includeIds).endOr().count()).isEqualTo(1);

        // Assignee
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskAssignee("kermit").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskAssignee("johndoe").endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskAssigneeLike("%ermit").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskAssigneeLike("%johndoe%").endOr().count()).isEqualTo(0);

        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskAssignee("kermit").endOr().or().taskAssigneeLike("%ermit").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskAssignee("kermit").endOr().or().taskAssigneeLike("%johndoe%").endOr().count()).isEqualTo(0);

        // Delete reason
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDeleteReason("deleted").endOr().count()).isEqualTo(0);

        // Task definition ID
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDefinitionKey("task").endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDefinitionKey("unexistingkey").endOr().count()).isEqualTo(0);

        // Task priority
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskPriority(1234).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskPriority(5678).endOr().count()).isEqualTo(0);

        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskMinPriority(1234).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskMinPriority(1000).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskMinPriority(1300).endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskMaxPriority(1234).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskMaxPriority(1300).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskMaxPriority(1000).endOr().count()).isEqualTo(0);

        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskPriority(1234).endOr().or().taskMinPriority(1234).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskPriority(1234).endOr().or().taskMinPriority(1300).endOr().count()).isEqualTo(0);

        // Due date
        Calendar anHourAgo = Calendar.getInstance();
        anHourAgo.setTime(dueDate);
        anHourAgo.add(Calendar.HOUR, -1);

        Calendar anHourLater = Calendar.getInstance();
        anHourLater.setTime(dueDate);
        anHourLater.add(Calendar.HOUR, 1);

        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDueDate(dueDate).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDueDate(anHourAgo.getTime()).endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDueDate(anHourLater.getTime()).endOr().count()).isEqualTo(0);

        // Due date before
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDueBefore(anHourLater.getTime()).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDueBefore(anHourAgo.getTime()).endOr().count()).isEqualTo(0);

        // Due date after
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDueAfter(anHourAgo.getTime()).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskDueAfter(anHourLater.getTime()).endOr().count()).isEqualTo(0);

        anHourAgo = new GregorianCalendar();
        anHourAgo.setTime(start.getTime());
        anHourAgo.add(Calendar.HOUR, -1);

        anHourLater = Calendar.getInstance();
        anHourLater.setTime(start.getTime());
        anHourLater.add(Calendar.HOUR, 1);

        // Start date
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCreatedOn(start.getTime()).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCreatedOn(anHourAgo.getTime()).endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCreatedAfter(anHourAgo.getTime()).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCreatedAfter(anHourLater.getTime()).endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCreatedBefore(anHourAgo.getTime()).endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCreatedBefore(anHourLater.getTime()).endOr().count()).isEqualTo(1);

        // Completed date
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCompletedAfter(anHourAgo.getTime()).endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCompletedAfter(anHourLater.getTime()).endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCompletedBefore(anHourAgo.getTime()).endOr().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskCompletedBefore(anHourLater.getTime()).endOr().count()).isEqualTo(1);

        // Filter based on identity-links
        // Assignee is involved
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskInvolvedUser("kermit").endOr().count()).isEqualTo(1);

        // Owner is involved
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskInvolvedUser("fozzie").endOr().count()).isEqualTo(1);

        // Manually involved person
        assertThat(historyService.createHistoricTaskInstanceQuery().or().taskInvolvedUser("gonzo").endOr().count()).isEqualTo(1);

        // Finished and Unfinished - Add anther other instance that has a
        // running task (unfinished)
        runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");

        assertThat(historyService.createHistoricTaskInstanceQuery().or().finished().endOr().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().or().unfinished().endOr().count()).isEqualTo(1);
    }

    @Deployment
    public void testHistoricTaskInstanceQueryProcessFinished() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TwoTaskHistoricTaskQueryTest");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

        // Running task on running process should be available
        assertThat(historyService.createHistoricTaskInstanceQuery().processUnfinished().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processFinished().count()).isEqualTo(0);

        // Finished and running task on running process should be available
        taskService.complete(task.getId());
        assertThat(historyService.createHistoricTaskInstanceQuery().processUnfinished().count()).isEqualTo(2);
        assertThat(historyService.createHistoricTaskInstanceQuery().processFinished().count()).isEqualTo(0);

        // 2 finished tasks are found for finished process after completing last
        // task of process
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
        assertThat(historyService.createHistoricTaskInstanceQuery().processUnfinished().count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().processFinished().count()).isEqualTo(2);
    }

    @Deployment
    public void testHistoricTaskInstanceQuerySorting() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");

        String taskId = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult().getId();
        taskService.complete(taskId);

        assertThat(historyService.createHistoricTaskInstanceQuery().orderByDeleteReason().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByExecutionId().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceId().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskCreateTime().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskName().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().asc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskId().asc().count()).isEqualTo(1);

        assertThat(historyService.createHistoricTaskInstanceQuery().orderByDeleteReason().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByExecutionId().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceId().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskCreateTime().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskName().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().desc().count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().orderByTaskId().desc().count()).isEqualTo(1);
    }

    @Deployment
    public void testHistoricIdentityLinksOnTask() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("historicIdentityLinks");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task).isNotNull();

        // Set additional identity-link not coming from process
        taskService.addUserIdentityLink(task.getId(),
                                        "gonzo",
                                        "customUseridentityLink");
        assertThat(taskService.getIdentityLinksForTask(task.getId()).size()).isEqualTo(4);

        // Check historic identity-links when task is still active
        List<HistoricIdentityLink> historicIdentityLinks = historyService.getHistoricIdentityLinksForTask(task.getId());
        assertThat(historicIdentityLinks).hasSize(4);

        // Validate all links
        boolean foundCandidateUser = false, foundCandidateGroup = false, foundAssignee = false, foundCustom = false;
        for (HistoricIdentityLink link : historicIdentityLinks) {
            assertThat(link.getTaskId()).isEqualTo(task.getId());
            if (link.getGroupId() != null) {
                assertThat(link.getGroupId()).isEqualTo("sales");
                foundCandidateGroup = true;
            } else {
                if (link.getType().equals("candidate")) {
                    assertThat(link.getUserId()).isEqualTo("fozzie");
                    foundCandidateUser = true;
                } else if (link.getType().equals("assignee")) {
                    assertThat(link.getUserId()).isEqualTo("kermit");
                    foundAssignee = true;
                } else if (link.getType().equals("customUseridentityLink")) {
                    assertThat(link.getUserId()).isEqualTo("gonzo");
                    foundCustom = true;
                }
            }
        }

        assertThat(foundAssignee).isTrue();
        assertThat(foundCandidateGroup).isTrue();
        assertThat(foundCandidateUser).isTrue();
        assertThat(foundCustom).isTrue();

        // Now complete the task and check if links are still there
        taskService.complete(task.getId());
        assertThat(historyService.getHistoricIdentityLinksForTask(task.getId())).hasSize(4);

        // After deleting historic task, exception should be thrown when trying to get links
        historyService.deleteHistoricTaskInstance(task.getId());

        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> historyService.getHistoricIdentityLinksForTask(task.getId()).size())
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(HistoricTaskInstance.class));
    }

    public void testInvalidSorting() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricTaskInstanceQuery().asc());

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricTaskInstanceQuery().desc());

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().list());
    }

    /**
     * Test to validate fix for ACT-1939: HistoryService loads invalid task local variables for completed task
     */
    @Deployment
    public void testVariableUpdateOrderHistoricTaskInstance() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("historicTask");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task).isNotNull();

        // Update task and process-variable 10 times
        for (int i = 0; i < 10; i++) {
            taskService.setVariableLocal(task.getId(), "taskVar", i);
            runtimeService.setVariable(task.getExecutionId(), "procVar", i);
        }

        taskService.complete(task.getId());

        // Check if all variables have the value for the latest revision
        HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).includeProcessVariables().singleResult();

        Object varValue = taskInstance.getProcessVariables().get("procVar");
        assertThat(varValue).isEqualTo(9);

        taskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).includeTaskLocalVariables().singleResult();

        varValue = taskInstance.getTaskLocalVariables().get("taskVar");
        assertThat(varValue).isEqualTo(9);
    }
}
