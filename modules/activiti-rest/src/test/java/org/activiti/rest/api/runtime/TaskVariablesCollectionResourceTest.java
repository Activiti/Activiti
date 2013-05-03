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

package org.activiti.rest.api.runtime;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.activiti.rest.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;


/**
 * Test for all REST-operations related to Task variables.
 * 
 * @author Frederik Heremans
 */
public class TaskVariablesCollectionResourceTest extends BaseRestTestCase {
  
  /**
   * Test getting all task variables.
   * GET runtime/tasks/{taskId}/variables
   */
  @Deployment
  public void testGetTaskvariables() throws Exception {
   
    Calendar cal = Calendar.getInstance();
    
    // Start process with all types of variables
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringProcVar", "This is a ProcVariable");
    processVariables.put("intProcVar", 123);
    processVariables.put("longProcVar", 1234L);
    processVariables.put("shortProcVar", (short) 123);
    processVariables.put("doubleProcVar", 99.99);
    processVariables.put("booleanProcVar", Boolean.TRUE);
    processVariables.put("dateProcVar", cal.getTime());
    processVariables.put("byteArrayProcVar", "Some raw bytes".getBytes());
    processVariables.put("overlappingVariable", "process-value");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    
    // Set local task variables, including one that has the same name as one that is defined in the parent scope (process instance)
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Map<String, Object> taskVariables = new HashMap<String, Object>();
    taskVariables.put("stringTaskVar", "This is a TaskVariable");
    taskVariables.put("intTaskVar", 123);
    taskVariables.put("longTaskVar", 1234L);
    taskVariables.put("shortTaskVar", (short) 123);
    taskVariables.put("doubleTaskVar", 99.99);
    taskVariables.put("booleanTaskVar", Boolean.TRUE);
    taskVariables.put("dateTaskVar", cal.getTime());
    taskVariables.put("byteArrayTaskVar", "Some raw bytes".getBytes());
    taskVariables.put("overlappingVariable", "task-value");
    taskService.setVariablesLocal(task.getId(), taskVariables);

    // Request all variables (no scope provides) which include global an local
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(17, responseNode.size());
    
    // Overlapping variable should contain task-value AND be defined as "local"
    boolean foundOverlapping = false;
    for(int i=0; i< responseNode.size(); i++) {
      JsonNode var = responseNode.get(i);
      if(var.get("name") != null && "overlappingVariable".equals(var.get("name").asText())) {
        foundOverlapping = true;
        assertEquals("task-value", var.get("value").asText());
        assertEquals("local", var.get("scope").asText());
        break;
      }
    }
    assertTrue(foundOverlapping);
    
    // Check local variables filering
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()) + "?scope=local");
    response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(9, responseNode.size());
    
    for(int i=0; i< responseNode.size(); i++) {
      JsonNode var = responseNode.get(i);
      assertEquals("local", var.get("scope").asText());
    }
    
    // Check global variables filering
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()) + "?scope=global");
    response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(9, responseNode.size());
    
    foundOverlapping = false;
    for(int i=0; i< responseNode.size(); i++) {
      JsonNode var = responseNode.get(i);
      assertEquals("global", var.get("scope").asText());
      if("overlappingVariable".equals(var.get("name").asText())) {
        foundOverlapping = true;
        assertEquals("process-value", var.get("value").asText());
      }
    }
    assertTrue(foundOverlapping);
    
    
  }
  
}
