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

import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@RestController
@Api(tags = { "Process Definitions" }, description = "Manage Process Definitions", authorizations = { @Authorization(value = "basicAuth") })
public class ProcessDefinitionImageResource extends BaseProcessDefinitionResource {

  @ApiOperation(value = "Get a process definition image", tags = {"Process Definitions"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates request was successful and the process-definitions are returned"),
      @ApiResponse(code = 404, message = "Indicates the requested process definition was not found.")
  })
  @RequestMapping(value = "/repository/process-definitions/{processDefinitionId}/image", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getModelResource(@ApiParam(name = "processDefinitionId") @PathVariable String processDefinitionId) {
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
    InputStream imageStream = repositoryService.getProcessDiagram(processDefinition.getId());

    if (imageStream != null) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.set("Content-Type", "image/png");
      try {
        return new ResponseEntity<byte[]>(IOUtils.toByteArray(imageStream), responseHeaders, HttpStatus.OK);
      } catch (Exception e) {
        throw new ActivitiException("Error reading image stream", e);
      }
    } else {
      throw new ActivitiIllegalArgumentException("Process definition with id '" + processDefinition.getId() + "' has no image.");
    }
  }

}
