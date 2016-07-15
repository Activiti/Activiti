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
package com.activiti.rest.idm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;

/**
 * REST controller for managing users.
 */
@RestController
public class AbstractUserResource {
  
  private final Logger log = LoggerFactory.getLogger(AbstractUserResource.class);
	
	private static final int MAX_RECENT_USERS = 50;
	
	protected static final String USER_ALREADY_REGISTERED = "ACCOUNT.SIGNUP.ERROR.ALREADY-REGISTERED";

  protected static final String UNEXISTING_USER_MESSAGE_KEY = "ACCOUNT.RESET-PASSWORD-REQUEST.ERROR.UNEXISTING-USER";

  @Autowired
  protected IdentityService identityService;
  
  public User getUser(String userId, HttpServletResponse response) {
    User user = identityService.createUserQuery().userId(userId).singleResult();
    
    if (user == null) {
        throw new NotFoundException("User with id: " + userId + " does not exist or is inactive");
    }
    
    if(!user.getId().equals(SecurityUtils.getCurrentUserId())) {
        throw new NotPermittedException("Can only get user details for authenticated user");
    }
    
    return user;
  }
  
  public User updateUser(String userId, User userRequest, HttpServletResponse response) {
    User user = identityService.createUserQuery().userId(userId).singleResult();
      
    if (user == null) {
        throw new NotFoundException("User with id: " + userId + " does not exist or is inactive");
    }
    
    if(!user.getId().equals(SecurityUtils.getCurrentUserId())) {
        throw new NotPermittedException("Can only update user for authenticated user");
    }
    
    user.setFirstName(userRequest.getFirstName());
    user.setLastName(userRequest.getLastName());
    user.setEmail(userRequest.getEmail());
    
    identityService.saveUser(user);
    
    return user;
  }
  
  public void registerUser(User signup, HttpServletRequest request) {
      
	  // Create a new user, leaving status empty. Appropriate status will be set for new users and actions will be performed if needed.
    User user = identityService.newUser(signup.getEmail());
    user.setFirstName(signup.getFirstName());
    user.setLastName(signup.getLastName());
    user.setEmail(signup.getEmail());
    user.setPassword(signup.getPassword());
    identityService.saveUser(user);
  }
}
