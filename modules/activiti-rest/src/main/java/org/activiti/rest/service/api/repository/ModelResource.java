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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.repository.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Models" }, description = "Manage Models", authorizations = { @Authorization(value = "basicAuth") })
public class ModelResource extends BaseModelResource {

  @ApiOperation(value = "Get a model", tags = {"Models"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the model was found and returned."),
      @ApiResponse(code = 404, message = "Indicates the requested model was not found.")
  })
  @RequestMapping(value = "/repository/models/{modelId}", method = RequestMethod.GET, produces = "application/json")
  public ModelResponse getModel(@ApiParam(name = "modelId", value="The id of the model to get.") @PathVariable String modelId, HttpServletRequest request) {
    Model model = getModelFromRequest(modelId);

    return restResponseFactory.createModelResponse(model);
  }

  @ApiOperation(value = "Update a model", tags = {"Models"},
      notes ="All request values are optional. "
          + "For example, you can only include the name attribute in the request body JSON-object, only updating the name of the model, leaving all other fields unaffected. "
          + "When an attribute is explicitly included and is set to null, the model-value will be updated to null. "
          + "Example: ```JSON \n{\"metaInfo\" : null}``` will clear the metaInfo of the model).")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the model was found and updated."),
      @ApiResponse(code = 404, message = "Indicates the requested model was not found.")
  })
  @RequestMapping(value = "/repository/models/{modelId}", method = RequestMethod.PUT, produces = "application/json")
  public ModelResponse updateModel(@ApiParam(name = "modelId") @PathVariable String modelId, @RequestBody ModelRequest modelRequest, HttpServletRequest request) {
    Model model = getModelFromRequest(modelId);

    if (modelRequest.isCategoryChanged()) {
      model.setCategory(modelRequest.getCategory());
    }
    if (modelRequest.isDeploymentChanged()) {
      model.setDeploymentId(modelRequest.getDeploymentId());
    }
    if (modelRequest.isKeyChanged()) {
      model.setKey(modelRequest.getKey());
    }
    if (modelRequest.isMetaInfoChanged()) {
      model.setMetaInfo(modelRequest.getMetaInfo());
    }
    if (modelRequest.isNameChanged()) {
      model.setName(modelRequest.getName());
    }
    if (modelRequest.isVersionChanged()) {
      model.setVersion(modelRequest.getVersion());
    }
    if (modelRequest.isTenantIdChanged()) {
      model.setTenantId(modelRequest.getTenantId());
    }

    repositoryService.saveModel(model);
    return restResponseFactory.createModelResponse(model);
  }

  @ApiOperation(value = "Delete a model", tags = {"Models"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the model was found and has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested model was not found.")
  })
  @RequestMapping(value = "/repository/models/{modelId}", method = RequestMethod.DELETE)
  public void deleteModel(@ApiParam(name = "modelId", value="The id of the model to delete.") @PathVariable String modelId, HttpServletResponse response) {
    Model model = getModelFromRequest(modelId);
    repositoryService.deleteModel(model.getId());
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}
