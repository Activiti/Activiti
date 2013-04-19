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

package org.activiti.rest.api.runtime;

import java.util.Calendar;
import java.util.List;

import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.activiti.rest.api.RestUrls;
import org.codehaus.jackson.node.ObjectNode;


/**
 * Test for all REST-operations related to the Task collection resource.
 * 
 * @author Frederik Heremans
 */
public class TaskQueryResourceTest extends BaseRestTestCase {
 
  /**
   * Test querying tasks.
   * GET runtime/tasks
   */
  @Deployment
  public void testQueryTasks() throws Exception {
    try {
      Calendar adhocTaskCreate = Calendar.getInstance();
      adhocTaskCreate.set(Calendar.MILLISECOND, 0);
      
      Calendar processTaskCreate = Calendar.getInstance();
      processTaskCreate.add(Calendar.HOUR, 2);
      processTaskCreate.set(Calendar.MILLISECOND, 0);
      
      Calendar inBetweenTaskCreation = Calendar.getInstance();
      inBetweenTaskCreation.add(Calendar.HOUR, 1);
      
      
      ClockUtil.setCurrentTime(adhocTaskCreate.getTime());
      Task adhocTask = taskService.newTask();
      adhocTask.setAssignee("gonzo");
      adhocTask.setOwner("owner");
      adhocTask.setDelegationState(DelegationState.PENDING);
      adhocTask.setDescription("Description one");
      adhocTask.setName("Name one");
      adhocTask.setDueDate(adhocTaskCreate.getTime());
      adhocTask.setPriority(100);
      taskService.saveTask(adhocTask);
      taskService.addUserIdentityLink(adhocTask.getId(), "misspiggy", IdentityLinkType.PARTICIPANT);
      
      ClockUtil.setCurrentTime(processTaskCreate.getTime());
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "myBusinessKey");
      Task processTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      processTask.setParentTaskId(adhocTask.getId());
      processTask.setPriority(50);
      processTask.setDueDate(processTaskCreate.getTime());
      taskService.saveTask(processTask);
      
      // Check filter-less to fetch all tasks
      String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_QUERY);
      ObjectNode requestNode = objectMapper.createObjectNode();
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId(), adhocTask.getId());
      
      // Name filtering
      requestNode.removeAll();
      requestNode.put("name", "Name one");
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Name like filtering
      requestNode.removeAll();
      requestNode.put("nameLike", "%one");
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());

      // Description filtering
      requestNode.removeAll();
      requestNode.put("description", "Description one");
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Description like filtering
      requestNode.removeAll();
      requestNode.put("descriptionLike", "%one");
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Priority filtering
      requestNode.removeAll();
      requestNode.put("priority", 100);
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Mininmum Priority filtering
      requestNode.removeAll();
      requestNode.put("minimumPriority", 70);
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Maximum Priority filtering
      requestNode.removeAll();
      requestNode.put("maximumPriority", 70);
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Owner filtering
      requestNode.removeAll();
      requestNode.put("owner", "owner");
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Assignee filtering
      requestNode.removeAll();
      requestNode.put("assignee", "gonzo");
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Unassigned filtering
      requestNode.removeAll();
      requestNode.put("unassigned", true);
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Delegation state filtering
      requestNode.removeAll();
      requestNode.put("delegationState", "pending");
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Candidate user filtering
      requestNode.removeAll();
      requestNode.put("candidateUser", "kermit");
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Candidate group filtering
      requestNode.removeAll();
      requestNode.put("candidateGroup", "sales");
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Involved user filtering
      requestNode.removeAll();
      requestNode.put("involvedUser", "misspiggy");
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Process instance filtering
      requestNode.removeAll();
      requestNode.put("processInstanceId", processInstance.getId());
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Execution filtering
      requestNode.removeAll();
      requestNode.put("executionId", processInstance.getId());
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Process instance businesskey filtering
      requestNode.removeAll();
      requestNode.put("processInstanceBusinessKey", "myBusinessKey");
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // CeatedOn filtering
      requestNode.removeAll();
      requestNode.put("createdOn", getISODateString(adhocTaskCreate.getTime()));
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // CreatedAfter filtering
      requestNode.removeAll();
      requestNode.put("createdAfter", getISODateString(inBetweenTaskCreation.getTime()));
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // CreatedBefore filtering
      requestNode.removeAll();
      requestNode.put("createdBefore", getISODateString(inBetweenTaskCreation.getTime()));
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Subtask exclusion
      requestNode.removeAll();
      requestNode.put("excludeSubTasks", true);
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Task definition key filtering
      requestNode.removeAll();
      requestNode.put("taskDefinitionKey", "processTask");
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Task definition key like filtering
      requestNode.removeAll();
      requestNode.put("taskDefinitionKeyLike", "process%");
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Duedate filtering
      requestNode.removeAll();
      requestNode.put("dueDate", getISODateString(adhocTaskCreate.getTime()));
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Due after filtering
      requestNode.removeAll();
      requestNode.put("dueAfter", getISODateString(inBetweenTaskCreation.getTime()));
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Due before filtering
      requestNode.removeAll();
      requestNode.put("dueBefore", getISODateString(inBetweenTaskCreation.getTime()));
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
      // Suspend process-instance to have a supended task
      runtimeService.suspendProcessInstanceById(processInstance.getId());
      
      // Suspended filering
      requestNode.removeAll();
      requestNode.put("active", false);
      assertResultsPresentInDataResponse(url, requestNode, processTask.getId());
      
      // Active filtering
      requestNode.removeAll();
      requestNode.put("active", true);
      assertResultsPresentInDataResponse(url, requestNode, adhocTask.getId());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        if(task.getExecutionId() == null) {
          taskService.deleteTask(task.getId(), true);
        }
      }
    }
  }
}
