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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Tasks" }, description = "Manage Tasks", authorizations = { @Authorization(value = "basicAuth") })
public class TaskVariableResource extends TaskVariableBaseResource {

  @Autowired
  protected ObjectMapper objectMapper;

  @ApiOperation(value = "Get a variable from a task", tags = {"Tasks"}, nickname = "getTaskInstanceVariable")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message =  "Indicates the task was found and the requested variables are returned."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found or the task doesn’t have a variable with the given name (in the given scope). Status message provides additional information.")
  })
  @RequestMapping(value = "/runtime/tasks/{taskId}/variables/{variableName}", method = RequestMethod.GET, produces = "application/json")
  public RestVariable getVariable(@ApiParam(name = "taskId",  value = "The id of the task to get a variable for.") @PathVariable("taskId") String taskId,@ApiParam(name = "variableName", value = "The name of the variable to get.") @PathVariable("variableName") String variableName,@ApiParam(name="scope", value="Scope of variable to be returned. When local, only task-local variable value is returned. When global, only variable value from the task’s parent execution-hierarchy are returned. When the parameter is omitted, a local variable will be returned if it exists, otherwise a global variable.") @RequestParam(value = "scope", required = false) String scope,
      HttpServletRequest request, HttpServletResponse response) {

    return getVariableFromRequest(taskId, variableName, scope, false);
  }

  @ApiOperation(value = "Update an existing variable on a task", tags = {"Tasks"}, nickname = "updateTaskInstanceVariable",
      notes="## Request body for updating simple (non-binary) variables\n\n"
          + " ```JSON\n" + "{\n" + "  \"name\" : \"myTaskVariable\",\n" + "  \"scope\" : \"local\",\n" + "  \"type\" : \"string\",\n"
          + "  \"value\" : \"Hello my friend\"\n" + "} ```"
          + "\n\n\n"
          + "- *name*: Required name of the variable\n" + "\n"
          + "- *scope*: Scope of variable that is updated. If omitted, local is assumed.\n" + "\n"
          + "- *type*: Type of variable that is updated. If omitted, reverts to raw JSON-value type (string, boolean, integer or double).\n" + "\n"
          + "- *value*: Variable value."
          + "\n\n\n"
          + "## Request body for updating simple (non-binary) variables\n\n"
          + "The request should be of type multipart/form-data. There should be a single file-part included with the binary value of the variable. On top of that, the following additional form-fields can be present:\n"
          + "\n"
          + "- *name*: Required name of the variable.\n" + "\n"
          + "- *scope*: Scope of variable that is updated. If omitted, local is assumed.\n" + "\n"
          + "- *type*: Type of variable that is updated. If omitted, binary is assumed and the binary data in the request will be stored as an array of bytes."
      )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the variables was updated and the result is returned."),
      @ApiResponse(code = 400, message = "Indicates the name of a variable to update was missing or that an attempt is done to update a variable on a standalone task (without a process associated) with scope global. Status message provides additional information."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found or the task doesn’t have a variable with the given name in the given scope. Status message contains additional information about the error."),
      @ApiResponse(code = 415, message = "Indicates the serializable data contains an object for which no class is present in the JVM running the Activiti engine and therefore cannot be deserialized."),
  })
  @RequestMapping(value = "/runtime/tasks/{taskId}/variables/{variableName}", method = RequestMethod.PUT, produces = "application/json")
  public RestVariable updateVariable(@ApiParam(name = "taskId", value="The id of the task to update the variable for.") @PathVariable("taskId") String taskId,@ApiParam(name = "variableName", value="The name of the variable to update.") @PathVariable("variableName") String variableName,@ApiParam(hidden=true) @RequestParam(value = "scope", required = false) String scope,
      HttpServletRequest request) {

    Task task = getTaskFromRequest(taskId);

    RestVariable result = null;
    if (request instanceof MultipartHttpServletRequest) {
      result = setBinaryVariable((MultipartHttpServletRequest) request, task, false);

      if (!result.getName().equals(variableName)) {
        throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
      }

    } else {

      RestVariable restVariable = null;

      try {
        restVariable = objectMapper.readValue(request.getInputStream(), RestVariable.class);
      } catch (Exception e) {
        throw new ActivitiIllegalArgumentException("Error converting request body to RestVariable instance", e);
      }

      if (restVariable == null) {
        throw new ActivitiException("Invalid body was supplied");
      }
      if (!restVariable.getName().equals(variableName)) {
        throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
      }

      result = setSimpleVariable(restVariable, task, false);
    }
    return result;
  }


  @ApiOperation(value = "Delete a variable on a task", tags = {"Tasks"}, nickname = "deleteTaskInstanceVariable")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the task variable was found and has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found or the task doesn’t have a variable with the given name. Status message contains additional information about the error.")
  })
  @RequestMapping(value = "/runtime/tasks/{taskId}/variables/{variableName}", method = RequestMethod.DELETE)
  public void deleteVariable(@ApiParam(name="taskId", value = "The id of the task the variable to delete belongs to.") @PathVariable("taskId") String taskId,@ApiParam(name="variableName", value = "The name of the variable to delete.") @PathVariable("variableName") String variableName,@ApiParam(hidden=true) @RequestParam(value = "scope", required = false) String scopeString,
      HttpServletResponse response) {

    Task task = getTaskFromRequest(taskId);

    // Determine scope
    RestVariableScope scope = RestVariableScope.LOCAL;
    if (scopeString != null) {
      scope = RestVariable.getScopeFromString(scopeString);
    }

    if (!hasVariableOnScope(task, variableName, scope)) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() + "' doesn't have a variable '" + variableName + "' in scope " + scope.name().toLowerCase(), VariableInstanceEntity.class);
    }

    if (scope == RestVariableScope.LOCAL) {
      taskService.removeVariableLocal(task.getId(), variableName);
    } else {
      // Safe to use executionId, as the hasVariableOnScope whould have
      // stopped a global-var update on standalone task
      runtimeService.removeVariable(task.getExecutionId(), variableName);
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}
