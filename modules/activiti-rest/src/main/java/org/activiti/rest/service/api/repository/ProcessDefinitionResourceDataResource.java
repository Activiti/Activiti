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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.common.api.ActivitiUtil;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionResourceDataResource extends BaseDeploymentResourceDataResource {

  @Get
  public InputRepresentation getProcessDefinitionResource() {
    if (authenticate() == false)
      return null;
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();
    return getDeploymentResource(processDefinition.getDeploymentId(), processDefinition.getResourceName());
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
