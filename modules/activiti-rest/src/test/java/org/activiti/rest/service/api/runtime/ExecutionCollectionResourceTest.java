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

import java.util.Map;

import org.activiti.engine.impl.cmd.ChangeDeploymentTenantIdCmd;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to the execution collection.
 * 
 * @author Frederik Heremans
 */
public class ExecutionCollectionResourceTest extends BaseSpringRestTestCase {

  /**
   * Test getting a list of executions, using all possible filters.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testGetExecutions() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", "myBusinessKey");
    String id = processInstance.getId();
    runtimeService.addUserIdentityLink(id, "kermit", "whatever");
    
    Execution childExecution = runtimeService.createExecutionQuery().activityId("processTask").singleResult();
    assertNotNull(childExecution);
    
    // Test without any parameters
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION);
    assertResultsPresentInDataResponse(url, id, childExecution.getId());
    
    // Process instance id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?id=" + id;
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?id=anotherId";
    assertResultsPresentInDataResponse(url);
    
    // Process instance business key
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?processInstanceBusinessKey=myBusinessKey";
    assertResultsPresentInDataResponse(url, id);
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?processInstanceBusinessKey=anotherBusinessKey";
    assertResultsPresentInDataResponse(url);
    
    // Process definition key
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?processDefinitionKey=processOne";
    assertResultsPresentInDataResponse(url, id, childExecution.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?processDefinitionKey=processTwo";
    assertResultsPresentInDataResponse(url);
    
    // Process definition id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?processDefinitionId=" + processInstance.getProcessDefinitionId();
    assertResultsPresentInDataResponse(url, id, childExecution.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?processDefinitionId=anotherId";
    assertResultsPresentInDataResponse(url);
    
    // Parent id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?parentId=" + id;
    assertResultsPresentInDataResponse(url, childExecution.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?parentId=anotherId";
    assertResultsPresentInDataResponse(url);
    
    // Activity id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?activityId=processTask";
    assertResultsPresentInDataResponse(url, childExecution.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?activityId=anotherId";
    assertResultsPresentInDataResponse(url);
    
    // Without tenant ID, before tenant is set
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?withoutTenantId=true";
    assertResultsPresentInDataResponse(url, id, childExecution.getId());
    
    // Update the tenant for the deployment
    managementService.executeCommand(new ChangeDeploymentTenantIdCmd(deploymentId, "myTenant"));
    
    // Without tenant ID, after tenant is set
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?withoutTenantId=true";
    assertResultsPresentInDataResponse(url);
    
    // Tenant id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?tenantId=myTenant";
    assertResultsPresentInDataResponse(url, id, childExecution.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?tenantId=myTenant2";
    assertResultsPresentInDataResponse(url);
    
    // Tenant id like
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?tenantIdLike=" + encode("%enant");
    assertResultsPresentInDataResponse(url, id, childExecution.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION) + "?tenantIdLike=" + encode("%whatever");
    assertResultsPresentInDataResponse(url);
  }
  
  /**
   *  Test signalling all executions
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-signal-event.bpmn20.xml"})
  public void testSignalEventExecutions() throws Exception {
    Execution signalExecution = runtimeService.startProcessInstanceByKey("processOne");
    assertNotNull(signalExecution);
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "signalEventReceived");
    requestNode.put("signalName", "alert");
    
    Execution waitingExecution = runtimeService.createExecutionQuery().activityId("waitState").singleResult();
    assertNotNull(waitingExecution);
    
    // Sending signal event causes the execution to end (scope-execution for the catching event)
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_NO_CONTENT));
    
    // Check if process is moved on to the other wait-state
    waitingExecution = runtimeService.createExecutionQuery().activityId("anotherWaitState").singleResult();
    assertNotNull(waitingExecution);
    assertEquals(signalExecution.getId(), waitingExecution.getId());
  }
  
  /**
   * Test signalling all executions with variables
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-signal-event.bpmn20.xml"})
  public void testSignalEventExecutionsWithvariables() throws Exception {
    Execution signalExecution = runtimeService.startProcessInstanceByKey("processOne");
    assertNotNull(signalExecution);
    
    ArrayNode variables = objectMapper.createArrayNode();
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "signalEventReceived");
    requestNode.put("signalName", "alert");
    requestNode.put("variables", variables);
    
    ObjectNode varNode = objectMapper.createObjectNode();
    variables.add(varNode);
    varNode.put("name", "myVar");
    varNode.put("value", "Variable set when signal event is receieved");
    
    Execution waitingExecution = runtimeService.createExecutionQuery().activityId("waitState").singleResult();
    assertNotNull(waitingExecution);
    
    // Sending signal event causes the execution to end (scope-execution for the catching event)
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_COLLECTION));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_NO_CONTENT));
    
    // Check if process is moved on to the other wait-state
    waitingExecution = runtimeService.createExecutionQuery().activityId("anotherWaitState").singleResult();
    assertNotNull(waitingExecution);
    assertEquals(signalExecution.getId(), waitingExecution.getId());
    
    Map<String, Object> vars = runtimeService.getVariables(waitingExecution.getId());
    assertEquals(1, vars.size());
    
    assertEquals("Variable set when signal event is receieved", vars.get("myVar"));
  }
}