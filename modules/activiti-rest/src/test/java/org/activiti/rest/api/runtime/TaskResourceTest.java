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
import java.util.List;

import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.activiti.rest.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;


/**
 * Test for all REST-operations related to a single Task resource.
 * 
 * @author Frederik Heremans
 */
public class TaskResourceTest extends BaseRestTestCase {
  
  /**
   * Test getting a single task, spawned by a process.
   * GET runtime/tasks/{taskId}
   */
  @Deployment
  public void testGetProcessTask() throws Exception {
    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setDueDate(task.getId(), now.getTime());
    taskService.setOwner(task.getId(), "owner");
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
    Representation response = client.get();
    
    // Check resulting task
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertEquals(task.getId(), responseNode.get("id").asText());
    assertEquals(task.getAssignee(), responseNode.get("assignee").asText());
    assertEquals(task.getOwner(), responseNode.get("owner").asText());
    assertEquals(task.getDescription(), responseNode.get("description").asText());
    assertEquals(task.getName(), responseNode.get("name").asText());
    assertEquals(task.getDueDate(), getDateFromISOString(responseNode.get("dueDate").asText()));
    assertEquals(task.getCreateTime(), getDateFromISOString(responseNode.get("createTime").asText()));
    assertEquals(task.getPriority(), responseNode.get("priority").asInt());
    assertTrue(responseNode.get("parentTask").isNull());
    assertTrue(responseNode.get("delegationState").isNull());
    
    assertTrue(responseNode.get("execution").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION, task.getExecutionId())));
    assertTrue(responseNode.get("processInstance").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE, task.getProcessInstanceId())));
    assertTrue(responseNode.get("processDefinition").asText().endsWith(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, encode(task.getProcessDefinitionId()))));
  }
  
  /**
   * Test getting a single task, created using the API.
   * GET runtime/tasks/{taskId}
   */
  public void testGetProcessAdhoc() throws Exception {
    try {
      
      Calendar now = Calendar.getInstance();
      ClockUtil.setCurrentTime(now.getTime());
      
      Task parentTask = taskService.newTask();
      taskService.saveTask(parentTask);
      
      Task task = taskService.newTask();
      task.setParentTaskId(parentTask.getId());
      task.setName("Task name");
      task.setDescription("Descriptions");
      task.setAssignee("kermit");
      task.setDelegationState(DelegationState.RESOLVED);
      task.setDescription("Description");
      task.setDueDate(now.getTime());
      task.setOwner("owner");
      task.setPriority(20);
      taskService.saveTask(task);

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId()));
      Representation response = client.get();
      
      // Check resulting task
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertEquals(task.getId(), responseNode.get("id").asText());
      assertEquals(task.getAssignee(), responseNode.get("assignee").asText());
      assertEquals(task.getOwner(), responseNode.get("owner").asText());
      assertEquals(task.getDescription(), responseNode.get("description").asText());
      assertEquals(task.getName(), responseNode.get("name").asText());
      assertEquals(task.getDueDate(), getDateFromISOString(responseNode.get("dueDate").asText()));
      assertEquals(task.getCreateTime(), getDateFromISOString(responseNode.get("createTime").asText()));
      assertEquals(task.getPriority(), responseNode.get("priority").asInt());
      assertEquals("resolved", responseNode.get("delegationState").asText());
      assertTrue(responseNode.get("execution").isNull());
      assertTrue(responseNode.get("processInstance").isNull());
      assertTrue(responseNode.get("processDefinition").isNull());
      
      assertTrue(responseNode.get("parentTask").asText().endsWith(
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, parentTask.getId())));
      
    } finally {
      
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId());
        if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
          historyService.deleteHistoricTaskInstance(task.getId());
        }
      }
    }
  }
}
