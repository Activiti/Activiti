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

package org.activiti.rest.api.repository;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RestActionRequest;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionResource extends SecuredResource {
  
  @Get
  public ProcessDefinitionResponse getProcessDefinition() {
    if(authenticate() == false) return null;
    
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();
   
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
     .createProcessDefinitionResponse(this, processDefinition);
  }
  
  @Put
  public ProcessDefinitionResponse executeProcessDefinitionAction(RestActionRequest actionRequest) {
    if(authenticate() == false) return null;
    
    if(actionRequest == null) {
      throw new ActivitiIllegalArgumentException("No action found in request body.");
    }
    
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();
    
    if(actionRequest.getAction() != null) {
      if("suspend".equals(actionRequest.getAction())) {
        return suspendProcessDefinition(processDefinition);
      } else if("activate".equals(actionRequest.getAction())) {
        return activateProcessDefinition(processDefinition);
      }
    }
    
    throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "', use 'suspend' or 'activate'.");
  }
  
  protected ProcessDefinitionResponse activateProcessDefinition(ProcessDefinition processDefinition) {
    if(!processDefinition.isSuspended()) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Process definition with id '" + processDefinition.getId() + "'is already active");
    }
    ActivitiUtil.getRepositoryService().activateProcessDefinitionById(processDefinition.getId());
   
    ProcessDefinitionResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessDefinitionResponse(this, processDefinition);
    
    // No need to re-fetch the ProcessDefinition, just alter the suspended state of the result-object
    response.setSuspended(false);
    return response;
  }

  protected ProcessDefinitionResponse suspendProcessDefinition(ProcessDefinition processDefinition) {
    if(processDefinition.isSuspended()) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Process definition with id '" + processDefinition.getId() + "'is already suspended");
    }
    ActivitiUtil.getRepositoryService().suspendProcessDefinitionById(processDefinition.getId());
    
    ProcessDefinitionResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessDefinitionResponse(this, processDefinition);
    
    // No need to re-fetch the ProcessDefinition, just alter the suspended state of the result-object
    response.setSuspended(true);
    return response;
  }

  /**
   * Returns the {@link ProcessDefinition} that is requested. Throws the right exceptions
   * when bad request was made or definition is not found.
   */
  protected ProcessDefinition getProcessDefinitionFromRequest() {
    String processDefinitionId = getAttribute("processDefinitionId");
    if(processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("The processDefinitionId cannot be null");
    }
    
    ProcessDefinition processDefinition = ActivitiUtil.getRepositoryService().createProcessDefinitionQuery()
            .processDefinitionId(processDefinitionId).singleResult();
   
   if(processDefinition == null) {
     throw new ActivitiObjectNotFoundException("Could not find a process definition with id '" + processDefinitionId + "'.", ProcessDefinition.class);
   }
   return processDefinition;
  }
}
