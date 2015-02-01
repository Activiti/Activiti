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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;


/**
 * Test for REST-operation related to the historic task instance query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceQueryResourceTest extends BaseSpringRestTestCase {
  
  protected ISO8601DateFormat dateFormat = new ISO8601DateFormat();
  
  /**
   * Test querying historic task instance. 
   * POST query/historic-task-instances
   */
  @Deployment
  public void testQueryTaskInstances() throws Exception {
    HashMap<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringVar", "Azerty");
    processVariables.put("intVar", 67890);
    processVariables.put("booleanVar", false);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",  "myBusinessKey", processVariables);
    processEngineConfiguration.getClock().setCurrentTime(new GregorianCalendar(2013, 0, 1).getTime());
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Task finishedTaskProcess1 = task;
    taskService.complete(task.getId());
    processEngineConfiguration.getClock().setCurrentTime(null);
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "local", "test");
    taskService.setOwner(task.getId(), "test");
    taskService.setDueDate(task.getId(), new GregorianCalendar(2013, 0, 1).getTime());
    
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE_QUERY);
    
    // Process variables
    ObjectNode requestNode = objectMapper.createObjectNode();
    ArrayNode variableArray = objectMapper.createArrayNode();
    ObjectNode variableNode = objectMapper.createObjectNode();
    variableArray.add(variableNode);
    requestNode.put("processVariables", variableArray);
    
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Azerty");
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67891);
    variableNode.put("operation", "lessThan");
    assertResultsPresentInPostDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "lessThan");
    assertResultsPresentInPostDataResponse(url, requestNode);
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "lessThanOrEquals");
    assertResultsPresentInPostDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67889);
    variableNode.put("operation", "greaterThan");
    assertResultsPresentInPostDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "greaterThan");
    assertResultsPresentInPostDataResponse(url, requestNode);
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "greaterThanOrEquals");
    assertResultsPresentInPostDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Azer%");
    variableNode.put("operation", "like");
    assertResultsPresentInPostDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "local");
    variableNode.put("value", "test");
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    variableArray = objectMapper.createArrayNode();
    variableNode = objectMapper.createObjectNode();
    variableArray.add(variableNode);
    requestNode.put("taskVariables", variableArray);
    variableNode.put("name", "local");
    variableNode.put("value", "test");
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    assertResultsPresentInPostDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance.getId());
    assertResultsPresentInPostDataResponse(url, requestNode, 2, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance2.getId());
    assertResultsPresentInPostDataResponse(url, requestNode, 1, task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskAssignee", "kermit");
    assertResultsPresentInPostDataResponse(url, requestNode, 2, task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskAssigneeLike", "%mit");
    assertResultsPresentInPostDataResponse(url, requestNode, 2, task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskAssignee", "fozzie");
    assertResultsPresentInPostDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskOwner", "test");
    assertResultsPresentInPostDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskOwnerLike", "t%");
    assertResultsPresentInPostDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskInvolvedUser", "test");
    assertResultsPresentInPostDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("dueDateAfter", dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()));
    assertResultsPresentInPostDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("dueDateAfter", dateFormat.format(new GregorianCalendar(2013, 4, 1).getTime()));
    assertResultsPresentInPostDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("dueDateBefore", dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()));
    assertResultsPresentInPostDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("dueDateBefore", dateFormat.format(new GregorianCalendar(2013, 4, 1).getTime()));
    assertResultsPresentInPostDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskCompletedAfter", dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()));
    assertResultsPresentInPostDataResponse(url, requestNode, 1, finishedTaskProcess1.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskCompletedAfter", dateFormat.format(new GregorianCalendar(2013, 4, 1).getTime()));
    assertResultsPresentInPostDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskCompletedBefore", dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()));
    assertResultsPresentInPostDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskCompletedAfter", dateFormat.format(new GregorianCalendar(2010, 3, 1).getTime()));
    assertResultsPresentInPostDataResponse(url, requestNode, 1, finishedTaskProcess1.getId());
    
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processBusinessKey", "myBusinessKey");
    assertResultsPresentInPostDataResponse(url, requestNode, 2, task.getId(), finishedTaskProcess1.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processBusinessKeyLike", "myBusiness%");
    assertResultsPresentInPostDataResponse(url, requestNode, 2, task.getId(), finishedTaskProcess1.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "someTaskProcess");
    assertResultsPresentInPostDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "oneTaskProcess");
    assertResultsPresentInPostDataResponse(url, requestNode, task.getId(), finishedTaskProcess1.getId(), task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKeyLike", "oneTask%");
    assertResultsPresentInPostDataResponse(url, requestNode, task.getId(), finishedTaskProcess1.getId(), task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKeyLike", "some%");
    assertResultsPresentInPostDataResponse(url, requestNode);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskDefinitionKey", "processTask");
    assertResultsPresentInPostDataResponse(url, requestNode, finishedTaskProcess1.getId(), task2.getId());
  }
  
  protected void assertResultsPresentInPostDataResponse(String url, ObjectNode body, int numberOfResultsExpected, String... expectedTaskIds) throws JsonProcessingException, IOException {
    // Do the actual call
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + url);
    httpPost.setEntity(new StringEntity(body.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_OK);
    JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
    closeResponse(response);
    assertEquals(numberOfResultsExpected, dataNode.size());

    // Check presence of ID's
    if (expectedTaskIds != null) {
      List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedTaskIds));
      Iterator<JsonNode> it = dataNode.iterator();
      while(it.hasNext()) {
        String id = it.next().get("id").textValue();
        toBeFound.remove(id);
      }
      assertTrue("Not all entries have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
    }
  }
}
