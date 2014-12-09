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

import org.activiti.engine.runtime.ProcessInstance;
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
 * Test for all REST-operations related to a identity links on a Task resource.
 * 
 * @author Frederik Heremans
 */
public class TaskIdentityLinkResourceTest extends BaseSpringRestTestCase {
  
  /**
   * Test getting all identity links.
   * GET runtime/tasks/{taskId}/identitylinks
   */
  @Deployment
  public void testGetIdentityLinks() throws Exception {
    
    // Test candidate user/groups links + manual added identityLink
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("identityLinkProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.addUserIdentityLink(task.getId(), "john", "customType");
    
    assertEquals(3, taskService.getIdentityLinksForTask(task.getId()).size());
    
    // Execute the request
    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINKS_COLLECTION, task.getId()));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(3, responseNode.size());
    
    boolean groupCandidateFound = false;
    boolean userCandidateFound = false;
    boolean customLinkFound = false;
    
    for (int i=0; i < responseNode.size(); i++) {
      ObjectNode link = (ObjectNode) responseNode.get(i);
      assertNotNull(link);
      if (!link.get("user").isNull()) {
        if (link.get("user").textValue().equals("john")) {
          assertEquals("customType", link.get("type").textValue());
          assertTrue(link.get("group").isNull());
          assertTrue(link.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), "users", "john", "customType")));
          customLinkFound = true;
        } else {
          assertEquals("kermit", link.get("user").textValue());
          assertEquals("candidate", link.get("type").textValue());
          assertTrue(link.get("group").isNull());
          assertTrue(link.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), "users", "kermit", "candidate")));
          userCandidateFound = true;
        }
      } else if (!link.get("group").isNull()) {
        assertEquals("sales", link.get("group").textValue());
        assertEquals("candidate", link.get("type").textValue());
        assertTrue(link.get("user").isNull());
        assertTrue(link.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS, "sales", "candidate")));
        groupCandidateFound = true;
      }
    }
    assertTrue(groupCandidateFound);
    assertTrue(userCandidateFound);
    assertTrue(customLinkFound);
  }
  
  /**
   * Test getting all identity links.
   * POST runtime/tasks/{taskId}/identitylinks
   */
  public void testCreateIdentityLink() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      
      // Add user link
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("user", "kermit");
      requestNode.put("type", "myType");
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINKS_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("kermit", responseNode.get("user").textValue());
      assertEquals("myType", responseNode.get("type").textValue());
      assertTrue(responseNode.get("group").isNull());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), 
          RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType")));
      
      // Add group link
      requestNode = objectMapper.createObjectNode();
      requestNode.put("group", "sales");
      requestNode.put("type", "myType");
      
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      response = executeRequest(httpPost, HttpStatus.SC_CREATED);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("sales", responseNode.get("group").textValue());
      assertEquals("myType", responseNode.get("type").textValue());
      assertTrue(responseNode.get("user").isNull());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), 
          RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS, "sales", "myType")));
      
      // Test with unexisting task
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINKS_COLLECTION, "unexistingtask"));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_NOT_FOUND));
      
      // Test with no user/group task
      requestNode = objectMapper.createObjectNode();
      requestNode.put("type", "myType");
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINKS_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
      
      // Test with no user/group task
      requestNode = objectMapper.createObjectNode();
      requestNode.put("type", "myType");
      requestNode.put("user", "kermit");
      requestNode.put("group", "sales");
      
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
      
      // Test with no type
      requestNode = objectMapper.createObjectNode();
      requestNode.put("group", "sales");
      
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting a single identity link for a task.
   * GET runtime/tasks/{taskId}/identitylinks/{family}/{identityId}/{type}
   */
  public void testGetSingleIdentityLink() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.addUserIdentityLink(task.getId(), "kermit", "myType");
      
      HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), "users", "kermit", "myType"));
      CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("kermit", responseNode.get("user").textValue());
      assertEquals("myType", responseNode.get("type").textValue());
      assertTrue(responseNode.get("group").isNull());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, 
          task.getId(), RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType")));
      
      // Test with unexisting task
      httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, "unexistingtask", RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType"));
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
   * Test deleting a single identity link for a task.
   * DELETE runtime/tasks/{taskId}/identitylinks/{family}/{identityId}/{type}
   */
  public void testDeleteSingleIdentityLink() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.addUserIdentityLink(task.getId(), "kermit", "myType");
      
      HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), "users", "kermit", "myType"));
      closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
      
      // Test with unexisting identitylink
      httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "unexistingtype"));
      closeResponse(executeRequest(httpDelete, HttpStatus.SC_NOT_FOUND));
      
      // Test with unexisting task
      httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, "unexistingtask", RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType"));
      closeResponse(executeRequest(httpDelete, HttpStatus.SC_NOT_FOUND));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
}
