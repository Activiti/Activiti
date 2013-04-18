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

package org.activiti.rest.api.task;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class TaskResource extends SecuredResource {

  @Get
  public TaskResponse getTask() {
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createTaskReponse(this, getTaskFromRequest());
  }
  
//  TODO: move to collection-resource
//  @Post
//  public TaskResponse createTask(TaskRequest taskRequest) {
//    Task task = ActivitiUtil.getTaskService().newTask();
//
//    // Populate the task
//    task.setName(taskRequest.getName());
//    task.setAssignee(taskRequest.getAssignee());
//    task.setDescription(taskRequest.getDescription());
//    task.setDueDate(taskRequest.getDueDate());
//    task.setOwner(taskRequest.getOwner());
//    task.setParentTaskId(taskRequest.getParentTaskId());
//    task.setPriority(taskRequest.getPriority());
//
//    DelegationState delegationState = getDelegationState(taskRequest.getDelegationState());
//    task.setDelegationState(delegationState);
//    
//    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
//            .createTaskReponse(this, task);
//  }
  
  
  @Put
  public TaskResponse updateTask(TaskRequest taskRequest) {
    Task task = getTaskFromRequest();

    // Populate the task properties based on the request
    if(taskRequest.isNameSet()) {
      task.setName(taskRequest.getName());
    }
    if(taskRequest.isAssigneeSet()) {
      task.setAssignee(taskRequest.getAssignee());
    }
    if(taskRequest.isDescriptionSet()) {
      task.setDescription(taskRequest.getDescription());
    }
    if(taskRequest.isDuedateSet()) {
      task.setDueDate(taskRequest.getDueDate());
    }
    if(taskRequest.isOwnerSet()) {
      task.setOwner(taskRequest.getOwner());
    }
    if(taskRequest.isParentTaskIdSet()) {
      task.setParentTaskId(taskRequest.getParentTaskId());
    }
    if(taskRequest.isPrioritySet()) {
      task.setPriority(taskRequest.getPriority());
    }

    if(taskRequest.isDelegationStateSet()) {
      DelegationState delegationState = getDelegationState(taskRequest.getDelegationState());
      task.setDelegationState(delegationState);
    }

    // Save the task and fetch agian, it's possible that an assignment-listener has updated
    // fields after it was saved so we can't use the in-memory task
    ActivitiUtil.getTaskService().saveTask(task);
    task = ActivitiUtil.getTaskService().createTaskQuery().taskId(task.getId()).singleResult();
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createTaskReponse(this, task);
  }
  
  @Delete
  public void deleteTask() {
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
  

  /**
   * Get valid task from request. Throws exception if task doen't exist or if task id is not provided.
   */
  protected Task getTaskFromRequest() {
    String taskId = getAttribute("taskId");

    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("The taskId cannot be null");
    }

    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    if (task == null) {
      throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskId + "'.", Task.class);
    }
    return task;
  }

  protected DelegationState getDelegationState(String delegationState) {
    DelegationState state = null;
    if(delegationState != null) {
      if(DelegationState.RESOLVED.name().toLowerCase().equals(delegationState)) {
        return DelegationState.RESOLVED;
      } else if(DelegationState.PENDING.name().toLowerCase().equals(delegationState)) {
        return DelegationState.PENDING;
      } else {
        throw new ActivitiIllegalArgumentException("Illegal value for delegationState: " + delegationState);
      }
    }
    return state;
  }

}
