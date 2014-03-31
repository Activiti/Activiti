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
 * Test for all REST-operations related to Process instance variables.
 * 
 * @author Frederik Heremans
 */
public class ProcessInstanceVariablesCollectionResourceTest extends BaseRestTestCase {
  
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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    Representation response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream()).get(0);
    assertNotNull(responseNode);
    assertEquals("myVariable", responseNode.get("name").asText());
    assertEquals("simple string value", responseNode.get("value").asText());
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("string", responseNode.get("type").asText());
    assertNull(responseNode.get("valueUrl"));
    
    assertTrue(runtimeService.hasVariableLocal(processInstance.getId(), "myVariable"));
    assertEquals("simple string value", runtimeService.getVariableLocal(processInstance.getId(), "myVariable"));
    response.release();
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
    
    // Upload a valid BPMN-file using multipart-data
    Representation uploadRepresentation = new HttpMultipartRepresentation("value",
            binaryContent, additionalFields);
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    Representation response = client.post(uploadRepresentation);
    
    // Check "CREATED" status
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
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
    Representation uploadRepresentation = new HttpMultipartRepresentation("value",
            binaryContent, additionalFields);
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    Representation response = client.post(uploadRepresentation);
    
    // Check "CREATED" status
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
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

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, "unexisting"));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a process instance with id 'unexisting'.", expected.getStatus().getDescription());
      }

      // Test trying to create already existing variable
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.setVariable(processInstance.getId(), "existingVariable", "I already exist");
      
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
        assertEquals("Variable 'existingVariable' is already present on execution '" + processInstance.getId() + "'.", expected.getStatus().getDescription());
      }

      // Test creating nameless variable
      variableNode.removeAll();
      variableNode.put("value", "simple string value");

      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Variable name is required", expected.getStatus().getDescription());
      }
      
      // Test passing in empty array
      requestNode.removeAll();
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Request didn't contain a list of variables to create.", expected.getStatus().getDescription());
      }
      
      // Test passing in object instead of array
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
      try {
        client.post(objectMapper.createObjectNode());
        fail("Exception expected");
      } catch (ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Request didn't contain a list of variables to create.", expected.getStatus().getDescription());
      }
  }
  
  /**
   * Test creating a single process variable, testing default types when omitted. 
   * POST runtime/process-instances/{processInstanceId}/variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml"})
  public void testCreateSingleProcessVariableDefaultTypes() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    
    // String type detection
    ArrayNode requestNode = objectMapper.createArrayNode();
    ObjectNode varNode = requestNode.addObject();
    varNode.put("name", "stringVar");
    varNode.put("value", "String value");
    client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    assertEquals("String value", runtimeService.getVariable(processInstance.getId(), "stringVar"));
    client.release();
    
    // Integer type detection
    varNode.put("name", "integerVar");
    varNode.put("value", 123);
    client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    assertEquals(123, runtimeService.getVariable(processInstance.getId(), "integerVar"));
    client.release();
    
    // Double type detection
    varNode.put("name", "doubleVar");
    varNode.put("value", 123.456);
    client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    assertEquals(123.456, runtimeService.getVariable(processInstance.getId(), "doubleVar"));
    client.release();
    
    // Boolean type detection
    varNode.put("name", "booleanVar");
    varNode.put("value", Boolean.TRUE);
    client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    assertEquals(Boolean.TRUE, runtimeService.getVariable(processInstance.getId(), "booleanVar"));
    client.release();
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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    Representation response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
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
    assertEquals(varCal.getTime(), variables.get("dateVariable"));
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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    Representation response = client.put(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
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

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    Representation response = client.delete();
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    assertEquals(0, response.getSize());
    
    // Check if local variables are gone and global remain unchanged
    assertEquals(0, runtimeService.getVariablesLocal(processInstance.getId()).size());
  }
}
