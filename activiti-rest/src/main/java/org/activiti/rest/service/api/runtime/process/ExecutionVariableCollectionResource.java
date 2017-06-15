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

package org.activiti.rest.service.api.runtime.process;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.runtime.Execution;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Executions" }, description = "Manage Executions", authorizations = { @Authorization(value = "basicAuth") })
public class ExecutionVariableCollectionResource extends BaseVariableCollectionResource {

  @ApiOperation(value = "List of variables for an execution", tags = {"Executions"}, nickname = "listExecutionVariables")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the execution was found and variables are returned."),
      @ApiResponse(code = 404, message = "Indicates the requested execution was not found.")
  })
  @RequestMapping(value = "/runtime/executions/{executionId}/variables", method = RequestMethod.GET, produces = "application/json")
  public List<RestVariable> getVariables(@ApiParam(name = "executionId", value="The id of the execution to the variables for.") @PathVariable String executionId,@ApiParam(name="scope", value="Either local or global. If omitted, both local and global scoped variables are returned.") @RequestParam(value = "scope", required = false) String scope, HttpServletRequest request) {

    Execution execution = getExecutionFromRequest(executionId);
    return processVariables(execution, scope, RestResponseFactory.VARIABLE_EXECUTION);
  }

  @ApiOperation(value = "Update variables on an execution", tags = {"Executions"}, nickname = "createOrUpdateExecutionVariable")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Indicates the execution was found and variable is created/updated."),
      @ApiResponse(code = 400, message = "Indicates the request body is incomplete or contains illegal values. The status description contains additional information about the error."),
      @ApiResponse(code = 404, message = "Indicates the requested execution was not found.")
  })
  @RequestMapping(value = "/runtime/executions/{executionId}/variables", method = RequestMethod.PUT, produces = "application/json")
  public Object createOrUpdateExecutionVariable(@ApiParam(name = "executionId", value="The id of the execution to the variables for.") @PathVariable String executionId, HttpServletRequest request, HttpServletResponse response) {

    Execution execution = getExecutionFromRequest(executionId);
    return createExecutionVariable(execution, true, RestResponseFactory.VARIABLE_EXECUTION, request, response);
  }

  @ApiOperation(value = "Create variables on an execution", tags = {"Executions"}, nickname = "createExecutionVariable")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Indicates the execution was found and variable is created/updated."),
      @ApiResponse(code = 400, message = "Indicates the request body is incomplete or contains illegal values. The status description contains additional information about the error."),
      @ApiResponse(code = 404, message = "Indicates the requested execution was not found."),
      @ApiResponse(code = 409, message = "Indicates the execution was found but already contains a variable with the given name. Use the update-method instead.")

  })
  @RequestMapping(value = "/runtime/executions/{executionId}/variables", method = RequestMethod.POST, produces = "application/json")
  public Object createExecutionVariable(@ApiParam(name = "executionId", value="The id of the execution to create the new variable for.") @PathVariable String executionId, HttpServletRequest request, HttpServletResponse response) {

    Execution execution = getExecutionFromRequest(executionId);
    return createExecutionVariable(execution, false, RestResponseFactory.VARIABLE_EXECUTION, request, response);
  }

  @ApiOperation(value = "Delete all variables for an execution", tags = {"Executions"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the execution was found and variables have been deleted."),
      @ApiResponse(code = 404, message = "Indicates the requested execution was not found.")
  })
  @RequestMapping(value = "/runtime/executions/{executionId}/variables", method = RequestMethod.DELETE)
  public void deleteLocalVariables(@ApiParam(name = "executionId") @PathVariable String executionId, HttpServletResponse response) {
    Execution execution = getExecutionFromRequest(executionId);
    deleteAllLocalVariables(execution, response);
  }

}
