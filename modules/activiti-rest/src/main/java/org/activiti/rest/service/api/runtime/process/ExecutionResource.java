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


import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Put;


/**
 * @author Frederik Heremans
 */
public class ExecutionResource extends ExecutionBaseResource {

  @Get
  public ExecutionResponse getExecution() {
    if(!authenticate()) {
      return null;
    }
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createExecutionResponse(this, getExecutionFromRequest());
  }
  
  @Put
  public ExecutionResponse performExecutionAction(ExecutionActionRequest actionRequest) {
    if(!authenticate()) {
      return null;
    }
    
    Execution execution = getExecutionFromRequest();
    
    if(ExecutionActionRequest.ACTION_SIGNAL.equals(actionRequest.getAction())) {
      if(actionRequest.getVariables() != null) {
        ActivitiUtil.getRuntimeService().signal(execution.getId(), getVariablesToSet(actionRequest));
      } else {
        ActivitiUtil.getRuntimeService().signal(execution.getId());
      }
    } else if(ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(actionRequest.getAction())) {
      if(actionRequest.getSignalName() == null) {
        throw new ActivitiIllegalArgumentException("Signal name is required");
      }
      if(actionRequest.getVariables() != null) {
        ActivitiUtil.getRuntimeService().signalEventReceived(actionRequest.getSignalName(), execution.getId(), getVariablesToSet(actionRequest));
      } else {
        ActivitiUtil.getRuntimeService().signalEventReceived(actionRequest.getSignalName(), execution.getId());
      }
    } else if(ExecutionActionRequest.ACTION_MESSAGE_EVENT_RECEIVED.equals(actionRequest.getAction())) {
      if(actionRequest.getMessageName() == null) {
        throw new ActivitiIllegalArgumentException("Message name is required");
      }
      if(actionRequest.getVariables() != null) {
        ActivitiUtil.getRuntimeService().messageEventReceived(actionRequest.getMessageName(), execution.getId(), getVariablesToSet(actionRequest));
      } else {
        ActivitiUtil.getRuntimeService().messageEventReceived(actionRequest.getMessageName(), execution.getId());
      }
    } else {
      throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
    }
    
    // Re-fetch the execution, could have changed due to action or even completed
    execution = ActivitiUtil.getRuntimeService().createExecutionQuery().executionId(execution.getId()).singleResult();
    if(execution == null) {
      // Execution is finished, return empty body to inform user
      setStatus(Status.SUCCESS_NO_CONTENT);
      return null;
    } else {
      return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
      .createExecutionResponse(this, execution);
    }
  }
}
