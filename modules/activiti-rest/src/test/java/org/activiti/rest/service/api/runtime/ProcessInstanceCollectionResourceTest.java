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

import java.util.Calendar;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
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
public class ProcessInstanceCollectionResourceTest extends BaseRestTestCase {

  /**
   * Test getting a list of process instance, using all possible filters.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testGetProcessInstances() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", "myBusinessKey");
    String id = processInstance.getId();
    runtimeService.addUserIdentityLink(id, "kermit", "whatever");
    
    // Test without any parameters
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION);
    assertResultsPresentInDataResponse(url, id);
    
    
    // Process instance id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?id=" + id;
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?id=anotherId";
    assertResultsPresentInDataResponse(url);
    
    // Process instance business key
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?businessKey=myBusinessKey";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?businessKey=anotherBusinessKey";
    assertResultsPresentInDataResponse(url);
    
    // Process definition key
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?processDefinitionKey=processOne";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?processDefinitionKey=processTwo";
    assertResultsPresentInDataResponse(url);
    
    // Process definition id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?processDefinitionId=" + processInstance.getProcessDefinitionId();
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?processDefinitionId=anotherId";
    assertResultsPresentInDataResponse(url);
    
     // Involved user
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?involvedUser=kermit";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?involvedUser=gonzo";
    assertResultsPresentInDataResponse(url);
    
    // Active process
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?suspended=false";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?suspended=true";
    assertResultsPresentInDataResponse(url);
    
    // Suspended process
    runtimeService.suspendProcessInstanceById(id);
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?suspended=true";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?suspended=false";
    assertResultsPresentInDataResponse(url);
    runtimeService.activateProcessInstanceById(id);
    
    // Complete first task in the process to have a subprocess created
    taskService.complete(taskService.createTaskQuery().processInstanceId(id).singleResult().getId());
    
    ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().superProcessInstanceId(id).singleResult();
    assertNotNull(subProcess);
    
    // Super-process instance id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?superProcessInstanceId=" + id;
    assertResultsPresentInDataResponse(url, subProcess.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?superProcessInstanceId=anotherId";
    assertResultsPresentInDataResponse(url);
    
    // Sub-process instance id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?subProcessInstanceId=" + subProcess.getId();
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?subProcessInstanceId=anotherId";
    assertResultsPresentInDataResponse(url);
  }
  
  /**
   * Test starting a process instance using procDefinitionId, key procDefinitionKey business-key.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testStartProcess() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Start using process definition key
    requestNode.put("processDefinitionKey", "processOne");
    Representation response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("id").getTextValue());
    assertTrue(responseNode.get("businessKey").isNull());
    assertEquals("processTask", responseNode.get("activityId").getTextValue());
    assertFalse(responseNode.get("suspended").getBooleanValue());
    
    assertTrue(responseNode.get("url").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId())));
    assertTrue(responseNode.get("processDefinitionUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, encode(processInstance.getProcessDefinitionId()))));
    runtimeService.deleteProcessInstance(processInstance.getId(), "testing");
    
    // Start using process definition id
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("processOne").singleResult().getId());
    response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("id").getTextValue());
    assertTrue(responseNode.get("businessKey").isNull());
    assertEquals("processTask", responseNode.get("activityId").getTextValue());
    assertFalse(responseNode.get("suspended").getBooleanValue());
    
    assertTrue(responseNode.get("url").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId())));
    assertTrue(responseNode.get("processDefinitionUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, encode(processInstance.getProcessDefinitionId()))));
    runtimeService.deleteProcessInstance(processInstance.getId(), "testing");
    
    // Start using message
    requestNode = objectMapper.createObjectNode();
    requestNode.put("message", "newInvoiceMessage");
    response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("id").getTextValue());
    assertTrue(responseNode.get("businessKey").isNull());
    assertEquals("processTask", responseNode.get("activityId").getTextValue());
    assertFalse(responseNode.get("suspended").getBooleanValue());
    
    assertTrue(responseNode.get("url").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId())));
    assertTrue(responseNode.get("processDefinitionUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, encode(processInstance.getProcessDefinitionId()))));
    
    // Start using process definition id and business key
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("processOne").singleResult().getId());
    requestNode.put("businessKey", "myBusinessKey");
    response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("myBusinessKey", responseNode.get("businessKey").getTextValue());
  }
  
  
  /**
   * Test starting a process instance passing in variables to set.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testStartProcessWithVariables() throws Exception {
    ArrayNode variablesNode = objectMapper.createArrayNode();
    
    // String variable
    ObjectNode stringVarNode = variablesNode.addObject();
    stringVarNode.put("name", "stringVariable");
    stringVarNode.put("value", "simple string value");
    stringVarNode.put("type", "string");

    ObjectNode integerVarNode = variablesNode.addObject();
    integerVarNode.put("name", "integerVariable");
    integerVarNode.put("value", 1234);
    integerVarNode.put("type", "integer");
    
    ObjectNode shortVarNode = variablesNode.addObject();
    shortVarNode.put("name", "shortVariable");
    shortVarNode.put("value", 123);
    shortVarNode.put("type", "short");
    
    ObjectNode longVarNode = variablesNode.addObject();
    longVarNode.put("name", "longVariable");
    longVarNode.put("value", 4567890L);
    longVarNode.put("type", "long");
    
    ObjectNode doubleVarNode = variablesNode.addObject();
    doubleVarNode.put("name", "doubleVariable");
    doubleVarNode.put("value", 123.456);
    doubleVarNode.put("type", "double");
    
    ObjectNode booleanVarNode = variablesNode.addObject();
    booleanVarNode.put("name", "booleanVariable");
    booleanVarNode.put("value", Boolean.TRUE);
    booleanVarNode.put("type", "boolean");
    
    // Date
    Calendar varCal = Calendar.getInstance();
    String isoString = getISODateString(varCal.getTime());
    ObjectNode dateVarNode = variablesNode.addObject();
    dateVarNode.put("name", "dateVariable");
    dateVarNode.put("value", isoString);
    dateVarNode.put("type", "date");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Start using process definition key, passing in variables
    requestNode.put("processDefinitionKey", "processOne");
    requestNode.put("variables", variablesNode);
    
    client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    
    // Check if engine has correct variables set
    Map<String, Object> processVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(7, processVariables.size());
    
    assertEquals("simple string value", processVariables.get("stringVariable"));
    assertEquals(1234, processVariables.get("integerVariable"));
    assertEquals((short)123, processVariables.get("shortVariable"));
    assertEquals(4567890L, processVariables.get("longVariable"));
    assertEquals(123.456, processVariables.get("doubleVariable"));
    assertEquals(Boolean.TRUE, processVariables.get("booleanVariable"));
    assertEquals(varCal.getTime(), processVariables.get("dateVariable"));
  }
  
  /**
   * Test starting a process instance, covering all edge-cases.
   */
  public void testStartProcessExceptions() throws Exception {
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Try starting without id and key
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Either processDefinitionId, processDefinitionKey or message is required.", expected.getStatus().getDescription());
    }
    client.release();
    
    // Try starting with both id and key
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", "123");
    requestNode.put("processDefinitionKey", "456");
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Only one of processDefinitionId, processDefinitionKey or message should be set.", expected.getStatus().getDescription());
    }
    client.release();
    
    // Try starting with both message and key
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", "123");
    requestNode.put("message", "456");
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Only one of processDefinitionId, processDefinitionKey or message should be set.", expected.getStatus().getDescription());
    }
    client.release();
    
    // Try starting with unexisting process definition key
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "123");
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("no processes deployed with key '123'", expected.getStatus().getDescription());
    }
    client.release();
    
    // Try starting with unexisting process definition id
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", "123");
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("no deployed process definition found with id '123'", expected.getStatus().getDescription());
    }
    client.release();
    
    // Try starting with unexisting message
    requestNode = objectMapper.createObjectNode();
    requestNode.put("message", "unexistingmessage");
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Cannot start process instance by message: no subscription to message with name 'unexistingmessage' found.", expected.getStatus().getDescription());
    }
  }
}
