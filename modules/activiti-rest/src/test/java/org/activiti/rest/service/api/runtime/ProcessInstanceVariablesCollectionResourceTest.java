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
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.HttpMultipartHelper;
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
 * Test for all REST-operations related to Process instance variables.
 * 
 * @author Frederik Heremans
 */
public class ProcessInstanceVariablesCollectionResourceTest extends BaseSpringRestTestCase {
  
  /**
   * Test getting all process variables.
   * GET runtime/process-instances/{processInstanceId}/variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testGetProcessVariables() throws Exception {
   
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
    
    // Request all variables (no scope provides) which include global an local
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId())), HttpStatus.SC_OK);
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(9, responseNode.size());
  }
  
  /**
   * Test creating a single process variable.
   * POST runtime/process-instance/{processInstanceId}/variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testCreateSingleProcessInstanceVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    ArrayNode requestNode = objectMapper.createArrayNode();
    ObjectNode variableNode = requestNode.addObject();
    variableNode.put("name", "myVariable");
    variableNode.put("value", "simple string value");
    variableNode.put("type", "string");
            
    // Create a new local variable
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeBinaryRequest(httpPost, HttpStatus.SC_CREATED);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent()).get(0);
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("myVariable", responseNode.get("name").asText());
    assertEquals("simple string value", responseNode.get("value").asText());
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("string", responseNode.get("type").asText());
    assertNull(responseNode.get("valueUrl"));
    
    assertTrue(runtimeService.hasVariableLocal(processInstance.getId(), "myVariable"));
    assertEquals("simple string value", runtimeService.getVariableLocal(processInstance.getId(), "myVariable"));
  }
  
  /**
   * Test creating a single process variable using a binary stream.
   * POST runtime/process-instances/{processInstanceId}/variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testCreateSingleBinaryProcessVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    InputStream binaryContent = new ByteArrayInputStream("This is binary content".getBytes()); 
    
    // Add name, type and scope
    Map<String, String> additionalFields = new HashMap<String, String>();
    additionalFields.put("name", "binaryVariable");
    additionalFields.put("type", "binary");
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/octet-stream", binaryContent, additionalFields));
    CloseableHttpResponse response = executeBinaryRequest(httpPost, HttpStatus.SC_CREATED);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("binaryVariable", responseNode.get("name").asText());
    assertTrue(responseNode.get("value").isNull());
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("binary", responseNode.get("type").asText());
    assertFalse(responseNode.get("valueUrl").isNull());
    assertTrue(responseNode.get("valueUrl").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "binaryVariable")));
    
    // Check actual value of variable in engine
    Object variableValue = runtimeService.getVariableLocal(processInstance.getId(), "binaryVariable");
    assertNotNull(variableValue);
    assertTrue(variableValue instanceof byte[]);
    assertEquals("This is binary content", new String((byte[])variableValue));
  }
  
  /**
   * Test creating a single process variable using a binary stream containing a serializable.
   * POST runtime/process-instances/{processInstanceId}/variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testCreateSingleSerializableProcessVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
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
    
    // Upload a valid BPMN-file using multipart-data
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/x-java-serialized-object", binaryContent, additionalFields));
    CloseableHttpResponse response = executeBinaryRequest(httpPost, HttpStatus.SC_CREATED);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("serializableVariable", responseNode.get("name").asText());
    assertTrue(responseNode.get("value").isNull());
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("serializable", responseNode.get("type").asText());
    assertFalse(responseNode.get("valueUrl").isNull());
    assertTrue(responseNode.get("valueUrl").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "serializableVariable")));
    
    // Check actual value of variable in engine
    Object variableValue = runtimeService.getVariableLocal(processInstance.getId(), "serializableVariable");
    assertNotNull(variableValue);
    assertTrue(variableValue instanceof TestSerializableVariable);
    assertEquals("some value", ((TestSerializableVariable)variableValue).getSomeField());
  }
  
  
  /**
   * Test creating a single process variable, testing edge case exceptions. 
   * POST runtime/process-instances/{processInstanceId}/variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testCreateSingleProcessVariableEdgeCases() throws Exception {
    // Test adding variable to unexisting execution
    ArrayNode requestNode = objectMapper.createArrayNode();
    ObjectNode variableNode = requestNode.addObject();
    variableNode.put("name", "existingVariable");
    variableNode.put("value", "simple string value");
    variableNode.put("type", "string");

    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, "unexisting"));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_NOT_FOUND));

    // Test trying to create already existing variable
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(processInstance.getId(), "existingVariable", "I already exist");
    
    httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_CONFLICT));

    // Test creating nameless variable
    variableNode.removeAll();
    variableNode.put("value", "simple string value");

    httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
    
    // Test passing in empty array
    requestNode.removeAll();
    httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
    
    // Test passing in object instead of array
    httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(new StringEntity(objectMapper.createObjectNode().toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
  }
  
  /**
   * Test creating a single process variable, testing default types when omitted. 
   * POST runtime/process-instances/{processInstanceId}/variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testCreateSingleProcessVariableDefaultTypes() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    // String type detection
    ArrayNode requestNode = objectMapper.createArrayNode();
    ObjectNode varNode = requestNode.addObject();
    varNode.put("name", "stringVar");
    varNode.put("value", "String value");
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_CREATED));
    
    assertEquals("String value", runtimeService.getVariable(processInstance.getId(), "stringVar"));
    
    // Integer type detection
    varNode.put("name", "integerVar");
    varNode.put("value", 123);
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_CREATED));
    
    assertEquals(123, runtimeService.getVariable(processInstance.getId(), "integerVar"));
    
    // Double type detection
    varNode.put("name", "doubleVar");
    varNode.put("value", 123.456);
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_CREATED));
    
    assertEquals(123.456, runtimeService.getVariable(processInstance.getId(), "doubleVar"));
   
    // Boolean type detection
    varNode.put("name", "booleanVar");
    varNode.put("value", Boolean.TRUE);
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_CREATED));
    
    assertEquals(Boolean.TRUE, runtimeService.getVariable(processInstance.getId(), "booleanVar"));
  }
  
  /**
   * Test creating multiple process variables in a single call.
   * POST runtime/process-instance/{processInstanceId}/variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testCreateMultipleProcessVariables() throws Exception {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    ArrayNode requestNode = objectMapper.createArrayNode();
    
    // String variable
    ObjectNode stringVarNode = requestNode.addObject();
    stringVarNode.put("name", "stringVariable");
    stringVarNode.put("value", "simple string value");
    stringVarNode.put("type", "string");
    
    // Integer
    ObjectNode integerVarNode = requestNode.addObject();
    integerVarNode.put("name", "integerVariable");
    integerVarNode.put("value", 1234);
    integerVarNode.put("type", "integer");
    
    // Short
    ObjectNode shortVarNode = requestNode.addObject();
    shortVarNode.put("name", "shortVariable");
    shortVarNode.put("value", 123);
    shortVarNode.put("type", "short");
    
    // Long
    ObjectNode longVarNode = requestNode.addObject();
    longVarNode.put("name", "longVariable");
    longVarNode.put("value", 4567890L);
    longVarNode.put("type", "long");
    
    // Double
    ObjectNode doubleVarNode = requestNode.addObject();
    doubleVarNode.put("name", "doubleVariable");
    doubleVarNode.put("value", 123.456);
    doubleVarNode.put("type", "double");
    
    // Boolean
    ObjectNode booleanVarNode = requestNode.addObject();
    booleanVarNode.put("name", "booleanVariable");
    booleanVarNode.put("value", Boolean.TRUE);
    booleanVarNode.put("type", "boolean");
    
    // Date
    Calendar varCal = Calendar.getInstance();
    String isoString = getISODateString(varCal.getTime());
    
    ObjectNode dateVarNode = requestNode.addObject();
    dateVarNode.put("name", "dateVariable");
    dateVarNode.put("value", isoString);
    dateVarNode.put("type", "date");
            
    // Create local variables with a single request
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(7, responseNode.size());
    
    // Check if engine has correct variables set
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstance.getId());
    assertEquals(7, variables.size());
    
    assertEquals("simple string value", variables.get("stringVariable"));
    assertEquals(1234, variables.get("integerVariable"));
    assertEquals((short)123, variables.get("shortVariable"));
    assertEquals(4567890L, variables.get("longVariable"));
    assertEquals(123.456, variables.get("doubleVariable"));
    assertEquals(Boolean.TRUE, variables.get("booleanVariable"));
    assertEquals(dateFormat.parse(isoString), variables.get("dateVariable"));
  }
  
  /**
   * Test creating multiple process variables in a single call.
   * POST runtime/process-instance/{processInstanceId}/variables?override=true
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testCreateMultipleProcessVariablesWithOverride() throws Exception {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(processInstance.getId(), "stringVariable", "initialValue");
    ArrayNode requestNode = objectMapper.createArrayNode();
    
    // String variable
    ObjectNode stringVarNode = requestNode.addObject();
    stringVarNode.put("name", "stringVariable");
    stringVarNode.put("value", "simple string value");
    stringVarNode.put("type", "string");
    
    ObjectNode anotherVariable = requestNode.addObject();
    anotherVariable.put("name", "stringVariable2");
    anotherVariable.put("value", "another string value");
    anotherVariable.put("type", "string");
    
    // Create local variables with a single request
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_CREATED);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(2, responseNode.size());
    
    // Check if engine has correct variables set
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstance.getId());
    assertEquals(2, variables.size());
    
    assertEquals("simple string value", variables.get("stringVariable"));
    assertEquals("another string value", variables.get("stringVariable2"));
  }
  
  /**
   * Test deleting all process variables.
   * DELETE runtime/process-instance/{processInstanceId}/variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testDeleteAllProcessVariables() throws Exception {
    
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("var1", "This is a ProcVariable");
    processVariables.put("var2", 123);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);

    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
    
    // Check if local variables are gone and global remain unchanged
    assertEquals(0, runtimeService.getVariablesLocal(processInstance.getId()).size());
  }
}
