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
package org.activiti.rest.auth;

import org.activiti.ProcessEngines;
import org.activiti.identity.Group;
import org.activiti.rest.Config;
import org.springframework.extensions.webscripts.Description;

import java.util.ArrayList;
import java.util.List;

/**
 * This class performs the authentication and authorization to make sure that the user that accesses the webscripts
 * has the right to do so.
 *
 * @author Erik Winlöf
 */
public class ActivitiBasicHttpAuthenticatorFactory extends AbstractBasicHttpAuthenticatorFactory {

  private Config config;

  /**
   * Constructor
   */
  public ActivitiBasicHttpAuthenticatorFactory() {
    super();
    basicRealm = "Activiti";
  }

  /**
   * The activiti config bean
   *
   * @param config The activiti config bean
   */
  public void setConfig(Config config) {
    this.config = config;
  }

  /**
   * Authenticates the user against the activiti database
   *
   * @param username The username
   * @param password The password
   * @return true if the username and password match
   */
  @Override
  public boolean doAuthenticate(String username, String password) {
    return ProcessEngines.getProcessEngine(config.getEngine()).getIdentityService().checkPassword(username, password);
  }

  /**
   * Authorizes the user against the activiti database
   *
   * @param username The username
   * @param role The role that the user MUST have
   * @return true if the user has the required role
   */
  @Override
  public boolean doAuthorize(String username, Description.RequiredAuthentication role)
  {
    List<String> grantedGroupIds = new ArrayList<String>();
    if (role == Description.RequiredAuthentication.user) {
      // This method is called after doAuthenticate which means the login was successful and the request was done by a user
      grantedGroupIds.add(config.getUserGroupId());
      grantedGroupIds.add(config.getManagerGroupId());
      grantedGroupIds.add(config.getAdminGroupId());
    }
    else if (role == Description.RequiredAuthentication.admin) {
      // Check if user is member of the admin group
      grantedGroupIds.add(config.getAdminGroupId());
    }
    if (grantedGroupIds.size() == 0) {
      // No group membership is required for the user
      return true;
    }
    else {
      // Certain group membership is required user
      List<Group> userGroups = ProcessEngines.getProcessEngine(config.getEngine()).getIdentityService().findGroupsByUserAndType(username, config.getSecurityRoleGroupTypeId());
      for (Group group : userGroups)
      {
        for (String grantedGroupId : grantedGroupIds) {
          if (group.getId().equals(grantedGroupId)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
