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

import org.activiti.engine.task.Task;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * Test for all REST-operations related to sub tasks.
 * 
 * @author Tijs Rademakers
 */
public class TaskSubTaskCollectionResourceTest extends BaseSpringRestTestCase {
  
  /**
   * Test getting all sub tasks.
   * GET runtime/tasks/{taskId}/subtasks
   */
  public void testGetSubTasks() throws Exception {
   
    Task parentTask = taskService.newTask();
    parentTask.setName("parent task");
    taskService.saveTask(parentTask);
    
    Task subTask = taskService.newTask();
    subTask.setName("sub task 1");
    subTask.setParentTaskId(parentTask.getId());
    taskService.saveTask(subTask);
    
    Task subTask2 = taskService.newTask();
    subTask2.setName("sub task 2");
    subTask2.setParentTaskId(parentTask.getId());
    taskService.saveTask(subTask2);

    // Request all sub tasks
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_SUBTASKS_COLLECTION, parentTask.getId())), HttpStatus.SC_OK);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(2, responseNode.size());
    
    boolean foundSubtask1 = false;
    boolean foundSubtask2 = false;
    for (int i=0; i< responseNode.size(); i++) {
      JsonNode var = responseNode.get(i);
      if ("sub task 1".equals(var.get("name").asText())) {
        foundSubtask1 = true;
        assertEquals(subTask.getId(), var.get("id").asText());
      } else if ("sub task 2".equals(var.get("name").asText())) {
        foundSubtask2 = true;
        assertEquals(subTask2.getId(), var.get("id").asText());
      }
    }
    assertTrue(foundSubtask1);
    assertTrue(foundSubtask2);
    
    taskService.deleteTask(parentTask.getId());
    taskService.deleteTask(subTask.getId());
    taskService.deleteTask(subTask2.getId());
    
    historyService.deleteHistoricTaskInstance(parentTask.getId());
    historyService.deleteHistoricTaskInstance(subTask.getId());
    historyService.deleteHistoricTaskInstance(subTask2.getId());
  }
}
