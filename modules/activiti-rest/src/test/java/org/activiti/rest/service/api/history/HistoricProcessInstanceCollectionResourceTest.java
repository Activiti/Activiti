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
 * Test for REST-operation related to the historic process instance query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricProcessInstanceCollectionResourceTest extends BaseRestTestCase {
  
  /**
   * Test querying historic process instance based on variables. 
   * GET history/historic-process-instances
   */
  @Deployment
  public void testQueryProcessInstances() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCES);
    
    assertResultsPresentInDataResponse(url + "?finished=true", processInstance.getId());
    
    assertResultsPresentInDataResponse(url + "?finished=false", processInstance2.getId());
    
    assertResultsPresentInDataResponse(url + "?processDefinitionId=" + processInstance.getProcessDefinitionId(), processInstance.getId(), processInstance2.getId());
    
    assertResultsPresentInDataResponse(url + "?processDefinitionId=" + processInstance.getProcessDefinitionId() + "&finished=true", processInstance.getId());
    
    assertResultsPresentInDataResponse(url + "?processDefinitionKey=oneTaskProcess", processInstance.getId(), processInstance2.getId());
    
    ClientResource client = getAuthenticatedClient(url + "?processDefinitionKey=oneTaskProcess&sort=startTime");
    Representation response = client.get();
    
    // Check status and size
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
    assertEquals(2, dataNode.size());
    assertEquals(processInstance.getId(), dataNode.get(0).get("id").asText());
    assertEquals(processInstance2.getId(), dataNode.get(1).get("id").asText());
  }
  
  protected void assertResultsPresentInDataResponse(String url, String... expectedResourceIds) throws JsonProcessingException, IOException {
    int numberOfResultsExpected = expectedResourceIds.length;
    
    // Do the actual call
    ClientResource client = getAuthenticatedClient(url);
    Representation response = client.get();
    
    // Check status and size
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
    assertEquals(numberOfResultsExpected, dataNode.size());

    // Check presence of ID's
    List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedResourceIds));
    Iterator<JsonNode> it = dataNode.iterator();
    while(it.hasNext()) {
      String id = it.next().get("id").getTextValue();
      toBeFound.remove(id);
    }
    assertTrue("Not all process instances have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
    
    client.release();
  }
}
