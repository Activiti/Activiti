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

import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class TaskCommentResourceTest extends BaseRestTestCase {

  /**
   * Test getting all comments for a task.
   * GET runtime/tasks/{taskId}/comments
   */
  public void testGetComments() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Add a comment as "kermit"
      identityService.setAuthenticatedUserId("kermit");
      Comment comment = taskService.addComment(task.getId(), null, "This is a comment...");
      identityService.setAuthenticatedUserId(null);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_COMMENT_COLLECTION, task.getId()));
      
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertTrue(responseNode.isArray());
      assertEquals(1, responseNode.size());
      
      ObjectNode commentNode = (ObjectNode) responseNode.get(0);
      assertEquals("kermit", commentNode.get("author").getTextValue());
      assertEquals("This is a comment...", commentNode.get("message").getTextValue());
      assertEquals(comment.getId(), commentNode.get("id").getTextValue());
      assertTrue(commentNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId())));
      
      // Test with unexisting task
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT_COLLECTION, "unexistingtask"));
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexistingtask'.", expected.getStatus().getDescription());
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
   * Test creating a comment for a task.
   * POST runtime/tasks/{taskId}/comments
   */
  public void testCreateComment() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_COMMENT_COLLECTION, task.getId()));
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("message", "This is a comment...");
      
      Representation response = client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());

      List<Comment> commentsOnTask = taskService.getTaskComments(task.getId());
      assertNotNull(commentsOnTask);
      assertEquals(1, commentsOnTask.size());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("kermit", responseNode.get("author").getTextValue());
      assertEquals("This is a comment...", responseNode.get("message").getTextValue());
      assertEquals(commentsOnTask.get(0).getId(), responseNode.get("id").getTextValue());
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), commentsOnTask.get(0).getId())));
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting a comment for a task.
   * GET runtime/tasks/{taskId}/comments/{commentId}
   */
  public void testGetComment() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Add a comment as "kermit"
      identityService.setAuthenticatedUserId("kermit");
      Comment comment = taskService.addComment(task.getId(), null, "This is a comment...");
      identityService.setAuthenticatedUserId(null);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId()));
      
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      
      assertEquals("kermit", responseNode.get("author").getTextValue());
      assertEquals("This is a comment...", responseNode.get("message").getTextValue());
      assertEquals(comment.getId(), responseNode.get("id").getTextValue());
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId())));
      
      // Test with unexisting task
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, "unexistingtask", "123"));
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexistingtask'.", expected.getStatus().getDescription());
      }
      
      // Test with unexisting comment
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), "unexistingcomment"));
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Task '" + task.getId() +"' doesn't have a comment with id 'unexistingcomment'.", expected.getStatus().getDescription());
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
   * Test deleting a comment for a task.
   * DELETE runtime/tasks/{taskId}/comments/{commentId}
   */
  public void testDeleteComment() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Add a comment as "kermit"
      identityService.setAuthenticatedUserId("kermit");
      Comment comment = taskService.addComment(task.getId(), null, "This is a comment...");
      identityService.setAuthenticatedUserId(null);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId()));
      
      Representation response = client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      assertEquals(0, response.getSize());
      
      // Test with unexisting task
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, "unexistingtask", "123"));
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexistingtask'.", expected.getStatus().getDescription());
      }
      
      // Test with unexisting comment
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), "unexistingcomment"));
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Task '" + task.getId() +"' doesn't have a comment with id 'unexistingcomment'.", expected.getStatus().getDescription());
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
   * Test getting a comment for a completed task.
   * GET runtime/tasks/{taskId}/comments/{commentId}
   */
  public void testGetCommentWithCompletedTask() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Add a comment as "kermit"
      identityService.setAuthenticatedUserId("kermit");
      Comment comment = taskService.addComment(task.getId(), null, "This is a comment...");
      identityService.setAuthenticatedUserId(null);
      
      taskService.complete(task.getId());
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId()));
      
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      
      assertEquals("kermit", responseNode.get("author").getTextValue());
      assertEquals("This is a comment...", responseNode.get("message").getTextValue());
      assertEquals(comment.getId(), responseNode.get("id").getTextValue());
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId())));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().list();
      for(HistoricTaskInstance task : tasks) {
        historyService.deleteHistoricTaskInstance(task.getId());
      }
    }
  }
}
