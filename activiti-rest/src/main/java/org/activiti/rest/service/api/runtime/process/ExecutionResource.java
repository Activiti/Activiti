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
import org.activiti.engine.runtime.Execution;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Executions" }, description = "Manage Executions", authorizations = { @Authorization(value = "basicAuth") })
public class ExecutionResource extends ExecutionBaseResource {

  @ApiOperation(value = "Get an execution", tags = {"Executions"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the execution was found and returned."),
      @ApiResponse(code = 404, message = "Indicates the execution was not found.")
  })
  @RequestMapping(value = "/runtime/executions/{executionId}", method = RequestMethod.GET, produces = "application/json")
  public ExecutionResponse getExecution(@ApiParam(name = "executionId", value="The id of the execution to get.") @PathVariable String executionId, HttpServletRequest request) {
    return restResponseFactory.createExecutionResponse(getExecutionFromRequest(executionId));
  }

  @ApiOperation(value = "Execute an action on an execution", tags = {"Executions"},
      notes = "## Request body (signal an execution):\n\n"
          + " ```JSON\n" + "{\n" + "  \"action\":\"signal\"\n" + "} ```"
          + "\n\n\n"
          + "Both a variables and transientVariables property is accepted with following structure"
          + " ```JSON\n" + "{\n" + "  \"action\":\"signal\",\n" + "  \"variables\" : [\n" + "    {\n" + "      \"name\": \"myVar\",\n"
          + "      \"value\": \"someValue\"\n" + "    }\n" + "  ]\n" + "}```"
          + "\n\n\n"
          + "## Request body (signal event received for execution)\n\n"
          + " ```JSON\n" + "{\n" + "  \"action\":\"signal\"\n" + "} ```"
          + "\n\n\n"
          + "Notifies the execution that a signal event has been received, requires a signalName parameter. Optional variables can be passed that are set on the execution before the action is executed."
          + "## Request body (signal event received for execution)\n\n"
          + " ```JSON\n" + "{\n" + "  \"action\":\"messageEventReceived\",\n" + "  \"messageName\":\"myMessage\"\n" + "  \"variables\": [  ]\n" + "} ```"
          + "\n\n\n"
      )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the execution was found and the action is performed."),
      @ApiResponse(code = 204, message = "Indicates the execution was found, the action was performed and the action caused the execution to end."),
      @ApiResponse(code = 400, message = "Indicates an illegal action was requested, required parameters are missing in the request body or illegal variables are passed in. Status description contains additional information about the error."),
      @ApiResponse(code = 404, message = "Indicates the execution was not found.")
  })
  @RequestMapping(value = "/runtime/executions/{executionId}", method = RequestMethod.PUT, produces = "application/json")
  public ExecutionResponse performExecutionAction(@ApiParam(name = "executionId", value="The id of the execution to execute action on.") @PathVariable String executionId, @RequestBody ExecutionActionRequest actionRequest, HttpServletRequest request, HttpServletResponse response) {

    Execution execution = getExecutionFromRequest(executionId);

    if (ExecutionActionRequest.ACTION_SIGNAL.equals(actionRequest.getAction())
        || ExecutionActionRequest.ACTION_TRIGGER.equals(actionRequest.getAction())) {
      if (actionRequest.getTransientVariables() != null && actionRequest.getVariables() != null) {
        runtimeService.trigger(execution.getId(), getVariablesToSet(actionRequest.getVariables()), getVariablesToSet(actionRequest.getTransientVariables()));
      } else if (actionRequest.getVariables() != null) {
        runtimeService.trigger(execution.getId(), getVariablesToSet(actionRequest.getVariables()));
      } else {
        runtimeService.trigger(execution.getId());
      }
    } else if (ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(actionRequest.getAction())) {
      if (actionRequest.getSignalName() == null) {
        throw new ActivitiIllegalArgumentException("Signal name is required");
      }
      if (actionRequest.getVariables() != null) {
        runtimeService.signalEventReceived(actionRequest.getSignalName(), execution.getId(), getVariablesToSet(actionRequest.getVariables()));
      } else {
        runtimeService.signalEventReceived(actionRequest.getSignalName(), execution.getId());
      }
    } else if (ExecutionActionRequest.ACTION_MESSAGE_EVENT_RECEIVED.equals(actionRequest.getAction())) {
      if (actionRequest.getMessageName() == null) {
        throw new ActivitiIllegalArgumentException("Message name is required");
      }
      if (actionRequest.getVariables() != null) {
        runtimeService.messageEventReceived(actionRequest.getMessageName(), execution.getId(), getVariablesToSet(actionRequest.getVariables()));
      } else {
        runtimeService.messageEventReceived(actionRequest.getMessageName(), execution.getId());
      }
    } else {
      throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
    }

    // Re-fetch the execution, could have changed due to action or even
    // completed
    execution = runtimeService.createExecutionQuery().executionId(execution.getId()).singleResult();
    if (execution == null) {
      // Execution is finished, return empty body to inform user
      response.setStatus(HttpStatus.NO_CONTENT.value());
      return null;
    } else {
      return restResponseFactory.createExecutionResponse(execution);
    }
  }
}
