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
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.impl.ModelQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.rest.common.api.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Models" }, description = "Manage Models", authorizations = { @Authorization(value = "basicAuth") })
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
    allowedSortProperties.put("tenantId", ModelQueryProperty.MODEL_TENANT_ID);
  }

  @ApiOperation(value = "Get a list of models", tags = {"Models"})
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", dataType = "string", value = "Only return models with the given version.", paramType = "query"),
    @ApiImplicitParam(name = "category", dataType = "string", value = "Only return models with the given category.", paramType = "query"),
    @ApiImplicitParam(name = "categoryLike", dataType = "string", value = "Only return models with a category like the given name.", paramType = "query"),
    @ApiImplicitParam(name = "categoryNotEquals", dataType = "string", value = "Only return models which don’t have the given category.", paramType = "query"),
    @ApiImplicitParam(name = "name", dataType = "string", value = "Only return models with the given name.", paramType = "query"),
    @ApiImplicitParam(name = "nameLike", dataType = "string", value = "Only return models with a name like the given name.", paramType = "query"),
    @ApiImplicitParam(name = "key", dataType = "string", value = "Only return models with the given key.", paramType = "query"),
    @ApiImplicitParam(name = "deploymentId", dataType = "string", value = "Only return models with the given category.", paramType = "query"),
    @ApiImplicitParam(name = "version", dataType = "integer", value = "Only return models with the given version.", paramType = "query"),
    @ApiImplicitParam(name = "latestVersion", dataType = "boolean", value = "If true, only return models which are the latest version. Best used in combination with key. If false is passed in as value, this is ignored and all versions are returned.", paramType = "query"),
    @ApiImplicitParam(name = "deployed", dataType = "boolean", value = "If true, only deployed models are returned. If false, only undeployed models are returned (deploymentId is null).", paramType = "query"),
    @ApiImplicitParam(name = "tenantId", dataType = "string", value = "Only return models with the given tenantId.", paramType = "query"),
    @ApiImplicitParam(name = "tenantIdLike", dataType = "string", value = "Only return models with a tenantId like the given value.", paramType = "query"),
    @ApiImplicitParam(name = "withoutTenantId", dataType = "boolean", value = "If true, only returns models without a tenantId set. If false, the withoutTenantId parameter is ignored.", paramType = "query"),
    @ApiImplicitParam(name = "sort", dataType = "string", value = "Property to sort on, to be used together with the order.", allowableValues ="id,category,createTime,key,lastUpdateTime,name,version,tenantId", paramType = "query"),
  })
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates request was successful and the models are returned"),
      @ApiResponse(code = 400, message = "Indicates a parameter was passed in the wrong format. The status-message contains additional information.")
  })
  @RequestMapping(value = "/repository/models", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getModels(@ApiParam(hidden = true) @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
    ModelQuery modelQuery = repositoryService.createModelQuery();

    if (allRequestParams.containsKey("id")) {
      modelQuery.modelId(allRequestParams.get("id"));
    }
    if (allRequestParams.containsKey("category")) {
      modelQuery.modelCategory(allRequestParams.get("category"));
    }
    if (allRequestParams.containsKey("categoryLike")) {
      modelQuery.modelCategoryLike(allRequestParams.get("categoryLike"));
    }
    if (allRequestParams.containsKey("categoryNotEquals")) {
      modelQuery.modelCategoryNotEquals(allRequestParams.get("categoryNotEquals"));
    }
    if (allRequestParams.containsKey("name")) {
      modelQuery.modelName(allRequestParams.get("name"));
    }
    if (allRequestParams.containsKey("nameLike")) {
      modelQuery.modelNameLike(allRequestParams.get("nameLike"));
    }
    if (allRequestParams.containsKey("key")) {
      modelQuery.modelKey(allRequestParams.get("key"));
    }
    if (allRequestParams.containsKey("version")) {
      modelQuery.modelVersion(Integer.valueOf(allRequestParams.get("version")));
    }
    if (allRequestParams.containsKey("latestVersion")) {
      boolean isLatestVersion = Boolean.valueOf(allRequestParams.get("latestVersion"));
      if (isLatestVersion) {
        modelQuery.latestVersion();
      }
    }
    if (allRequestParams.containsKey("deploymentId")) {
      modelQuery.deploymentId(allRequestParams.get("deploymentId"));
    }
    if (allRequestParams.containsKey("deployed")) {
      boolean isDeployed = Boolean.valueOf(allRequestParams.get("deployed"));
      if (isDeployed) {
        modelQuery.deployed();
      } else {
        modelQuery.notDeployed();
      }
    }
    if (allRequestParams.containsKey("tenantId")) {
      modelQuery.modelTenantId(allRequestParams.get("tenantId"));
    }
    if (allRequestParams.containsKey("tenantIdLike")) {
      modelQuery.modelTenantIdLike(allRequestParams.get("tenantIdLike"));
    }
    if (allRequestParams.containsKey("withoutTenantId")) {
      boolean withoutTenantId = Boolean.valueOf(allRequestParams.get("withoutTenantId"));
      if (withoutTenantId) {
        modelQuery.modelWithoutTenantId();
      }
    }
    return new ModelsPaginateList(restResponseFactory).paginateList(allRequestParams, modelQuery, "id", allowedSortProperties);
  }

  @ApiOperation(value = "Create a model", tags = {"Models"}, notes = "All request values are optional. For example, you can only include the name attribute in the request body JSON-object, only setting the name of the model, leaving all other fields null.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the model was created.")
  })
  @RequestMapping(value = "/repository/models", method = RequestMethod.POST, produces = "application/json")
  public ModelResponse createModel(@RequestBody ModelRequest modelRequest, HttpServletRequest request, HttpServletResponse response) {
    Model model = repositoryService.newModel();
    model.setCategory(modelRequest.getCategory());
    model.setDeploymentId(modelRequest.getDeploymentId());
    model.setKey(modelRequest.getKey());
    model.setMetaInfo(modelRequest.getMetaInfo());
    model.setName(modelRequest.getName());
    model.setVersion(modelRequest.getVersion());
    model.setTenantId(modelRequest.getTenantId());

    repositoryService.saveModel(model);
    response.setStatus(HttpStatus.CREATED.value());
    return restResponseFactory.createModelResponse(model);
  }
}
