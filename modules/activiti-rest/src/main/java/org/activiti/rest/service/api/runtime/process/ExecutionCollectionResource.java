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


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.rest.common.api.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class ExecutionCollectionResource extends ExecutionBaseResource {

  @RequestMapping(value="/runtime/executions", method = RequestMethod.GET, produces="application/json")
  public DataResponse getProcessInstances(@RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    // Populate query based on request
    ExecutionQueryRequest queryRequest = new ExecutionQueryRequest();
    
    if (allRequestParams.containsKey("id")) {
      queryRequest.setId(allRequestParams.get("id"));
    }
    
    if (allRequestParams.containsKey("processInstanceId")) {
      queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }
    
    if (allRequestParams.containsKey("processInstanceBusinessKey")) {
      queryRequest.setProcessBusinessKey(allRequestParams.get("processInstanceBusinessKey"));
    }
    
    if (allRequestParams.containsKey("processDefinitionKey")) {
      queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
    }
    
    if (allRequestParams.containsKey("processDefinitionId")) {
      queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
    }
    
    if (allRequestParams.containsKey("messageEventSubscriptionName")) {
      queryRequest.setMessageEventSubscriptionName(allRequestParams.get("messageEventSubscriptionName"));
    }
    
    if (allRequestParams.containsKey("signalEventSubscriptionName")) {
      queryRequest.setSignalEventSubscriptionName(allRequestParams.get("signalEventSubscriptionName"));
    }
    
    if (allRequestParams.containsKey("activityId")) {
      queryRequest.setActivityId(allRequestParams.get("activityId"));
    }
    
    if (allRequestParams.containsKey("parentId")) {
      queryRequest.setParentId(allRequestParams.get("parentId"));
    }
    
    if (allRequestParams.containsKey("tenantId")) {
      queryRequest.setTenantId(allRequestParams.get("tenantId"));
    }
    
    if (allRequestParams.containsKey("tenantIdLike")) {
      queryRequest.setTenantIdLike(allRequestParams.get("tenantIdLike"));
    }
    
    if (allRequestParams.containsKey("withoutTenantId")) {
      if (Boolean.valueOf(allRequestParams.get("withoutTenantId"))) {
        queryRequest.setWithoutTenantId(Boolean.TRUE);
      }
    }
    
    return getQueryResponse(queryRequest, allRequestParams, 
        request.getRequestURL().toString().replace("/runtime/executions", ""));
  }
  
  @RequestMapping(value="/runtime/executions", method = RequestMethod.PUT)
  public void executeExecutionAction(@RequestBody ExecutionActionRequest actionRequest, HttpServletResponse response) {
    if (!ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(actionRequest.getAction())) {
      throw new ActivitiIllegalArgumentException("Illegal action: '" + actionRequest.getAction() +"'.");
    }
    
    if (actionRequest.getSignalName() == null) {
      throw new ActivitiIllegalArgumentException("Signal name is required.");
    }
    
    if (actionRequest.getVariables() != null) {
      runtimeService.signalEventReceived(actionRequest.getSignalName(), getVariablesToSet(actionRequest));
    } else {
      runtimeService.signalEventReceived(actionRequest.getSignalName());
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}
