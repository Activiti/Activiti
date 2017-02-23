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

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Deployment" }, description = "Manage Deployment", authorizations = { @Authorization(value = "basicAuth") })
public class DeploymentResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;

  @Autowired
  protected RepositoryService repositoryService;

  @ApiOperation(value = "Get a deployment", tags = {"Deployment"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the deployment was found and returned."),
      @ApiResponse(code = 404, message = "Indicates the requested deployment was not found.")
  })
  @RequestMapping(value = "/repository/deployments/{deploymentId}", method = RequestMethod.GET, produces = "application/json")
  public DeploymentResponse getDeployment(@ApiParam(name = "deploymentId", value = "The id of the deployment to get.") @PathVariable String deploymentId, HttpServletRequest request) {
    Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

    if (deployment == null) {
      throw new ActivitiObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.", Deployment.class);
    }

    return restResponseFactory.createDeploymentResponse(deployment);
  }

  @ApiOperation(value = "Delete a deployment", tags = {"Deployment"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the deployment was found and has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested deployment was not found.")
  })
  @RequestMapping(value = "/repository/deployments/{deploymentId}", method = RequestMethod.DELETE, produces = "application/json")
  public void deleteDeployment(@ApiParam(name = "deploymentId" , value = "The id of the deployment to delete.") @PathVariable String deploymentId, @RequestParam(value = "cascade", required = false, defaultValue = "false") Boolean cascade, HttpServletResponse response) {

    if (cascade) {
      repositoryService.deleteDeployment(deploymentId, true);
    } else {
      repositoryService.deleteDeployment(deploymentId);
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}
