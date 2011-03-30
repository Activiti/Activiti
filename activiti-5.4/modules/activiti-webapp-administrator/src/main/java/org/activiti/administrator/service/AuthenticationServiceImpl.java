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
package org.activiti.administrator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.activiti.engine.IdentityService;

/**
 * Provides access to the activiti authentication service.
 * 
 * @author Patrick Oberg
 * 
 */
@Service(value = "authenticationService")
@Scope(value = "singleton")
@Lazy
public class AuthenticationServiceImpl implements AuthenticationService {

  private static final long serialVersionUID = 1L;

  @Autowired
  private IdentityService identityService;

  /**
   * Checks the user credentials and authenticates the user if successful
   * 
   * @return <tt>true</tt> only if the authentication was successful
   */
  public boolean authenticate(String username, String password) {

    if (getIdentityService() != null) {

      // Check user
      if (!getIdentityService().checkPassword(username, password)) {

        // Login failed
        return false;
      } else {
        // Authenticate the session
        getIdentityService().setAuthenticatedUserId(username);

        // Login successful
        return true;
      }
    } else {
      // Login failed
      return false;
    }
  }

  /**
   * Get activiti identity service
   * 
   * @return the identityService
   */
  private IdentityService getIdentityService() {
    return identityService;
  }

}