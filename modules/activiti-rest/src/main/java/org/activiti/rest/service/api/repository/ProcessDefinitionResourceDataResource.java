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

import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class ProcessDefinitionResourceDataResource extends BaseDeploymentResourceDataResource {

  @RequestMapping(value="/repository/process-definitions/{processDefinitionId}/resourcedata", method = RequestMethod.GET)
  public @ResponseBody byte[] getProcessDefinitionResource(@PathVariable String processDefinitionId, HttpServletResponse response) {
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
    return getDeploymentResourceData(processDefinition.getDeploymentId(), processDefinition.getResourceName(), response);
  }
  
  /**
   * Returns the {@link ProcessDefinition} that is requested. Throws the right exceptions
   * when bad request was made or definition is not found.
   */
  protected ProcessDefinition getProcessDefinitionFromRequest(String processDefinitionId) {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(processDefinitionId).singleResult();
   
    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("Could not find a process definition with id '" + processDefinitionId + "'.", ProcessDefinition.class);
    }
    return processDefinition;
  }
}
