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

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;

import com.activiti.model.common.ResultListDataRepresentation;

/**
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class AbstractIdmUsersResource {
    
  private static final int MAX_USER_SIZE = 100;

  @Autowired
  protected IdentityService identityService;
  
  public ResultListDataRepresentation getUsers(String filter, String sort, Integer start, Integer size, String groupId) {

  	// Query
    ResultListDataRepresentation result = new ResultListDataRepresentation();
    
    List<User> users = identityService.createUserQuery().userFullNameLike(filter).listPage(start, (size != null && size > 0) ? size : MAX_USER_SIZE);
    Long totalCount = identityService.createUserQuery().userFullNameLike(filter).count();
    result.setTotal(Long.valueOf(totalCount.intValue()));
    result.setStart(start);
    result.setSize(users.size());
    result.setData(users);
    
    return result;
  }
  
  public User getUser(String userId) {
    return identityService.createUserQuery().userId(userId).singleResult();
  }

  /*public void updateUserDetails(Long userId, UserRepresentation userRepresentation) {
  	User user = userService.findUser(userId);
  	
  	try {
  		userService.updateUser(userId, userRepresentation.getEmail(), 
    			userRepresentation.getFirstName(), 
    			userRepresentation.getLastName(), 
    			userRepresentation.getCompany());
  	} catch (IllegalStateException e) {
  		throw new ConflictingRequestException("Email already registered", "ACCOUNT.SIGNUP.ERROR.ALREADY-REGISTERED");
  	}
  }
  
  public UserRepresentation createNewUser(UserRepresentation userRepresentation) {
  	
      if(StringUtils.isBlank(userRepresentation.getEmail()) ||
      		StringUtils.isBlank(userRepresentation.getPassword()) || 
      		StringUtils.isBlank(userRepresentation.getLastName())) {
          throw new BadRequestException("Email, password and last name are required");
      }

      User newUser = userService.createNewUser(userRepresentation.getEmail(),
      		userRepresentation.getFirstName(), 
      		userRepresentation.getLastName(), 
      		userRepresentation.getPassword(), 
              userRepresentation.getCompany());
      
      if (newUser == null) {
      	
      	// Check if there is already a user with the same email
      	if (userRepresentation.getEmail() != null && userRepository.findByEmail(userRepresentation.getEmail()) != null) {
      		throw new ConflictingRequestException("User already registered", "ACCOUNT.SIGNUP.ERROR.ALREADY-REGISTERED");
      	} else {
      		throw new BadRequestException("Could not create user: please verify the parameters used to create this user");
      	}
      }
      
      return new UserRepresentation(newUser);
  }*/
}
