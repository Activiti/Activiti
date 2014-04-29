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
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class ProcessInstanceResource extends SecuredResource {

  @Get
  public ProcessInstanceResponse getProcessInstance() {
    if(!authenticate()) {
      return null;
    }
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessInstanceResponse(this, getProcessInstanceFromRequest());
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
  
  @Put
  public ProcessInstanceResponse performProcessInstanceAction(ProcessInstanceActionRequest actionRequest) {
    if(!authenticate()) {
      return null;
    }
    
    ProcessInstance processInstance = getProcessInstanceFromRequest();
    if(ProcessInstanceActionRequest.ACTION_ACTIVATE.equals(actionRequest.getAction())) {
      return activateProcessInstance(processInstance);
    } else if(ProcessInstanceActionRequest.ACTION_SUSPEND.equals(actionRequest.getAction())) {
      return suspendProcessInstance(processInstance);
    }
    throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
  }
  
  protected ProcessInstanceResponse activateProcessInstance(ProcessInstance processInstance) {
    if(!processInstance.isSuspended()) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "Process instance with id '" + processInstance.getId() + "' is already active.", null, null);
    }
    ActivitiUtil.getRuntimeService().activateProcessInstanceById(processInstance.getId());
   
    ProcessInstanceResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessInstanceResponse(this, processInstance);
    
    // No need to re-fetch the instance, just alter the suspended state of the result-object
    response.setSuspended(false);
    return response;
  }

  protected ProcessInstanceResponse suspendProcessInstance(ProcessInstance processInstance) {
    if(processInstance.isSuspended()) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "Process instance with id '" + processInstance.getId() + "' is already suspended.", null, null);
    }
    ActivitiUtil.getRuntimeService().suspendProcessInstanceById(processInstance.getId());
    
    ProcessInstanceResponse response = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessInstanceResponse(this, processInstance);
    
    // No need to re-fetch the instance, just alter the suspended state of the result-object
    response.setSuspended(true);
    return response;
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
