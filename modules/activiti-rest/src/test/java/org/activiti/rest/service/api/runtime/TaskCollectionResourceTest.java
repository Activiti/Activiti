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

package org.activiti.rest.service.api.runtime;

import java.util.Calendar;
import java.util.List;

import org.activiti.engine.impl.cmd.ChangeDeploymentTenantIdCmd;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * Test for all REST-operations related to the Task collection resource.
 * 
 * @author Frederik Heremans
 */
public class TaskCollectionResourceTest extends BaseRestTestCase {
  
  /**
   * Test creating a task.
   * POST runtime/tasks
   */
  public void testCreateTask() throws Exception {
    try {
      Task parentTask = taskService.newTask();
      taskService.saveTask(parentTask);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION));
      ObjectNode requestNode = objectMapper.createObjectNode();
      
      Calendar dueDate = Calendar.getInstance();
      String dueDateString = getISODateString(dueDate.getTime());
      
      requestNode.put("name", "New task name");
      requestNode.put("description", "New task description");
      requestNode.put("assignee", "assignee");
      requestNode.put("owner", "owner");
      requestNode.put("priority", 20);
      requestNode.put("delegationState", "resolved");
      requestNode.put("dueDate", dueDateString);
      requestNode.put("parentTaskId", parentTask.getId());
      
      // Execute the request
      Representation response = client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      String createdTaskId =  responseNode.get("id").asText();
      
      // Check if task is created with right arguments
      Task task = taskService.createTaskQuery().taskId(createdTaskId).singleResult();
      assertEquals("New task name", task.getName());
      assertEquals("New task description", task.getDescription());
      assertEquals("assignee", task.getAssignee());
      assertEquals("owner", task.getOwner());
      assertEquals(20, task.getPriority());
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
      assertEquals(dueDate.getTime(), task.getDueDate());
      assertEquals(parentTask.getId(), task.getParentTaskId());
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test creating a task.
   * POST runtime/tasks
   */
  public void testCreateTaskNoBody() throws Exception {
    try {
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION));
      
      try {
        client.post(null);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, expected.getStatus());
        assertEquals("A request body was expected when creating the task.", expected.getStatus().getDescription());
      }
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting a collection of tasks.
   * GET runtime/tasks
   */
  @Deployment
  public void testGetTasks() throws Exception {
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
      String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION);
      assertResultsPresentInDataResponse(url, adhocTask.getId(), processTask.getId());
      
      // Name filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?name=Name one";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Name like filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?nameLike=" + encode("%one");
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Description filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?description=Description one";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Description like filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?descriptionLike=" + encode("%one");
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Priority filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?priority=100";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Mininmum Priority filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?minimumPriority=70";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Maximum Priority filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?maximumPriority=70";
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Owner filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?owner=owner";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Assignee filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?assignee=gonzo";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Unassigned filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?unassigned=true";
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Delegation state filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?delegationState=pending";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Candidate user filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?candidateUser=kermit";
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Candidate group filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?candidateGroup=sales";
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Involved user filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?involvedUser=misspiggy";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Process instance filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?processInstanceId=" + processInstance.getId();
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Execution filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?executionId=" + processInstance.getId();
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Process instance businesskey filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?processInstanceBusinessKey=myBusinessKey";
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // CeatedOn filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?createdOn=" + getISODateString(adhocTaskCreate.getTime());
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // CreatedAfter filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?createdAfter=" + getISODateString(inBetweenTaskCreation.getTime());
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // CreatedBefore filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?createdBefore=" + getISODateString(inBetweenTaskCreation.getTime());
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Subtask exclusion
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?excludeSubTasks=true";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Task definition key filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?taskDefinitionKey=processTask";
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Task definition key like filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?taskDefinitionKeyLike=" + encode("process%");
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Duedate filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?dueDate=" + getISODateString(adhocTaskCreate.getTime());
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Due after filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?dueAfter=" + getISODateString(inBetweenTaskCreation.getTime());
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Due before filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?dueBefore=" + getISODateString(inBetweenTaskCreation.getTime());
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Without tenantId filtering before tenant set
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?withoutTenantId=true";
      assertResultsPresentInDataResponse(url, adhocTask.getId(), processTask.getId());

      // Set tenant on deployment
      managementService.executeCommand(new ChangeDeploymentTenantIdCmd(deploymentId, "myTenant"));
      
      // Without tenantId filtering after tenant set, only adhoc task should remain
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?withoutTenantId=true";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
      // Tenant id filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?tenantId=myTenant";
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Tenant id like filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?tenantIdLike=" + encode("%enant");
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Suspend process-instance to have a supended task
      runtimeService.suspendProcessInstanceById(processInstance.getId());
      
      // Suspended filering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?active=false";
      assertResultsPresentInDataResponse(url, processTask.getId());
      
      // Active filtering
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION) + "?active=true";
      assertResultsPresentInDataResponse(url, adhocTask.getId());
      
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
