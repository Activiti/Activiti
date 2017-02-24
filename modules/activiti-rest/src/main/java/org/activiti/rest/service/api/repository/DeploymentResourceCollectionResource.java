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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.rest.common.application.ContentTypeResolver;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Deployment" }, description = "Manage Deployment", authorizations = { @Authorization(value = "basicAuth") })
public class DeploymentResourceCollectionResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;

  @Autowired
  protected ContentTypeResolver contentTypeResolver;

  @Autowired
  protected RepositoryService repositoryService;

  @ApiOperation(value = "List resources in a deployment", tags = {"Deployment"}, notes="The dataUrl property in the resulting JSON for a single resource contains the actual URL to use for retrieving the binary resource.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the deployment was found and the resource list has been returned."),
      @ApiResponse(code = 404, message = "Indicates the requested deployment was not found.")
  })
  @RequestMapping(value = "/repository/deployments/{deploymentId}/resources", method = RequestMethod.GET, produces = "application/json")
  public List<DeploymentResourceResponse> getDeploymentResources(@ApiParam(name = "deploymentId", value = "The id of the deployment to get the resources for.") @PathVariable String deploymentId, HttpServletRequest request) {
    // Check if deployment exists
    Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
    if (deployment == null) {
      throw new ActivitiObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.", Deployment.class);
    }

    List<String> resourceList = repositoryService.getDeploymentResourceNames(deploymentId);

    return restResponseFactory.createDeploymentResourceResponseList(deploymentId, resourceList, contentTypeResolver);
  }
}
