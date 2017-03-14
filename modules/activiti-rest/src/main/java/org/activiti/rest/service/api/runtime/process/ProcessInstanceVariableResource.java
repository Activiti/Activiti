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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.service.api.RestResponseFactory;
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
@Api(tags = { "Process Instances" }, description = "Manage Process Instances", authorizations = { @Authorization(value = "basicAuth") })
public class ProcessInstanceVariableResource extends BaseExecutionVariableResource {

  @Autowired
  protected ObjectMapper objectMapper;

  @ApiOperation(value = "Get a variable for a process instance", tags = { "Process Instances" }, nickname = "getProcessInstanceVariable")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates both the process instance and variable were found and variable is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested process instance was not found or the process instance does not have a variable with the given name. Status description contains additional information about the error.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}/variables/{variableName}", method = RequestMethod.GET, produces = "application/json")
  public RestVariable getVariable(@ApiParam(name = "processInstanceId", value="The id of the process instance to the variables for.") @PathVariable("processInstanceId") String processInstanceId,@ApiParam(name = "variableName", value="Name of the variable to get.") @PathVariable("variableName") String variableName,
      @RequestParam(value = "scope", required = false) String scope, HttpServletRequest request) {

    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    return getVariableFromRequest(execution, variableName, scope, false);
  }

  @ApiOperation(value = "Update a single variable on a process instance", tags = { "Process Instances" }, nickname = "updateProcessInstanceVariable")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates both the process instance and variable were found and variable is updated."),
      @ApiResponse(code = 404, message = "Indicates the requested process instance was not found or the process instance does not have a variable with the given name. Status description contains additional information about the error.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}/variables/{variableName}", method = RequestMethod.PUT, produces = "application/json")
  public RestVariable updateVariable(@ApiParam(name = "processInstanceId", value="The id of the process instance to the variables for.") @PathVariable("processInstanceId") String processInstanceId,@ApiParam(name = "variableName", value="Name of the variable to get.") @PathVariable("variableName") String variableName, HttpServletRequest request) {

    Execution execution = getProcessInstanceFromRequest(processInstanceId);

    RestVariable result = null;
    if (request instanceof MultipartHttpServletRequest) {
      result = setBinaryVariable((MultipartHttpServletRequest) request, execution, RestResponseFactory.VARIABLE_PROCESS, false);

      if (!result.getName().equals(variableName)) {
        throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
      }

    } else {
      RestVariable restVariable = null;
      try {
        restVariable = objectMapper.readValue(request.getInputStream(), RestVariable.class);
      } catch (Exception e) {
        throw new ActivitiIllegalArgumentException("request body could not be transformed to a RestVariable instance.");
      }

      if (restVariable == null) {
        throw new ActivitiException("Invalid body was supplied");
      }
      if (!restVariable.getName().equals(variableName)) {
        throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
      }

      result = setSimpleVariable(restVariable, execution, false);
    }
    return result;
  }

  @ApiOperation(value = "Delete a variable", tags = {"Process Instances"}, nickname = "deleteProcessInstanceVariable")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the variable was found and has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested variable was not found.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}/variables/{variableName}", method = RequestMethod.DELETE)
  public void deleteVariable(@ApiParam(name = "processInstanceId") @PathVariable("processInstanceId") String processInstanceId,@ApiParam(name = "variableName") @PathVariable("variableName") String variableName,
      @RequestParam(value = "scope", required = false) String scope, HttpServletResponse response) {

    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    // Determine scope
    RestVariableScope variableScope = RestVariableScope.LOCAL;
    if (scope != null) {
      variableScope = RestVariable.getScopeFromString(scope);
    }

    if (!hasVariableOnScope(execution, variableName, variableScope)) {
      throw new ActivitiObjectNotFoundException("Execution '" + execution.getId() + "' doesn't have a variable '" + variableName + "' in scope " + variableScope.name().toLowerCase(),
          VariableInstanceEntity.class);
    }

    if (variableScope == RestVariableScope.LOCAL) {
      runtimeService.removeVariableLocal(execution.getId(), variableName);
    } else {
      // Safe to use parentId, as the hasVariableOnScope would have
      // stopped a global-var update on a root-execution
      runtimeService.removeVariable(execution.getParentId(), variableName);
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  @Override
  protected RestVariable constructRestVariable(String variableName, Object value, RestVariableScope variableScope, String executionId, boolean includeBinary) {

    return restResponseFactory.createRestVariable(variableName, value, null, executionId, RestResponseFactory.VARIABLE_PROCESS, includeBinary);
  }
}
