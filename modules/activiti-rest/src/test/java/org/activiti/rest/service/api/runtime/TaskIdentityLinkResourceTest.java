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
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Test for all REST-operations related to a identity links on a Task resource.
 * 
 * @author Frederik Heremans
 */
public class TaskIdentityLinkResourceTest extends BaseRestTestCase {
  
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
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINKS_COLLECTION, task.getId()));
    // Execute the request
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(3, responseNode.size());
    
    boolean groupCandidateFound = false;
    boolean userCandidateFound = false;
    boolean customLinkFound = false;
    
    for(int i=0; i < responseNode.size(); i++) {
      ObjectNode link = (ObjectNode) responseNode.get(i);
      assertNotNull(link);
      if(!link.get("user").isNull()) {
        if(link.get("user").textValue().equals("john")) {
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
      } else if(!link.get("group").isNull()) {
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
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINKS_COLLECTION, task.getId()));
      
      // Add user link
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("user", "kermit");
      requestNode.put("type", "myType");
      
      Representation response = client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("kermit", responseNode.get("user").textValue());
      assertEquals("myType", responseNode.get("type").textValue());
      assertTrue(responseNode.get("group").isNull());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType")));
      
      
      // Add group link
      requestNode = objectMapper.createObjectNode();
      requestNode.put("group", "sales");
      requestNode.put("type", "myType");
      
      client.release();
      response = client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("sales", responseNode.get("group").textValue());
      assertEquals("myType", responseNode.get("type").textValue());
      assertTrue(responseNode.get("user").isNull());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS, "sales", "myType")));
      
      // Test with unexisting task
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINKS_COLLECTION, "unexistingtask"));
      try {
        client.post(null);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexistingtask'.", expected.getStatus().getDescription());
      }
      
      // Test with no user/group task
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINKS_COLLECTION, task.getId()));
      requestNode = objectMapper.createObjectNode();
      requestNode.put("type", "myType");
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("A group or a user is required to create an identity link.", expected.getStatus().getDescription());
      }
      
      // Test with no user/group task
      requestNode = objectMapper.createObjectNode();
      requestNode.put("type", "myType");
      requestNode.put("user", "kermit");
      requestNode.put("group", "sales");
      try {
        client.release();
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Only one of user or group can be used to create an identity link.", expected.getStatus().getDescription());
      }
      
      // Test with no type
      requestNode = objectMapper.createObjectNode();
      requestNode.put("group", "sales");
      try {
        client.release();
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("The identity link type is required.", expected.getStatus().getDescription());
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
   * Test getting a single identity link for a task.
   * GET runtime/tasks/{taskId}/identitylinks/{family}/{identityId}/{type}
   */
  public void testGetSingleIdentityLink() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.addUserIdentityLink(task.getId(), "kermit", "myType");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_IDENTITYLINK, task.getId(), "users", "kermit", "myType"));
      
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("kermit", responseNode.get("user").textValue());
      assertEquals("myType", responseNode.get("type").textValue());
      assertTrue(responseNode.get("group").isNull());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType")));
      
      
      // Test with unexisting task
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, "unexistingtask", RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType"));
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
   * Test deleting a single identity link for a task.
   * DELETE runtime/tasks/{taskId}/identitylinks/{family}/{identityId}/{type}
   */
  public void testDeleteSingleIdentityLink() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.addUserIdentityLink(task.getId(), "kermit", "myType");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_IDENTITYLINK, task.getId(), "users", "kermit", "myType"));
      
      client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      
      // Test with unexisting identitylink
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, task.getId(), RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "unexistingtype"));
      try {
        client.delete();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find the requested identity link.", expected.getStatus().getDescription());
      }
      
      // Test with unexisting task
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, "unexistingtask", RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType"));
      try {
        client.delete();
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
}
