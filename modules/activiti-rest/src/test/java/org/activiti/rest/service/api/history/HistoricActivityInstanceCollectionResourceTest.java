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
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;


/**
 * Test for REST-operation related to the historic activity instance query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricActivityInstanceCollectionResourceTest extends BaseRestTestCase {
  
  /**
   * Test querying historic activity instance. 
   * GET history/historic-activity-instances
   */
  @Deployment
  public void testQueryActivityInstances() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_ACTIVITY_INSTANCES);
    
    assertResultsPresentInDataResponse(url + "?activityId=processTask", 2, "processTask");
   
    assertResultsPresentInDataResponse(url + "?activityId=processTask&finished=true", 1, "processTask");
    
    assertResultsPresentInDataResponse(url + "?activityId=processTask&finished=false", 1, "processTask");
    
    assertResultsPresentInDataResponse(url + "?activityId=processTask2", 1, "processTask2");
    
    assertResultsPresentInDataResponse(url + "?activityId=processTask3", 0);
    
    assertResultsPresentInDataResponse(url + "?activityName=Process%20task", 2, "processTask");
    
    assertResultsPresentInDataResponse(url + "?activityName=Process%20task2", 1, "processTask2");
    
    assertResultsPresentInDataResponse(url + "?activityName=Process%20task3", 0);
    
    assertResultsPresentInDataResponse(url + "?activityType=userTask", 3, "processTask","processTask2");
    
    assertResultsPresentInDataResponse(url + "?activityType=startEvent", 2, "theStart");
    
    assertResultsPresentInDataResponse(url + "?activityType=receiveTask", 0);
    
    assertResultsPresentInDataResponse(url + "?processInstanceId=" + processInstance.getId(), 3, "theStart", "processTask", "processTask2");
    
    assertResultsPresentInDataResponse(url + "?processInstanceId=" + processInstance2.getId(), 2, "theStart", "processTask");
    
    assertResultsPresentInDataResponse(url + "?processDefinitionId=" + processInstance.getProcessDefinitionId(), 5, "theStart", "processTask", "processTask2");
    
    assertResultsPresentInDataResponse(url + "?taskAssignee=kermit", 2, "processTask");
    
    assertResultsPresentInDataResponse(url + "?taskAssignee=fozzie", 1, "processTask2");
    
    assertResultsPresentInDataResponse(url + "?taskAssignee=fozzie2", 0);
  }
  
  protected void assertResultsPresentInDataResponse(String url, int numberOfResultsExpected, String... expectedActivityIds) throws JsonProcessingException, IOException {
    
    // Do the actual call
    ClientResource client = getAuthenticatedClient(url);
    Representation response = client.get();
    
    // Check status and size
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
    assertEquals(numberOfResultsExpected, dataNode.size());

    // Check presence of ID's
    if (expectedActivityIds != null) {
      List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedActivityIds));
      Iterator<JsonNode> it = dataNode.iterator();
      while(it.hasNext()) {
        String activityId = it.next().get("activityId").getTextValue();
        toBeFound.remove(activityId);
      }
      assertTrue("Not all entries have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
    }
    
    client.release();
  }
}
