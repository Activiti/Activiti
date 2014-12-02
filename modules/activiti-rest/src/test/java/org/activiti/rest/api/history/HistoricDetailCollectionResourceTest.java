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

package org.activiti.rest.api.history;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * Test for REST-operation related to the historic detail query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricDetailCollectionResourceTest extends BaseSpringRestTestCase {
  
  /**
   * Test querying historic detail. 
   * GET history/historic-detail
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

    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_DETAIL);
    
    assertResultsPresentInDataResponse(url + "?processInstanceId=" + processInstance.getId(), 5, "stringVar", "Azerty");
    assertResultsPresentInDataResponse(url + "?taskId=" + task.getId(), 1, "taskVariable", "test");
    assertResultsPresentInDataResponse(url + "?processInstanceId=" + processInstance2.getId(), 4, "intVar", 67890);
    assertResultsPresentInDataResponse(url + "?processInstanceId=" + processInstance2.getId() + "&selectOnlyVariableUpdates=true", 4, "booleanVar", false);
    
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        url + "?processInstanceId=" + processInstance2.getId()), HttpStatus.SC_OK);
    
    // Check status and size
    JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
    closeResponse(response);
    
    boolean byteVarFound = false;
    Iterator<JsonNode> it = dataNode.iterator();
    while(it.hasNext()) {
      JsonNode variableNode = it.next().get("variable");
      String name = variableNode.get("name").textValue();
      if ("byteVar".equals(name)) {
        byteVarFound = true;
        String valueUrl = variableNode.get("valueUrl").textValue();
        
        response = executeRequest(new HttpGet(valueUrl), HttpStatus.SC_OK);
        byte[] varInput = IOUtils.toByteArray(response.getEntity().getContent());
        closeResponse(response);
        assertEquals("test", new String(varInput));
        break;
      }
    }
    assertTrue(byteVarFound);
  }
  
  protected void assertResultsPresentInDataResponse(String url, int numberOfResultsExpected, String variableName, Object variableValue) throws JsonProcessingException, IOException {
    
    // Do the actual call
  	CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + url), HttpStatus.SC_OK);
    
    JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
    closeResponse(response);
    
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
  }
}
