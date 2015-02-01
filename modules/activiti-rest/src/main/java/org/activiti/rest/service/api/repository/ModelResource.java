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
public class ModelResource extends BaseModelResource {

  @RequestMapping(value="/repository/models/{modelId}", method = RequestMethod.GET, produces = "application/json")
  public ModelResponse getModel(@PathVariable String modelId, HttpServletRequest request) {
    Model model = getModelFromRequest(modelId);
    
    return restResponseFactory.createModelResponse(model);
  }
  
  @RequestMapping(value="/repository/models/{modelId}", method = RequestMethod.PUT, produces = "application/json")
  public ModelResponse updateModel(@PathVariable String modelId, @RequestBody ModelRequest modelRequest, HttpServletRequest request) {
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

  @RequestMapping(value="/repository/models/{modelId}", method = RequestMethod.DELETE)
  public void deleteModel(@PathVariable String modelId, HttpServletResponse response) {
    Model model = getModelFromRequest(modelId);
    repositoryService.deleteModel(model.getId());
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}
