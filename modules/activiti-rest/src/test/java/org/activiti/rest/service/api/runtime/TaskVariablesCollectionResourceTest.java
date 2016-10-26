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
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.HttpMultipartHelper;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Test for all REST-operations related to Task variables.
 * 
 * @author Frederik Heremans
 */
public class TaskVariablesCollectionResourceTest extends BaseSpringRestTestCase {
  
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
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId())), HttpStatus.SC_OK);
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
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
    
    // Check local variables filtering
    response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()) + "?scope=local"), HttpStatus.SC_OK);
    
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(9, responseNode.size());
    
    for(int i=0; i< responseNode.size(); i++) {
      JsonNode var = responseNode.get(i);
      assertEquals("local", var.get("scope").asText());
    }
    
    // Check global variables filtering
    response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()) + "?scope=global"), HttpStatus.SC_OK);
    
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
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
  public void testCreateSingleTaskVariablePost() throws Exception {
    doSingleTaskVariableTest("POST");
  }

  /**
   * Test creating a single task variable using PUT (update or create).
   * PUT runtime/tasks/{taskId}/variables
   */
  @Deployment
  public void testCreateSingleTaskVariablePut() throws Exception {
    doSingleTaskVariableTest("PUT");
  }

  private void doSingleTaskVariableTest(String httpMethod) throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    ArrayNode requestNode = objectMapper.createArrayNode();
    ObjectNode variableNode = requestNode.addObject();
    variableNode.put("name", "myVariable");
    variableNode.put("value", "simple string value");
    variableNode.put("scope", "local");
    variableNode.put("type", "string");

    HttpEntityEnclosingRequestBase httpCall;
    // Create a new local variable
    if (httpMethod.equals("POST"))  {
      httpCall = new HttpPost(SERVER_URL_PREFIX +
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    } else {
      httpCall = new HttpPut(SERVER_URL_PREFIX +
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    }
    httpCall.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpCall, HttpStatus.SC_CREATED);

    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent()).get(0);
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("myVariable", responseNode.get("name").asText());
    assertEquals("simple string value", responseNode.get("value").asText());
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("string", responseNode.get("type").asText());
    assertNull(responseNode.get("valueUrl"));

    assertTrue(taskService.hasVariableLocal(task.getId(), "myVariable"));
    assertEquals("simple string value", taskService.getVariableLocal(task.getId(), "myVariable"));

    // Create a new global variable
    variableNode.put("name", "myVariable");
    variableNode.put("value", "Another simple string value");
    variableNode.put("scope", "global");
    variableNode.put("type", "string");

    if (httpMethod.equals("POST")) {
      httpCall = new HttpPost(SERVER_URL_PREFIX +
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    } else {
      httpCall = new HttpPut(SERVER_URL_PREFIX +
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    }
    httpCall.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpCall, HttpStatus.SC_CREATED);
    responseNode = objectMapper.readTree(response.getEntity().getContent()).get(0);
    closeResponse(response);
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

    if (httpMethod.equals("POST")) {
      httpCall = new HttpPost(SERVER_URL_PREFIX +
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    } else {
      httpCall = new HttpPut(SERVER_URL_PREFIX +
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    }
    httpCall.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpCall, HttpStatus.SC_CREATED);
    responseNode = objectMapper.readTree(response.getEntity().getContent()).get(0);
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("scopelessVariable", responseNode.get("name").asText());
    assertEquals("simple string value", responseNode.get("value").asText());
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("string", responseNode.get("type").asText());
    assertNull(responseNode.get("valueUrl"));

    assertTrue(taskService.hasVariableLocal(task.getId(), "scopelessVariable"));
    assertEquals("simple string value", taskService.getVariableLocal(task.getId(), "scopelessVariable"));
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
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/octet-stream", binaryContent, additionalFields));
      CloseableHttpResponse response = executeBinaryRequest(httpPost, HttpStatus.SC_CREATED);
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
      assertEquals("This is binary content", new String((byte[]) variableValue));
      
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
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/x-java-serialized-object", 
          binaryContent, additionalFields));
      CloseableHttpResponse response = executeBinaryRequest(httpPost, HttpStatus.SC_CREATED);
      
      // Check "CREATED" status
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
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

      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, "unexisting"));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_NOT_FOUND));

      // Test trying to create already existing variable
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setVariable(task.getId(), "existingVariable", "Value 1");
      
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_CONFLICT));

      // Test same thing but using PUT (create or update)
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX +
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPut, HttpStatus.SC_CREATED));

      // Test setting global variable on standalone task
      variableNode.put("name", "myVariable");
      variableNode.put("value", "simple string value");
      variableNode.put("scope", "global");
      variableNode.put("type", "string");

      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_BAD_REQUEST));

      // Test creating nameless variable
      variableNode.removeAll();
      variableNode.put("value", "simple string value");

      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
      
      // Test passing in empty array
      requestNode.removeAll();
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
      
      // Test passing in object instead of array
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(objectMapper.createObjectNode().toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
      
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
      
      // String type detection
      ArrayNode requestNode = objectMapper.createArrayNode();
      ObjectNode varNode = requestNode.addObject();
      varNode.put("name", "stringVar");
      varNode.put("value", "String value");
      varNode.put("scope", "local");
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_CREATED));
      
      assertEquals("String value", taskService.getVariable(task.getId(), "stringVar"));
      
      // Integer type detection
      varNode.put("name", "integerVar");
      varNode.put("value", 123);
      varNode.put("scope", "local");
      
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_CREATED));
      
      assertEquals(123, taskService.getVariable(task.getId(), "integerVar"));
      
      // Double type detection
      varNode.put("name", "doubleVar");
      varNode.put("value", 123.456);
      varNode.put("scope", "local");
      
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_CREATED));
      
      assertEquals(123.456, taskService.getVariable(task.getId(), "doubleVar"));
      
      // Boolean type detection
      varNode.put("name", "booleanVar");
      varNode.put("value", Boolean.TRUE);
      varNode.put("scope", "local");
      
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_CREATED));
      
      assertEquals(Boolean.TRUE, taskService.getVariable(task.getId(), "booleanVar"));
      
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
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeBinaryRequest(httpPost, HttpStatus.SC_CREATED);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
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
      assertEquals(dateFormat.parse(isoString), taskVariables.get("dateVariable"));

      // repeat the process with additional variables, testing PUT of a mixed set of variables
      // where some exist and others do not

      requestNode = objectMapper.createArrayNode();

      // new String variable
      ObjectNode stringVarNode2 = requestNode.addObject();
      stringVarNode2.put("name", "new stringVariable");
      stringVarNode2.put("value", "simple string value 2");
      stringVarNode2.put("scope", "local");
      stringVarNode2.put("type", "string");

      // changed Integer variable
      ObjectNode integerVarNode2 = requestNode.addObject();
      integerVarNode2.put("name", "integerVariable");
      integerVarNode2.put("value", 4321);
      integerVarNode2.put("scope", "local");
      integerVarNode2.put("type", "integer");


      // Create or update local variables with a single request
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX +
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
      httpPut.setEntity(new StringEntity(requestNode.toString()));

      response = executeBinaryRequest(httpPut, HttpStatus.SC_CREATED);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertTrue(responseNode.isArray());
      assertEquals(2, responseNode.size());

      // Check if engine has correct variables set
      taskVariables = taskService.getVariablesLocal(task.getId());
      assertEquals(8, taskVariables.size());

      assertEquals("simple string value", taskVariables.get("stringVariable"));
      assertEquals("simple string value 2", taskVariables.get("new stringVariable"));
      assertEquals(4321, taskVariables.get("integerVariable"));
      assertEquals((short)123, taskVariables.get("shortVariable"));
      assertEquals(4567890L, taskVariables.get("longVariable"));
      assertEquals(123.456, taskVariables.get("doubleVariable"));
      assertEquals(Boolean.TRUE, taskVariables.get("booleanVariable"));
      assertEquals(dateFormat.parse(isoString), taskVariables.get("dateVariable"));

      
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

    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    closeResponse(executeBinaryRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
    
    // Check if local variables are gone and global remain unchanged
    assertEquals(0, taskService.getVariablesLocal(task.getId()).size());
    assertEquals(1, taskService.getVariables(task.getId()).size());
  }
}
