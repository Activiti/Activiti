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

import org.activiti.engine.runtime.Execution;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to a single execution resource.
 * 
 * @author Frederik Heremans
 */
public class ExecutionResourceTest extends BaseSpringRestTestCase {

  /**
   * Test getting a single execution.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testGetExecution() throws Exception {
    Execution parentExecution = runtimeService.startProcessInstanceByKey("processOne");
    Execution childExecution = runtimeService.createExecutionQuery().activityId("processTask").singleResult();
    assertNotNull(childExecution);
    
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, parentExecution.getId())), HttpStatus.SC_OK);
    
    // Check resulting parent execution
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals(parentExecution.getId(), responseNode.get("id").textValue());
    assertTrue(responseNode.get("activityId").isNull());
    assertFalse(responseNode.get("suspended").booleanValue());
    assertTrue(responseNode.get("parentUrl").isNull());
    assertFalse(responseNode.get("suspended").booleanValue());
    
    assertTrue(responseNode.get("url").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, parentExecution.getId())));
    
    assertTrue(responseNode.get("processInstanceUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, parentExecution.getId())));
    
    // Check resulting child execution
    response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, childExecution.getId())), HttpStatus.SC_OK);
    
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals(childExecution.getId(), responseNode.get("id").textValue());
    assertEquals("processTask", responseNode.get("activityId").textValue());
    assertFalse(responseNode.get("suspended").booleanValue());
    assertFalse(responseNode.get("suspended").booleanValue());
    
    assertTrue(responseNode.get("url").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, childExecution.getId())));
    
    assertTrue(responseNode.get("parentUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, parentExecution.getId())));
    
    assertTrue(responseNode.get("processInstanceUrl").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, parentExecution.getId())));
  }
  
  /**
   * Test getting an unexisting execution.
   */
  public void testGetUnexistingExecution() throws Exception {
  	CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, "unexisting")), HttpStatus.SC_NOT_FOUND);
  	closeResponse(response);
  }
  
  /**
   * Test signalling a single execution, without signal name.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-signal.bpmn20.xml"})
  public void testSignalExecution() throws Exception {
    Execution signalExecution = runtimeService.startProcessInstanceByKey("processOne");
    assertNotNull(signalExecution);
    assertEquals("waitState", signalExecution.getActivityId());
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "signal");

    // Signalling one causes process to move on to second signal and execution is not finished yet
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, signalExecution.getId()));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertEquals("anotherWaitState", responseNode.get("activityId").textValue());
    assertEquals("anotherWaitState", runtimeService.createExecutionQuery().executionId(signalExecution.getId()).singleResult().getActivityId());
    
    // Signalling again causes process to end
    response = executeRequest(httpPut, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);
    
    // Check if process is actually ended
    assertNull(runtimeService.createExecutionQuery().executionId(signalExecution.getId()).singleResult());
  }
  
  /**
   * Test signalling a single execution, without signal name.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-signal-event.bpmn20.xml"})
  public void testSignalEventExecution() throws Exception {
    Execution signalExecution = runtimeService.startProcessInstanceByKey("processOne");
    assertNotNull(signalExecution);
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "signalEventReceived");
    requestNode.put("signalName", "unexisting");
    
    Execution waitingExecution = runtimeService.createExecutionQuery().activityId("waitState").singleResult();
    assertNotNull(waitingExecution);
    
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, waitingExecution.getId()));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    closeResponse(response);
    
    requestNode.put("signalName", "alert");
    
    // Sending signal event causes the execution to end (scope-execution for the catching event)
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpPut, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);
    
    // Check if process is moved on to the other wait-state
    waitingExecution = runtimeService.createExecutionQuery().activityId("anotherWaitState").singleResult();
    assertNotNull(waitingExecution);
    assertEquals(signalExecution.getId(), waitingExecution.getId());
    
  }
  
  /**
   * Test signalling a single execution, with signal event.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-signal-event.bpmn20.xml"})
  public void testSignalEventExecutionWithvariables() throws Exception {
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
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, waitingExecution.getId()));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);
    
    // Check if process is moved on to the other wait-state
    waitingExecution = runtimeService.createExecutionQuery().activityId("anotherWaitState").singleResult();
    assertNotNull(waitingExecution);
    assertEquals(signalExecution.getId(), waitingExecution.getId());
    
    Map<String, Object> vars = runtimeService.getVariables(waitingExecution.getId());
    assertEquals(1, vars.size());
    
    assertEquals("Variable set when signal event is receieved", vars.get("myVar"));
  }
  
  /**
   * Test signalling a single execution, without signal event and variables.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-message-event.bpmn20.xml"})
  public void testMessageEventExecution() throws Exception {
    Execution execution = runtimeService.startProcessInstanceByKey("processOne");
    assertNotNull(execution);
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "messageEventReceived");
    requestNode.put("messageName", "unexisting");
    Execution waitingExecution = runtimeService.createExecutionQuery().activityId("waitState").singleResult();
    assertNotNull(waitingExecution);
    
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, waitingExecution.getId()));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    closeResponse(response);
    
    requestNode.put("messageName", "paymentMessage");
    
    // Sending signal event causes the execution to end (scope-execution for the catching event)
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpPut, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);
    
    // Check if process is moved on to the other wait-state
    waitingExecution = runtimeService.createExecutionQuery().activityId("anotherWaitState").singleResult();
    assertNotNull(waitingExecution);
    assertEquals(execution.getId(), waitingExecution.getId());
  }
  
  /**
   * Test messaging a single execution with variables.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-message-event.bpmn20.xml"})
  public void testMessageEventExecutionWithvariables() throws Exception {
    Execution signalExecution = runtimeService.startProcessInstanceByKey("processOne");
    assertNotNull(signalExecution);
    
    ArrayNode variables = objectMapper.createArrayNode();
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "messageEventReceived");
    requestNode.put("messageName", "paymentMessage");
    requestNode.put("variables", variables);
    
    ObjectNode varNode = objectMapper.createObjectNode();
    variables.add(varNode);
    varNode.put("name", "myVar");
    varNode.put("value", "Variable set when signal event is receieved");
    
    Execution waitingExecution = runtimeService.createExecutionQuery().activityId("waitState").singleResult();
    assertNotNull(waitingExecution);
    
    // Sending signal event causes the execution to end (scope-execution for the catching event)
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, waitingExecution.getId()));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);
    
    // Check if process is moved on to the other wait-state
    waitingExecution = runtimeService.createExecutionQuery().activityId("anotherWaitState").singleResult();
    assertNotNull(waitingExecution);
    assertEquals(signalExecution.getId(), waitingExecution.getId());
    
    Map<String, Object> vars = runtimeService.getVariables(waitingExecution.getId());
    assertEquals(1, vars.size());
    
    assertEquals("Variable set when signal event is receieved", vars.get("myVar"));
  }
  
  /**
   * Test executing an illegal action on an execution.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
  public void testIllegalExecutionAction() throws Exception {
    Execution execution = runtimeService.startProcessInstanceByKey("processOne");
    assertNotNull(execution);
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "badaction");
    
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, execution.getId()));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_BAD_REQUEST);
    closeResponse(response);
  }
}