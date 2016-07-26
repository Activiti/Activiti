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

import javax.servlet.http.HttpServletResponse;

import org.activiti.dmn.api.DmnDeployment;
import org.activiti.dmn.api.DmnRepositoryService;
import org.activiti.dmn.engine.ActivitiDmnObjectNotFoundException;
import org.activiti.rest.dmn.service.api.DmnRestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yvo Swillens
 */
@RestController
public class DmnDeploymentResource {

  @Autowired
  protected DmnRestResponseFactory dmnRestResponseFactory;

  @Autowired
  protected DmnRepositoryService dmnRepositoryService;

  @RequestMapping(value = "/dmn-repository/deployments/{deploymentId}", method = RequestMethod.GET, produces = "application/json")
  public DmnDeploymentResponse getDmnDeployment(@PathVariable String deploymentId) {
    DmnDeployment deployment = dmnRepositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

    if (deployment == null) {
      throw new ActivitiDmnObjectNotFoundException("Could not find a DMN deployment with id '" + deploymentId);
    }

    return dmnRestResponseFactory.createDmnDeploymentResponse(deployment);
  }

  @RequestMapping(value = "/dmn-repository/deployments/{deploymentId}", method = RequestMethod.DELETE, produces = "application/json")
  public void deleteDmnDeployment(@PathVariable String deploymentId, HttpServletResponse response) {

    dmnRepositoryService.deleteDeployment(deploymentId);

    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}