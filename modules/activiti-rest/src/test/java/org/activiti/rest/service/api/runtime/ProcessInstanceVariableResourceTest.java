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
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
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
public class ProcessInstanceVariableResourceTest extends BaseSpringRestTestCase {

  /**
   * Test getting a process instance variable. GET
   * runtime/process-instances/{processInstanceId}/variables/{variableName}
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariableResourceTest.testProcess.bpmn20.xml"})
  public void testGetProcessInstanceVariable() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(processInstance.getId(), "variable", "processValue");
    
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "variable")), HttpStatus.SC_OK);
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("processValue", responseNode.get("value").asText());
    assertEquals("variable", responseNode.get("name").asText());
    assertEquals("string", responseNode.get("type").asText());
    
    // Illegal scope
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "variable") + "?scope=illegal"), HttpStatus.SC_BAD_REQUEST));
    
    // Unexisting process
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, "unexisting", "variable")), HttpStatus.SC_NOT_FOUND));
    
    // Unexisting variable
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "unexistingVariable")), HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test getting a process instance variable data.
   * GET runtime/process-instances/{processInstanceId}/variables/{variableName}
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariableResourceTest.testProcess.bpmn20.xml"})
  public void testGetProcessInstanceVariableData() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariableLocal(processInstance.getId(), "var", "This is a binary piece of text".getBytes());

    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "var")), HttpStatus.SC_OK);
    
    String actualResponseBytesAsText = IOUtils.toString(response.getEntity().getContent());
    closeResponse(response);
    assertEquals("This is a binary piece of text", actualResponseBytesAsText);
    assertEquals("application/octet-stream", response.getEntity().getContentType().getValue());
  }
  
  /**
   * Test getting a process instance variable data.
   * GET runtime/process-instances/{processInstanceId}/variables/{variableName}
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariableResourceTest.testProcess.bpmn20.xml"})
  public void testGetProcessInstanceVariableDataSerializable() throws Exception {
    
    TestSerializableVariable originalSerializable = new TestSerializableVariable();
    originalSerializable.setSomeField("This is some field");
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariableLocal(processInstance.getId(), "var", originalSerializable);

    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "var")), HttpStatus.SC_OK);
    
    // Read the serializable from the stream
    ObjectInputStream stream = new ObjectInputStream(response.getEntity().getContent());
    Object readSerializable = stream.readObject();
    assertNotNull(readSerializable);
    assertTrue(readSerializable instanceof TestSerializableVariable);
    assertEquals("This is some field", ((TestSerializableVariable) readSerializable).getSomeField());
    assertEquals("application/x-java-serialized-object", response.getEntity().getContentType().getValue());
    closeResponse(response);
  }
  
  /**
   * Test getting a process instance variable, for illegal vars.
   * GET runtime/process-instances/{processInstanceId}/variables/{variableName}
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariableResourceTest.testProcess.bpmn20.xml"})
  public void testGetProcessInstanceVariableDataForIllegalVariables() throws Exception {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariableLocal(processInstance.getId(), "localTaskVariable", "this is a plain string variable");

    // Try getting data for non-binary variable
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "localTaskVariable")), HttpStatus.SC_NOT_FOUND));

    // Try getting data for unexisting property
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "unexistingVariable")), HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test deleting a single process variable in, including "not found" check.
   * 
   * DELETE runtime/process-instances/{processInstanceId}/variables/{variableName}
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariableResourceTest.testProcess.bpmn20.xml"})
  public void testDeleteProcessVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", 
            Collections.singletonMap("myVariable", (Object) "processValue"));
    
    // Delete variable
    closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "myVariable")), HttpStatus.SC_NO_CONTENT));
    
    assertFalse(runtimeService.hasVariable(processInstance.getId(), "myVariable"));
    
    // Run the same delete again, variable is not there so 404 should be returned
    closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "myVariable")), HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test updating a single process variable, including "not found" check.
   * 
   * PUT runtime/process-instances/{processInstanceId}/variables/{variableName}
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariableResourceTest.testProcess.bpmn20.xml"})
  public void testUpdateProcessVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", 
            Collections.singletonMap("overlappingVariable", (Object) "processValue"));
    runtimeService.setVariable(processInstance.getId(), "myVar", "value");
    
    // Update variable 
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("name", "myVar");
    requestNode.put("value", "updatedValue");
    requestNode.put("type", "string");
    
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "myVar"));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("updatedValue", responseNode.get("value").asText());
           
    // Try updating with mismatch between URL and body variableName
    requestNode.put("name", "unexistingVariable");
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_BAD_REQUEST));
    
    // Try updating unexisting property
    requestNode.put("name", "unexistingVariable");
    httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "unexistingVariable"));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test updating a single process variable using a binary stream.
   * PUT runtime/process-instances/{processInstanceId}/variables/{variableName}
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariableResourceTest.testProcess.bpmn20.xml"})
  public void testUpdateBinaryProcessVariable() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", 
            Collections.singletonMap("overlappingVariable", (Object) "processValue"));
    runtimeService.setVariable(processInstance.getId(), "binaryVariable", "Initial binary value".getBytes());
    
    InputStream binaryContent = new ByteArrayInputStream("This is binary content".getBytes());

    // Add name and type
    Map<String, String> additionalFields = new HashMap<String, String>();
    additionalFields.put("name", "binaryVariable");
    additionalFields.put("type", "binary");

    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "binaryVariable"));
    httpPut.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/octet-stream", binaryContent, additionalFields));
    CloseableHttpResponse response = executeBinaryRequest(httpPut, HttpStatus.SC_OK);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("binaryVariable", responseNode.get("name").asText());
    assertTrue(responseNode.get("value").isNull());
    assertEquals("binary", responseNode.get("type").asText());
    assertNotNull(responseNode.get("valueUrl").isNull());
    assertTrue(responseNode.get("valueUrl").asText()
            .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA,processInstance.getId(), "binaryVariable")));

    // Check actual value of variable in engine
    Object variableValue = runtimeService.getVariableLocal(processInstance.getId(), "binaryVariable");
    assertNotNull(variableValue);
    assertTrue(variableValue instanceof byte[]);
    assertEquals("This is binary content", new String((byte[]) variableValue));
  }
}
