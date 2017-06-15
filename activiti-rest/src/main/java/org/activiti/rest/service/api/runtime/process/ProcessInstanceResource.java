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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.exception.ActivitiConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Process Instances" }, description = "Manage Process Instances", authorizations = { @Authorization(value = "basicAuth") })
public class ProcessInstanceResource extends BaseProcessInstanceResource {

  @ApiOperation(value = "Get a process instance", tags = {"Process Instances"}, nickname = "getProcessInstance")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the process instance was found and returned."),
      @ApiResponse(code = 404, message = "Indicates the requested process instance was not found.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}", method = RequestMethod.GET, produces = "application/json")
  public ProcessInstanceResponse getProcessInstance(@ApiParam(name = "processInstanceId", value="The id of the process instance to get.") @PathVariable String processInstanceId, HttpServletRequest request) {
    return restResponseFactory.createProcessInstanceResponse(getProcessInstanceFromRequest(processInstanceId));
  }

  @ApiOperation(value = "Delete a process instance", tags = {"Process Instances"}, nickname = "deleteProcessInstance")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the process instance was found and deleted. Response body is left empty intentionally."),
      @ApiResponse(code = 404, message = "Indicates the requested process instance was not found.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}", method = RequestMethod.DELETE)
  public void deleteProcessInstance(@ApiParam(name = "processInstanceId", value="The id of the process instance to delete.") @PathVariable String processInstanceId, @RequestParam(value = "deleteReason", required = false) String deleteReason, HttpServletResponse response) {

    ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);

    runtimeService.deleteProcessInstance(processInstance.getId(), deleteReason);
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  @ApiOperation(value = "Activate or suspend a process instance", tags = {"Process Instances"},
      notes="## Activate a process instance\n\n"
          + " ```JSON\n" + "{\n" + "  \"action\" : \"suspend\"\n" + "} ```"
          + "\n\n\n"
          + "## Suspend a process instance\n\n"
          + " ```JSON\n" + "{\n" + "  \"action\" : \"activate\"\n" + "} ```"
          + "\n\n\n"
      )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the process instance was found and action was executed."),
      @ApiResponse(code = 400, message = "\t\n" + "Indicates an invalid action was supplied."),
      @ApiResponse(code = 409, message = "Indicates the requested process instance action cannot be executed since the process-instance is already activated/suspended."),
      @ApiResponse(code = 404, message = "Indicates the requested process instance was not found.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}", method = RequestMethod.PUT, produces = "application/json")
  public ProcessInstanceResponse performProcessInstanceAction(@ApiParam(name = "processInstanceId", value="The id of the process instance to activate/suspend.") @PathVariable String processInstanceId, @RequestBody ProcessInstanceActionRequest actionRequest, HttpServletRequest request) {

    ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);

    if (ProcessInstanceActionRequest.ACTION_ACTIVATE.equals(actionRequest.getAction())) {
      return activateProcessInstance(processInstance);

    } else if (ProcessInstanceActionRequest.ACTION_SUSPEND.equals(actionRequest.getAction())) {
      return suspendProcessInstance(processInstance);
    }
    throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
  }


  protected ProcessInstanceResponse activateProcessInstance(ProcessInstance processInstance) {
    if (!processInstance.isSuspended()) {
      throw new ActivitiConflictException("Process instance with id '" + processInstance.getId() + "' is already active.");
    }
    runtimeService.activateProcessInstanceById(processInstance.getId());

    ProcessInstanceResponse response = restResponseFactory.createProcessInstanceResponse(processInstance);

    // No need to re-fetch the instance, just alter the suspended state of
    // the result-object
    response.setSuspended(false);
    return response;
  }

  protected ProcessInstanceResponse suspendProcessInstance(ProcessInstance processInstance) {
    if (processInstance.isSuspended()) {
      throw new ActivitiConflictException("Process instance with id '" + processInstance.getId() + "' is already suspended.");
    }
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    ProcessInstanceResponse response = restResponseFactory.createProcessInstanceResponse(processInstance);

    // No need to re-fetch the instance, just alter the suspended state of
    // the result-object
    response.setSuspended(true);
    return response;
  }
}
