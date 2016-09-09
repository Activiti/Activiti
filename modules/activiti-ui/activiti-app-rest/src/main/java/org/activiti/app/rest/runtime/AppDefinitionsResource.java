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
package org.activiti.app.rest.runtime;

import javax.inject.Inject;

import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.runtime.AppDefinitionRepresentation;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.engine.repository.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST controller for managing the app definitions.
 */
@RestController
public class AppDefinitionsResource extends AbstractAppDefinitionsResource {

  private final Logger logger = LoggerFactory.getLogger(AppDefinitionsResource.class);

  @Inject
  protected ObjectMapper objectMapper;

  @Inject
  protected PermissionService permissionService;

  @RequestMapping(value = "/rest/runtime/app-definitions", method = RequestMethod.GET)
  public ResultListDataRepresentation getAppDefinitions() {
    return super.getAppDefinitions();
  }

  @RequestMapping(value = "/rest/runtime/app-definitions/{deploymentKey}", method = RequestMethod.GET)
  public AppDefinitionRepresentation getAppDefinition(@PathVariable("deploymentKey") String deploymentKey) {
    Deployment deployment = repositoryService.createDeploymentQuery().deploymentKey(deploymentKey).latest().singleResult();

    if (deployment == null) {
      throw new NotFoundException("No app definition is found with key: " + deploymentKey);
    }

    return createRepresentation(deployment);
  }

}
