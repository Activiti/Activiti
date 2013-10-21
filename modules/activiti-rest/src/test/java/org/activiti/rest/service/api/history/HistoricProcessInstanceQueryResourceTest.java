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

package org.activiti.rest.service.api.history;

import java.util.HashMap;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;


/**
 * Test for REST-operation related to the historic process instance query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricProcessInstanceQueryResourceTest extends BaseRestTestCase {
  
  /**
   * Test querying historic process instance based on variables. 
   * POST query/historic-process-instances
   */
  @Deployment
  public void testQueryProcessInstancesWithVariables() throws Exception {
    HashMap<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringVar", "Azerty");
    processVariables.put("intVar", 67890);
    processVariables.put("booleanVar", false);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);

    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE_QUERY);
    
    // Process variables
    ObjectNode requestNode = objectMapper.createObjectNode();
    ArrayNode variableArray = objectMapper.createArrayNode();
    ObjectNode variableNode = objectMapper.createObjectNode();
    variableArray.add(variableNode);
    requestNode.put("variables", variableArray);
    
    // String equals
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Azerty");
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());

    // Integer equals
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());
    
    // Boolean equals
    variableNode.removeAll();
    variableNode.put("name", "booleanVar");
    variableNode.put("value", false);
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());
    
    // String not equals
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "ghijkl");
    variableNode.put("operation", "notEquals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());

    // Integer not equals
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 45678);
    variableNode.put("operation", "notEquals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());
    
    // Boolean not equals
    variableNode.removeAll();
    variableNode.put("name", "booleanVar");
    variableNode.put("value", true);
    variableNode.put("operation", "notEquals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());
    
    // String equals ignore case
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "azeRTY");
    variableNode.put("operation", "equalsIgnoreCase");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());
    
    // String not equals ignore case (not supported)
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "HIJKLm");
    variableNode.put("operation", "notEqualsIgnoreCase");
    assertErrorResult(url, requestNode, Status.CLIENT_ERROR_BAD_REQUEST);
    
    // String equals without value
    variableNode.removeAll();
    variableNode.put("value", "Azerty");
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());
    
    // String equals with non existing value
    variableNode.removeAll();
    variableNode.put("value", "Azerty2");
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("finished", true);
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("finished", false);
    assertResultsPresentInDataResponse(url, requestNode, processInstance2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", processInstance.getProcessDefinitionId());
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "oneTaskProcess");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId(), processInstance2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "oneTaskProcess");
    
    ClientResource client = getAuthenticatedClient(url + "?sort=startTime");
    Representation response = client.post(requestNode);
    
    // Check status and size
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
    assertEquals(2, dataNode.size());
    assertEquals(processInstance.getId(), dataNode.get(0).get("id").asText());
    assertEquals(processInstance2.getId(), dataNode.get(1).get("id").asText());
  }
}
