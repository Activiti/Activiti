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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.runtime.Execution;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@RestController
@Api(tags = { "Process Instances" }, description = "Manage Process Instances", authorizations = { @Authorization(value = "basicAuth") })
public class ProcessInstanceVariableCollectionResource extends BaseVariableCollectionResource {

  @ApiOperation(value = "List of variables for a process instance", tags = { "Process Instances" },
      notes="In case the variable is a binary variable or serializable, the valueUrl points to an URL to fetch the raw value. If it’s a plain variable, the value is present in the response. Note that only local scoped variables are returned, as there is no global scope for process-instance variables.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the process instance was found and variables are returned."),
      @ApiResponse(code = 400, message = "Indicates the requested process instance was not found.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}/variables", method = RequestMethod.GET, produces = "application/json")
  public List<RestVariable> getVariables(@ApiParam(name = "processInstanceId", value="The id of the process instance to the variables for.") @PathVariable String processInstanceId, @RequestParam(value = "scope", required = false) String scope, HttpServletRequest request) {

    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    return processVariables(execution, scope, RestResponseFactory.VARIABLE_PROCESS);
  }

  @ApiOperation(value = "Update a single or binary variable or multiple variables on a process instance", tags = { "Process Instances" }, nickname = "createOrUpdateProcessVariable",
      notes="## Update multiples variables\n\n"
          + " ```JSON\n" + "[\n" + "   {\n" + "      \"name\":\"intProcVar\"\n" + "      \"type\":\"integer\"\n" + "      \"value\":123\n" + "   },\n"
          + "\n" + "   ...\n" + "] ```"
          + "\n\n\n"
          +" Any number of variables can be passed into the request body array. More information about the variable format can be found in the REST variables section. Note that scope is ignored, only local variables can be set in a process instance."
          + "\n\n\n"
          + "## Update a single variable\n\n"
          + "```JSON\n {\n" + "    \"name\":\"intProcVar\"\n" + "    \"type\":\"integer\"\n" + "    \"value\":123\n" + " } ```"
          + "\n\n\n"
          + "##  Update an existing binary variable\n\n"
          + "\n\n\n"
          + "The request should be of type multipart/form-data. There should be a single file-part included with the binary value of the variable. On top of that, the following additional form-fields can be present:\n"
          + "\n" + "name: Required name of the variable.\n" + "\n"
          + "type: Type of variable that is created. If omitted, binary is assumed and the binary data in the request will be stored as an array of bytes."
      )
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Indicates the process instance was found and variable is created."),
      @ApiResponse(code = 400, message = "Indicates the request body is incomplete or contains illegal values. The status description contains additional information about the error."),
      @ApiResponse(code = 404, message = "Indicates the requested process instance was not found."),
      @ApiResponse(code = 415, message = "Indicates the serializable data contains an object for which no class is present in the JVM running the Activiti engine and therefore cannot be deserialized.")

  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}/variables", method = RequestMethod.PUT, produces = "application/json")
  public Object createOrUpdateExecutionVariable(@ApiParam(name = "processInstanceId", value="The id of the process instance to create the new variable for.") @PathVariable String processInstanceId, HttpServletRequest request, HttpServletResponse response) {

    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    return createExecutionVariable(execution, true, RestResponseFactory.VARIABLE_PROCESS, request, response);
  }

  @ApiOperation(value = "Create variables or new binary variable on a process instance", tags = { "Process Instances" }, nickname = "createProcessInstanceVariable",
      notes="## Update multiples variables\n\n"
          + " ```JSON\n" + "[\n" + "   {\n" + "      \"name\":\"intProcVar\"\n" + "      \"type\":\"integer\"\n" + "      \"value\":123\n" + "   },\n"
          + "\n" + "   ...\n" + "] ```"
          + "\n\n\n"
          +" Any number of variables can be passed into the request body array. More information about the variable format can be found in the REST variables section. Note that scope is ignored, only local variables can be set in a process instance."
          + "\n\n\n"
          + "The request should be of type multipart/form-data. There should be a single file-part included with the binary value of the variable. On top of that, the following additional form-fields can be present:\n"
          + "\n" + "name: Required name of the variable.\n" + "\n"
          + "type: Type of variable that is created. If omitted, binary is assumed and the binary data in the request will be stored as an array of bytes."
      )
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Indicates the process instance was found and variable is created."),
      @ApiResponse(code = 400, message = "Indicates the request body is incomplete or contains illegal values. The status description contains additional information about the error."),
      @ApiResponse(code = 404, message = "Indicates the requested process instance was not found."),
      @ApiResponse(code = 409, message = "Indicates the process instance was found but already contains a variable with the given name (only thrown when POST method is used). Use the update-method instead."),

  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}/variables", method = RequestMethod.POST, produces = "application/json")
  public Object createExecutionVariable(@ApiParam(name = "processInstanceId", value="The id of the process instance to create the new variable for") @PathVariable String processInstanceId, HttpServletRequest request, HttpServletResponse response) {

    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    return createExecutionVariable(execution, false, RestResponseFactory.VARIABLE_PROCESS, request, response);
  }

  @ApiOperation(value = "Delete all variables", tags = {"Process Instances"}, nickname = "deleteLocalProcessVariable")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates variables were found and have been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested process instance was not found.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}/variables", method = RequestMethod.DELETE)
  public void deleteLocalVariables(@ApiParam(name = "processInstanceId") @PathVariable String processInstanceId, HttpServletResponse response) {
    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    deleteAllLocalVariables(execution, response);
  }

  @Override
  protected void addGlobalVariables(Execution execution, int variableType, Map<String, RestVariable> variableMap) {
    // no global variables
  }

  // For process instance there's only one scope. Using the local variables
  // method for that
  @Override
  protected void addLocalVariables(Execution execution, int variableType, Map<String, RestVariable> variableMap) {
    Map<String, Object> rawVariables = runtimeService.getVariables(execution.getId());
    List<RestVariable> globalVariables = restResponseFactory.createRestVariables(rawVariables, execution.getId(), variableType, RestVariableScope.LOCAL);

    // Overlay global variables over local ones. In case they are present
    // the values are not overridden,
    // since local variables get precedence over global ones at all times.
    for (RestVariable var : globalVariables) {
      if (!variableMap.containsKey(var.getName())) {
        variableMap.put(var.getName(), var);
      }
    }
  }
}
