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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserInfoResource extends BaseUserResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected IdentityService identityService;
  
  @RequestMapping(value="/identity/users/{userId}/info/{key}", method = RequestMethod.GET, produces = "application/json")
  public UserInfoResponse getUserInfo(@PathVariable("userId") String userId, @PathVariable("key") String key, HttpServletRequest request) {
    User user = getUserFromRequest(userId);
    
    String existingValue = identityService.getUserInfo(user.getId(), key);
    if (existingValue == null) {
      throw new ActivitiObjectNotFoundException("User info with key '" + key + "' does not exists for user '" + user.getId() + "'.", null);
    }
    
    return restResponseFactory.createUserInfoResponse(key, existingValue, user.getId());
  }
  
  
  @RequestMapping(value="/identity/users/{userId}/info/{key}", method = RequestMethod.PUT, produces = "application/json")
  public UserInfoResponse setUserInfo(@PathVariable("userId") String userId, @PathVariable("key") String key, 
      @RequestBody UserInfoRequest userRequest, HttpServletRequest request) {
    
    User user = getUserFromRequest(userId);
    String validKey = getValidKeyFromRequest(user, key);
    
    if (userRequest.getValue() == null) {
      throw new ActivitiIllegalArgumentException("The value cannot be null.");
    }
    
    if (userRequest.getKey() == null || validKey.equals(userRequest.getKey())) {
      identityService.setUserInfo(user.getId(), key, userRequest.getValue());
    } else {
      throw new ActivitiIllegalArgumentException("Key provided in request body doesn't match the key in the resource URL.");
    }
    
    return restResponseFactory.createUserInfoResponse(key, userRequest.getValue(), user.getId());
  }
  
  @RequestMapping(value="/identity/users/{userId}/info/{key}", method = RequestMethod.DELETE)
  public void deleteUserInfo(@PathVariable("userId") String userId, @PathVariable("key") String key, HttpServletResponse response) {
    User user = getUserFromRequest(userId);
    String validKey = getValidKeyFromRequest(user, key);
    
    identityService.setUserInfo(user.getId(), validKey, null);
    
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
  
  protected String getValidKeyFromRequest(User user, String key) {
    String existingValue = identityService.getUserInfo(user.getId(), key);
    if (existingValue == null) {
      throw new ActivitiObjectNotFoundException("User info with key '" + key + "' does not exists for user '" + user.getId() + "'.", null);
    }
    
    return key;
  }
}
