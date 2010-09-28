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
package org.activiti.rest.util;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.rest.Config;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Helper class for all activiti webscripts.
 * 
 * @author Erik Winlof
 */
public class ActivitiWebScript extends DeclarativeWebScript {

  /**
   * The activiti config bean
   */
  protected Config config;

  /**
   * Setter for the activiti config bean
   *
   * @param config The activiti config bean
   */
  public void setConfig(Config config) {
    this.config = config;
  }

  /**
   * The entry point for the webscript.
   *
   * Will create a model and call the executeWebScript() so extending activiti
   * webscripts may implement custom logic.
   *
   * @param req The webscript request 
   * @param status The webscripts status
   * @param cache The webscript cache
   * @return The webscript template model
   */
  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    // Prepare model with process engine info
    Map<String, Object> model = new HashMap<String, Object>();
    try {
      // Create activiti request to add heler methods
      ActivitiRequest ar = new ActivitiRequest(req);

      // Set logged in web user as current user in engine api
      getIdentityService().setAuthenticatedUserId(ar.getCurrentUserId());

      // Let implementing webscript do something useful
      executeWebScript(ar, status, cache, model);
    }
    finally {
      // Reset the current engine api user
      getIdentityService().setAuthenticatedUserId(null);
    }
    // Return model
    return model;
  }

  /**
   * Override this class to implement custom logic.
   *
   * @param req
   *          The webscript request
   * @param status
   *          The webscript
   * @param cache
   * @param model
   */
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    // Override to make something useful
  }

  /**
   * Returns the process engine info.
   *
   * @return The process engine info
   */
  protected ProcessEngineInfo getProcessEngineInfo() {
    return ProcessEngines.getProcessEngineInfo(config.getEngine());
  }

  /**
   * Returns the process engine.
   *
   * @return The process engine
   */
  protected ProcessEngine getProcessEngine() {
    return ProcessEngines.getProcessEngine(config.getEngine());
  }

  /**
   * Returns the identity service.
   *
   * @return The identity service
   */
  protected IdentityService getIdentityService() {
    return getProcessEngine().getIdentityService();
  }

  /**
   * Returns the management service.
   *
   * @return The management service.
   */
  protected ManagementService getManagementService() {
    return getProcessEngine().getManagementService();
  }

  /**
   * Returns The process service.
   *
   * @return The process service
   */
  protected RuntimeService getRuntimeService() {
    return getProcessEngine().getRuntimeService();
  }

  /**
   * Returns The repository service.
   *
   * @return The repository service
   */
  protected RepositoryService getRepositoryService() {
    return getProcessEngine().getRepositoryService();
  }

  /**
   * Returns the task service.
   *
   * @return The task service
   */
  protected TaskService getTaskService() {
    return getProcessEngine().getTaskService();
  }


}
