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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.CycleServiceImpl;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public abstract class ActivitiCycleWebScript extends ActivitiWebScript {

  protected CycleService cycleService;

  private void init(ActivitiRequest req) {
    String cuid = req.getCurrentUserId();

    HttpSession session = req.getHttpServletRequest().getSession(true);

    // Retrieve the list of configured connectors for the current user (either
    // from the session or, if not present, from the database
    List<RepositoryConnector> connectors = CycleServiceImpl.getConfiguredRepositoryConnectors(cuid, session);

    // Make sure we know username and password for all connectors that require
    // login. If it is not stored in the users configuration it should be
    // provided as a parameter in the request.
    Map<String, String> connectorsWithoutLoginMap = new HashMap<String, String>();
    for (RepositoryConnector connector : getPasswordEnabledConnectors(connectors)) {
      PasswordEnabledRepositoryConnectorConfiguration conf = (PasswordEnabledRepositoryConnectorConfiguration) connector.getConfiguration();
      String username = req.getString(conf.getId() + "_username");
      String password = req.getString(conf.getId() + "_password");
      if (username != null && password != null) {
        conf.setUser(username);
        conf.setPassword(password);
      } else if (conf.getUser() == null || conf.getPassword() == null) {
        connectorsWithoutLoginMap.put(conf.getId(), conf.getName());
      }
      // If one or more logins are missing (not provided in either the
      // configuration or as HTTP parameter) we'll throw an authentication
      // exception with the list of connectors that are missing login
      // information
    }
    if (connectorsWithoutLoginMap.size() > 0) {
      throw new RepositoryAuthenticationException("Repository authentication error: missing login", connectorsWithoutLoginMap);
    }
    // Initialize the cycleService
    this.cycleService = CycleServiceImpl.getCycleService(cuid, session, connectors);
  }

  private List<RepositoryConnector> getPasswordEnabledConnectors(List<RepositoryConnector> connectors) {
    List<RepositoryConnector> LoginEnabledconnectors = new ArrayList<RepositoryConnector>();
    for (RepositoryConnector connector : connectors) {
      RepositoryConnectorConfiguration conf = connector.getConfiguration();
      if (PasswordEnabledRepositoryConnectorConfiguration.class.isInstance(conf)) {
        LoginEnabledconnectors.add(connector);
      }
    }
    return LoginEnabledconnectors;
  }

  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    try {
      init(req);
      execute(req, status, cache, model);
    } catch (RepositoryAuthenticationException e) {
      model.put("authenticationError", e.getMessage());
      model.put("reposInError", e.getConnectors());
    }
  }

  abstract void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model);
}
