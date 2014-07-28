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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Test for REST-operation related to the historic detail query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricDetailQueryResourceTest extends BaseRestTestCase {
  
  /**
   * Test querying historic detail. 
   * POST query/historic-detail
   */
  @Deployment
  public void testQueryDetail() throws Exception {
    HashMap<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringVar", "Azerty");
    processVariables.put("intVar", 67890);
    processVariables.put("booleanVar", false);
    processVariables.put("byteVar", "test".getBytes());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "taskVariable", "test");
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);

    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_DETAIL_QUERY);
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance.getId());
    assertResultsPresentInDataResponse(url, requestNode, 5, "stringVar", "Azerty");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskId", task.getId());
    assertResultsPresentInDataResponse(url, requestNode, 1, "taskVariable", "test");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance2.getId());
    assertResultsPresentInDataResponse(url, requestNode, 4, "intVar", 67890);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance2.getId());
    requestNode.put("selectOnlyFormProperties", true);
    assertResultsPresentInDataResponse(url, requestNode, 0, null, null);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance2.getId());
    requestNode.put("selectOnlyVariableUpdates", true);
    assertResultsPresentInDataResponse(url, requestNode, 4, "booleanVar", false);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance2.getId());
    ClientResource client = getAuthenticatedClient(url);
    Representation response = client.post(requestNode);
    
    // Check status and size
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
    
    boolean byteVarFound = false;
    Iterator<JsonNode> it = dataNode.iterator();
    while(it.hasNext()) {
      JsonNode variableNode = it.next().get("variable");
      String name = variableNode.get("name").textValue();
      if ("byteVar".equals(name)) {
        byteVarFound = true;
        String valueUrl = variableNode.get("valueUrl").textValue();
        client.release();
        client = new ClientResource(valueUrl);
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "kermit", "kermit");
        response = client.get();
        assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
        byte[] varInput = IOUtils.toByteArray(response.getStream());
        assertEquals("test", new String(varInput));
        break;
      }
    }
    assertTrue(byteVarFound);
  }
  
  protected void assertResultsPresentInDataResponse(String url, ObjectNode body, int numberOfResultsExpected, String variableName, Object variableValue) throws JsonProcessingException, IOException {
    
    // Do the actual call
    ClientResource client = getAuthenticatedClient(url);
    Representation response = client.post(body);
    
    // Check status and size
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
    assertEquals(numberOfResultsExpected, dataNode.size());

    // Check presence of ID's
    if (variableName != null) {
      boolean variableFound = false;
      Iterator<JsonNode> it = dataNode.iterator();
      while(it.hasNext()) {
        JsonNode variableNode = it.next().get("variable");
        String name = variableNode.get("name").textValue();
        if (variableName.equals(name)) {
          variableFound = true;
          if (variableValue instanceof Boolean) {
            assertTrue("Variable value is not equal", variableNode.get("value").asBoolean() == (Boolean) variableValue);
          } else if (variableValue instanceof Integer) {
            assertTrue("Variable value is not equal", variableNode.get("value").asInt() == (Integer) variableValue);
          } else {
            assertTrue("Variable value is not equal", variableNode.get("value").asText().equals((String) variableValue));
          }
        }
      }
      assertTrue("Variable " + variableName + " is missing", variableFound);
    }
    
    client.release();
  }
}
