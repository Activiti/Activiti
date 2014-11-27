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

package org.activiti.rest.service.api.identity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.identity.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class UserResource extends BaseUserResource {

  @RequestMapping(value="/identity/users/{userId}", method = RequestMethod.GET, produces = "application/json")
  public UserResponse getUser(@PathVariable String userId, HttpServletRequest request) {
    return restResponseFactory.createUserResponse(getUserFromRequest(userId), false);
  }
  
  @RequestMapping(value="/identity/users/{userId}", method = RequestMethod.PUT, produces = "application/json")
  public UserResponse updateUser(@PathVariable String userId, @RequestBody UserRequest userRequest, HttpServletRequest request) {
    User user = getUserFromRequest(userId);
    if (userRequest.isEmailChanged()) {
      user.setEmail(userRequest.getEmail());
    }
    if (userRequest.isFirstNameChanged()) {
      user.setFirstName(userRequest.getFirstName());
    }
    if (userRequest.isLastNameChanged()) {
      user.setLastName(userRequest.getLastName());
    }
    if (userRequest.isPasswordChanged()) {
      user.setPassword(userRequest.getPassword());
    }
    
    identityService.saveUser(user);
    
    return restResponseFactory.createUserResponse(user, false);
  }
  
  @RequestMapping(value="/identity/users/{userId}", method = RequestMethod.DELETE)
  public void deleteUser(@PathVariable String userId, HttpServletResponse response) {
    User user = getUserFromRequest(userId);
    identityService.deleteUser(user.getId());
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}
