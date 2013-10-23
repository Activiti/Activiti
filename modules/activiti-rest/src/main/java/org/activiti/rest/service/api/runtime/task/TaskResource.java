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

package org.activiti.rest.service.api.runtime.task;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class TaskResource extends TaskBaseResource {

  @Get
  public TaskResponse getTask() {
    if(!authenticate()) { return null; }
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createTaskResponse(this, getTaskFromRequest());
  }
  
  @Put
  public TaskResponse updateTask(TaskRequest taskRequest) {
    if(!authenticate()) { return null; }
    
    if(taskRequest == null) {
      throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(),
              "A request body was expected when updating the task.", null, null));
    }
    
    Task task = getTaskFromRequest();

    // Populate the task properties based on the request
    populateTaskFromRequest(task, taskRequest);
    
    // Save the task and fetch agian, it's possible that an assignment-listener has updated
    // fields after it was saved so we can't use the in-memory task
    ActivitiUtil.getTaskService().saveTask(task);
    task = ActivitiUtil.getTaskService().createTaskQuery().taskId(task.getId()).singleResult();
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createTaskResponse(this, task);
  }
  
  @Post
  public void executeTaskAction(TaskActionRequest actionRequest) {
    if (!authenticate()) {
      return;
    }

    if (actionRequest == null) {
      throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(), "A request body was expected when executing a task action.",
              null, null));
    }

    Task task = getTaskFromRequest();
    if (TaskActionRequest.ACTION_COMPLETE.equals(actionRequest.getAction())) {
      completeTask(task, actionRequest);
    } else if (TaskActionRequest.ACTION_CLAIM.equals(actionRequest.getAction())) {
      claimTask(task, actionRequest);
    } else if (TaskActionRequest.ACTION_DELEGATE.equals(actionRequest.getAction())) {
      delegateTask(task, actionRequest);
    } else if (TaskActionRequest.ACTION_RESOLVE.equals(actionRequest.getAction())) {
      resolveTask(task, actionRequest);
    } else {
      throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
    }
  }

  @Delete
  public void deleteTask() {
    if(!authenticate()) { return; }
    
    Form query = getQuery();
    Boolean cascadeHistory = getQueryParameterAsBoolean("cascadeHistory", query);
    String deleteReason = getQueryParameter("deleteReason", query);
    
    Task taskToDelete = getTaskFromRequest();
    if(taskToDelete.getExecutionId() != null) {
      // Can't delete a task that is part of a process instance
      throw new ResourceException(new Status(Status.CLIENT_ERROR_FORBIDDEN.getCode(), 
              "Cannot delete a task that is part of a process-instance.", null, null));
    }
    
    if(cascadeHistory != null) {
      // Ignore delete-reason since the task-history (where the reason is recorded) will be deleted anyway 
      ActivitiUtil.getTaskService().deleteTask(taskToDelete.getId(), cascadeHistory);
    } else {
      // Delete with delete-reason
      ActivitiUtil.getTaskService().deleteTask(taskToDelete.getId(), deleteReason);
    }
    getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
  }

  protected void completeTask(Task task, TaskActionRequest actionRequest) {
    if(actionRequest.getVariables() != null) {
      Map<String, Object> variablesToSet = new HashMap<String, Object>(); 
      for(RestVariable var : actionRequest.getVariables()) {
        if(var.getName() == null) {
          throw new ActivitiIllegalArgumentException("Variable name is required");
        }
        
        Object actualVariableValue = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
                .getVariableValue(var);
        
        variablesToSet.put(var.getName(), actualVariableValue);
      }
      
      ActivitiUtil.getTaskService().complete(task.getId(), variablesToSet);
    } else {
      ActivitiUtil.getTaskService().complete(task.getId());
    }
    
  }

  protected void resolveTask(Task task, TaskActionRequest actionRequest) {
    ActivitiUtil.getTaskService().resolveTask(task.getId());
  }

  protected void delegateTask(Task task, TaskActionRequest actionRequest) {
    if(actionRequest.getAssignee() == null) {
      throw new ActivitiIllegalArgumentException("An assignee is required when delegating a task.");
    }
    ActivitiUtil.getTaskService().delegateTask(task.getId(), actionRequest.getAssignee());
  }

  protected void claimTask(Task task, TaskActionRequest actionRequest) {
    if(actionRequest.getAssignee() == null) {
      throw new ActivitiIllegalArgumentException("An assignee is required when claiming a task.");
    }
    // In case the task is already claimed, a ActivitiTaskAlreadyClaimedException is thown and converted to
    // a CONFLICT response by the StatusService
    ActivitiUtil.getTaskService().claim(task.getId(), actionRequest.getAssignee());
  }
}
