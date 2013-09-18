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
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.HttpMultipartRepresentation;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
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
    
    ArrayNode requestNode = objectMapper.createArrayNode();
    ObjectNode variableNode = requestNode.addObject();
    variableNode.put("name", "myVariable");
    variableNode.put("value", "simple string value");
    variableNode.put("scope", "local");
    variableNode.put("type", "string");
            
    // Create a new local variable
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    Representation response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream()).get(0);
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
    variableNode.put("name", "myVariable");
    variableNode.put("value", "Another simple string value");
    variableNode.put("scope", "global");
    variableNode.put("type", "string");
    
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream()).get(0);
    assertNotNull(responseNode);
    assertEquals("myVariable", responseNode.get("name").asText());
    assertEquals("Another simple string value", responseNode.get("value").asText());
    assertEquals("global", responseNode.get("scope").asText());
    assertEquals("string", responseNode.get("type").asText());
    assertNull(responseNode.get("valueUrl"));
    
    assertTrue(runtimeService.hasVariable(task.getExecutionId(), "myVariable"));
    assertEquals("Another simple string value", runtimeService.getVariableLocal(task.getExecutionId(), "myVariable"));
    
            
    // Create a new scope-less variable, which defaults to local variables
    variableNode.removeAll();
    variableNode.put("name", "scopelessVariable");
    variableNode.put("value", "simple string value");
    variableNode.put("type", "string");
    
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream()).get(0);
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
      ArrayNode requestNode = objectMapper.createArrayNode();
      ObjectNode variableNode = requestNode.addObject();
      variableNode.put("name", "existingVariable");
      variableNode.put("value", "simple string value");
      variableNode.put("scope", "local");
      variableNode.put("type", "string");

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
      variableNode.put("name", "myVariable");
      variableNode.put("value", "simple string value");
      variableNode.put("scope", "global");
      variableNode.put("type", "string");

      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Cannot set global variables on task '" + task.getId() + "', task is not part of process.", expected.getStatus()
                .getDescription());
      }

      // Test creating nameless variable
      variableNode.removeAll();
      variableNode.put("value", "simple string value");

      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Variable name is required", expected.getStatus().getDescription());
      }
      
      // Test passing in empty array
      requestNode.removeAll();
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Request didn't cantain a list of variables to create.", expected.getStatus().getDescription());
      }
      
      // Test passing in object instead of array
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      try {
        client.post(objectMapper.createObjectNode());
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Request didn't cantain a list of variables to create.", expected.getStatus().getDescription());
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
      ArrayNode requestNode = objectMapper.createArrayNode();
      ObjectNode varNode = requestNode.addObject();
      varNode.put("name", "stringVar");
      varNode.put("value", "String value");
      varNode.put("scope", "local");
      client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      assertEquals("String value", taskService.getVariable(task.getId(), "stringVar"));
      client.release();
      
      // Integer type detection
      varNode.put("name", "integerVar");
      varNode.put("value", 123);
      varNode.put("scope", "local");
      client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      assertEquals(123, taskService.getVariable(task.getId(), "integerVar"));
      client.release();
      
      // Double type detection
      varNode.put("name", "doubleVar");
      varNode.put("value", 123.456);
      varNode.put("scope", "local");
      client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      assertEquals(123.456, taskService.getVariable(task.getId(), "doubleVar"));
      client.release();
      
      // Boolean type detection
      varNode.put("name", "booleanVar");
      varNode.put("value", Boolean.TRUE);
      varNode.put("scope", "local");
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
  
  /**
   * Test creating a multipe task variable in a single call.
   * POST runtime/tasks/{taskId}/variables
   */
  public void testCreateMultipleTaskVariables() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      
      ArrayNode requestNode = objectMapper.createArrayNode();
      
      // String variable
      ObjectNode stringVarNode = requestNode.addObject();
      stringVarNode.put("name", "stringVariable");
      stringVarNode.put("value", "simple string value");
      stringVarNode.put("scope", "local");
      stringVarNode.put("type", "string");
      
      // Integer
      ObjectNode integerVarNode = requestNode.addObject();
      integerVarNode.put("name", "integerVariable");
      integerVarNode.put("value", 1234);
      integerVarNode.put("scope", "local");
      integerVarNode.put("type", "integer");
      
      // Short
      ObjectNode shortVarNode = requestNode.addObject();
      shortVarNode.put("name", "shortVariable");
      shortVarNode.put("value", 123);
      shortVarNode.put("scope", "local");
      shortVarNode.put("type", "short");
      
      // Long
      ObjectNode longVarNode = requestNode.addObject();
      longVarNode.put("name", "longVariable");
      longVarNode.put("value", 4567890L);
      longVarNode.put("scope", "local");
      longVarNode.put("type", "long");
      
      // Double
      ObjectNode doubleVarNode = requestNode.addObject();
      doubleVarNode.put("name", "doubleVariable");
      doubleVarNode.put("value", 123.456);
      doubleVarNode.put("scope", "local");
      doubleVarNode.put("type", "double");
      
      // Boolean
      ObjectNode booleanVarNode = requestNode.addObject();
      booleanVarNode.put("name", "booleanVariable");
      booleanVarNode.put("value", Boolean.TRUE);
      booleanVarNode.put("scope", "local");
      booleanVarNode.put("type", "boolean");
      
      // Date
      Calendar varCal = Calendar.getInstance();
      String isoString = getISODateString(varCal.getTime());
      
      ObjectNode dateVarNode = requestNode.addObject();
      dateVarNode.put("name", "dateVariable");
      dateVarNode.put("value", isoString);
      dateVarNode.put("scope", "local");
      dateVarNode.put("type", "date");
              
      // Create local variables with a single request
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      Representation response = client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertTrue(responseNode.isArray());
      assertEquals(7, responseNode.size());
      
      // Check if engine has correct variables set
      Map<String, Object> taskVariables = taskService.getVariablesLocal(task.getId());
      assertEquals(7, taskVariables.size());
      
      assertEquals("simple string value", taskVariables.get("stringVariable"));
      assertEquals(1234, taskVariables.get("integerVariable"));
      assertEquals((short)123, taskVariables.get("shortVariable"));
      assertEquals(4567890L, taskVariables.get("longVariable"));
      assertEquals(123.456, taskVariables.get("doubleVariable"));
      assertEquals(Boolean.TRUE, taskVariables.get("booleanVariable"));
      assertEquals(varCal.getTime(), taskVariables.get("dateVariable"));
      
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test deleting all local task variables.
   * DELETE runtime/tasks/{taskId}/variables
   */
  @Deployment
  public void testDeleteAllLocalVariables() throws Exception {
    // Start process with all types of variables
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("var1", "This is a ProcVariable");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    
    // Set local task variables
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Map<String, Object> taskVariables = new HashMap<String, Object>();
    taskVariables.put("var1", "This is a TaskVariable");
    taskVariables.put("var2", 123);
    taskService.setVariablesLocal(task.getId(), taskVariables);
    assertEquals(2, taskService.getVariablesLocal(task.getId()).size());

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    Representation response = client.delete();
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    assertEquals(0, response.getSize());
    
    // Check if local variables are gone and global remain unchanged
    assertEquals(0, taskService.getVariablesLocal(task.getId()).size());
    assertEquals(1, taskService.getVariables(task.getId()).size());
  }
}
