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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.task.Task;
import org.activiti.rest.exception.ActivitiForbiddenException;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class TaskResource extends TaskBaseResource {

  @RequestMapping(value="/runtime/tasks/{taskId}", method = RequestMethod.GET, produces="application/json")
  public TaskResponse getTask(@PathVariable String taskId, HttpServletRequest request) {
    return restResponseFactory.createTaskResponse(getTaskFromRequest(taskId));
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}", method = RequestMethod.PUT, produces="application/json")
  public TaskResponse updateTask(@PathVariable String taskId, 
      @RequestBody TaskRequest taskRequest, HttpServletRequest request) {
    
    if (taskRequest == null) {
      throw new ActivitiException("A request body was expected when updating the task.");
    }
    
    Task task = getTaskFromRequest(taskId);

    // Populate the task properties based on the request
    populateTaskFromRequest(task, taskRequest);
    
    // Save the task and fetch agian, it's possible that an assignment-listener has updated
    // fields after it was saved so we can't use the in-memory task
    taskService.saveTask(task);
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    
    return restResponseFactory.createTaskResponse(task);
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}", method = RequestMethod.POST)
  @ResponseStatus(value = HttpStatus.OK)
  public void executeTaskAction(@PathVariable String taskId, @RequestBody TaskActionRequest actionRequest) {
    if (actionRequest == null) {
      throw new ActivitiException("A request body was expected when executing a task action.");
    }

    Task task = getTaskFromRequest(taskId);
    
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

  @RequestMapping(value="/runtime/tasks/{taskId}", method = RequestMethod.DELETE)
  public void deleteTask(@PathVariable String taskId, @RequestParam(value="cascadeHistory", required=false) Boolean cascadeHistory,
      @RequestParam(value="deleteReason", required=false) String deleteReason, HttpServletResponse response) {
    
    Task taskToDelete = getTaskFromRequest(taskId);
    if (taskToDelete.getExecutionId() != null) {
      // Can't delete a task that is part of a process instance
      throw new ActivitiForbiddenException("Cannot delete a task that is part of a process-instance.");
    }
    
    if (cascadeHistory != null) {
      // Ignore delete-reason since the task-history (where the reason is recorded) will be deleted anyway 
      taskService.deleteTask(taskToDelete.getId(), cascadeHistory);
    } else {
      // Delete with delete-reason
      taskService.deleteTask(taskToDelete.getId(), deleteReason);
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  protected void completeTask(Task task, TaskActionRequest actionRequest) {
    if(actionRequest.getVariables() != null) {
      Map<String, Object> variablesToSet = new HashMap<String, Object>(); 
      for(RestVariable var : actionRequest.getVariables()) {
        if(var.getName() == null) {
          throw new ActivitiIllegalArgumentException("Variable name is required");
        }
        
        Object actualVariableValue = restResponseFactory.getVariableValue(var);
        variablesToSet.put(var.getName(), actualVariableValue);
      }
      
      taskService.complete(task.getId(), variablesToSet);
    } else {
      taskService.complete(task.getId());
    }
    
  }

  protected void resolveTask(Task task, TaskActionRequest actionRequest) {
    taskService.resolveTask(task.getId());
  }

  protected void delegateTask(Task task, TaskActionRequest actionRequest) {
    if (actionRequest.getAssignee() == null) {
      throw new ActivitiIllegalArgumentException("An assignee is required when delegating a task.");
    }
    taskService.delegateTask(task.getId(), actionRequest.getAssignee());
  }

  protected void claimTask(Task task, TaskActionRequest actionRequest) {
    // In case the task is already claimed, a ActivitiTaskAlreadyClaimedException is thrown and converted to
    // a CONFLICT response by the ExceptionHandlerAdvice
    taskService.claim(task.getId(), actionRequest.getAssignee());
  }
}
