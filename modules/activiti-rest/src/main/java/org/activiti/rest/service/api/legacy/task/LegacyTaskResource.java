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

package org.activiti.rest.service.api.legacy.task;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.RequestUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.legacy.AttachmentResponse;
import org.activiti.rest.service.api.legacy.IdentityLinkResponse;
import org.activiti.rest.service.api.legacy.SubTaskResponse;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
@Deprecated
public class LegacyTaskResource extends SecuredResource {
  
  @Get
  public LegacyTaskResponse getTask() {
    if(authenticate() == false) return null;
    String taskId = (String) getRequest().getAttributes().get("taskId");
    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    
    if(task == null) {
      throw new ActivitiObjectNotFoundException("Task not found for id " + taskId, Task.class);
    }
    
    LegacyTaskResponse response = new LegacyTaskResponse(task);
    
    TaskFormData taskFormData = ActivitiUtil.getFormService().getTaskFormData(taskId);
    if(taskFormData != null) {
      response.setFormResourceKey(taskFormData.getFormKey());     
    }
    
    List<Task> subTaskList = ActivitiUtil.getTaskService().getSubTasks(task.getId());
    if(subTaskList != null) {
      for (Task subTask : subTaskList) {
        SubTaskResponse subTaskResponse = new SubTaskResponse(subTask);
        response.addSubTask(subTaskResponse);
      }
    }
    
    List<IdentityLink> linkList = ActivitiUtil.getTaskService().getIdentityLinksForTask(task.getId());
    if(linkList != null) {
      for (IdentityLink identityLink : linkList) {
        IdentityLinkResponse linkResponse = new IdentityLinkResponse(identityLink);
        response.addIdentityLink(linkResponse);
      }
    }
    
    List<Attachment> attachmentList = null;
    if(task.getProcessInstanceId() != null && task.getProcessInstanceId().length() > 0) {
      attachmentList = ActivitiUtil.getTaskService().getProcessInstanceAttachments(task.getProcessInstanceId());
    } else {
      attachmentList = ActivitiUtil.getTaskService().getTaskAttachments(task.getId());
    }
    
    if(attachmentList != null) {
      for (Attachment attachment : attachmentList) {
        AttachmentResponse attachmentResponse = new AttachmentResponse(attachment);
        response.addAttachment(attachmentResponse);
      }
    }
    
    return response;
  }
  
  @Post
  public LegacyTaskResponse updateTask(Representation entity) {
    if(authenticate() == false) return null;
    String taskId = (String) getRequest().getAttributes().get("taskId");
    
    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    
    if(task == null) {
      throw new ActivitiObjectNotFoundException("Task not found for id " + taskId, Task.class);
    }
    
    try {
      String taskParams = entity.getText();
      JsonNode taskJSON = new ObjectMapper().readTree(taskParams);
      
      String description = null;
      if(taskJSON.path("description") != null && taskJSON.path("description").textValue() != null) {
        description = taskJSON.path("description").textValue();
        task.setDescription(description);
      }
      
      String assignee = null;
      if(taskJSON.path("assignee") != null && taskJSON.path("assignee").textValue() != null) {
        assignee = taskJSON.path("assignee").textValue();
        task.setAssignee(assignee);
      }
      
      String owner = null;
      if(taskJSON.path("owner") != null && taskJSON.path("owner").textValue() != null) {
        owner = taskJSON.path("owner").textValue();
        task.setOwner(owner);
      }
      
      String priority = null;
      if(taskJSON.path("priority") != null && taskJSON.path("priority").textValue() != null) {
        priority = taskJSON.path("priority").textValue();
        task.setPriority(RequestUtil.parseToInteger(priority));
      }
      
      String dueDate = null;
      if(taskJSON.path("dueDate") != null && taskJSON.path("dueDate").textValue() != null) {
        dueDate = taskJSON.path("dueDate").textValue();
        task.setDueDate(RequestUtil.parseToDate(dueDate));
      }
      
      ActivitiUtil.getTaskService().saveTask(task);
      
      LegacyTaskResponse response = new LegacyTaskResponse(task);
      return response;
      
    } catch (Exception e) {
      if(e instanceof ActivitiException) {
        throw (ActivitiException) e;
      }
      throw new ActivitiException("Failed to update task " + taskId, e);
    }
  }
  
  @Delete
  public void deleteTask(Representation entity) {
    if(authenticate() == false) return;
    String taskId = (String) getRequest().getAttributes().get("taskId");
    
    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    
    if(task == null) {
      throw new ActivitiObjectNotFoundException("Task not found for id " + taskId, Task.class);
    }
    
    ActivitiUtil.getTaskService().deleteTask(taskId);
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}
