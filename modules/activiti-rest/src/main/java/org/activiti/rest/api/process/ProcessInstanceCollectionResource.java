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


import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;


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
  
  @Delete
  public void deleteProcessInstance() {
    if(!authenticate()) {
      return;
    }
    ProcessInstance processInstance = getProcessInstanceFromRequest();
    String deleteReason = getQueryParameter("deleteReason", getQuery());
    
    ActivitiUtil.getRuntimeService().deleteProcessInstance(processInstance.getId(), deleteReason);
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
  
  protected ProcessInstance getProcessInstanceFromRequest() {
    String processInstanceId = getAttribute("processInstanceId");
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
    }
    
   ProcessInstance processInstance = ActivitiUtil.getRuntimeService().createProcessInstanceQuery()
           .processInstanceId(processInstanceId).singleResult();
    if (processInstance == null) {
      throw new ActivitiObjectNotFoundException("Could not find a process instance with id '" + processInstanceId + "'.", ProcessInstance.class);
    }
    return processInstance;
  }
}
