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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.runtime.AppDefinitionRepresentation;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractAppDefinitionsResource {

  private final Logger logger = LoggerFactory.getLogger(AbstractAppDefinitionsResource.class);

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected ObjectMapper objectMapper;

  protected static final AppDefinitionRepresentation kickstartAppDefinitionRepresentation = AppDefinitionRepresentation.createDefaultAppDefinitionRepresentation("kickstart");

  protected static final AppDefinitionRepresentation taskAppDefinitionRepresentation = AppDefinitionRepresentation.createDefaultAppDefinitionRepresentation("tasks");

  protected static final AppDefinitionRepresentation idmAppDefinitionRepresentation = AppDefinitionRepresentation.createDefaultAppDefinitionRepresentation("identity");

  protected ResultListDataRepresentation getAppDefinitions() {
    List<AppDefinitionRepresentation> resultList = new ArrayList<AppDefinitionRepresentation>();

    // Default app: kickstart
    resultList.add(kickstartAppDefinitionRepresentation);

    // Default app: tasks and IDM (available for all)
    resultList.add(taskAppDefinitionRepresentation);
    resultList.add(idmAppDefinitionRepresentation);

    // Custom apps
    Map<String, Deployment> deploymentMap = new HashMap<String, Deployment>();
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    for (Deployment deployment : deployments) {
      if (deployment.getKey() != null) {
        if (deploymentMap.containsKey(deployment.getKey()) == false) {
          deploymentMap.put(deployment.getKey(), deployment);
        } else if (deploymentMap.get(deployment.getKey()).getDeploymentTime().before(deployment.getDeploymentTime())) {
          deploymentMap.put(deployment.getKey(), deployment);
        }
      }
    }
    
    for (Deployment deployment : deploymentMap.values()) {
      resultList.add(createRepresentation(deployment));
    }

    ResultListDataRepresentation result = new ResultListDataRepresentation(resultList);
    return result;
  }

  protected AppDefinitionRepresentation createDefaultAppDefinition(String id) {
    AppDefinitionRepresentation app = new AppDefinitionRepresentation();
    return app;
  }

  protected AppDefinitionRepresentation createRepresentation(Deployment deployment) {
    AppDefinitionRepresentation resultAppDef = new AppDefinitionRepresentation();
    resultAppDef.setDeploymentId(deployment.getId());
    resultAppDef.setDeploymentKey(deployment.getKey());
    resultAppDef.setName(deployment.getName());
    return resultAppDef;
  }
}
