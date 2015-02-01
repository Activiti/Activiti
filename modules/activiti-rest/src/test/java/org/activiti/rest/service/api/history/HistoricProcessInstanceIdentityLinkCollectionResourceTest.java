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
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;


/**
 * Test for REST-operation related to the historic process instance identity links resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricProcessInstanceIdentityLinkCollectionResourceTest extends BaseSpringRestTestCase {
  
  protected ISO8601DateFormat dateFormat = new ISO8601DateFormat();
  
  /**
   * GET history/historic-process-instances/{processInstanceId}/identitylinks
   */
  @Deployment
  public void testGetIdentityLinks() throws Exception {
    HashMap<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringVar", "Azerty");
    processVariables.put("intVar", 67890);
    processVariables.put("booleanVar", false);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setOwner(task.getId(), "test");
    
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE_IDENTITY_LINKS, processInstance.getId());
    
    // Do the actual call
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + url), HttpStatus.SC_OK);
    
    JsonNode linksArray = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertEquals(3, linksArray.size());
    Map<String, JsonNode> linksMap = new HashMap<String, JsonNode>();
    for (JsonNode linkNode : linksArray) {
      linksMap.put(linkNode.get("userId").asText(), linkNode);
    }
    JsonNode participantNode = linksMap.get("kermit");
    assertNotNull(participantNode);
    assertEquals("participant", participantNode.get("type").asText());
    assertEquals("kermit", participantNode.get("userId").asText());
    assertTrue(participantNode.get("groupId").isNull());
    assertTrue(participantNode.get("taskId").isNull());
    assertTrue(participantNode.get("taskUrl").isNull());
    assertEquals(processInstance.getId(), participantNode.get("processInstanceId").asText());
    assertNotNull(participantNode.get("processInstanceUrl").asText());
    
    participantNode = linksMap.get("fozzie");
    assertNotNull(participantNode);
    assertEquals("participant", participantNode.get("type").asText());
    assertEquals("fozzie", participantNode.get("userId").asText());
    assertTrue(participantNode.get("groupId").isNull());
    assertTrue(participantNode.get("taskId").isNull());
    assertTrue(participantNode.get("taskUrl").isNull());
    assertEquals(processInstance.getId(), participantNode.get("processInstanceId").asText());
    assertNotNull(participantNode.get("processInstanceUrl").asText());
    
    participantNode = linksMap.get("test");
    assertNotNull(participantNode);
    assertEquals("participant", participantNode.get("type").asText());
    assertEquals("test", participantNode.get("userId").asText());
    assertTrue(participantNode.get("groupId").isNull());
    assertTrue(participantNode.get("taskId").isNull());
    assertTrue(participantNode.get("taskUrl").isNull());
    assertEquals(processInstance.getId(), participantNode.get("processInstanceId").asText());
    assertNotNull(participantNode.get("processInstanceUrl").asText());
  }
}
