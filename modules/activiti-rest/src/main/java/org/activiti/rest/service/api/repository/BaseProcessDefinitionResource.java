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

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Tijs Rademakers
 */
public class BaseProcessDefinitionResource {
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected RepositoryService repositoryService;

  /**
   * Returns the {@link ProcessDefinition} that is requested. Throws the right exceptions
   * when bad request was made or definition is not found.
   */
  protected ProcessDefinition getProcessDefinitionFromRequest(String processDefinitionId) {
    ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
   
    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("Could not find a process definition with id '" + processDefinitionId + "'.", ProcessDefinition.class);
    }
    return processDefinition;
  }
}
