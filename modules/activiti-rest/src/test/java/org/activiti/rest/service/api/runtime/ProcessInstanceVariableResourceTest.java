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
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.HttpMultipartRepresentation;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Test for all REST-operations related to a single task variable.
 * 
 * @author Frederik Heremans
 */
public class ProcessInstanceVariableResourceTest extends BaseRestTestCase {

  /**
   * Test getting a process instance variable. GET
   * runtime/process-instances/{processInstanceId}/variables/{variableName}
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariableResourceTest.testProcess.bpmn20.xml"})
  public void testGetProcessInstanceVariable() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(processInstance.getId(), "variable", "processValue");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "variable"));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("processValue", responseNode.get("value").asText());
    assertEquals("variable", responseNode.get("name").asText());
    assertEquals("string", responseNode.get("type").asText());
    
    // Illegal scope
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "variable") + "?scope=illegal");
    try {
      response = client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Invalid variable scope: 'illegal'", expected.getStatus().getDescription());
    }
    
    // Unexisting process
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, "unexisting", "variable"));
    try {
      response = client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("execution unexisting doesn't exist", expected.getStatus().getDescription());
    }
    
    // Unexisting variable
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "unexistingVariable"));
    try {
      response = client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Execution '" + processInstance.getId() + "' doesn't have a variable with name: 'unexistingVariable'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test getting a process instance variable data.
   * GET runtime/process-instances/{processInstanceId}/variables/{variableName}
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceVariableResourceTest.testProcess.bpmn20.xml"})
  public void testGetProcessInstanceVariableData() throws Exception {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.setVariableLocal(processInstance.getId(), "var", "This is a binary piece of text".getBytes());

      // Force content-type to TEXT_PLAIN to make sure this is ignored and application-octect-stream is always returned
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "var"));
      client.get(MediaType.TEXT_PLAIN);
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      String actualResponseBytesAsText = client.getResponse().getEntityAsText();
      assertEquals("This is a binary piece of text", actualResponseBytesAsText);
      assertEquals(MediaType.APPLICATION_OCTET_STREAM.getName(), getMediaType(client));
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

    // Force content-type to TEXT_PLAIN to make sure this is ignored and application-octect-stream is always returned
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "var"));
    client.get(MediaType.TEXT_PLAIN);
    
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    // Read the serializable from the stream
    ObjectInputStream stream = new ObjectInputStream(client.getResponse().getEntity().getStream());
    Object readSerializable = stream.readObject();
    assertNotNull(readSerializable);
    assertTrue(readSerializable instanceof TestSerializableVariable);
    assertEquals("This is some field", ((TestSerializableVariable) readSerializable).getSomeField());
    assertEquals(MediaType.APPLICATION_JAVA_OBJECT.getName(), getMediaType(client));
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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "localTaskVariable"));
    try {
      client.get();
      fail("Exception expected");
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("The variable does not have a binary data stream.", expected.getStatus().getDescription());
    }

    // Try getting data for unexisting property
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstance.getId(), "unexistingVariable"));
    try {
      client.get();
      fail("Exception expected");
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Execution '" + processInstance.getId() + "' doesn't have a variable with name: 'unexistingVariable'.", expected.getStatus().getDescription());
    }

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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "myVariable"));
    Representation response = client.delete();
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    assertEquals(0L, response.getSize());
    assertFalse(runtimeService.hasVariable(processInstance.getId(), "myVariable"));
    
    // Run the same delete again, variable is not there so 404 should be returned
    client.release();
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Execution '" + processInstance.getId() + "' doesn't have a variable 'myVariable' in scope local", expected.getStatus().getDescription());
    }
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
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "myVar"));
    Representation response = client.put(requestNode);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("updatedValue", responseNode.get("value").asText());
    
           
    // Try updating with mismatch between URL and body variableName
    try {
      requestNode.put("name", "unexistingVariable");
      client.put(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Variable name in the body should be equal to the name used in the requested URL.", expected.getStatus().getDescription());
    }
    
    // Try updating unexisting property
    try {
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "unexistingVariable"));
      requestNode.put("name", "unexistingVariable");
      client.put(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Execution '" + processInstance.getId() + "' doesn't have a variable with name: 'unexistingVariable'.", expected.getStatus().getDescription());
    }
    
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

    // Upload a valid BPMN-file using multipart-data
    Representation uploadRepresentation = new HttpMultipartRepresentation("value", binaryContent, additionalFields);

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE, processInstance.getId(), "binaryVariable"));
    Representation response = client.put(uploadRepresentation);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    JsonNode responseNode = objectMapper.readTree(response.getStream());
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
  
  protected String getMediaType(ClientResource client) {
    Form headers = (Form) client.getResponseAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
    return headers.getFirstValue(HeaderConstants.HEADER_CONTENT_TYPE);
  }
}
