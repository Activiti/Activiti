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

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * Test for all REST-operations related to a single Task resource.
 * 
 * @author Frederik Heremans
 */
public class TaskResourceTest extends BaseRestTestCase {
  
  /**
   * Test getting a single task, spawned by a process.
   * GET runtime/tasks/{taskId}
   */
  @Deployment
  public void testGetProcessTask() throws Exception {
    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setDueDate(task.getId(), now.getTime());
    taskService.setOwner(task.getId(), "owner");
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    // Check resulting task
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertEquals(task.getId(), responseNode.get("id").asText());
    assertEquals(task.getAssignee(), responseNode.get("assignee").asText());
    assertEquals(task.getOwner(), responseNode.get("owner").asText());
    assertEquals(task.getDescription(), responseNode.get("description").asText());
    assertEquals(task.getName(), responseNode.get("name").asText());
    assertEquals(task.getDueDate(), getDateFromISOString(responseNode.get("dueDate").asText()));
    assertEquals(task.getCreateTime(), getDateFromISOString(responseNode.get("createTime").asText()));
    assertEquals(task.getPriority(), responseNode.get("priority").asInt());
    assertTrue(responseNode.get("parentTaskId").isNull());
    assertTrue(responseNode.get("delegationState").isNull());
    
    assertTrue(responseNode.get("executionUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, task.getExecutionId())));
    assertTrue(responseNode.get("processInstanceUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, task.getProcessInstanceId())));
    assertTrue(responseNode.get("processDefinitionUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, encode(task.getProcessDefinitionId()))));
  }
  
  /**
   * Test getting a single task, created using the API.
   * GET runtime/tasks/{taskId}
   */
  public void testGetProcessAdhoc() throws Exception {
    try {
      
      Calendar now = Calendar.getInstance();
      ClockUtil.setCurrentTime(now.getTime());
      
      Task parentTask = taskService.newTask();
      taskService.saveTask(parentTask);
      
      Task task = taskService.newTask();
      task.setParentTaskId(parentTask.getId());
      task.setName("Task name");
      task.setDescription("Descriptions");
      task.setAssignee("kermit");
      task.setDelegationState(DelegationState.RESOLVED);
      task.setDescription("Description");
      task.setDueDate(now.getTime());
      task.setOwner("owner");
      task.setPriority(20);
      taskService.saveTask(task);

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      // Check resulting task
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertEquals(task.getId(), responseNode.get("id").asText());
      assertEquals(task.getAssignee(), responseNode.get("assignee").asText());
      assertEquals(task.getOwner(), responseNode.get("owner").asText());
      assertEquals(task.getDescription(), responseNode.get("description").asText());
      assertEquals(task.getName(), responseNode.get("name").asText());
      assertEquals(task.getDueDate(), getDateFromISOString(responseNode.get("dueDate").asText()));
      assertEquals(task.getCreateTime(), getDateFromISOString(responseNode.get("createTime").asText()));
      assertEquals(task.getPriority(), responseNode.get("priority").asInt());
      assertEquals("resolved", responseNode.get("delegationState").asText());
      assertTrue(responseNode.get("executionId").isNull());
      assertTrue(responseNode.get("processInstanceId").isNull());
      assertTrue(responseNode.get("processDefinitionId").isNull());
      
      assertTrue(responseNode.get("parentTaskUrl").asText().endsWith(
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, parentTask.getId())));
      
    } finally {
      
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
 
  /**
   * Test updating a single task without passing in any value, no values should be altered.
   * PUT runtime/tasks/{taskId}
   */
  public void testUpdateTaskNoOverrides() throws Exception {
    try {
      Calendar now = Calendar.getInstance();
      Task parentTask = taskService.newTask();
      taskService.saveTask(parentTask);
      
      Task task = taskService.newTask();
      task.setParentTaskId(parentTask.getId());
      task.setName("Task name");
      task.setDescription("Description");
      task.setAssignee("kermit");
      task.setDelegationState(DelegationState.RESOLVED);
      task.setDescription("Description");
      task.setDueDate(now.getTime());
      task.setOwner("owner");
      task.setPriority(20);
      taskService.saveTask(task);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
      ObjectNode requestNode = objectMapper.createObjectNode();
      
      // Execute the request with an empty request JSON-object
      client.put(requestNode);
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
      assertEquals("Task name", task.getName());
      assertEquals("Description", task.getDescription());
      assertEquals("kermit", task.getAssignee());
      assertEquals("owner", task.getOwner());
      assertEquals(20, task.getPriority());
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
      assertEquals(now.getTime(), task.getDueDate());
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
   * Test updating a single task.
   * PUT runtime/tasks/{taskId}
   */
  public void testUpdateTask() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      
      Task parentTask = taskService.newTask();
      taskService.saveTask(parentTask);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
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
      client.put(requestNode);
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      
      task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
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
   * Test updating an unexisting task.
   * PUT runtime/tasks/{taskId}
   */
  public void testUpdateUnexistingTask() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, "unexistingtask"));
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Execute the request with an empty request JSON-object
    try {
      client.put(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a task with id 'unexistingtask'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test deleting a single task.
   * DELETE runtime/tasks/{taskId}
   */
  public void testDeleteTask() throws Exception {
    try {
      
      // 1. Simple delete
      Task task = taskService.newTask();
      taskService.saveTask(task);
      String taskId = task.getId();
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      
      // Execute the request
      client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      
      task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
      assertNull(task);
      
      if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
        // Check that the historic task has not been deleted
        assertNotNull(historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult());
      }
      
      // 2. Cascade delete
      task = taskService.newTask();
      taskService.saveTask(task);
      taskId = task.getId();
      
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId) + "?cascadeHistory=true");
      
      // Execute the request
      client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      
      task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
      assertNull(task);
      
      if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
        // Check that the historic task has been deleted
        assertNull(historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult());
      }
      
      // 3. Delete with reason
      task = taskService.newTask();
      taskService.saveTask(task);
      taskId = task.getId();
      
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId) + "?deleteReason=fortestingpurposes");
      
      // Execute the request
      client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      
      task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
      assertNull(task);
      
      if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
        // Check that the historic task has been deleted and delete-reason has been set
        HistoricTaskInstance instance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult(); 
        assertNotNull(instance);
        assertEquals("fortestingpurposes", instance.getDeleteReason());
      }
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
      
      // Clean historic tasks with no runtime-counterpart
      List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery().list();
      for(HistoricTaskInstance task : historicTasks) {
        historyService.deleteHistoricTaskInstance(task.getId());
      }
    }
  }
  
  /**
   * Test updating an unexisting task.
   * PUT runtime/tasks/{taskId}
   */
  public void testDeleteUnexistingTask() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, "unexistingtask"));
    
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a task with id 'unexistingtask'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test updating a task that is part of a process.
   * PUT runtime/tasks/{taskId}
   */
  @Deployment
  public void testDeleteTaskInProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
    
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_FORBIDDEN, expected.getStatus());
      assertEquals("Cannot delete a task that is part of a process-instance.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test completing a single task.
   * POST runtime/tasks/{taskId}
   */
  @Deployment
  public void testCompleteTask() throws Exception {
    try {
      
      Task task = taskService.newTask();
      taskService.saveTask(task);
      String taskId = task.getId();
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "complete");
      client.post(requestNode);
      
      // Task shouldn't exist anymore
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNull(task);
     
      // Test completing with variables
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      taskId = task.getId();
      
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      requestNode = objectMapper.createObjectNode();
      ArrayNode variablesNode = objectMapper.createArrayNode();
      requestNode.put("action", "complete");
      requestNode.put("variables", variablesNode);
      
      ObjectNode var1 = objectMapper.createObjectNode();
      variablesNode.add(var1);
      var1.put("name", "var1");
      var1.put("value", "First value");
      ObjectNode var2 = objectMapper.createObjectNode();
      variablesNode.add(var2);
      var2.put("name", "var2");
      var2.put("value", "Second value");
      
      client.post(requestNode);
      
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNull(task);
      
      if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        assertNotNull(historicTaskInstance);
        List<HistoricVariableInstance> updates =  historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list();
        assertNotNull(updates);
        assertEquals(2, updates.size());
        boolean foundFirst = false;
        boolean foundSecond = false;
        for(HistoricVariableInstance var : updates) {
          if(var.getVariableName().equals("var1")) {
            assertEquals("First value", var.getValue());
            foundFirst = true;
          } else if(var.getVariableName().equals("var2")) {
            assertEquals("Second value", var.getValue());
            foundSecond = true;
          }
        }
        
        assertTrue(foundFirst);
        assertTrue(foundSecond);
      }
      
      
    }
    finally
    {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
      
      // Clean historic tasks with no runtime-counterpart
      List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery().list();
      for(HistoricTaskInstance task : historicTasks) {
        historyService.deleteHistoricTaskInstance(task.getId());
      }
    }
  }
  
  /**
   * Test claiming a single task and all exceptional cases related to claiming.
   * POST runtime/tasks/{taskId}
   */
  public void testClaimTask() throws Exception {
    try {
      
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.addCandidateUser(task.getId(), "newAssignee");
      
      assertEquals(1L, taskService.createTaskQuery().taskCandidateUser("newAssignee").count());
      // Add candidate group
      String taskId = task.getId();
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      
      // Claiming without assignee fails
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "claim");
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("An assignee is required when claiming a task.", expected.getStatus().getDescription());
      }

      // Claim the task and check result
      requestNode.put("assignee", "newAssignee");
      client.post(requestNode);
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("newAssignee", task.getAssignee());
      assertEquals(0L, taskService.createTaskQuery().taskCandidateUser("newAssignee").count());
      
      // Claiming with the same user shouldn't cause an exception
      client.post(requestNode);
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("newAssignee", task.getAssignee());
      assertEquals(0L, taskService.createTaskQuery().taskCandidateUser("newAssignee").count());
      
      // Claiming with another user should cause exception
      requestNode.put("assignee", "anotherUser");
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
        assertEquals("Task '" + task.getId() + "' is already claimed by someone else.", expected.getStatus().getDescription());
      }
      
    }
    finally
    {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
      
      // Clean historic tasks with no runtime-counterpart
      List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery().list();
      for(HistoricTaskInstance task : historicTasks) {
        historyService.deleteHistoricTaskInstance(task.getId());
      }
    }
  }
  
  /**
   * Test delegating a single task and all exceptional cases related to delegation.
   * POST runtime/tasks/{taskId}
   */
  public void testDelegateTask() throws Exception {
    try {
      
      Task task = taskService.newTask();
      task.setAssignee("initialAssignee");
      taskService.saveTask(task);
      String taskId = task.getId();
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      
      // Delegating without assignee fails
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "delegate");
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("An assignee is required when delegating a task.", expected.getStatus().getDescription());
      }

      // Delegate the task and check result
      requestNode.put("assignee", "newAssignee");
      client.post(requestNode);
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("newAssignee", task.getAssignee());
      assertEquals("initialAssignee", task.getOwner());
      assertEquals(DelegationState.PENDING, task.getDelegationState());
      
      // Delegating again shouldn't cause an exception and should delegate to user without affecting initial delegator (owner)
      requestNode.put("assignee", "anotherAssignee");
      client.post(requestNode);
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("anotherAssignee", task.getAssignee());
      assertEquals("initialAssignee", task.getOwner());
      assertEquals(DelegationState.PENDING, task.getDelegationState());
    }
    finally
    {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test resolving a single task and all exceptional cases related to resolution.
   * POST runtime/tasks/{taskId}
   */
  public void testResolveTask() throws Exception {
    try {
      
      Task task = taskService.newTask();
      task.setAssignee("initialAssignee");
      taskService.saveTask(task);
      taskService.delegateTask(task.getId(), "anotherUser");
      String taskId = task.getId();
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      
      // Resolve the task and check result
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "resolve");
      client.post(requestNode);
      
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("initialAssignee", task.getAssignee());
      assertEquals("initialAssignee", task.getOwner());
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
      
      // Resolving again shouldn't cause an exception
      client.post(requestNode);
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("initialAssignee", task.getAssignee());
      assertEquals("initialAssignee", task.getOwner());
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
    }
    finally
    {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test executing an invalid action on a single task.
   * POST runtime/tasks/{taskId}
   */
  public void testInvalidTaskAction() throws Exception {
    try {
      
      Task task = taskService.newTask();
      taskService.saveTask(task);
      String taskId = task.getId();
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "unexistingaction");
      
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Invalid action: 'unexistingaction'.", expected.getStatus().getDescription());
      }
    }
    finally
    {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
      
      // Clean historic tasks with no runtime-counterpart
      List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery().list();
      for(HistoricTaskInstance task : historicTasks) {
        historyService.deleteHistoricTaskInstance(task.getId());
      }
    }
  }
  
  /**
   * Test actions on an unexisting task.
   * POST runtime/tasks/{taskId}
   */
  public void testActionsUnexistingTask() throws Exception {
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, "unexisting"));
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "complete");
      
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexisting'.", expected.getStatus().getDescription());
      }
      
      requestNode.put("action", "claim");
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexisting'.", expected.getStatus().getDescription());
      }
      
      requestNode.put("action", "delegate");
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexisting'.", expected.getStatus().getDescription());
      }
      
      requestNode.put("action", "resolve");
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexisting'.", expected.getStatus().getDescription());
      }
  }
}
