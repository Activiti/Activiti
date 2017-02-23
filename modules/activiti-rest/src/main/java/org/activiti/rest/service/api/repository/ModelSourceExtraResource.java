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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Models" }, description = "Manage Models", authorizations = { @Authorization(value = "basicAuth") })
public class ModelSourceExtraResource extends BaseModelSourceResource {

  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the model was found and source is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested model was not found.")
  })
  @ApiOperation(value = "Get the extra editor source for a model", tags = {"Models"},
  notes = "Response body contains the model’s raw editor source. The response’s content-type is set to application/octet-stream, regardless of the content of the source.")
  @RequestMapping(value = "/repository/models/{modelId}/source-extra", method = RequestMethod.GET)
  protected @ResponseBody
  byte[] getModelBytes(@ApiParam(name = "modelId", value="The id of the model.") @PathVariable String modelId, HttpServletResponse response) {
    byte[] editorSource = repositoryService.getModelEditorSourceExtra(modelId);
    if (editorSource == null) {
      throw new ActivitiObjectNotFoundException("Model with id '" + modelId + "' does not have extra source available.", String.class);
    }
    response.setContentType("application/octet-stream");
    return editorSource;
  }

  @ApiOperation(value = "Set the extra editor source for a model", tags = {"Models"}, consumes = "multipart/form-data",
      notes = "Response body contains the model’s raw editor source. The response’s content-type is set to application/octet-stream, regardless of the content of the source.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the model was found and the extra source has been updated."),
      @ApiResponse(code = 404, message = "Indicates the requested model was not found.")
  })
  @RequestMapping(value = "/repository/models/{modelId}/source-extra", method = RequestMethod.PUT)
  protected void setModelSource(@ApiParam(name = "modelId", value="The id of the model.") @PathVariable String modelId, HttpServletRequest request, HttpServletResponse response) {
    Model model = getModelFromRequest(modelId);
    if (model != null) {
      try {

        if (request instanceof MultipartHttpServletRequest == false) {
          throw new ActivitiIllegalArgumentException("Multipart request is required");
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        if (multipartRequest.getFileMap().size() == 0) {
          throw new ActivitiIllegalArgumentException("Multipart request with file content is required");
        }

        MultipartFile file = multipartRequest.getFileMap().values().iterator().next();

        repositoryService.addModelEditorSourceExtra(model.getId(), file.getBytes());
        response.setStatus(HttpStatus.NO_CONTENT.value());

      } catch (Exception e) {
        throw new ActivitiException("Error adding model editor source extra", e);
      }
    }
  }

}
