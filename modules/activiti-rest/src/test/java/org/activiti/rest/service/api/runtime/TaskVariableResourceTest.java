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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.HttpMultipartHelper;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to a single task variable.
 * 
 * @author Frederik Heremans
 */
public class TaskVariableResourceTest extends BaseSpringRestTestCase {

  /**
   * Test getting a task variable. GET
   * runtime/tasks/{taskId}/variables/{variableName}
   */
  @Deployment
  public void testGetTaskVariable() throws Exception {
    try {
      // Test variable behaviour on standalone tasks
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setVariableLocal(task.getId(), "localTaskVariable", "localValue");

      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, task.getId(), "localTaskVariable")), HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("local", responseNode.get("scope").asText());
      assertEquals("localValue", responseNode.get("value").asText());
      assertEquals("localTaskVariable", responseNode.get("name").asText());
      assertEquals("string", responseNode.get("type").asText());
      

      // Test variable behaviour for a process-task
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", Collections.singletonMap("sharedVariable", (Object) "processValue"));
      Task processTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      
      taskService.setVariableLocal(processTask.getId(), "sharedVariable", "taskValue");
      
      // ANY scope, local should get precedence
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, processTask.getId(), "sharedVariable")), HttpStatus.SC_OK);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("local", responseNode.get("scope").asText());
      assertEquals("taskValue", responseNode.get("value").asText());
      assertEquals("sharedVariable", responseNode.get("name").asText());
      assertEquals("string", responseNode.get("type").asText());
      
      // LOCAL scope
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, processTask.getId(), "sharedVariable") + "?scope=local"), HttpStatus.SC_OK);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("local", responseNode.get("scope").asText());
      assertEquals("taskValue", responseNode.get("value").asText());
      assertEquals("sharedVariable", responseNode.get("name").asText());
      assertEquals("string", responseNode.get("type").asText());
      
      // GLOBAL scope
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, processTask.getId(), "sharedVariable") + "?scope=global"), HttpStatus.SC_OK);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("global", responseNode.get("scope").asText());
      assertEquals("processValue", responseNode.get("value").asText());
      assertEquals("sharedVariable", responseNode.get("name").asText());
      assertEquals("string", responseNode.get("type").asText());
      
      // Illegal scope
      closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, processTask.getId(), "sharedVariable") + "?scope=illegal"), HttpStatus.SC_BAD_REQUEST));
      
      // Unexisting task
      closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, "unexisting", "sharedVariable")), HttpStatus.SC_NOT_FOUND));
      
      // Unexisting variable
      closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, processTask.getId(), "unexistingVariable")), HttpStatus.SC_NOT_FOUND));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        if(task.getExecutionId() == null) {
          taskService.deleteTask(task.getId(), true);
        }
      }
    }
  }
  
  /**
   * Test getting a task variable. GET
   * runtime/tasks/{taskId}/variables/{variableName}/data
   */
  public void testGetTaskVariableData() throws Exception {
    try {
      // Test variable behaviour on standalone tasks
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setVariableLocal(task.getId(), "localTaskVariable", "This is a binary piece of text".getBytes());

      // Force content-type to TEXT_PLAIN to make sure this is ignored and application-octect-stream is always returned
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, task.getId(), "localTaskVariable")), HttpStatus.SC_OK);
      
      String actualResponseBytesAsText = IOUtils.toString(response.getEntity().getContent());
      closeResponse(response);
      assertEquals("This is a binary piece of text", actualResponseBytesAsText);
      assertEquals("application/octet-stream", response.getEntity().getContentType().getValue());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting a task variable. GET
   * runtime/tasks/{taskId}/variables/{variableName}/data
   */
  public void testGetTaskVariableDataSerializable() throws Exception {
    try {
      TestSerializableVariable originalSerializable = new TestSerializableVariable();
      originalSerializable.setSomeField("This is some field");
      
      // Test variable behaviour on standalone tasks
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setVariableLocal(task.getId(), "localTaskVariable", originalSerializable);

      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, task.getId(), "localTaskVariable")), HttpStatus.SC_OK);
      
      // Read the serializable from the stream
      ObjectInputStream stream = new ObjectInputStream(response.getEntity().getContent());
      Object readSerializable = stream.readObject();
      assertNotNull(readSerializable);
      assertTrue(readSerializable instanceof TestSerializableVariable);
      assertEquals("This is some field", ((TestSerializableVariable) readSerializable).getSomeField());
      assertEquals("application/x-java-serialized-object", response.getEntity().getContentType().getValue());
      closeResponse(response);
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting a task variable. GET
   * runtime/tasks/{taskId}/variables/{variableName}/data
   */
  public void testGetTaskVariableDataForIllegalVariables() throws Exception {
    try {
      // Test variable behaviour on standalone tasks
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setVariableLocal(task.getId(), "localTaskVariable", "this is a plain string variable");

      // Try getting data for non-binary variable
      closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, task.getId(), "localTaskVariable")), HttpStatus.SC_NOT_FOUND));
      
      // Try getting data for unexisting property
      closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, task.getId(), "unexistingVariable")), HttpStatus.SC_NOT_FOUND));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test deleting a single task variable in all scopes, including "not found" check.
   * 
   * DELETE runtime/tasks/{taskId}/variables/{variableName}
   */
  @Deployment
  public void testDeleteTaskVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", 
            Collections.singletonMap("overlappingVariable", (Object) "processValue"));
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "overlappingVariable", "taskValue");
    taskService.setVariableLocal(task.getId(), "anotherTaskVariable", "taskValue");
    
    // Delete variable without scope, local should be presumed -> local removed and global should be retained
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, task.getId(), "overlappingVariable"));
    closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
    
    assertFalse(taskService.hasVariableLocal(task.getId(), "overlappingVariable"));
    assertTrue(taskService.hasVariable(task.getId(), "overlappingVariable"));
    
    // Delete local scope variable
    httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, task.getId(), "anotherTaskVariable") + "?scope=local");
    closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
    
    assertFalse(taskService.hasVariableLocal(task.getId(), "anotherTaskVariable"));
    
    // Delete global scope variable
    assertTrue(taskService.hasVariable(task.getId(), "overlappingVariable"));
    httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, task.getId(), "overlappingVariable") + "?scope=global");
    closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
    
    assertFalse(taskService.hasVariable(task.getId(), "overlappingVariable"));

    // Run the same delete again, variable is not there so 404 should be returned
    closeResponse(executeRequest(httpDelete, HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test updating a single task variable in all scopes, including "not found" check.
   * 
   * PUT runtime/tasks/{taskId}/variables/{variableName}
   */
  @Deployment
  public void testUpdateTaskVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", 
            Collections.singletonMap("overlappingVariable", (Object) "processValue"));
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "overlappingVariable", "taskValue");
    
    // Update variable without scope, local should be presumed -> local updated and global should be retained
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("name", "overlappingVariable");
    requestNode.put("value", "updatedLocalValue");
    requestNode.put("type", "string");
    
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, task.getId(), "overlappingVariable"));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("updatedLocalValue", responseNode.get("value").asText());
    assertEquals("local", responseNode.get("scope").asText());
    // Check local value is changed in engine and global one remains unchanged
    assertEquals("updatedLocalValue", taskService.getVariableLocal(task.getId(), "overlappingVariable"));
    assertEquals("processValue", runtimeService.getVariable(task.getExecutionId(), "overlappingVariable"));
    
    
    // Update variable in local scope
    requestNode = objectMapper.createObjectNode();
    requestNode.put("name", "overlappingVariable");
    requestNode.put("value", "updatedLocalValueOnceAgain");
    requestNode.put("type", "string");
    requestNode.put("scope", "local");
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpPut, HttpStatus.SC_OK);
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("updatedLocalValueOnceAgain", responseNode.get("value").asText());
    assertEquals("local", responseNode.get("scope").asText());
    // Check local value is changed in engine and global one remains unchanged
    assertEquals("updatedLocalValueOnceAgain", taskService.getVariableLocal(task.getId(), "overlappingVariable"));
    assertEquals("processValue", runtimeService.getVariable(task.getExecutionId(), "overlappingVariable"));
    
    
    // Update variable in global scope
    requestNode = objectMapper.createObjectNode();
    requestNode.put("name", "overlappingVariable");
    requestNode.put("value", "updatedInGlobalScope");
    requestNode.put("type", "string");
    requestNode.put("scope", "global");
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpPut, HttpStatus.SC_OK);
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("updatedInGlobalScope", responseNode.get("value").asText());
    assertEquals("global", responseNode.get("scope").asText());
    // Check global value is changed in engine and local one remains unchanged
    assertEquals("updatedLocalValueOnceAgain", taskService.getVariableLocal(task.getId(), "overlappingVariable"));
    assertEquals("updatedInGlobalScope", runtimeService.getVariable(task.getExecutionId(), "overlappingVariable"));
    
    // Try updating with mismatch between URL and body variableName unexisting property
    requestNode.put("name", "unexistingVariable");
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_BAD_REQUEST));
    
    // Try updating unexisting property
    requestNode.put("name", "unexistingVariable");
    httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, task.getId(), "unexistingVariable"));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test updating a single task variable using a binary stream.
   * PUT runtime/tasks/{taskId}/variables/{variableName}
   */
  public void testUpdateBinaryTaskVariable() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setVariable(task.getId(), "binaryVariable", "Original value".getBytes());
      
      InputStream binaryContent = new ByteArrayInputStream("This is binary content".getBytes()); 
      
      // Add name, type and scope
      Map<String, String> additionalFields = new HashMap<String, String>();
      additionalFields.put("name", "binaryVariable");
      additionalFields.put("type", "binary");
      additionalFields.put("scope", "local");
      
      // Upload a valid BPMN-file using multipart-data
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, task.getId(), "binaryVariable"));
      httpPut.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/octet-stream", binaryContent, additionalFields));
      CloseableHttpResponse response = executeBinaryRequest(httpPut, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("binaryVariable", responseNode.get("name").asText());
      assertTrue(responseNode.get("value").isNull());
      assertEquals("local", responseNode.get("scope").asText());
      assertEquals("binary", responseNode.get("type").asText());
      assertNotNull(responseNode.get("valueUrl").isNull());
      assertTrue(responseNode.get("valueUrl").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, task.getId(), "binaryVariable")));
      
      // Check actual value of variable in engine
      Object variableValue = taskService.getVariableLocal(task.getId(), "binaryVariable");
      assertNotNull(variableValue);
      assertTrue(variableValue instanceof byte[]);
      assertEquals("This is binary content", new String((byte[])variableValue));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
}
