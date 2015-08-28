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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cmd.ChangeDeploymentTenantIdCmd;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to a single Process instance resource.
 * 
 * @author Frederik Heremans
 * @author Saeid Mirzaei
 */
public class ProcessInstanceCollectionResourceTest extends BaseSpringRestTestCase {

  // check if process instance query with business key with and without includeProcess Variables
  // related to https://activiti.atlassian.net/browse/ACT-1992
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testGetProcessInstancesByBusinessKeyAndIncludeVariables() throws Exception {
  	HashMap<String, Object> variables = new HashMap<String, Object>();
  	variables.put("myVar1", "myVar1");
  	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", "myBusinessKey", variables);
  	String processId = processInstance.getId();

  	// check that the right process is returned with no variables
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?businessKey=myBusinessKey";
    
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + url), HttpStatus.SC_OK);
    
    JsonNode rootNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertTrue(rootNode.size() > 0 );
    assertEquals(1, rootNode.get("data").size());
    JsonNode dataNode = rootNode.get("data").get(0);
    assertEquals(processId, dataNode.get("id").asText());
    assertEquals(processInstance.getProcessDefinitionId(), dataNode.get("processDefinitionId").asText());
    assertTrue(dataNode.get("processDefinitionUrl").asText().contains(processInstance.getProcessDefinitionId()));
    JsonNode variableNodes = dataNode.get("variables");
    assertEquals(0, variableNodes.size());
   
    
    // check that the right process is returned along with the variables when includeProcessvariable is set
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?businessKey=myBusinessKey&includeProcessVariables=true";
	
    response = executeRequest(new HttpGet(SERVER_URL_PREFIX + url), HttpStatus.SC_OK);
    
    rootNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertTrue(rootNode.size() > 0 );
    assertEquals(1, rootNode.get("data").size());
    dataNode = rootNode.get("data").get(0);
    assertEquals(processId, dataNode.get("id").textValue());
    assertEquals(processInstance.getProcessDefinitionId(), dataNode.get("processDefinitionId").asText());
    assertTrue(dataNode.get("processDefinitionUrl").asText().contains(processInstance.getProcessDefinitionId()));
    variableNodes = dataNode.get("variables");
    assertEquals(1, variableNodes.size());
    
    variableNodes = dataNode.get("variables");
    assertEquals(1, variableNodes.size());
    assertNotNull(variableNodes.get(0).get("name"));
    assertNotNull(variableNodes.get(0).get("value"));
   
    assertEquals("myVar1", variableNodes.get(0).get("name").asText());
    assertEquals("myVar1", variableNodes.get(0).get("value").asText());
  }
  
	
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
   * Test getting a list of process instance, using all tenant filters.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testGetProcessInstancesTenant() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", "myBusinessKey");
    String id = processInstance.getId();
    
    // Test without tenant id
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?withoutTenantId=true";
    assertResultsPresentInDataResponse(url, id);
    
    // Update the tenant for the deployment
    managementService.executeCommand(new ChangeDeploymentTenantIdCmd(deploymentId, "myTenant"));
    
    // Test tenant id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?tenantId=myTenant";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?tenantId=anotherTenant";
    assertResultsPresentInDataResponse(url);
    
    // Test tenant id like
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?tenantIdLike=" + encode("%enant");
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?tenantIdLike=" + encode("%what");
    assertResultsPresentInDataResponse(url);
    
    // Test without tenant id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION) + "?withoutTenantId=true";
    assertResultsPresentInDataResponse(url);
  }
  
  
  
  /**
   * Test starting a process instance using procDefinitionId, key procDefinitionKey business-key.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testStartProcess() throws Exception {
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Start using process definition key
    requestNode.put("processDefinitionKey", "processOne");
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(historicProcessInstance);
    assertEquals("kermit", historicProcessInstance.getStartUserId());
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("id").textValue());
    assertTrue(responseNode.get("businessKey").isNull());
    assertEquals("processTask", responseNode.get("activityId").textValue());
    assertFalse(responseNode.get("suspended").booleanValue());
    
    assertTrue(responseNode.get("url").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId())));
    assertTrue(responseNode.get("processDefinitionUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId())));
    runtimeService.deleteProcessInstance(processInstance.getId(), "testing");
    
    // Start using process definition id
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("processOne").singleResult().getId());
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("id").textValue());
    assertTrue(responseNode.get("businessKey").isNull());
    assertEquals("processTask", responseNode.get("activityId").textValue());
    assertFalse(responseNode.get("suspended").booleanValue());
    
    assertTrue(responseNode.get("url").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId())));
    assertTrue(responseNode.get("processDefinitionUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId())));
    runtimeService.deleteProcessInstance(processInstance.getId(), "testing");
    
    // Start using message
    requestNode = objectMapper.createObjectNode();
    requestNode.put("message", "newInvoiceMessage");
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("id").textValue());
    assertTrue(responseNode.get("businessKey").isNull());
    assertEquals("processTask", responseNode.get("activityId").textValue());
    assertFalse(responseNode.get("suspended").booleanValue());
    
    assertTrue(responseNode.get("url").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId())));
    assertTrue(responseNode.get("processDefinitionUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId())));
    
    // Start using process definition id and business key
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("processOne").singleResult().getId());
    requestNode.put("businessKey", "myBusinessKey");
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("myBusinessKey", responseNode.get("businessKey").textValue());
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
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Start using process definition key, passing in variables
    requestNode.put("processDefinitionKey", "processOne");
    requestNode.put("variables", variablesNode);
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertEquals("processTask", responseNode.get("activityId").asText());
    assertEquals(false, responseNode.get("ended").asBoolean());
    JsonNode variablesArrayNode = responseNode.get("variables");
    assertEquals(0, variablesArrayNode.size());
    
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
    assertEquals(dateFormat.parse(isoString), processVariables.get("dateVariable"));
  }
  
  /**
   * Test starting a process instance passing in variables to set.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testStartProcessWithVariablesAndReturnVariables() throws Exception {
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
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Start using process definition key, passing in variables
    requestNode.put("processDefinitionKey", "processOne");
    requestNode.put("returnVariables", true);
    requestNode.put("variables", variablesNode);
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertEquals("processTask", responseNode.get("activityId").asText());
    assertEquals(false, responseNode.get("ended").asBoolean());
    JsonNode variablesArrayNode = responseNode.get("variables");
    assertEquals(2, variablesArrayNode.size());
    for (JsonNode variableNode : variablesArrayNode) {
      if ("stringVariable".equals(variableNode.get("name").asText())) {
        assertEquals("simple string value", variableNode.get("value").asText());
        assertEquals("string", variableNode.get("type").asText());
        
      } else if ("integerVariable".equals(variableNode.get("name").asText())) {
        assertEquals(1234, variableNode.get("value").asInt());
        assertEquals("integer", variableNode.get("type").asText());
      
      } else {
        fail("Unexpected variable " + variableNode.get("name").asText());
      }
    }
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    
    // Check if engine has correct variables set
    Map<String, Object> processVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(2, processVariables.size());
    
    assertEquals("simple string value", processVariables.get("stringVariable"));
    assertEquals(1234, processVariables.get("integerVariable"));
  }
  
  @Deployment(resources = {"org/activiti/rest/service/api/oneTaskProcess.bpmn20.xml"})
  public void testStartProcessUsingKeyAndTenantId() throws Exception {
  	org.activiti.engine.repository.Deployment tenantDeployment = null;
  	
  	try {
	  	// Deploy the same process, in another tenant
	  	tenantDeployment = repositoryService.createDeployment()
  			.addClasspathResource("org/activiti/rest/service/api/oneTaskProcess.bpmn20.xml")
  			.tenantId("tenant1")
  			.deploy();
  	
  	ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Start using process definition key, in tenant 1
    requestNode.put("processDefinitionKey", "oneTaskProcess");
    requestNode.put("tenantId", "tenant1");
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    closeResponse(response);
    
    // Only one process should have been started
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    assertEquals("tenant1", processInstance.getTenantId());
    
    // Start using an unexisting tenant
    requestNode.put("processDefinitionKey", "oneTaskProcess");
    requestNode.put("tenantId", "tenantThatDoesntExist");
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST);
    closeResponse(response);
    
  	} finally {
  		// Cleanup deployment in tenant
  		if(tenantDeployment != null) {
  			repositoryService.deleteDeployment(tenantDeployment.getId(), true);
  		}
  	}
  }
  
  /**
   * Test starting a process instance, covering all edge-cases.
   */
  public void testStartProcessExceptions() throws Exception {
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Try starting without id and key
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
    
    // Try starting with both id and key
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", "123");
    requestNode.put("processDefinitionKey", "456");
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
    
    // Try starting with both message and key
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", "123");
    requestNode.put("message", "456");
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
    
    // Try starting with unexisting process definition key
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "123");
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
    
    // Try starting with unexisting process definition id
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", "123");
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
    
    // Try starting with unexisting message
    requestNode = objectMapper.createObjectNode();
    requestNode.put("message", "unexistingmessage");
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
  }
  
  /**
   * Explicitly testing the statelessness of the Rest API.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ProcessInstanceResourceTest.process-one.bpmn20.xml"})
  public void testStartProcessWithSameHttpClient() throws Exception {
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    // Start using process definition key
    requestNode.put("processDefinitionKey", "processOne");
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    
    // First call
    closeResponse(executeRequest(httpPost, HttpStatus.SC_CREATED));
    
    // Second call
    closeResponse(executeRequest(httpPost, HttpStatus.SC_CREATED));
    
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    assertEquals(2, processInstances.size());
    for (ProcessInstance processInstance :  processInstances) {
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
      assertNotNull(historicProcessInstance);
      assertEquals("kermit", historicProcessInstance.getStartUserId());
    }
    
  }
}
