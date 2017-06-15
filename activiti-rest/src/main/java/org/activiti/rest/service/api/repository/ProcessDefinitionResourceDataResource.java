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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

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
@Api(tags = { "Process Definitions" }, description = "Manage Process Definitions", authorizations = { @Authorization(value = "basicAuth") })
public class ProcessDefinitionResourceDataResource extends BaseDeploymentResourceDataResource {

  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates both process definition and resource have been found and the resource data has been returned."),
      @ApiResponse(code = 404, message = "Indicates the requested process definition was not found or there is no resource with the given id present in the process definition. The status-description contains additional information.")
  })
  @ApiOperation(value = "Get a process definition resource content", tags = {"Process Definitions"})
  @RequestMapping(value = "/repository/process-definitions/{processDefinitionId}/resourcedata", method = RequestMethod.GET)
  public @ResponseBody
  byte[] getProcessDefinitionResource(@ApiParam(name="processDefinitionId",value="The id of the process definition to get the resource data for.") @PathVariable String processDefinitionId, HttpServletResponse response) {
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
    return getDeploymentResourceData(processDefinition.getDeploymentId(), processDefinition.getResourceName(), response);
  }

  /**
   * Returns the {@link ProcessDefinition} that is requested. Throws the right exceptions when bad request was made or definition is not found.
   */
  protected ProcessDefinition getProcessDefinitionFromRequest(String processDefinitionId) {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();

    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("Could not find a process definition with id '" + processDefinitionId + "'.", ProcessDefinition.class);
    }
    return processDefinition;
  }
}
