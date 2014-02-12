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

import java.util.HashMap;

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


/**
 * Test for all REST-operations related to the process instance query resource.
 * 
 * @author Frederik Heremans
 */
public class ProcessInstanceQueryResourceTest extends BaseRestTestCase {
  
  /**
   * Test querying process instance based on variables. 
   * POST query/process-instances
   */
  @Deployment
  public void testQueryProcessInstancesWithVariables() throws Exception {
    HashMap<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringVar", "Azerty");
    processVariables.put("intVar", 67890);
    processVariables.put("booleanVar", false);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);

    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_QUERY);
    
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
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());

    // Integer equals
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());
    
    // Boolean equals
    variableNode.removeAll();
    variableNode.put("name", "booleanVar");
    variableNode.put("value", false);
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());
    
    // String not equals
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "ghijkl");
    variableNode.put("operation", "notEquals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());

    // Integer not equals
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 45678);
    variableNode.put("operation", "notEquals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());
    
    // Boolean not equals
    variableNode.removeAll();
    variableNode.put("name", "booleanVar");
    variableNode.put("value", true);
    variableNode.put("operation", "notEquals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());
    
    // String equals ignore case
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "azeRTY");
    variableNode.put("operation", "equalsIgnoreCase");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());
    
    // String not equals ignore case
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "HIJKLm");
    variableNode.put("operation", "notEqualsIgnoreCase");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());
    
    // String equals without value
    variableNode.removeAll();
    variableNode.put("value", "Azerty");
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, processInstance.getId());
    
    // String equals with non existing value
    variableNode.removeAll();
    variableNode.put("value", "Azerty2");
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode);
  }
  
  
  /**
   * Test querying process instance based on variables. 
   * POST query/process-instances
   */
  @Deployment
  public void testQueryProcessInstancesPagingAndSorting() throws Exception {
  	ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("aOneTaskProcess");
  	ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("bOneTaskProcess");
  	ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("cOneTaskProcess");
  	
  	// Create request node
  	ObjectNode requestNode = objectMapper.createObjectNode();
  	requestNode.put("order", "desc");
  	requestNode.put("sort", "processDefinitionKey");
    
  	String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_QUERY);
  	ClientResource client = getAuthenticatedClient(url);
    Representation response = client.post(requestNode);
    
    // Check order
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode rootNode = objectMapper.readTree(response.getStream());
    JsonNode dataNode = rootNode.get("data");
    assertEquals(3, dataNode.size());
    
    assertEquals(processInstance3.getId(), dataNode.get(0).get("id").asText());
    assertEquals(processInstance2.getId(), dataNode.get(1).get("id").asText());
    assertEquals(processInstance1.getId(), dataNode.get(2).get("id").asText());
    response.release();
    
    // Check paging size
    requestNode = objectMapper.createObjectNode();
  	requestNode.put("start", 0);
  	requestNode.put("size", 1);
  	
  	response = client.post(requestNode);
    
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    rootNode = objectMapper.readTree(response.getStream());
    dataNode = rootNode.get("data");
    assertEquals(1, dataNode.size());
    
    // Check paging start and size
    requestNode = objectMapper.createObjectNode();
  	requestNode.put("start", 1);
  	requestNode.put("size", 1);
  	requestNode.put("order", "desc");
  	requestNode.put("sort", "processDefinitionKey");
    
  	response = client.post(requestNode);
    
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    rootNode = objectMapper.readTree(response.getStream());
    dataNode = rootNode.get("data");
    assertEquals(1, dataNode.size());
    assertEquals(processInstance2.getId(), dataNode.get(0).get("id").asText());
  }
  
}
