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
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.restlet.data.Form;
import org.restlet.resource.Get;
import org.restlet.resource.Put;


/**
 * @author Frederik Heremans
 */
public class ExecutionCollectionResource extends ExecutionBaseResource {

  @Get
  public DataResponse getProcessInstances() {
    if(!authenticate()) {
      return null;
    }
    Form urlQuery = getQuery();
   
    // Populate query based on request
    ExecutionQueryRequest queryRequest = new ExecutionQueryRequest();
    
    if(getQueryParameter("id", urlQuery) != null) {
      queryRequest.setId(getQueryParameter("id", urlQuery));
    }
    
    if(getQueryParameter("processInstanceId", urlQuery) != null) {
      queryRequest.setProcessInstanceId(getQueryParameter("processInstanceId", urlQuery));
    }
    
    if(getQueryParameter("processInstanceBusinessKey", urlQuery) != null) {
      queryRequest.setProcessBusinessKey(getQueryParameter("processInstanceBusinessKey", urlQuery));
    }
    
    if(getQueryParameter("processDefinitionKey", urlQuery) != null) {
      queryRequest.setProcessDefinitionKey(getQueryParameter("processDefinitionKey", urlQuery));
    }
    
    if(getQueryParameter("processDefinitionId", urlQuery) != null) {
      queryRequest.setProcessDefinitionId(getQueryParameter("processDefinitionId", urlQuery));
    }
    
    if(getQueryParameter("messageEventSubscriptionName", urlQuery) != null) {
      queryRequest.setMessageEventSubscriptionName(getQueryParameter("messageEventSubscriptionName", urlQuery));
    }
    
    if(getQueryParameter("signalEventSubscriptionName", urlQuery) != null) {
      queryRequest.setSignalEventSubscriptionName(getQueryParameter("signalEventSubscriptionName", urlQuery));
    }
    
    if(getQueryParameter("activityId", urlQuery) != null) {
      queryRequest.setActivityId(getQueryParameter("activityId", urlQuery));
    }
    
    if(getQueryParameter("parentId", urlQuery) != null) {
      queryRequest.setParentId(getQueryParameter("parentId", urlQuery));
    }
    return getQueryResponse(queryRequest, urlQuery);
  }
  
  @Put
  public void executeExecutionAction(ExecutionActionRequest actionRequest) {
    if(!ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(actionRequest.getAction())) {
      throw new ActivitiIllegalArgumentException("Illegal action: '" + actionRequest.getAction() +"'.");
    }
    
    if(actionRequest.getSignalName() == null) {
      throw new ActivitiIllegalArgumentException("Signal name is required.");
    }
    
    if(actionRequest.getVariables() != null) {
      ActivitiUtil.getRuntimeService().signalEventReceived(actionRequest.getSignalName(), getVariablesToSet(actionRequest));
    } else {
      ActivitiUtil.getRuntimeService().signalEventReceived(actionRequest.getSignalName());
    }
  }
}
