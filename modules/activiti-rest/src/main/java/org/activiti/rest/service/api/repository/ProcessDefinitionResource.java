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

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.exception.ActivitiConflictException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class ProcessDefinitionResource extends BaseProcessDefinitionResource {
  
  @RequestMapping(value="/repository/process-definitions/{processDefinitionId}", method = RequestMethod.GET, produces = "application/json")
  public ProcessDefinitionResponse getProcessDefinition(@PathVariable String processDefinitionId, HttpServletRequest request) {
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
   
    return restResponseFactory.createProcessDefinitionResponse(processDefinition);
  }
  
  @RequestMapping(value="/repository/process-definitions/{processDefinitionId}", method = RequestMethod.PUT, produces = "application/json")
  public ProcessDefinitionResponse executeProcessDefinitionAction(@PathVariable String processDefinitionId, 
      @RequestBody ProcessDefinitionActionRequest actionRequest, HttpServletRequest request) {
    
    if (actionRequest == null) {
      throw new ActivitiIllegalArgumentException("No action found in request body.");
    }
    
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
    
    if (actionRequest.getCategory() != null) {
      // Update of category required
      repositoryService.setProcessDefinitionCategory(processDefinition.getId(), actionRequest.getCategory());
      
      // No need to re-fetch the ProcessDefinition entity, just update category in response
      ProcessDefinitionResponse response = restResponseFactory.createProcessDefinitionResponse(processDefinition);
      response.setCategory(actionRequest.getCategory());
      return response;
      
    } else {
      // Actual action
      if (actionRequest.getAction() != null) {
        if (ProcessDefinitionActionRequest.ACTION_SUSPEND.equals(actionRequest.getAction())) {
          return suspendProcessDefinition(processDefinition, actionRequest.isIncludeProcessInstances(), actionRequest.getDate());
          
        } else if (ProcessDefinitionActionRequest.ACTION_ACTIVATE.equals(actionRequest.getAction())) {
          return activateProcessDefinition(processDefinition, actionRequest.isIncludeProcessInstances(), actionRequest.getDate());
        }
      }
      
      throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
    }
  }
  
  protected ProcessDefinitionResponse activateProcessDefinition(ProcessDefinition processDefinition, boolean suspendInstances, Date date) {
    
    if (!repositoryService.isProcessDefinitionSuspended(processDefinition.getId())) {
      throw new ActivitiConflictException("Process definition with id '" + processDefinition.getId() + " ' is already active");
    }
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), suspendInstances, date);
   
    ProcessDefinitionResponse response = restResponseFactory.createProcessDefinitionResponse(processDefinition);
    
    // No need to re-fetch the ProcessDefinition, just alter the suspended state of the result-object
    response.setSuspended(false);
    return response;
  }

  protected ProcessDefinitionResponse suspendProcessDefinition(ProcessDefinition processDefinition, boolean suspendInstances, Date date) {
    
    if (repositoryService.isProcessDefinitionSuspended(processDefinition.getId())) {
      throw new ActivitiConflictException("Process definition with id '" + processDefinition.getId() + " ' is already suspended");
    }
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), suspendInstances, date);
    
    ProcessDefinitionResponse response = restResponseFactory.createProcessDefinitionResponse(processDefinition);
    
    // No need to re-fetch the ProcessDefinition, just alter the suspended state of the result-object
    response.setSuspended(true);
    return response;
  }

}
