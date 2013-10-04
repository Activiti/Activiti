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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.ModelQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author Frederik Heremans
 */
public class ModelCollectionResource extends BaseModelResource {

 private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();
  
  static {
    allowedSortProperties.put("id", ModelQueryProperty.MODEL_ID);
    allowedSortProperties.put("category", ModelQueryProperty.MODEL_CATEGORY);
    allowedSortProperties.put("createTime", ModelQueryProperty.MODEL_CREATE_TIME);
    allowedSortProperties.put("key", ModelQueryProperty.MODEL_KEY);
    allowedSortProperties.put("lastUpdateTime", ModelQueryProperty.MODEL_LAST_UPDATE_TIME);
    allowedSortProperties.put("name", ModelQueryProperty.MODEL_NAME);
    allowedSortProperties.put("version", ModelQueryProperty.MODEL_VERSION);
  }
  
  @Get
  public DataResponse getModels() {
    ModelQuery modelQuery = ActivitiUtil.getRepositoryService().createModelQuery();
    Form form = getQuery();
    Set<String> names = form.getNames();
    
    if(names.contains("id")) {
      modelQuery.modelId(getQueryParameter("id", form));
    }
    if(names.contains("category")) {
      modelQuery.modelCategory(getQueryParameter("category", form));
    }
    if(names.contains("categoryLike")) {
      modelQuery.modelCategoryLike(getQueryParameter("categoryLike", form));
    }
    if(names.contains("categoryNotEquals")) {
      modelQuery.modelCategoryNotEquals(getQueryParameter("categoryNotEquals", form));
    }
    if(names.contains("name")) {
      modelQuery.modelName(getQueryParameter("name", form));
    }
    if(names.contains("nameLike")) {
      modelQuery.modelNameLike(getQueryParameter("nameLike", form));
    }
    if(names.contains("key")) {
      modelQuery.modelKey(getQueryParameter("key", form));
    }
    if(names.contains("version")) {
      modelQuery.modelVersion(getQueryParameterAsInt("version", form));
    }
    if(names.contains("latestVersion")) {
      boolean isLatestVersion = getQueryParameterAsBoolean("latestVersion", form);
      if(isLatestVersion) {
        modelQuery.latestVersion();
      }
    }
    if(names.contains("deploymentId")) {
      modelQuery.deploymentId(getQueryParameter("deploymentId", form));
    }
    if(names.contains("deployed")) {
      boolean isDeployed = getQueryParameterAsBoolean("deployed", form);
      if(isDeployed) {
        modelQuery.deployed();
      } else {
        modelQuery.notDeployed();
      }
    }
    return new ModelsPaginateList(this).paginateList(form, modelQuery, "id", allowedSortProperties);
  }
  
  @Post
  public ModelResponse createModel(ModelRequest request) {
    Model model = ActivitiUtil.getRepositoryService().newModel();
    model.setCategory(request.getCategory());
    model.setDeploymentId(request.getDeploymentId());
    model.setKey(request.getKey());
    model.setMetaInfo(request.getMetaInfo());
    model.setName(request.getName());
    model.setVersion(request.getVersion());

    ActivitiUtil.getRepositoryService().saveModel(model);
    setStatus(Status.SUCCESS_CREATED);
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory().createModelResponse(this, model);
  }
}
