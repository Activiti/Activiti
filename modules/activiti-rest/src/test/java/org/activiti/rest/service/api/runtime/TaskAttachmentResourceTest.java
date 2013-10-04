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
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.HttpMultipartRepresentation;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;


/**
 * @author Frederik Heremans
 */
public class TaskAttachmentResourceTest extends BaseRestTestCase {

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
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_TASK_ATTACHMENT_COLLECTION, task.getId()));
      
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertTrue(responseNode.isArray());
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
   * Test getting all attachments for a task.
   * GET runtime/tasks/{taskId}/attachments
   */
  public void testGetAttachmentsUnexistingTask() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_COLLECTION, "unexistingtask"));
    
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a task with id 'unexistingtask'.", expected.getStatus().getDescription());
    }
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
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId()));

      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertEquals(urlAttachment.getId(), responseNode.get("id").getTextValue());
      assertEquals("simpleType", responseNode.get("type").getTextValue());
      assertEquals("Simple attachment", responseNode.get("name").getTextValue());
      assertEquals("Simple attachment description", responseNode.get("description").getTextValue());
      assertEquals("http://activiti.org", responseNode.get("externalUrl").getTextValue());
      assertTrue(responseNode.get("url").getTextValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("contentUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      
      
      // Get binary attachment
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId()));

      response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      responseNode = objectMapper.readTree(response.getStream());
      assertEquals(binaryAttachment.getId(), responseNode.get("id").getTextValue());
      assertEquals("binaryType", responseNode.get("type").getTextValue());
      assertEquals("Binary attachment", responseNode.get("name").getTextValue());
      assertEquals("Binary attachment description", responseNode.get("description").getTextValue());
      assertTrue(responseNode.get("url").getTextValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("contentUrl").getTextValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("externalUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
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


      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, 
              "unexistingtask", urlAttachment.getId()));

      // Get attachment for unexisting task
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a task with id 'unexistingtask'.", expected.getStatus().getDescription());
      }
      
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, 
              task.getId(), "unexistingattachment"));

      // Get attachment for task attachment
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Task '" + task.getId() +"' doesn't have an attachment with id 'unexistingattachment'.", expected.getStatus().getDescription());
      }
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
  @SuppressWarnings("unchecked")
  public void testGetAttachmentContent() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Create Binary-attachment
      Attachment binaryAttachment = taskService.createAttachment("binaryType", task.getId(), null, "Binary attachment", "Binary attachment description",
              new ByteArrayInputStream("This is binary content".getBytes()));
      taskService.saveAttachment(binaryAttachment);

      // Get external url attachment
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId()));

      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      // Check response body
      String responseBodyString = response.getText();
      assertEquals("This is binary content", responseBodyString);
      
      // Check response headers
      Series<Parameter> headers = (Series<Parameter>) client.getResponseAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
      assertEquals("application/octet-stream", headers.getFirstValue(HeaderConstants.HEADER_CONTENT_TYPE));

    
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
  @SuppressWarnings("unchecked")
  public void testGetAttachmentContentWithMimeType() throws Exception {
    try {
      Task task = taskService.newTask();
      taskService.saveTask(task);

      // Create Binary-attachment
      Attachment binaryAttachment = taskService.createAttachment("application/xml", task.getId(), null, "Binary attachment", "Binary attachment description",
              new ByteArrayInputStream("<p>This is binary content</p>".getBytes()));
      taskService.saveAttachment(binaryAttachment);

      // Get external url attachment
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId()));

      client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      // Check response headers
      Series<Parameter> headers = (Series<Parameter>) client.getResponseAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
      assertEquals("application/xml", headers.getFirstValue(HeaderConstants.HEADER_CONTENT_TYPE));
    
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


      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, 
              task.getId(), urlAttachment.getId()));

      // Get attachment content for non-binary attachment 
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Attachment with id '" + urlAttachment.getId() + "' doesn't have content associated with it.", expected.getStatus().getDescription());
      }
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

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_COLLECTION, 
              task.getId()));

      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "Simple attachment");
      requestNode.put("description", "Simple attachment description");
      requestNode.put("type", "simpleType");
      requestNode.put("externalUrl", "http://activiti.org");

      Representation response = client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());

      // Check if attachment is created
      List<Attachment> attachments = taskService.getTaskAttachments(task.getId());
      assertEquals(1, attachments.size());
      
      Attachment urlAttachment = attachments.get(0);
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertEquals(urlAttachment.getId(), responseNode.get("id").getTextValue());
      assertEquals("simpleType", responseNode.get("type").getTextValue());
      assertEquals("Simple attachment", responseNode.get("name").getTextValue());
      assertEquals("Simple attachment description", responseNode.get("description").getTextValue());
      assertEquals("http://activiti.org", responseNode.get("externalUrl").getTextValue());
      assertTrue(responseNode.get("url").getTextValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("contentUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      
      
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
      
      // Upload a valid BPMN-file using multipart-data
      Representation uploadRepresentation = new HttpMultipartRepresentation("value",
              binaryContent, additionalFields);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_COLLECTION, 
              task.getId()));
      
      Representation response = client.post(uploadRepresentation);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      // Check if attachment is created
      List<Attachment> attachments = taskService.getTaskAttachments(task.getId());
      assertEquals(1, attachments.size());
      
      Attachment binaryAttachment = attachments.get(0);
      assertEquals("This is binary content", IOUtils.toString(taskService.getAttachmentContent(binaryAttachment.getId())));
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertEquals(binaryAttachment.getId(), responseNode.get("id").getTextValue());
      assertEquals("myType", responseNode.get("type").getTextValue());
      assertEquals("An attachment", responseNode.get("name").getTextValue());
      assertEquals("An attachment description", responseNode.get("description").getTextValue());
      assertTrue(responseNode.get("url").getTextValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("contentUrl").getTextValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("externalUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      
    
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

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_COLLECTION, 
              task.getId()));
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("description", "Simple attachment description");
      requestNode.put("type", "simpleType");
      requestNode.put("externalUrl", "http://activiti.org");

      // Post JSON without name
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("Attachment name is required.", expected.getStatus().getDescription());
      }
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


      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, 
              task.getId(), urlAttachment.getId()));

      // Delete the attachment
      Representation response = client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      assertTrue(response.getSize() == 0L);

      // Check if attachment is really deleted
      assertNull(taskService.getAttachment(urlAttachment.getId()));
      
      // Deleting again should result in 404
      try {
        client.delete();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Task '" + task.getId() +"' doesn't have an attachment with id '" + urlAttachment.getId() + "'.", expected.getStatus().getDescription());
      }
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
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId()));

      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertEquals(urlAttachment.getId(), responseNode.get("id").getTextValue());
      assertEquals("simpleType", responseNode.get("type").getTextValue());
      assertEquals("Simple attachment", responseNode.get("name").getTextValue());
      assertEquals("Simple attachment description", responseNode.get("description").getTextValue());
      assertEquals("http://activiti.org", responseNode.get("externalUrl").getTextValue());
      assertTrue(responseNode.get("url").getTextValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), urlAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

      assertTrue(responseNode.get("contentUrl").isNull());
      assertTrue(responseNode.get("processInstanceUrl").isNull());
      
      
      // Get binary attachment
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId()));

      response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      responseNode = objectMapper.readTree(response.getStream());
      assertEquals(binaryAttachment.getId(), responseNode.get("id").getTextValue());
      assertEquals("binaryType", responseNode.get("type").getTextValue());
      assertEquals("Binary attachment", responseNode.get("name").getTextValue());
      assertEquals("Binary attachment description", responseNode.get("description").getTextValue());
      assertTrue(responseNode.get("url").getTextValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("contentUrl").getTextValue()
              .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, task.getId(), binaryAttachment.getId())));
      assertTrue(responseNode.get("taskUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK, task.getId())));

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

