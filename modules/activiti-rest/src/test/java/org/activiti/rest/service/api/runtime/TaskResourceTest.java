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
import org.activiti.engine.impl.cmd.ChangeDeploymentTenantIdCmd;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Test for all REST-operations related to a single Task resource.
 * 
 * @author Frederik Heremans
 */
public class TaskResourceTest extends BaseSpringRestTestCase {
  
  /**
   * Test getting a single task, spawned by a process.
   * GET runtime/tasks/{taskId}
   */
  @Deployment
  public void testGetProcessTask() throws Exception {
    Calendar now = Calendar.getInstance();
    processEngineConfiguration.getClock().setCurrentTime(now.getTime());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setDueDate(task.getId(), now.getTime());
    taskService.setOwner(task.getId(), "owner");
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    String url = buildUrl(RestUrls.URL_TASK, task.getId());
    CloseableHttpResponse response = executeRequest(new HttpGet(url), HttpStatus.SC_OK);
    
    // Check resulting task
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertEquals(task.getId(), responseNode.get("id").asText());
    assertEquals(task.getAssignee(), responseNode.get("assignee").asText());
    assertEquals(task.getOwner(), responseNode.get("owner").asText());
    assertEquals(task.getFormKey(), responseNode.get("formKey").asText());
    assertEquals(task.getDescription(), responseNode.get("description").asText());
    assertEquals(task.getName(), responseNode.get("name").asText());
    assertEquals(task.getDueDate(), getDateFromISOString(responseNode.get("dueDate").asText()));
    assertEquals(task.getCreateTime(), getDateFromISOString(responseNode.get("createTime").asText()));
    assertEquals(task.getPriority(), responseNode.get("priority").asInt());
    assertTrue(responseNode.get("parentTaskId").isNull());
    assertTrue(responseNode.get("delegationState").isNull());
    assertEquals("", responseNode.get("tenantId").textValue());
    
    assertTrue(responseNode.get("executionUrl").asText().equals(buildUrl(RestUrls.URL_EXECUTION, task.getExecutionId())));
    assertTrue(responseNode.get("processInstanceUrl").asText().equals(buildUrl(RestUrls.URL_PROCESS_INSTANCE, task.getProcessInstanceId())));
    assertTrue(responseNode.get("processDefinitionUrl").asText().equals(buildUrl(RestUrls.URL_PROCESS_DEFINITION, task.getProcessDefinitionId())));
    assertTrue(responseNode.get("url").asText().equals(url));
    
    // Set tenant on deployment
    managementService.executeCommand(new ChangeDeploymentTenantIdCmd(deploymentId, "myTenant"));
    
    response = executeRequest(new HttpGet(url), HttpStatus.SC_OK);
    
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertEquals("myTenant", responseNode.get("tenantId").asText());
  }
  
  /**
   * Test getting a single task, created using the API.
   * GET runtime/tasks/{taskId}
   */
  public void testGetProcessAdhoc() throws Exception {
    try {
      
      Calendar now = Calendar.getInstance();
      processEngineConfiguration.getClock().setCurrentTime(now.getTime());
      
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

      String url = buildUrl(RestUrls.URL_TASK, task.getId());
      CloseableHttpResponse response = executeRequest(new HttpGet(url), HttpStatus.SC_OK);
      
      // Check resulting task
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
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
      assertEquals("", responseNode.get("tenantId").textValue());
      
      assertTrue(responseNode.get("parentTaskUrl").asText().equals(buildUrl(RestUrls.URL_TASK, parentTask.getId())));
      assertTrue(responseNode.get("url").asText().equals(url));
      
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
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      
      // Execute the request with an empty request JSON-object
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPut, HttpStatus.SC_OK));
      
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
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPut, HttpStatus.SC_OK));
      
      task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
      assertEquals("New task name", task.getName());
      assertEquals("New task description", task.getDescription());
      assertEquals("assignee", task.getAssignee());
      assertEquals("owner", task.getOwner());
      assertEquals(20, task.getPriority());
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
      assertEquals(dateFormat.parse(dueDateString), task.getDueDate());
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
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Execute the request with an empty request JSON-object
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, "unexistingtask"));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_NOT_FOUND));
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
      
      // Execute the request
      HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
      
      task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
      assertNull(task);
      
      if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
        // Check that the historic task has not been deleted
        assertNotNull(historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult());
      }
      
      // 2. Cascade delete
      task = taskService.newTask();
      taskService.saveTask(task);
      taskId = task.getId();
      
      // Execute the request
      httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId) + "?cascadeHistory=true");
      closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
      
      task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
      assertNull(task);
      
      if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
        // Check that the historic task has been deleted
        assertNull(historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult());
      }
      
      // 3. Delete with reason
      task = taskService.newTask();
      taskService.saveTask(task);
      taskId = task.getId();
      
      // Execute the request
      httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId) + "?deleteReason=fortestingpurposes");
      closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
      
      task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
      assertNull(task);
      
      if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
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
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, "unexistingtask"));
    closeResponse(executeRequest(httpDelete, HttpStatus.SC_NOT_FOUND));
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
    
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
    closeResponse(executeRequest(httpDelete, HttpStatus.SC_FORBIDDEN));
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
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "complete");
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
      
      // Task shouldn't exist anymore
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNull(task);
     
      // Test completing with variables
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      taskId = task.getId();
      
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
      
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
      
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNull(task);
      
      if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        assertNotNull(historicTaskInstance);
        List<HistoricVariableInstance> updates =  historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list();
        assertNotNull(updates);
        assertEquals(2, updates.size());
        boolean foundFirst = false;
        boolean foundSecond = false;
        
        for (HistoricVariableInstance var : updates) {
          if (var.getVariableName().equals("var1")) {
            assertEquals("First value", var.getValue());
            foundFirst = true;
          } else if (var.getVariableName().equals("var2")) {
            assertEquals("Second value", var.getValue());
            foundSecond = true;
          }
        }
        
        assertTrue(foundFirst);
        assertTrue(foundSecond);
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
      
      task.setAssignee("fred");
      // Claiming without assignee should set asisgnee to null
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "claim");
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertNull(task.getAssignee());
      assertEquals(1L, taskService.createTaskQuery().taskCandidateUser("newAssignee").count());

      // Claim the task and check result
      requestNode.put("assignee", "newAssignee");
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("newAssignee", task.getAssignee());
      assertEquals(0L, taskService.createTaskQuery().taskCandidateUser("newAssignee").count());
      
      // Claiming with the same user shouldn't cause an exception
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("newAssignee", task.getAssignee());
      assertEquals(0L, taskService.createTaskQuery().taskCandidateUser("newAssignee").count());
      
      // Claiming with another user should cause exception
      requestNode.put("assignee", "anotherUser");
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_CONFLICT));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
      
      // Clean historic tasks with no runtime-counterpart
      List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery().list();
      for (HistoricTaskInstance task : historicTasks) {
        historyService.deleteHistoricTaskInstance(task.getId());
      }
    }
  }
  
  @Deployment
  public void testReclaimTask() throws Exception {
   
    // Start process instance
    runtimeService.startProcessInstanceByKey("reclaimTest");
    
    // Get task id
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION);
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + url), HttpStatus.SC_OK);
    JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
    closeResponse(response);
    String taskId = ((ArrayNode) dataNode).get(0).get("id").asText();
    assertNotNull(taskId);
    
    // Claim
    assertEquals(0L, taskService.createTaskQuery().taskAssignee("kermit").count());
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "claim");
    requestNode.put("assignee", "kermit");
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
    
    assertEquals(1L, taskService.createTaskQuery().taskAssignee("kermit").count());
    
    // Unclaim
    requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "claim");
    httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
    assertEquals(0L, taskService.createTaskQuery().taskAssignee("kermit").count());
    
    // Claim again
    requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "claim");
    requestNode.put("assignee", "kermit");
    httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
    assertEquals(1L, taskService.createTaskQuery().taskAssignee("kermit").count());
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
      
      // Delegating without assignee fails
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "delegate");
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));

      // Delegate the task and check result
      requestNode.put("assignee", "newAssignee");
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("newAssignee", task.getAssignee());
      assertEquals("initialAssignee", task.getOwner());
      assertEquals(DelegationState.PENDING, task.getDelegationState());
      
      // Delegating again shouldn't cause an exception and should delegate to user without affecting initial delegator (owner)
      requestNode.put("assignee", "anotherAssignee");
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("anotherAssignee", task.getAssignee());
      assertEquals("initialAssignee", task.getOwner());
      assertEquals(DelegationState.PENDING, task.getDelegationState());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
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
      
      // Resolve the task and check result
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "resolve");
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
      
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("initialAssignee", task.getAssignee());
      assertEquals("initialAssignee", task.getOwner());
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
      
      // Resolving again shouldn't cause an exception
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_OK));
      task = taskService.createTaskQuery().taskId(taskId).singleResult();
      assertNotNull(task);
      assertEquals("initialAssignee", task.getAssignee());
      assertEquals("initialAssignee", task.getOwner());
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
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
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "unexistingaction");
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, taskId));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
      
      // Clean historic tasks with no runtime-counterpart
      List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery().list();
      for (HistoricTaskInstance task : historicTasks) {
        historyService.deleteHistoricTaskInstance(task.getId());
      }
    }
  }
  
  /**
   * Test actions on an unexisting task.
   * POST runtime/tasks/{taskId}
   */
  public void testActionsUnexistingTask() throws Exception {
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "complete");
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, "unexisting"));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_NOT_FOUND));
    
    requestNode.put("action", "claim");
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_NOT_FOUND));
    
    requestNode.put("action", "delegate");
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_NOT_FOUND));
    
    requestNode.put("action", "resolve");
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_NOT_FOUND));
  }
}
