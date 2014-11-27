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
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Frederik Heremans
 */
public class TaskCommentResourceTest extends BaseSpringRestTestCase {

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
      
      HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT_COLLECTION, task.getId()));
      CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertTrue(responseNode.isArray());
      assertEquals(1, responseNode.size());
      
      ObjectNode commentNode = (ObjectNode) responseNode.get(0);
      assertEquals("kermit", commentNode.get("author").textValue());
      assertEquals("This is a comment...", commentNode.get("message").textValue());
      assertEquals(comment.getId(), commentNode.get("id").textValue());
      assertTrue(commentNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId())));
      assertEquals(task.getId(), commentNode.get("taskId").asText());
      assertTrue(commentNode.get("processInstanceUrl").isNull());
      assertTrue(commentNode.get("processInstanceId").isNull());
      
      // Test with unexisting task
      httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT_COLLECTION, "unexistingtask"));
      closeResponse(executeRequest(httpGet, HttpStatus.SC_NOT_FOUND));
      
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

      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("message", "This is a comment...");
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
      List<Comment> commentsOnTask = taskService.getTaskComments(task.getId());
      assertNotNull(commentsOnTask);
      assertEquals(1, commentsOnTask.size());
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("kermit", responseNode.get("author").textValue());
      assertEquals("This is a comment...", responseNode.get("message").textValue());
      assertEquals(commentsOnTask.get(0).getId(), responseNode.get("id").textValue());
      assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), commentsOnTask.get(0).getId())));
      assertEquals(task.getId(), responseNode.get("taskId").asText());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      assertTrue(responseNode.get("processInstanceId").isNull());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  @Deployment(resources = {"org/activiti/rest/service/api/oneTaskProcess.bpmn20.xml"})
  public void testCreateCommentWithProcessInstanceId() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().singleResult();

    ObjectNode requestNode = objectMapper.createObjectNode();
    String message = "test";
    requestNode.put("message", message);
    requestNode.put("saveProcessInstanceId", true);

    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT_COLLECTION, task.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);

    List<Comment> commentsOnTask = taskService.getTaskComments(task.getId());
    assertNotNull(commentsOnTask);
    assertEquals(1, commentsOnTask.size());

    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals(processInstance.getId(), responseNode.get("processInstanceId").asText());
    assertEquals(task.getId(), responseNode.get("taskId").asText());
    assertEquals(message, responseNode.get("message").asText());
    assertNotNull(responseNode.get("time").asText());

    assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), commentsOnTask.get(0).getId())));
    assertTrue(responseNode.get("processInstanceUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE_COMMENT, processInstance.getId(), commentsOnTask.get(0).getId())));
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
      
      HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId()));
      CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      
      assertEquals("kermit", responseNode.get("author").textValue());
      assertEquals("This is a comment...", responseNode.get("message").textValue());
      assertEquals(comment.getId(), responseNode.get("id").textValue());
      assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId())));
      assertEquals(task.getId(), responseNode.get("taskId").asText());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      assertTrue(responseNode.get("processInstanceId").isNull());
      
      // Test with unexisting task
      httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, "unexistingtask", "123"));
      closeResponse(executeRequest(httpGet, HttpStatus.SC_NOT_FOUND));
      
      // Test with unexisting comment
      httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), "unexistingcomment"));
      closeResponse(executeRequest(httpGet, HttpStatus.SC_NOT_FOUND));
       
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
      
      HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId()));
      closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
      
      // Test with unexisting task
      HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, "unexistingtask", "123"));
      closeResponse(executeRequest(httpGet, HttpStatus.SC_NOT_FOUND));
      
      // Test with unexisting comment
      httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), "unexistingcomment"));
      closeResponse(executeRequest(httpGet, HttpStatus.SC_NOT_FOUND));
      
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
      
      HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId()));
      CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      
      assertEquals("kermit", responseNode.get("author").textValue());
      assertEquals("This is a comment...", responseNode.get("message").textValue());
      assertEquals(comment.getId(), responseNode.get("id").textValue());
      assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COMMENT, task.getId(), comment.getId())));
      assertEquals(task.getId(), responseNode.get("taskId").asText());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      assertTrue(responseNode.get("processInstanceId").isNull());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().list();
      for(HistoricTaskInstance task : tasks) {
        historyService.deleteHistoricTaskInstance(task.getId());
      }
    }
  }
}
