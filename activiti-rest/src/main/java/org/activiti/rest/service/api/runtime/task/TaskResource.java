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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

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
@Api(tags = { "Tasks" }, description = "Manage Tasks", authorizations = { @Authorization(value = "basicAuth") })
public class TaskResource extends TaskBaseResource {


  @ApiOperation(value = "Get a task", tags = {"Tasks"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message =  "Indicates the task was found and returned."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found.")
  })
  @RequestMapping(value = "/runtime/tasks/{taskId}", method = RequestMethod.GET, produces = "application/json")
  public TaskResponse getTask(@ApiParam(name="taskId", value="The id of the task to get.") @PathVariable String taskId, HttpServletRequest request) {
    return restResponseFactory.createTaskResponse(getTaskFromRequest(taskId));
  }

  @ApiOperation(value = "Update a task", tags = {"Tasks"},
      notes = "All request values are optional. For example, you can only include the assignee attribute in the request body JSON-object, only updating the assignee of the task, leaving all other fields unaffected. When an attribute is explicitly included and is set to null, the task-value will be updated to null. Example: {\"dueDate\" : null} will clear the duedate of the task).")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message =  "Indicates the task was updated."),
      @ApiResponse(code = 404, message =  "Indicates the requested task was not found."),
      @ApiResponse(code = 409, message = "Indicates the requested task was updated simultaneously.")
  })
  @RequestMapping(value = "/runtime/tasks/{taskId}", method = RequestMethod.PUT, produces = "application/json")
  public TaskResponse updateTask(@ApiParam(name="taskId") @PathVariable String taskId, @RequestBody TaskRequest taskRequest, HttpServletRequest request) {

    if (taskRequest == null) {
      throw new ActivitiException("A request body was expected when updating the task.");
    }

    Task task = getTaskFromRequest(taskId);

    // Populate the task properties based on the request
    populateTaskFromRequest(task, taskRequest);

    // Save the task and fetch agian, it's possible that an
    // assignment-listener has updated
    // fields after it was saved so we can't use the in-memory task
    taskService.saveTask(task);
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();

    return restResponseFactory.createTaskResponse(task);
  }


  @ApiOperation(value = "Tasks actions", tags = {"Tasks"},
      notes="## Complete a task - Request Body\n\n"
          + " ```JSON\n" + "{\n" + "  \"action\" : \"complete\",\n" + "  \"variables\" : []\n" + "} ```"
          + "Completes the task. Optional variable array can be passed in using the variables property. More information about the variable format can be found in the REST variables section. Note that the variable-scope that is supplied is ignored and the variables are set on the parent-scope unless a variable exists in a local scope, which is overridden in this case. This is the same behavior as the TaskService.completeTask(taskId, variables) invocation.\n"
          + "\n"
          + "Note that also a transientVariables property is accepted as part of this json, that follows the same structure as the variables property."
          + "\n\n\n"
          + "## Claim a task - Request Body \n\n"
          + " ```JSON\n" + "{\n" + "  \"action\" : \"claim\",\n" + "  \"assignee\" : \"userWhoClaims\"\n" + "} ```"
          + "\n\n\n"
          + "Claims the task by the given assignee. If the assignee is null, the task is assigned to no-one, claimable again."
          + "\n\n\n"
          + "## Delegate a task - Request Body \n\n"
          + " ```JSON\n" + "{\n" + "  \"action\" : \"delegate\",\n" + "  \"assignee\" : \"userToDelegateTo\"\n" + "} ```"
          + "\n\n\n"
          + "Delegates the task to the given assignee. The assignee is required."
          + "\n\n\n"
          + "## Suspend a process instance\n\n"
          + " ```JSON\n" + "{\n" + "  \"action\" : \"resolve\"\n" + "} ```"
          + "\n\n\n"
          + "Resolves the task delegation. The task is assigned back to the task owner (if any)."
      )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message =  "Indicates the action was executed."),
      @ApiResponse(code = 400, message =  "When the body contains an invalid value or when the assignee is missing when the action requires it."),
      @ApiResponse(code = 404, message =  "Indicates the requested task was not found."),
      @ApiResponse(code = 409, message = "Indicates the action cannot be performed due to a conflict. Either the task was updates simultaneously or the task was claimed by another user, in case of the claim action.")
  })	  
  @RequestMapping(value = "/runtime/tasks/{taskId}", method = RequestMethod.POST)
  @ResponseStatus(value = HttpStatus.OK)
  public void executeTaskAction(@ApiParam(name="taskId") @PathVariable String taskId, @RequestBody TaskActionRequest actionRequest) {
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



  @ApiOperation(value = "Delete a task", tags = {"Tasks"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message =  "Indicates the task was found and has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 403, message = "Indicates the requested task cannot be deleted because it’s part of a workflow."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found.")
  })	  
  @RequestMapping(value = "/runtime/tasks/{taskId}", method = RequestMethod.DELETE)
  public void deleteTask(@ApiParam(name="taskId", value="The id of the task to delete.") @PathVariable String taskId,@ApiParam(hidden=true) @RequestParam(value = "cascadeHistory", required = false) Boolean cascadeHistory,
      @ApiParam(hidden=true) @RequestParam(value = "deleteReason", required = false) String deleteReason, HttpServletResponse response) {

    Task taskToDelete = getTaskFromRequest(taskId);
    if (taskToDelete.getExecutionId() != null) {
      // Can't delete a task that is part of a process instance
      throw new ActivitiForbiddenException("Cannot delete a task that is part of a process-instance.");
    }

    if (cascadeHistory != null) {
      // Ignore delete-reason since the task-history (where the reason is
      // recorded) will be deleted anyway
      taskService.deleteTask(taskToDelete.getId(), cascadeHistory);
    } else {
      // Delete with delete-reason
      taskService.deleteTask(taskToDelete.getId(), deleteReason);
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  protected void completeTask(Task task, TaskActionRequest actionRequest) {
    Map<String, Object> variablesToSet = null;
    Map<String, Object> transientVariablesToSet = null;

    if (actionRequest.getVariables() != null) {
      variablesToSet = new HashMap<String, Object>();
      for (RestVariable var : actionRequest.getVariables()) {
        if (var.getName() == null) {
          throw new ActivitiIllegalArgumentException("Variable name is required");
        }

        Object actualVariableValue = restResponseFactory.getVariableValue(var);
        variablesToSet.put(var.getName(), actualVariableValue);
      }
    }

    if (actionRequest.getTransientVariables() != null) {
      transientVariablesToSet = new HashMap<String, Object>();
      for (RestVariable var : actionRequest.getTransientVariables()) {
        if (var.getName() == null) {
          throw new ActivitiIllegalArgumentException("Transient variable name is required");
        }

        Object actualVariableValue = restResponseFactory.getVariableValue(var);
        transientVariablesToSet.put(var.getName(), actualVariableValue);
      }
    }

    taskService.complete(task.getId(), variablesToSet, transientVariablesToSet);
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
    // In case the task is already claimed, a
    // ActivitiTaskAlreadyClaimedException is thrown and converted to
    // a CONFLICT response by the ExceptionHandlerAdvice
    taskService.claim(task.getId(), actionRequest.getAssignee());
  }
}
