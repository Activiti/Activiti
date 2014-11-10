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

package org.activiti.rest.common.filter;

import javax.servlet.http.HttpServletRequest;


/**
 * Allows enabling/disabling authentication on specific requests and allows authorisation of request after successful
 * authentication.
 * 
 * @author Frederik Heremans
 */
public interface RestAuthenticator {

  /**
   * Called before check is done to see if the request originates from a valid user. 
   * Allows disabling authentication and authorisation for certain requests.
   * 
   * @return true, if the request requires a valid and authorised user. Return false, if the request
   * can be executed without authentication or authorisation. If false is returned, the {@link #isRequestAuthorized(HttpServletRequest)}
   * won't be called for this request.
   */
  boolean requestRequiresAuthentication(HttpServletRequest request);
  
  
  /**
   * Called after a user is successfully authenticated against the Activiti identity-management. The logged in user
   * can be retrieved from the request's clientInfo object.
   * 
   * @return true, if the user is authorised to perform the request. Return false, if the request is not authorised
   * for the given user.
   */
  boolean isRequestAuthorized(HttpServletRequest request);
  
}
