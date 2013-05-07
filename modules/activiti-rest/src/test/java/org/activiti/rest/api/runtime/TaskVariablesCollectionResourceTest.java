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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.activiti.rest.HttpMultipartRepresentation;
import org.activiti.rest.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * Test for all REST-operations related to Task variables.
 * 
 * @author Frederik Heremans
 */
public class TaskVariablesCollectionResourceTest extends BaseRestTestCase {
  
  /**
   * Test getting all task variables.
   * GET runtime/tasks/{taskId}/variables
   */
  @Deployment
  public void testGetTaskVariables() throws Exception {
   
    Calendar cal = Calendar.getInstance();
    
    // Start process with all types of variables
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringProcVar", "This is a ProcVariable");
    processVariables.put("intProcVar", 123);
    processVariables.put("longProcVar", 1234L);
    processVariables.put("shortProcVar", (short) 123);
    processVariables.put("doubleProcVar", 99.99);
    processVariables.put("booleanProcVar", Boolean.TRUE);
    processVariables.put("dateProcVar", cal.getTime());
    processVariables.put("byteArrayProcVar", "Some raw bytes".getBytes());
    processVariables.put("overlappingVariable", "process-value");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    
    // Set local task variables, including one that has the same name as one that is defined in the parent scope (process instance)
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Map<String, Object> taskVariables = new HashMap<String, Object>();
    taskVariables.put("stringTaskVar", "This is a TaskVariable");
    taskVariables.put("intTaskVar", 123);
    taskVariables.put("longTaskVar", 1234L);
    taskVariables.put("shortTaskVar", (short) 123);
    taskVariables.put("doubleTaskVar", 99.99);
    taskVariables.put("booleanTaskVar", Boolean.TRUE);
    taskVariables.put("dateTaskVar", cal.getTime());
    taskVariables.put("byteArrayTaskVar", "Some raw bytes".getBytes());
    taskVariables.put("overlappingVariable", "task-value");
    taskService.setVariablesLocal(task.getId(), taskVariables);

    // Request all variables (no scope provides) which include global an local
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(17, responseNode.size());
    
    // Overlapping variable should contain task-value AND be defined as "local"
    boolean foundOverlapping = false;
    for(int i=0; i< responseNode.size(); i++) {
      JsonNode var = responseNode.get(i);
      if(var.get("name") != null && "overlappingVariable".equals(var.get("name").asText())) {
        foundOverlapping = true;
        assertEquals("task-value", var.get("value").asText());
        assertEquals("local", var.get("scope").asText());
        break;
      }
    }
    assertTrue(foundOverlapping);
    
    // Check local variables filering
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()) + "?scope=local");
    response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(9, responseNode.size());
    
    for(int i=0; i< responseNode.size(); i++) {
      JsonNode var = responseNode.get(i);
      assertEquals("local", var.get("scope").asText());
    }
    
    // Check global variables filering
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()) + "?scope=global");
    response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(9, responseNode.size());
    
    foundOverlapping = false;
    for(int i=0; i< responseNode.size(); i++) {
      JsonNode var = responseNode.get(i);
      assertEquals("global", var.get("scope").asText());
      if("overlappingVariable".equals(var.get("name").asText())) {
        foundOverlapping = true;
        assertEquals("process-value", var.get("value").asText());
      }
    }
    assertTrue(foundOverlapping);
  }
  
  /**
   * Test creating a single task variable.
   * POST runtime/tasks/{taskId}/variables
   */
  @Deployment
  public void testCreateSingleTaskVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("name", "myVariable");
    requestNode.put("value", "simple string value");
    requestNode.put("scope", "local");
    requestNode.put("type", "string");
            
    // Create a new local variable
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    Representation response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("myVariable", responseNode.get("name").asText());
    assertEquals("simple string value", responseNode.get("value").asText());
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("string", responseNode.get("type").asText());
    assertNull(responseNode.get("valueUrl"));
    
    assertTrue(taskService.hasVariableLocal(task.getId(), "myVariable"));
    assertEquals("simple string value", taskService.getVariableLocal(task.getId(), "myVariable"));
    response.release();
    
    // Create a new global variable
    requestNode.put("name", "myVariable");
    requestNode.put("value", "Another simple string value");
    requestNode.put("scope", "global");
    requestNode.put("type", "string");
    
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("myVariable", responseNode.get("name").asText());
    assertEquals("Another simple string value", responseNode.get("value").asText());
    assertEquals("global", responseNode.get("scope").asText());
    assertEquals("string", responseNode.get("type").asText());
    assertNull(responseNode.get("valueUrl"));
    
    assertTrue(runtimeService.hasVariable(task.getExecutionId(), "myVariable"));
    assertEquals("Another simple string value", runtimeService.getVariableLocal(task.getExecutionId(), "myVariable"));
    
            
    // Create a new scope-less variable, which defaults to local variables
    requestNode = objectMapper.createObjectNode();
    requestNode.put("name", "scopelessVariable");
    requestNode.put("value", "simple string value");
    requestNode.put("type", "string");
    
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("scopelessVariable", responseNode.get("name").asText());
    assertEquals("simple string value", responseNode.get("value").asText());
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("string", responseNode.get("type").asText());
    assertNull(responseNode.get("valueUrl"));
    
    assertTrue(taskService.hasVariableLocal(task.getId(), "scopelessVariable"));
    assertEquals("simple string value", taskService.getVariableLocal(task.getId(), "scopelessVariable"));
    response.release();
  }
  
  /**
   * Test creating a single task variable using a binary stream.
   * POST runtime/tasks/{taskId}/variables
   */
  public void testCreateSingleBinaryTaskVariable() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      
      InputStream binaryContent = new ByteArrayInputStream("This is binary content".getBytes()); 
      
      // Add name, type and scope
      Map<String, String> additionalFields = new HashMap<String, String>();
      additionalFields.put("name", "binaryVariable");
      additionalFields.put("type", "binary");
      additionalFields.put("scope", "local");
      
      // Upload a valid BPMN-file using multipart-data
      Representation uploadRepresentation = new HttpMultipartRepresentation("value",
              binaryContent, additionalFields);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      Representation response = client.post(uploadRepresentation);
      
      // Check "CREATED" status
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
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
  
  /**
   * Test creating a single task variable using a binary stream.
   * POST runtime/tasks/{taskId}/variables
   */
  public void testCreateSingleSerializableTaskVariable() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      TestSerializableVariable serializable = new TestSerializableVariable();
      serializable.setSomeField("some value");
      
      // Serialize object to readable stream for representation
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      ObjectOutputStream output = new ObjectOutputStream(buffer);
      output.writeObject(serializable);
      output.close();
      
      InputStream binaryContent = new ByteArrayInputStream(buffer.toByteArray()); 
      
      // Add name, type and scope
      Map<String, String> additionalFields = new HashMap<String, String>();
      additionalFields.put("name", "serializableVariable");
      additionalFields.put("type", "serializable");
      additionalFields.put("scope", "local");
      
      // Upload a valid BPMN-file using multipart-data
      Representation uploadRepresentation = new HttpMultipartRepresentation("value",
              binaryContent, additionalFields);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      Representation response = client.post(uploadRepresentation);
      
      // Check "CREATED" status
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("serializableVariable", responseNode.get("name").asText());
      assertTrue(responseNode.get("value").isNull());
      assertEquals("local", responseNode.get("scope").asText());
      assertEquals("serializable", responseNode.get("type").asText());
      assertNotNull(responseNode.get("valueUrl").isNull());
      assertTrue(responseNode.get("valueUrl").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, task.getId(), "serializableVariable")));
      
      // Check actual value of variable in engine
      Object variableValue = taskService.getVariableLocal(task.getId(), "serializableVariable");
      assertNotNull(variableValue);
      assertTrue(variableValue instanceof TestSerializableVariable);
      assertEquals("some value", ((TestSerializableVariable)variableValue).getSomeField());
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  
  /**
   * Test creating a single task variable, testing edge case exceptions. 
   * POST runtime/tasks/{taskId}/variables
   */
  public void testCreateSingleTaskVariableEdgeCases() throws Exception {
    try {
      // Test adding variable to unexisting task
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "existingVariable");
      requestNode.put("value", "simple string value");
      requestNode.put("scope", "local");
      requestNode.put("type", "string");

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, "unexisting"));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexisting'.", expected.getStatus().getDescription());
      }

      // Test trying to create already existing variable
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setVariable(task.getId(), "existingVariable", "Value 1");
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
        assertEquals("Variable 'existingVariable' is already present on task '" + task.getId() + "'.", expected.getStatus().getDescription());
      }

      // Test setting global variable on standalone task
      requestNode.put("name", "myVariable");
      requestNode.put("value", "simple string value");
      requestNode.put("scope", "global");
      requestNode.put("type", "string");

      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Cannot set global variable 'myVariable' on task '" + task.getId() + "', task is not part of process.", expected.getStatus()
                .getDescription());
      }

      // Test creating nameless variable
      requestNode = objectMapper.createObjectNode();
      requestNode.put("value", "simple string value");

      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Variable name is required", expected.getStatus().getDescription());
      }
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test creating a single task variable, testing default types when omitted. 
   * POST runtime/tasks/{taskId}/variables
   */
  public void testCreateSingleTaskVariableDefaultTypes() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      
      // String type detection
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "stringVar");
      requestNode.put("value", "String value");
      requestNode.put("scope", "local");
      client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      assertEquals("String value", taskService.getVariable(task.getId(), "stringVar"));
      client.release();
      
      // Integer type detection
      requestNode.put("name", "integerVar");
      requestNode.put("value", 123);
      requestNode.put("scope", "local");
      client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      assertEquals(123, taskService.getVariable(task.getId(), "integerVar"));
      client.release();
      
      // Double type detection
      requestNode.put("name", "doubleVar");
      requestNode.put("value", 123.456);
      requestNode.put("scope", "local");
      client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      assertEquals(123.456, taskService.getVariable(task.getId(), "doubleVar"));
      client.release();
      
      // Boolean type detection
      requestNode.put("name", "booleanVar");
      requestNode.put("value", Boolean.TRUE);
      requestNode.put("scope", "local");
      client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      assertEquals(Boolean.TRUE, taskService.getVariable(task.getId(), "booleanVar"));
      client.release();
      
      
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
}
