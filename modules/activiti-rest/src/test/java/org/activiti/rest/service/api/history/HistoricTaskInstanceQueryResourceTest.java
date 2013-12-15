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

import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.util.ISO8601DateFormat;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;


/**
 * Test for REST-operation related to the historic task instance query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceQueryResourceTest extends BaseRestTestCase {
  
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
    ClockUtil.setCurrentTime(new GregorianCalendar(2013, 0, 1).getTime());
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Task finishedTaskProcess1 = task;
    taskService.complete(task.getId());
    ClockUtil.setCurrentTime(null);
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
    assertResultsPresentInDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67891);
    variableNode.put("operation", "lessThan");
    assertResultsPresentInDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "lessThan");
    assertResultsPresentInDataResponse(url, requestNode);
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "lessThanOrEquals");
    assertResultsPresentInDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67889);
    variableNode.put("operation", "greaterThan");
    assertResultsPresentInDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "greaterThan");
    assertResultsPresentInDataResponse(url, requestNode);
    
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "greaterThanOrEquals");
    assertResultsPresentInDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Azer%");
    variableNode.put("operation", "like");
    assertResultsPresentInDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    variableNode.put("name", "local");
    variableNode.put("value", "test");
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    variableArray = objectMapper.createArrayNode();
    variableNode = objectMapper.createObjectNode();
    variableArray.add(variableNode);
    requestNode.put("taskVariables", variableArray);
    variableNode.put("name", "local");
    variableNode.put("value", "test");
    variableNode.put("operation", "equals");
    assertResultsPresentInDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    assertResultsPresentInDataResponse(url, requestNode, 3, task.getId(), task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance.getId());
    assertResultsPresentInDataResponse(url, requestNode, 2, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processInstanceId", processInstance2.getId());
    assertResultsPresentInDataResponse(url, requestNode, 1, task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskAssignee", "kermit");
    assertResultsPresentInDataResponse(url, requestNode, 2, task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskAssigneeLike", "%mit");
    assertResultsPresentInDataResponse(url, requestNode, 2, task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskAssignee", "fozzie");
    assertResultsPresentInDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskOwner", "test");
    assertResultsPresentInDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskOwnerLike", "t%");
    assertResultsPresentInDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskInvolvedUser", "test");
    assertResultsPresentInDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("dueDateAfter", dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()));
    assertResultsPresentInDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("dueDateAfter", dateFormat.format(new GregorianCalendar(2013, 4, 1).getTime()));
    assertResultsPresentInDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("dueDateBefore", dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()));
    assertResultsPresentInDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("dueDateBefore", dateFormat.format(new GregorianCalendar(2013, 4, 1).getTime()));
    assertResultsPresentInDataResponse(url, requestNode, 1, task.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskCompletedAfter", dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()));
    assertResultsPresentInDataResponse(url, requestNode, 1, finishedTaskProcess1.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskCompletedAfter", dateFormat.format(new GregorianCalendar(2013, 4, 1).getTime()));
    assertResultsPresentInDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskCompletedBefore", dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()));
    assertResultsPresentInDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskCompletedAfter", dateFormat.format(new GregorianCalendar(2010, 3, 1).getTime()));
    assertResultsPresentInDataResponse(url, requestNode, 1, finishedTaskProcess1.getId());
    
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processBusinessKey", "myBusinessKey");
    assertResultsPresentInDataResponse(url, requestNode, 2, task.getId(), finishedTaskProcess1.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processBusinessKeyLike", "myBusiness%");
    assertResultsPresentInDataResponse(url, requestNode, 2, task.getId(), finishedTaskProcess1.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "someTaskProcess");
    assertResultsPresentInDataResponse(url, requestNode, 0);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "oneTaskProcess");
    assertResultsPresentInDataResponse(url, requestNode, task.getId(), finishedTaskProcess1.getId(), task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKeyLike", "oneTask%");
    assertResultsPresentInDataResponse(url, requestNode, task.getId(), finishedTaskProcess1.getId(), task2.getId());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKeyLike", "some%");
    assertResultsPresentInDataResponse(url, requestNode);
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("taskDefinitionKey", "processTask");
    assertResultsPresentInDataResponse(url, requestNode, finishedTaskProcess1.getId(), task2.getId());
  }
  
  protected void assertResultsPresentInDataResponse(String url, ObjectNode body, int numberOfResultsExpected, String... expectedTaskIds) throws JsonProcessingException, IOException {
    
    // Do the actual call
    ClientResource client = getAuthenticatedClient(url);
    Representation response = client.post(body);
    
    // Check status and size
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
    assertEquals(numberOfResultsExpected, dataNode.size());

    // Check presence of ID's
    if (expectedTaskIds != null) {
      List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedTaskIds));
      Iterator<JsonNode> it = dataNode.iterator();
      while(it.hasNext()) {
        String id = it.next().get("id").getTextValue();
        toBeFound.remove(id);
      }
      assertTrue("Not all entries have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
    }
    
    client.release();
  }
}
