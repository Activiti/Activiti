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

import java.util.Collections;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.activiti.rest.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Test for all REST-operations related to a single task variable.
 * 
 * @author Frederik Heremans
 */
public class TaskVariableResourceTest extends BaseRestTestCase {

  /**
   * Test getting a task variable. GET
   * runtime/tasks/{taskId}/variables/{variableName}
   */
  @Deployment
  public void testGetTaskVariable() throws Exception {
    try {
      // Test variable behaviour on standalone tasks
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setVariableLocal(task.getId(), "localTaskVariable", "localValue");

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, task.getId(), "localTaskVariable"));
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("local", responseNode.get("scope").asText());
      assertEquals("localValue", responseNode.get("value").asText());
      assertEquals("localTaskVariable", responseNode.get("name").asText());
      assertEquals("string", responseNode.get("type").asText());
      

      // Test variable behaviour for a process-task
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", Collections.singletonMap("sharedVariable", (Object) "processValue"));
      Task processTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      
      taskService.setVariableLocal(processTask.getId(), "sharedVariable", "taskValue");
      
      // ANY scope, local should get precedence
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, processTask.getId(), "sharedVariable"));
      response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("local", responseNode.get("scope").asText());
      assertEquals("taskValue", responseNode.get("value").asText());
      assertEquals("sharedVariable", responseNode.get("name").asText());
      assertEquals("string", responseNode.get("type").asText());
      
      // LOCAL scope
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, processTask.getId(), "sharedVariable") + "?scope=local");
      response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("local", responseNode.get("scope").asText());
      assertEquals("taskValue", responseNode.get("value").asText());
      assertEquals("sharedVariable", responseNode.get("name").asText());
      assertEquals("string", responseNode.get("type").asText());
      
      // GLOBAL scope
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, processTask.getId(), "sharedVariable") + "?scope=global");
      response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("global", responseNode.get("scope").asText());
      assertEquals("processValue", responseNode.get("value").asText());
      assertEquals("sharedVariable", responseNode.get("name").asText());
      assertEquals("string", responseNode.get("type").asText());
      
      // Illegal scope
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLE, processTask.getId(), "sharedVariable") + "?scope=illegal");
      try {
        response = client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Invalid variable scope: 'illegal'", expected.getStatus().getDescription());
      }
      
      
    } finally {

      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        if(task.getExecutionId() == null) {
          taskService.deleteTask(task.getId(), true);
        }
      }
    }
  }
}
