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

package org.activiti.rest.service.api.runtime;

import java.util.Calendar;
import java.util.List;

import org.activiti.engine.task.Event;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class TaskEventResourceTest extends BaseRestTestCase {

  /**
   * Test getting all events for a task.
   * GET runtime/tasks/{taskId}/events
   */
  public void testGetEvents() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setAssignee(task.getId(), "kermit");
      taskService.addUserIdentityLink(task.getId(), "gonzo", "someType");

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_EVENT_COLLECTION, task.getId()));
      
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertTrue(responseNode.isArray());
      
      // 2 events expected: assigned event and involvement event.
      assertEquals(2, responseNode.size());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting a single event for a task.
   * GET runtime/tasks/{taskId}/events/{eventId}
   */
  public void testGetEvent() throws Exception {
    try {
      Calendar now = Calendar.getInstance();
      now.set(Calendar.MILLISECOND, 0);
      processEngineConfiguration.getClock().setCurrentTime(now.getTime());
      
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.addUserIdentityLink(task.getId(), "gonzo", "someType");

      Event event = taskService.getTaskEvents(task.getId()).get(0);
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_EVENT, task.getId(), event.getId()));
      
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals(event.getId(), responseNode.get("id").getTextValue());
      assertEquals(event.getAction(), responseNode.get("action").getTextValue());
      assertEquals(event.getUserId(), responseNode.get("userId").getTextValue());
      assertTrue(responseNode.get("url").asText().endsWith(
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_EVENT, task.getId(), event.getId())));
      assertTrue(responseNode.get("taskUrl").asText().endsWith(
              RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));
      assertEquals(now.getTime(), getDateFromISOString(responseNode.get("time").getTextValue()));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting an unexisting event for task and event for unexisting task.
   * GET runtime/tasks/{taskId}/events/{eventId}
   */
  public void testGetUnexistingEventAndTask() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.addUserIdentityLink(task.getId(), "gonzo", "someType");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_EVENT, task.getId(), "unexisting"));
      
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Task '" + task.getId() +"' doesn't have an event with id 'unexisting'.", expected.getStatus().getDescription());
      }
      
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_EVENT, "unexisting", "unexistingEvent"));
      
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexisting'.", expected.getStatus().getDescription());
      }
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test delete event for a task.
   * DELETE runtime/tasks/{taskId}/events/{eventId}
   */
  public void testDeleteEvent() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setAssignee(task.getId(), "kermit");
      taskService.addUserIdentityLink(task.getId(), "gonzo", "someType");

      List<Event> events = taskService.getTaskEvents(task.getId());
      assertEquals(2, events.size());
      for (Event event : events) {
        ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
                RestUrls.URL_TASK_EVENT, task.getId(), event.getId()));
        
        client.delete();
        assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      }
      
      events = taskService.getTaskEvents(task.getId());
      assertEquals(0, events.size());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
}
