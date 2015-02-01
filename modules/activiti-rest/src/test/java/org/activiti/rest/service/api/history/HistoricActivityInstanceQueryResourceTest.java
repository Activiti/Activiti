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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Test for REST-operation related to the historic activity instance query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricActivityInstanceQueryResourceTest extends BaseSpringRestTestCase {
  
  /**
   * Test querying historic activity instance. 
   * POST query/historic-activity-instances
   */
  @Deployment
  public void testQueryActivityInstances() throws Exception {
    HashMap<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringVar", "Azerty");
    processVariables.put("intVar", 67890);
    processVariables.put("booleanVar", false);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);

    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_ACTIVITY_INSTANCE_QUERY);
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("activityId", "processTask");
    assertResultsPresentInDataResponse(url, requestNode, 2, "processTask");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityId", "processTask");
    requestNode.put("finished", true);
    assertResultsPresentInDataResponse(url, requestNode, 1, "processTask");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityId", "processTask");
    requestNode.put("finished", false);
    assertResultsPresentInDataResponse(url, requestNode, 1, "processTask");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityId", "processTask2");
    assertResultsPresentInDataResponse(url, requestNode, 1, "processTask2");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityId", "processTask3");
    assertResultsPresentInDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityName", "Process task");
    assertResultsPresentInDataResponse(url, requestNode, 2, "processTask");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityName", "Process task2");
    assertResultsPresentInDataResponse(url, requestNode, 1, "processTask2");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityName", "Process task3");
    assertResultsPresentInDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityType", "userTask");
    assertResultsPresentInDataResponse(url, requestNode, 3, "processTask", "processTask2");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityType", "startEvent");
    assertResultsPresentInDataResponse(url, requestNode, 2, "theStart");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("activityType", "receiveTask");
    assertResultsPresentInDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance.getId());
    assertResultsPresentInDataResponse(url, requestNode, 3, "theStart", "processTask", "processTask2");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance2.getId());
    assertResultsPresentInDataResponse(url, requestNode, 2, "theStart", "processTask");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", processInstance.getProcessDefinitionId());
    assertResultsPresentInDataResponse(url, requestNode, 5, "theStart", "processTask", "processTask2");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskAssignee", "kermit");
    assertResultsPresentInDataResponse(url, requestNode, 2, "processTask");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskAssignee", "fozzie");
    assertResultsPresentInDataResponse(url, requestNode, 1, "processTask2");
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskAssignee", "fozzie2");
    assertResultsPresentInDataResponse(url, requestNode, 0);
  }
  
  protected void assertResultsPresentInDataResponse(String url, ObjectNode body, int numberOfResultsExpected, String... expectedActivityIds) throws JsonProcessingException, IOException {
    
    // Do the actual call
    HttpPost post = new HttpPost(SERVER_URL_PREFIX + url);
    post.setEntity(new StringEntity(body.toString()));
    CloseableHttpResponse response = executeRequest(post, 200);
    
    // Check status and size
    JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
    closeResponse(response);
    assertEquals(numberOfResultsExpected, dataNode.size());

    // Check presence of ID's
    if (expectedActivityIds != null) {
      List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedActivityIds));
      Iterator<JsonNode> it = dataNode.iterator();
      while(it.hasNext()) {
        String activityId = it.next().get("activityId").textValue();
        toBeFound.remove(activityId);
      }
      assertTrue("Not all entries have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
    }
  }
}
