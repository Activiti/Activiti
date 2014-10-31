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
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Test for REST-operation related to the historic detail query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricDetailQueryResourceTest extends BaseSpringRestTestCase {
  
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
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + url);
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, 200);
    
    // Check status and size
    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
    closeResponse(response);
    
    boolean byteVarFound = false;
    Iterator<JsonNode> it = dataNode.iterator();
    while (it.hasNext()) {
      JsonNode variableNode = it.next().get("variable");
      String name = variableNode.get("name").textValue();
      if ("byteVar".equals(name)) {
        byteVarFound = true;
        String valueUrl = variableNode.get("valueUrl").textValue();
        response = executeRequest(new HttpGet(valueUrl), 200);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        byte[] varInput = IOUtils.toByteArray(response.getEntity().getContent());
        closeResponse(response);
        assertEquals("test", new String(varInput));
        break;
      }
    }
    assertTrue(byteVarFound);
  }
  
  protected void assertResultsPresentInDataResponse(String url, ObjectNode body, int numberOfResultsExpected, 
      String variableName, Object variableValue) throws JsonProcessingException, IOException {
    
    // Do the actual call
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + url);
    httpPost.setEntity(new StringEntity(body.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, 200);
    
    // Check status and size
    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
    closeResponse(response);
    assertEquals(numberOfResultsExpected, dataNode.size());

    // Check presence of ID's
    if (variableName != null) {
      boolean variableFound = false;
      Iterator<JsonNode> it = dataNode.iterator();
      while (it.hasNext()) {
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
  }
}
