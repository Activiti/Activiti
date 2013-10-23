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

package org.activiti.rest.service.api.repository;

import java.util.Date;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionResource extends BaseProcessDefinitionResource {
  
  @Get
  public ProcessDefinitionResponse getProcessDefinition() {
    if(authenticate() == false) return null;
    
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();
   
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
     .createProcessDefinitionResponse(this, processDefinition);
  }
  
  @Put
  public ProcessDefinitionResponse executeProcessDefinitionAction(ProcessDefinitionActionRequest actionRequest) {
    if(authenticate() == false) return null;
    
    if(actionRequest == null) {
      throw new ActivitiIllegalArgumentException("No action found in request body.");
    }
    
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();
    
    if(actionRequest.getCategory() != null) {
      // Update of category required
      ActivitiUtil.getRepositoryService().setProcessDefinitionCategory(processDefinition.getId(), actionRequest.getCategory());
      
      // No need to re-fetch the ProcessDefinition entity, just update category in response
      ProcessDefinitionResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
              .createProcessDefinitionResponse(this, processDefinition);
      response.setCategory(actionRequest.getCategory());
      return response;
      
    } else {
      // Actual action
      if(actionRequest.getAction() != null) {
        if(ProcessDefinitionActionRequest.ACTION_SUSPEND.equals(actionRequest.getAction())) {
          return suspendProcessDefinition(processDefinition, actionRequest.isIncludeProcessInstances(), actionRequest.getDate());
        } else if(ProcessDefinitionActionRequest.ACTION_ACTIVATE.equals(actionRequest.getAction())) {
          return activateProcessDefinition(processDefinition, actionRequest.isIncludeProcessInstances(), actionRequest.getDate());
        }
      }
      
      throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
    }
  }
  
  protected ProcessDefinitionResponse activateProcessDefinition(ProcessDefinition processDefinition, boolean suspendInstances, Date date) {
    if(!processDefinition.isSuspended()) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "Process definition with id '" + processDefinition.getId() + " ' is already active", null, null);
    }
    ActivitiUtil.getRepositoryService().activateProcessDefinitionById(processDefinition.getId(), suspendInstances, date);
   
    ProcessDefinitionResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessDefinitionResponse(this, processDefinition);
    
    // No need to re-fetch the ProcessDefinition, just alter the suspended state of the result-object
    response.setSuspended(false);
    return response;
  }

  protected ProcessDefinitionResponse suspendProcessDefinition(ProcessDefinition processDefinition, boolean suspendInstances, Date date) {
    if(processDefinition.isSuspended()) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "Process definition with id '" + processDefinition.getId() + " ' is already suspended", null, null);
    }
    ActivitiUtil.getRepositoryService().suspendProcessDefinitionById(processDefinition.getId(), suspendInstances, date);
    
    ProcessDefinitionResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessDefinitionResponse(this, processDefinition);
    
    // No need to re-fetch the ProcessDefinition, just alter the suspended state of the result-object
    response.setSuspended(true);
    return response;
  }

}
