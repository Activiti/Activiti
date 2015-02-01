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
public class ExecutionResource extends ExecutionBaseResource {

  @RequestMapping(value="/runtime/executions/{executionId}", method = RequestMethod.GET, produces="application/json")
  public ExecutionResponse getExecution(@PathVariable String executionId, HttpServletRequest request) {
    return restResponseFactory.createExecutionResponse(getExecutionFromRequest(executionId));
  }
  
  @RequestMapping(value="/runtime/executions/{executionId}", method = RequestMethod.PUT, produces="application/json")
  public ExecutionResponse performExecutionAction(@PathVariable String executionId, @RequestBody ExecutionActionRequest actionRequest, 
      HttpServletRequest request, HttpServletResponse response) {
    
    Execution execution = getExecutionFromRequest(executionId);
    
    if (ExecutionActionRequest.ACTION_SIGNAL.equals(actionRequest.getAction())) {
      if (actionRequest.getVariables() != null) {
        runtimeService.signal(execution.getId(), getVariablesToSet(actionRequest));
      } else {
        runtimeService.signal(execution.getId());
      }
    } else if(ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(actionRequest.getAction())) {
      if (actionRequest.getSignalName() == null) {
        throw new ActivitiIllegalArgumentException("Signal name is required");
      }
      if (actionRequest.getVariables() != null) {
        runtimeService.signalEventReceived(actionRequest.getSignalName(), execution.getId(), getVariablesToSet(actionRequest));
      } else {
        runtimeService.signalEventReceived(actionRequest.getSignalName(), execution.getId());
      }
    } else if (ExecutionActionRequest.ACTION_MESSAGE_EVENT_RECEIVED.equals(actionRequest.getAction())) {
      if (actionRequest.getMessageName() == null) {
        throw new ActivitiIllegalArgumentException("Message name is required");
      }
      if (actionRequest.getVariables() != null) {
        runtimeService.messageEventReceived(actionRequest.getMessageName(), execution.getId(), getVariablesToSet(actionRequest));
      } else {
        runtimeService.messageEventReceived(actionRequest.getMessageName(), execution.getId());
      }
    } else {
      throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
    }
    
    // Re-fetch the execution, could have changed due to action or even completed
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
