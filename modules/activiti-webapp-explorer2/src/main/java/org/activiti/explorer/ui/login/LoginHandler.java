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

package org.activiti.explorer.ui.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.explorer.identity.LoggedInUser;


/**
 * Class handling authentication for the explorer ui.
 * 
 * @author Frederik Heremans
 */
public interface LoginHandler {

  /**
   * Authenticate the user with the given username and given password.
   * 
   * @return the logged in user. Return null of authentication failed. 
   */
  LoggedInUser authenticate(String userName, String password);
  
  /**
   * Authenticate the current user. Use this to eg. shared autentication, 
   * which can be done without the user actually having to provide 
   * credentials.
   * 
   * @return The logged in user. Return null, if no user can be logged in
   * automatically. When null is returned, user will be requested to provide
   * credentials.
   */
  LoggedInUser authenticate(HttpServletRequest request, HttpServletResponse response);
    
  /**
   * Called when the user is logged out, should clear all context related
   * to authorization and authentication for the current logged in user.
   */
  void logout(LoggedInUser userTologout);
  
  /**
   * Called when request started. Allows eg. validating of authentication or
   * renewing.
   */
  void onRequestStart(HttpServletRequest request, HttpServletResponse response);
  
  /**
   * Called when request started. Allows eg. validating of authentication or
   * renewing.
   */
  void onRequestEnd(HttpServletRequest request, HttpServletResponse response);
}
