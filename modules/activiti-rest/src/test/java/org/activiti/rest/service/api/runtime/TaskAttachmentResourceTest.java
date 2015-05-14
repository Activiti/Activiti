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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.HttpMultipartHelper;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
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
public class TaskAttachmentResourceTest extends BaseSpringRestTestCase {

  /**
   * Test getting all attachments for a task.
   * GET runtime/tasks/{taskId}/attachments
   */
  public void testGetAttachments() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);
      
      // Create URL-attachment
      Attachment urlAttachment = taskService.createAttachment("simpleType", task.getId(), null, "Simple attachment", 
              "Simple attachment description", "http://activiti.org");
      taskService.saveAttachment(urlAttachment);
      
      // Create Binary-attachment
      Attachment binaryAttachment = taskService.createAttachment("binaryType", task.getId(), null, "Binary attachment", 
              "Binary attachment description", new ByteArrayInputStream("This is binary content".getBytes()));
      taskService.saveAttachment(binaryAttachment);
      
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_COLLECTION, task.getId())), HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertTrue(responseNode.isArray());
      assertEquals(2, responseNode.size());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting all attachments for a task.
   * GET runtime/tasks/{taskId}/attachments
   */
  public void testGetAttachmentsUnexistingTask() throws Exception {
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_COLLECTION, "unexistingtask")), HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test getting a single attachments for a task.
   * GET runtime/tasks/{taskId}/attachments/{attachmentId}
   */
  public void testGetAttachment() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Create URL-attachment
      Attachment urlAttachment = taskService.createAttachment("simpleType", task.getId(), null, "Simple attachment", "Simple attachment description",
              "http://activiti.org");
      taskService.saveAttachment(urlAttachment);

      // Create Binary-attachment
      Attachment binaryAttachment = taskService.createAttachment("binaryType", task.getId(), null, "Binary attachment", "Binary attachment description",
              new ByteArrayInputStream("This is binary content".getBytes()));
      taskService.saveAttachment(binaryAttachment);

      // Get external url attachment
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId())), HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertEquals(urlAttachment.getId(), responseNode.get("id").textValue());
      assertEquals("simpleType", responseNode.get("type").textValue());
      assertEquals("Simple attachment", responseNode.get("name").textValue());
      assertEquals("Simple attachment description", responseNode.get("description").textValue());
      assertEquals("http://activiti.org", responseNode.get("externalUrl").textValue());
      assertTrue(responseNode.get("url").textValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("contentUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      assertFalse(responseNode.get("time").isNull());
      
      
      // Get binary attachment
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId())), HttpStatus.SC_OK);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertEquals(binaryAttachment.getId(), responseNode.get("id").textValue());
      assertEquals("binaryType", responseNode.get("type").textValue());
      assertEquals("Binary attachment", responseNode.get("name").textValue());
      assertEquals("Binary attachment description", responseNode.get("description").textValue());
      assertTrue(responseNode.get("url").textValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("contentUrl").textValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("externalUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());

      assertFalse(responseNode.get("time").isNull());

    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting a single attachments for a task, using unexisting task and unexisting attachment.
   * GET runtime/tasks/{taskId}/attachments/{attachmentId}
   */
  public void testGetAttachmentUnexistingTaskAndAttachment() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Create URL-attachment
      Attachment urlAttachment = taskService.createAttachment("simpleType", task.getId(), null, "Simple attachment", "Simple attachment description",
              "http://activiti.org");
      taskService.saveAttachment(urlAttachment);

      // Get attachment for unexisting task
      closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, "unexistingtask", urlAttachment.getId())), HttpStatus.SC_NOT_FOUND));
      
      // Get attachment for task attachment
      closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), "unexistingattachment")), HttpStatus.SC_NOT_FOUND));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting the content for a single attachments for a task.
   * GET runtime/tasks/{taskId}/attachments/{attachmentId}/content
   */
  public void testGetAttachmentContent() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Create Binary-attachment
      Attachment binaryAttachment = taskService.createAttachment("binaryType", task.getId(), null, "Binary attachment", "Binary attachment description",
              new ByteArrayInputStream("This is binary content".getBytes()));
      taskService.saveAttachment(binaryAttachment);

      // Get external url attachment
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId())), HttpStatus.SC_OK);
      
      // Check response body
      String responseBodyString = IOUtils.toString(response.getEntity().getContent());
      assertEquals("This is binary content", responseBodyString);
      
      // Check response headers
      assertEquals("application/octet-stream", response.getEntity().getContentType().getValue());
      closeResponse(response);

    
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting the content for a single attachments for a task, with a mime-type set.
   * GET runtime/tasks/{taskId}/attachments/{attachmentId}/content
   */
  public void testGetAttachmentContentWithMimeType() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Create Binary-attachment
      Attachment binaryAttachment = taskService.createAttachment("application/xml", task.getId(), null, "Binary attachment", "Binary attachment description",
              new ByteArrayInputStream("<p>This is binary content</p>".getBytes()));
      taskService.saveAttachment(binaryAttachment);

      // Get external url attachment
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId())), HttpStatus.SC_OK);
      
      // Check response headers
      assertEquals("application/xml", response.getEntity().getContentType().getValue());
      closeResponse(response);
    
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting the content for a single attachments for a task, for an attachment without content.
   * GET runtime/tasks/{taskId}/attachments/{attachmentId}/content
   */
  public void testGetAttachmentContentWithoutContent() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Create URL-attachment
      Attachment urlAttachment = taskService.createAttachment("simpleType", task.getId(), null, "Simple attachment", "Simple attachment description",
              "http://activiti.org");
      taskService.saveAttachment(urlAttachment);

      // Get attachment content for non-binary attachment
      closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), urlAttachment.getId())), HttpStatus.SC_NOT_FOUND));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test creating a single attachments for a task
   * POST runtime/tasks/{taskId}/attachments/{attachmentId}
   */
  public void testCreateAttachment() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "Simple attachment");
      requestNode.put("description", "Simple attachment description");
      requestNode.put("type", "simpleType");
      requestNode.put("externalUrl", "http://activiti.org");
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
      
      // Check if attachment is created
      List<Attachment> attachments = taskService.getTaskAttachments(task.getId());
      assertEquals(1, attachments.size());
      
      Attachment urlAttachment = attachments.get(0);
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertEquals(urlAttachment.getId(), responseNode.get("id").textValue());
      assertEquals("simpleType", responseNode.get("type").textValue());
      assertEquals("Simple attachment", responseNode.get("name").textValue());
      assertEquals("Simple attachment description", responseNode.get("description").textValue());
      assertEquals("http://activiti.org", responseNode.get("externalUrl").textValue());
      assertTrue(responseNode.get("url").textValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("contentUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      assertFalse(responseNode.get("time").isNull());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test creating a single attachments for a task, using multipart-request to supply content
   * POST runtime/tasks/{taskId}/attachments/{attachmentId}
   */
  public void testCreateAttachmentWithContent() throws Exception {
    
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      InputStream binaryContent = new ByteArrayInputStream("This is binary content".getBytes()); 
      
      // Add name, type and scope
      Map<String, String> additionalFields = new HashMap<String, String>();
      additionalFields.put("name", "An attachment");
      additionalFields.put("description", "An attachment description");
      additionalFields.put("type", "myType");
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_COLLECTION, task.getId()));
      httpPost.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/octet-stream", 
          binaryContent, additionalFields));
      CloseableHttpResponse response = executeBinaryRequest(httpPost, HttpStatus.SC_CREATED);
      
      // Check if attachment is created
      List<Attachment> attachments = taskService.getTaskAttachments(task.getId());
      assertEquals(1, attachments.size());
      
      Attachment binaryAttachment = attachments.get(0);
      assertEquals("This is binary content", IOUtils.toString(taskService.getAttachmentContent(binaryAttachment.getId())));
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertEquals(binaryAttachment.getId(), responseNode.get("id").textValue());
      assertEquals("myType", responseNode.get("type").textValue());
      assertEquals("An attachment", responseNode.get("name").textValue());
      assertEquals("An attachment description", responseNode.get("description").textValue());
      assertTrue(responseNode.get("url").textValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("contentUrl").textValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("externalUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      assertFalse(responseNode.get("time").isNull());
      
    
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
    
  }
  
  /**
   * Test creating a single attachments for a task, without a name
   * POST runtime/tasks/{taskId}/attachments/{attachmentId}
   */
  public void testCreateAttachmentNoName() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("description", "Simple attachment description");
      requestNode.put("type", "simpleType");
      requestNode.put("externalUrl", "http://activiti.org");

      // Post JSON without name
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_COLLECTION, task.getId()));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeBinaryRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
     
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test deleting a single attachments for a task
   * DELETE runtime/tasks/{taskId}/attachments/{attachmentId}
   */
  public void testDeleteAttachment() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Create URL-attachment
      Attachment urlAttachment = taskService.createAttachment("simpleType", task.getId(), null, "Simple attachment", "Simple attachment description",
              "http://activiti.org");
      taskService.saveAttachment(urlAttachment);

      // Delete the attachment
      HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId()));
      closeResponse(executeBinaryRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
      
      // Check if attachment is really deleted
      assertNull(taskService.getAttachment(urlAttachment.getId()));
      
      // Deleting again should result in 404
      closeResponse(executeBinaryRequest(httpDelete, HttpStatus.SC_NOT_FOUND));
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
  
  /**
   * Test getting a single attachments for a task.
   * GET runtime/tasks/{taskId}/attachments/{attachmentId}
   */
  public void testGetAttachmentForCompletedTask() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Create URL-attachment
      Attachment urlAttachment = taskService.createAttachment("simpleType", task.getId(), null, "Simple attachment", "Simple attachment description",
              "http://activiti.org");
      taskService.saveAttachment(urlAttachment);

      // Create Binary-attachment
      Attachment binaryAttachment = taskService.createAttachment("binaryType", task.getId(), null, "Binary attachment", "Binary attachment description",
              new ByteArrayInputStream("This is binary content".getBytes()));
      taskService.saveAttachment(binaryAttachment);
      
      taskService.complete(task.getId());

      // Get external url attachment
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId())), HttpStatus.SC_OK);
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertEquals(urlAttachment.getId(), responseNode.get("id").textValue());
      assertEquals("simpleType", responseNode.get("type").textValue());
      assertEquals("Simple attachment", responseNode.get("name").textValue());
      assertEquals("Simple attachment description", responseNode.get("description").textValue());
      assertEquals("http://activiti.org", responseNode.get("externalUrl").textValue());
      assertTrue(responseNode.get("url").textValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("contentUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      assertFalse(responseNode.get("time").isNull());
      
      
      // Get binary attachment
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId())), HttpStatus.SC_OK);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertEquals(binaryAttachment.getId(), responseNode.get("id").textValue());
      assertEquals("binaryType", responseNode.get("type").textValue());
      assertEquals("Binary attachment", responseNode.get("name").textValue());
      assertEquals("Binary attachment description", responseNode.get("description").textValue());
      assertTrue(responseNode.get("url").textValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("contentUrl").textValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("externalUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().list();
      for (HistoricTaskInstance task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }
}
