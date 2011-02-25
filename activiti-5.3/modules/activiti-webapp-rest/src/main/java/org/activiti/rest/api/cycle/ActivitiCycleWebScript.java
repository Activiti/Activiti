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

package org.activiti.rest.api.cycle;

import java.util.Map;
import java.util.logging.Logger;

import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.service.CycleCommentService;
import org.activiti.cycle.service.CycleConfigurationService;
import org.activiti.cycle.service.CycleContentService;
import org.activiti.cycle.service.CyclePluginService;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.cycle.service.CycleTagService;
import org.activiti.rest.api.cycle.session.CycleHttpSession;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public abstract class ActivitiCycleWebScript extends ActivitiWebScript {
  
  protected Logger log = Logger.getLogger(getClass().getName());

  protected CycleRepositoryService repositoryService;
  protected CycleTagService tagService;
  protected CycleCommentService commentService;
  protected CycleConfigurationService configurationService;
  protected CycleContentService contentService;
  protected CyclePluginService pluginService;

  public ActivitiCycleWebScript() {
    configurationService = CycleServiceFactory.getConfigurationService();
    repositoryService = CycleServiceFactory.getRepositoryService();
    tagService = CycleServiceFactory.getTagService();
    commentService = CycleServiceFactory.getCommentService();
    contentService = CycleServiceFactory.getContentService();
    pluginService = CycleServiceFactory.getCyclePluginService();
  }

  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    try {
      // open cycle ui-session
      CycleHttpSession.openSession(req);
      // execute the request in the context of a CycleHttpSession
      execute(req, status, cache, model);
    } catch (RepositoryAuthenticationException e) {
      try {
        // try to login
        CycleHttpSession.tryConnectorLogin(req, e.getConnectorId());
        // retry to execute the request
        execute(req, status, cache, model);
      } catch (RepositoryAuthenticationException e2) {
        // throw exception
        model.put("authenticationException", e2);
      }

    } finally {
      // close the CycleHttpSession
      CycleHttpSession.closeSession();
    }
  }

  abstract void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model);
}
