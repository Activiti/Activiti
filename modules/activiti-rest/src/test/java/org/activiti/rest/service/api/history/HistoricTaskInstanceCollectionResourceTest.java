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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.impl.cmd.ChangeDeploymentTenantIdCmd;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.util.ISO8601DateFormat;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;


/**
 * Test for REST-operation related to the historic task instance query resource.
 * 
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceCollectionResourceTest extends BaseRestTestCase {
  
  protected ISO8601DateFormat dateFormat = new ISO8601DateFormat();
  
  /**
   * Test querying historic task instance. 
   * GET history/historic-task-instances
   */
  @Deployment
  public void testQueryTaskInstances() throws Exception {
    HashMap<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringVar", "Azerty");
    processVariables.put("intVar", 67890);
    processVariables.put("booleanVar", false);
    
    Calendar created = Calendar.getInstance();
    created.set(Calendar.YEAR, 2001);
    created.set(Calendar.MILLISECOND, 0);
    processEngineConfiguration.getClock().setCurrentTime(created.getTime());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    processEngineConfiguration.getClock().reset();
    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task1.getId());
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "local", "test");
    taskService.setOwner(task.getId(), "test");
    taskService.setDueDate(task.getId(), new GregorianCalendar(2013, 0, 1).getTime());
    
    // Set tenant on deployment
    managementService.executeCommand(new ChangeDeploymentTenantIdCmd(deploymentId, "myTenant"));
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", processVariables, "myTenant");
    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_TASK_INSTANCES);
    
    assertResultsPresentInDataResponse(url, 3, task.getId(), task2.getId());
    
    assertResultsPresentInDataResponse(url + "?processInstanceId=" + processInstance.getId(), 2, task.getId());
    
    assertResultsPresentInDataResponse(url + "?processInstanceId=" + processInstance2.getId(), 1, task2.getId());
    
    assertResultsPresentInDataResponse(url + "?taskAssignee=kermit", 2, task2.getId());
    
    assertResultsPresentInDataResponse(url + "?taskAssigneeLike=" + encode("%mit"), 2, task2.getId());
    
    assertResultsPresentInDataResponse(url + "?taskAssignee=fozzie", 1, task.getId());
    
    assertResultsPresentInDataResponse(url + "?taskOwner=test", 1, task.getId());
   
    assertResultsPresentInDataResponse(url + "?taskOwnerLike=" + encode("t%"), 1, task.getId());
    
    assertResultsPresentInDataResponse(url + "?taskInvolvedUser=test", 1, task.getId());
    
    assertResultsPresentInDataResponse(url + "?dueDateAfter=" + dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()), 1, task.getId());
    
    assertResultsPresentInDataResponse(url + "?dueDateAfter=" + dateFormat.format(new GregorianCalendar(2013, 4, 1).getTime()), 0);
    
    assertResultsPresentInDataResponse(url + "?dueDateBefore=" + dateFormat.format(new GregorianCalendar(2010, 0, 1).getTime()), 0);
    
    assertResultsPresentInDataResponse(url + "?dueDateBefore=" + dateFormat.format(new GregorianCalendar(2013, 4, 1).getTime()), 1, task.getId());
    
    assertResultsPresentInDataResponse(url + "?taskCreatedOn=" + dateFormat.format(created.getTime()), 1, task1.getId());
    
    created.set(Calendar.YEAR, 2002);
    assertResultsPresentInDataResponse(url + "?taskCreatedBefore=" + dateFormat.format(created.getTime()), 1, task1.getId());
    
    created.set(Calendar.YEAR, 2000);
    assertResultsPresentInDataResponse(url + "?taskCreatedAfter=" + dateFormat.format(created.getTime()), 3, task1.getId(), task2.getId());
    
    // Without tenant id
    assertResultsPresentInDataResponse(url + "?withoutTenantId=true", 2, task.getId(), task1.getId());
    
    // Tenant id
    assertResultsPresentInDataResponse(url + "?tenantId=myTenant", 1, task2.getId());
    assertResultsPresentInDataResponse(url + "?tenantId=anotherTenant", 0);
    
    // Tenant id like
    assertResultsPresentInDataResponse(url + "?tenantIdLike=" + encode("%enant"), 1, task2.getId());
    assertResultsPresentInDataResponse(url + "?tenantIdLike=anotherTenant", 0);
  }
  
  protected void assertResultsPresentInDataResponse(String url, int numberOfResultsExpected, String... expectedTaskIds) throws JsonProcessingException, IOException {
    
    // Do the actual call
    ClientResource client = getAuthenticatedClient(url);
    Representation response = client.get();
    
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
