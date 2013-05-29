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

package org.activiti.rest.api.process;


import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.RestResponseFactory;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;


/**
 * @author Frederik Heremans
 */
public class ProcessInstanceCollectionResource extends ProcessInstanceBasedResource {

  @Get
  public DataResponse getProcessInstances() {
    if(!authenticate()) {
      return null;
    }
    Form urlQuery = getQuery();
   
    // Populate query based on request
    ProcessInstanceQueryRequest queryRequest = new ProcessInstanceQueryRequest();
    
    if(getQueryParameter("id", urlQuery) != null) {
      queryRequest.setProcessInstanceId(getQueryParameter("id", urlQuery));
    }
    
    if(getQueryParameter("processDefinitionKey", urlQuery) != null) {
      queryRequest.setProcessDefinitionKey(getQueryParameter("processDefinitionKey", urlQuery));
    }
    
    if(getQueryParameter("processDefinitionId", urlQuery) != null) {
      queryRequest.setProcessDefinitionId(getQueryParameter("processDefinitionId", urlQuery));
    }
    
    if(getQueryParameter("businessKey", urlQuery) != null) {
      queryRequest.setProcessBusinessKey(getQueryParameter("businessKey", urlQuery));
    }
    
    if(getQueryParameter("involvedUser", urlQuery) != null) {
      queryRequest.setInvolvedUser(getQueryParameter("involvedUser", urlQuery));
    }
    
    if(getQueryParameter("suspended", urlQuery) != null) {
      queryRequest.setSuspended(getQueryParameterAsBoolean("suspended", urlQuery));
    }
    
    if(getQueryParameter("superProcessInstanceId", urlQuery) != null) {
      queryRequest.setSuperProcessInstanceId(getQueryParameter("superProcessInstanceId", urlQuery));
    }
    
    if(getQueryParameter("subProcessInstanceId", urlQuery) != null) {
      queryRequest.setSubProcessInstanceId(getQueryParameter("subProcessInstanceId", urlQuery));
    }
    
    return getQueryResponse(queryRequest, urlQuery);
  }
  
  
  @Post
  public ProcessInstanceResponse createProcessInstance(ProcessInstanceCreateRequest request) {
    
    if(request.getProcessDefinitionId() == null && request.getProcessDefinitionKey() == null && request.getMessage() == null) {
      throw new ActivitiIllegalArgumentException("Either processDefinitionId, processDefinitionKey or message is required.");
    }
    
    int paramsSet = ((request.getProcessDefinitionId() != null) ? 1 : 0)
            + ((request.getProcessDefinitionKey() != null) ? 1 : 0)
            + ((request.getMessage() != null) ? 1 : 0);
    
    if(paramsSet > 1) {
      throw new ActivitiIllegalArgumentException("Only one of processDefinitionId, processDefinitionKey or message should be set.");
    }
    
    RestResponseFactory factory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    
    Map<String, Object> startVariables = null;
    if(request.getVariables() != null) {
      startVariables = new HashMap<String, Object>();
      for(RestVariable variable : request.getVariables()) {
        if(variable.getName() == null) {
          throw new ActivitiIllegalArgumentException("Variable name is required.");
        }
        startVariables.put(variable.getName(), factory.getVariableValue(variable));
      }
    }
    
    // Actually start the instance based on key or id
    try {
      ProcessInstance instance = null;
      if(request.getProcessDefinitionId() != null) {
        instance = ActivitiUtil.getRuntimeService().startProcessInstanceById(
                request.getProcessDefinitionId(), request.getBusinessKey(), startVariables);
      } else if(request.getProcessDefinitionKey() != null){
        instance = ActivitiUtil.getRuntimeService().startProcessInstanceByKey(
                request.getProcessDefinitionKey(), request.getBusinessKey(), startVariables);
      } else {
        instance = ActivitiUtil.getRuntimeService().startProcessInstanceByMessage(
                request.getMessage(), request.getBusinessKey(), startVariables);
      }
      
      setStatus(Status.SUCCESS_CREATED);
      return factory.createProcessInstanceResponse(this, instance);
    } catch(ActivitiObjectNotFoundException aonfe) {
      throw new ActivitiIllegalArgumentException(aonfe.getMessage(), aonfe);
    }
  }
}
