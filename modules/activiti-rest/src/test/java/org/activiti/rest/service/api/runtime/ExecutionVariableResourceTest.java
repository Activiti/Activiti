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

import org.activiti.engine.runtime.Execution;
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
 * Test for all REST-operations related to a single execution variable.
 * 
 * @author Frederik Heremans
 */
public class ExecutionVariableResourceTest extends BaseRestTestCase {

  /**
   * Test getting an execution variable. GET
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testGetExecutionVariable() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne");
    runtimeService.setVariable(processInstance.getId(), "variable", "processValue");
    
    Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
    assertNotNull(childExecution);
    runtimeService.setVariableLocal(childExecution.getId(), "variable", "childValue");
    

    // Get local scope variable
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "variable"));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("childValue", responseNode.get("value").asText());
    assertEquals("variable", responseNode.get("name").asText());
    assertEquals("string", responseNode.get("type").asText());
    
    // Get global scope variable
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "variable") + "?scope=global");
    response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("global", responseNode.get("scope").asText());
    assertEquals("processValue", responseNode.get("value").asText());
    assertEquals("variable", responseNode.get("name").asText());
    assertEquals("string", responseNode.get("type").asText());
    
    // Illegal scope
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, processInstance.getId(), "variable") + "?scope=illegal");
    try {
      response = client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Invalid variable scope: 'illegal'", expected.getStatus().getDescription());
    }
    
    // Unexisting process
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, "unexisting", "variable"));
    try {
      response = client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("execution unexisting doesn't exist", expected.getStatus().getDescription());
    }
    
    // Unexisting variable
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, processInstance.getId(), "unexistingVariable"));
    try {
      response = client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Execution '" + processInstance.getId() + "' doesn't have a variable with name: 'unexistingVariable'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test getting execution variable data.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testGetExecutionVariableData() throws Exception {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne");
      runtimeService.setVariableLocal(processInstance.getId(), "var", "This is a binary piece of text".getBytes());
      
      Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
      assertNotNull(childExecution);
      runtimeService.setVariableLocal(childExecution.getId(), "var", "This is a binary piece of text in the child execution".getBytes());

      // Force content-type to TEXT_PLAIN to make sure this is ignored and application-octect-stream is always returned
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, childExecution.getId(), "var"));
      client.get(MediaType.TEXT_PLAIN);
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      String actualResponseBytesAsText = client.getResponse().getEntityAsText();
      assertEquals("This is a binary piece of text in the child execution", actualResponseBytesAsText);
      assertEquals(MediaType.APPLICATION_OCTET_STREAM.getName(), getMediaType(client));
      
      // Test global scope
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, childExecution.getId(), "var") + "?scope=global");
      client.get();
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      actualResponseBytesAsText = client.getResponse().getEntityAsText();
      assertEquals("This is a binary piece of text", actualResponseBytesAsText);
      assertEquals(MediaType.APPLICATION_OCTET_STREAM.getName(), getMediaType(client));
  }
  
  /**
   * Test getting an execution variable data.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testGetExecutionVariableDataSerializable() throws Exception {
    
    TestSerializableVariable originalSerializable = new TestSerializableVariable();
    originalSerializable.setSomeField("This is some field");
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne");
    runtimeService.setVariableLocal(processInstance.getId(), "var", originalSerializable);

    // Force content-type to TEXT_PLAIN to make sure this is ignored and application-octect-stream is always returned
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, processInstance.getId(), "var"));
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
   * Test getting an execution variable, for illegal vars.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testGetExecutionDataForIllegalVariables() throws Exception {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne");
    runtimeService.setVariableLocal(processInstance.getId(), "localTaskVariable", "this is a plain string variable");

    // Try getting data for non-binary variable
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, processInstance.getId(), "localTaskVariable"));
    try {
      client.get();
      fail("Exception expected");
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("The variable does not have a binary data stream.", expected.getStatus().getDescription());
    }

    // Try getting data for unexisting property
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, processInstance.getId(), "unexistingVariable"));
    try {
      client.get();
      fail("Exception expected");
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Execution '" + processInstance.getId() + "' doesn't have a variable with name: 'unexistingVariable'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test deleting a single execution variable, including "not found" check.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testDeleteExecutionVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", 
            Collections.singletonMap("myVariable", (Object) "processValue"));
    
    Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
    assertNotNull(childExecution);
    runtimeService.setVariableLocal(childExecution.getId(), "myVariable", "childValue");
    
    // Delete variable local
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "myVariable"));
    Representation response = client.delete();
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    assertEquals(0L, response.getSize());
    assertFalse(runtimeService.hasVariableLocal(childExecution.getId(), "myVariable"));
    // Global variable should remain unaffected
    assertTrue(runtimeService.hasVariable(childExecution.getId(), "myVariable"));
    
    // Delete variable global
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "myVariable") + "?scope=global");
    response = client.delete();
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    assertEquals(0L, response.getSize());
    assertFalse(runtimeService.hasVariableLocal(childExecution.getId(), "myVariable"));
    assertFalse(runtimeService.hasVariable(childExecution.getId(), "myVariable"));
    
    // Run the same delete again, variable is not there so 404 should be returned
    client.release();
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Execution '" + childExecution.getId() + "' doesn't have a variable 'myVariable' in scope global", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test updating a single execution variable, including "not found" check.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testUpdateExecutionVariable() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", 
            Collections.singletonMap("overlappingVariable", (Object) "processValue"));
    runtimeService.setVariableLocal(processInstance.getId(), "myVar", "processValue");
    
    Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
    assertNotNull(childExecution);
    runtimeService.setVariableLocal(childExecution.getId(), "myVar", "childValue");
    
    // Update variable local
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("name", "myVar");
    requestNode.put("value", "updatedValue");
    requestNode.put("type", "string");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "myVar"));
    Representation response = client.put(requestNode);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("updatedValue", responseNode.get("value").asText());
    assertEquals("local", responseNode.get("scope").asText());
    
    // Global value should be unaffected
    assertEquals("processValue", runtimeService.getVariable(processInstance.getId(), "myVar"));
    assertEquals("updatedValue", runtimeService.getVariableLocal(childExecution.getId(), "myVar"));
    
    // Update variable global
    requestNode = objectMapper.createObjectNode();
    requestNode.put("name", "myVar");
    requestNode.put("value", "updatedValueGlobal");
    requestNode.put("type", "string");
    requestNode.put("scope", "global");
    
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "myVar"));
    response = client.put(requestNode);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("updatedValueGlobal", responseNode.get("value").asText());
    assertEquals("global", responseNode.get("scope").asText());
    
    // Local value should be unaffected
    assertEquals("updatedValueGlobal", runtimeService.getVariable(processInstance.getId(), "myVar"));
    assertEquals("updatedValue", runtimeService.getVariableLocal(childExecution.getId(), "myVar"));
    
           
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
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "unexistingVariable"));
      requestNode.put("name", "unexistingVariable");
      client.put(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Execution '" + childExecution.getId() + "' doesn't have a variable with name: 'unexistingVariable'.", expected.getStatus().getDescription());
    }
    
  }
  
  /**
   * Test updating a single execution variable using a binary stream.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testUpdateBinaryExecutionVariable() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", 
            Collections.singletonMap("overlappingVariable", (Object) "processValue"));
    runtimeService.setVariableLocal(processInstance.getId(), "binaryVariable", "Initial binary value".getBytes());
    
    Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
    assertNotNull(childExecution);
    runtimeService.setVariableLocal(childExecution.getId(), "binaryVariable", "Initial binary value child".getBytes());
    
    InputStream binaryContent = new ByteArrayInputStream("This is binary content".getBytes());

    // Add name and type
    Map<String, String> additionalFields = new HashMap<String, String>();
    additionalFields.put("name", "binaryVariable");
    additionalFields.put("type", "binary");

    // Upload a valid BPMN-file using multipart-data
    Representation uploadRepresentation = new HttpMultipartRepresentation("value", binaryContent, additionalFields);

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "binaryVariable"));
    Representation response = client.put(uploadRepresentation);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("binaryVariable", responseNode.get("name").asText());
    assertTrue(responseNode.get("value").isNull());
    assertEquals("local", responseNode.get("scope").asText());
    assertEquals("binary", responseNode.get("type").asText());
    assertNotNull(responseNode.get("valueUrl").isNull());
    assertTrue(responseNode.get("valueUrl").asText()
            .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, childExecution.getId(), "binaryVariable")));

    // Check actual value of variable in engine
    Object variableValue = runtimeService.getVariableLocal(childExecution.getId(), "binaryVariable");
    assertNotNull(variableValue);
    assertTrue(variableValue instanceof byte[]);
    assertEquals("This is binary content", new String((byte[]) variableValue));
    
    // Update variable in global scope
    additionalFields.put("scope", "global");
    binaryContent = new ByteArrayInputStream("This is binary content global".getBytes());
    uploadRepresentation = new HttpMultipartRepresentation("value", binaryContent, additionalFields);
    
    response = client.put(uploadRepresentation);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("binaryVariable", responseNode.get("name").asText());
    assertTrue(responseNode.get("value").isNull());
    assertEquals("global", responseNode.get("scope").asText());
    assertEquals("binary", responseNode.get("type").asText());
    assertNotNull(responseNode.get("valueUrl").isNull());
    assertTrue(responseNode.get("valueUrl").asText()
            .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, childExecution.getId(), "binaryVariable")));

    // Check actual global value of variable in engine
    variableValue = runtimeService.getVariableLocal(processInstance.getId(), "binaryVariable");
    assertNotNull(variableValue);
    assertTrue(variableValue instanceof byte[]);
    assertEquals("This is binary content global", new String((byte[]) variableValue));
    
    // local value should remain unchainged
    variableValue = runtimeService.getVariableLocal(childExecution.getId(), "binaryVariable");
    assertNotNull(variableValue);
    assertTrue(variableValue instanceof byte[]);
    assertEquals("This is binary content", new String((byte[]) variableValue));
    
  }
  
  protected String getMediaType(ClientResource client) {
    Form headers = (Form) client.getResponseAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
    return headers.getFirstValue(HeaderConstants.HEADER_CONTENT_TYPE);
  }
}
