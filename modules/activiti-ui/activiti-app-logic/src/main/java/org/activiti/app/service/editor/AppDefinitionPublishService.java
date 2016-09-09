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
package org.activiti.app.service.editor;

import org.activiti.app.domain.editor.Model;
import org.activiti.app.service.api.AppDefinitionService;
import org.activiti.app.service.api.DeploymentService;
import org.activiti.app.service.api.ModelService;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Can't merge this with {@link AppDefinitionService}, as it doesn't have visibility of domain models needed to do the publication.
 * 
 * @author jbarrez
 */
@Service
@Transactional
public class AppDefinitionPublishService {

  private static final Logger logger = LoggerFactory.getLogger(AppDefinitionPublishService.class);

  @Autowired
  protected ModelService modelService;

  @Autowired
  protected DeploymentService deploymentService;

  public void publishAppDefinition(String comment, Model appDefinitionModel, User user) {

    // Create new version of the app model
    modelService.createNewModelVersion(appDefinitionModel, comment, user);

    // Deploy the app model to be executable
    deploymentService.updateAppDefinition(appDefinitionModel, user);
  }

}
