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
package org.activiti.rest.dmn.service.api.repository;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.dmn.api.DmnDeployment;
import org.activiti.dmn.api.DmnDeploymentBuilder;
import org.activiti.dmn.api.DmnDeploymentQuery;
import org.activiti.dmn.api.DmnRepositoryService;
import org.activiti.dmn.api.QueryProperty;
import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.ActivitiDmnIllegalArgumentException;
import org.activiti.dmn.engine.impl.DeploymentQueryProperty;
import org.activiti.rest.dmn.common.DataResponse;
import org.activiti.rest.dmn.service.api.DmnRestResponseFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Yvo Swillens
 */
@RestController
public class DmnDeploymentCollectionResource {

  private static Map<String, QueryProperty> allowedSortProperties = new HashMap<>();

  static {
    allowedSortProperties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
    allowedSortProperties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
    allowedSortProperties.put("deployTime", DeploymentQueryProperty.DEPLOY_TIME);
    allowedSortProperties.put("tenantId", DeploymentQueryProperty.DEPLOYMENT_TENANT_ID);
  }

  @Autowired
  protected DmnRestResponseFactory dmnRestResponseFactory;

  @Autowired
  protected DmnRepositoryService dmnRepositoryService;

  @RequestMapping(value = "/dmn-repository/deployments", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getDeployments(@RequestParam Map<String, String> allRequestParams) {
    DmnDeploymentQuery deploymentQuery = dmnRepositoryService.createDeploymentQuery();

    // Apply filters
    if (allRequestParams.containsKey("name")) {
      deploymentQuery.deploymentName(allRequestParams.get("name"));
    }
    if (allRequestParams.containsKey("nameLike")) {
      deploymentQuery.deploymentNameLike(allRequestParams.get("nameLike"));
    }
    if (allRequestParams.containsKey("category")) {
      deploymentQuery.deploymentCategory(allRequestParams.get("category"));
    }
    if (allRequestParams.containsKey("categoryNotEquals")) {
      deploymentQuery.deploymentCategoryNotEquals(allRequestParams.get("categoryNotEquals"));
    }
    if (allRequestParams.containsKey("tenantId")) {
      deploymentQuery.deploymentTenantId(allRequestParams.get("tenantId"));
    }
    if (allRequestParams.containsKey("tenantIdLike")) {
      deploymentQuery.deploymentTenantIdLike(allRequestParams.get("tenantIdLike"));
    }
    if (allRequestParams.containsKey("withoutTenantId")) {
      Boolean withoutTenantId = Boolean.valueOf(allRequestParams.get("withoutTenantId"));
      if (withoutTenantId) {
        deploymentQuery.deploymentWithoutTenantId();
      }
    }

    DataResponse response = new DmnDeploymentsDmnPaginateList(dmnRestResponseFactory).paginateList(allRequestParams, deploymentQuery, "id", allowedSortProperties);
    return response;
  }

  @RequestMapping(value = "/dmn-repository/deployments", method = RequestMethod.POST, produces = "application/json")
  public DmnDeploymentResponse uploadDeployment(@RequestParam(value = "tenantId", required = false) String tenantId, HttpServletRequest request, HttpServletResponse response) {

    if (request instanceof MultipartHttpServletRequest == false) {
      throw new ActivitiDmnIllegalArgumentException("Multipart request is required");
    }

    MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

    if (multipartRequest.getFileMap().size() == 0) {
      throw new ActivitiDmnIllegalArgumentException("Multipart request with file content is required");
    }

    MultipartFile file = multipartRequest.getFileMap().values().iterator().next();

    try {
      DmnDeploymentBuilder deploymentBuilder = dmnRepositoryService.createDeployment();
      String fileName = file.getOriginalFilename();
      if (StringUtils.isEmpty(fileName) || !(fileName.endsWith(".dmn") || fileName.endsWith(".xml"))) {
        fileName = file.getName();
      }

      if (fileName.endsWith(".dmn") || fileName.endsWith(".xml")) {
        deploymentBuilder.addInputStream(fileName, file.getInputStream());
      } else {
        throw new ActivitiDmnIllegalArgumentException("File must be of type .xml or .dmn");
      }
      deploymentBuilder.name(fileName);

      if (tenantId != null) {
        deploymentBuilder.tenantId(tenantId);
      }

      DmnDeployment deployment = deploymentBuilder.deploy();

      response.setStatus(HttpStatus.CREATED.value());

      return dmnRestResponseFactory.createDmnDeploymentResponse(deployment);

    } catch (Exception e) {
      if (e instanceof ActivitiDmnException) {
        throw (ActivitiDmnException) e;
      }
      throw new ActivitiDmnException(e.getMessage(), e);
    }
  }
}
