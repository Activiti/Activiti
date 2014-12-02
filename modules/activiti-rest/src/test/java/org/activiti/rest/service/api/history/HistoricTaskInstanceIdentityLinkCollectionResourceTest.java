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
 * Test for REST-operation related to the historic task instance identity links resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceIdentityLinkCollectionResourceTest extends BaseSpringRestTestCase {
  
  protected ISO8601DateFormat dateFormat = new ISO8601DateFormat();
  
  /**
   * Test querying historic task instance. 
   * GET history/historic-task-instances/{taskId}/identitylinks
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
    
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE_IDENTITY_LINKS, task.getId());
    
    // Do the actual call
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + url), HttpStatus.SC_OK);
    
    // Check status and size
    JsonNode linksArray = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertEquals(2, linksArray.size());
    Map<String, JsonNode> linksMap = new HashMap<String, JsonNode>();
    for (JsonNode linkNode : linksArray) {
      linksMap.put(linkNode.get("type").asText(), linkNode);
    }
    JsonNode assigneeNode = linksMap.get("assignee");
    assertNotNull(assigneeNode);
    assertEquals("fozzie", assigneeNode.get("userId").asText());
    assertTrue(assigneeNode.get("groupId").isNull());
    assertEquals(task.getId(), assigneeNode.get("taskId").asText());
    assertNotNull(assigneeNode.get("taskUrl").asText());
    assertTrue(assigneeNode.get("processInstanceId").isNull());
    assertTrue(assigneeNode.get("processInstanceUrl").isNull());
    
    JsonNode ownerNode = linksMap.get("owner");
    assertNotNull(ownerNode);
    assertEquals("test", ownerNode.get("userId").asText());
    assertTrue(ownerNode.get("groupId").isNull());
    assertEquals(task.getId(), ownerNode.get("taskId").asText());
    assertNotNull(ownerNode.get("taskUrl").asText());
    assertTrue(ownerNode.get("processInstanceId").isNull());
    assertTrue(ownerNode.get("processInstanceUrl").isNull());
  }
}
