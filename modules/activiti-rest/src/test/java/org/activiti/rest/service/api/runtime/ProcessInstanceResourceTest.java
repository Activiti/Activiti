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

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Test for all REST-operations related to a single Process instance resource.
 * 
 * @author Frederik Heremans
 */
public class ProcessInstanceResourceTest extends BaseRestTestCase {

  /**
   * Test getting a single process instance.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testGetProcessInstance() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", "myBusinessKey");
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    // Check resulting instance
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("id").getTextValue());
    assertEquals("myBusinessKey", responseNode.get("businessKey").getTextValue());
    assertEquals("processTask", responseNode.get("activityId").getTextValue());
    assertFalse(responseNode.get("suspended").getBooleanValue());
    
    assertTrue(responseNode.get("url").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId())));
    assertTrue(responseNode.get("processDefinitionUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, encode(processInstance.getProcessDefinitionId()))));
  }
  
  /**
   * Test getting an unexisting process instance.
   */
  public void testGetUnexistingProcessInstance() {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, "unexistingpi"));
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a process instance with id 'unexistingpi'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test deleting a single process instance.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testDeleteProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", "myBusinessKey");
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
    client.delete();
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    
    // Check if process-instance is gone
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }
  
  /**
   * Test deleting an unexisting process instance.
   */
  public void testDeleteUnexistingProcessInstance() {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, "unexistingpi"));
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a process instance with id 'unexistingpi'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test suspending a single process instance.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testSuspendProcessInstance() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", "myBusinessKey");
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "suspend");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
    Representation response = client.put(requestNode);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    // Check engine id instance is suspended
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().processInstanceId(processInstance.getId()).count());
    
    // Check resulting instance is suspended
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("id").getTextValue());
    assertTrue(responseNode.get("suspended").getBooleanValue());
    
    // Suspending again should result in conflict
    try {
      client.put(requestNode);
      fail("Expected exception");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
      assertEquals("Process instance with id '" + processInstance.getId() + "' is already suspended.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test suspending a single process instance.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testActivateProcessInstance() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", "myBusinessKey");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "activate");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
    Representation response = client.put(requestNode);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    // Check engine id instance is suspended
    assertEquals(1, runtimeService.createProcessInstanceQuery().active().processInstanceId(processInstance.getId()).count());
    
    // Check resulting instance is suspended
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("id").getTextValue());
    assertFalse(responseNode.get("suspended").getBooleanValue());
    
    // Activating again should result in conflict
    try {
      client.put(requestNode);
      fail("Expected exception");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
      assertEquals("Process instance with id '" + processInstance.getId() + "' is already active.", expected.getStatus().getDescription());
    }
  }
}
