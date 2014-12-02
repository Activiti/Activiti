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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.UserQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.exception.ActivitiConflictException;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class UserCollectionResource {

  protected static HashMap<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  static {
    properties.put("id", UserQueryProperty.USER_ID);
    properties.put("firstName", UserQueryProperty.FIRST_NAME);
    properties.put("lastName", UserQueryProperty.LAST_NAME);
    properties.put("email", UserQueryProperty.EMAIL);
  }
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected IdentityService identityService;
  
  @RequestMapping(value="/identity/users", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getUsers(@RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    UserQuery query = identityService.createUserQuery();
    
    if (allRequestParams.containsKey("id")) {
      query.userId(allRequestParams.get("id"));
    }
    if (allRequestParams.containsKey("firstName")) {
      query.userFirstName(allRequestParams.get("firstName"));
    }
    if (allRequestParams.containsKey("lastName")) {
      query.userLastName(allRequestParams.get("lastName"));
    }
    if (allRequestParams.containsKey("email")) {
      query.userEmail(allRequestParams.get("email"));
    }
    if (allRequestParams.containsKey("firstNameLike")) {
      query.userFirstNameLike(allRequestParams.get("firstNameLike"));
    }
    if (allRequestParams.containsKey("lastNameLike")) {
      query.userLastNameLike(allRequestParams.get("lastNameLike"));
    }
    if (allRequestParams.containsKey("emailLike")) {
      query.userEmailLike(allRequestParams.get("emailLike"));
    }
    if (allRequestParams.containsKey("memberOfGroup")) {
      query.memberOfGroup(allRequestParams.get("memberOfGroup"));
    }
    if (allRequestParams.containsKey("potentialStarter")) {
      query.potentialStarter(allRequestParams.get("potentialStarter"));
    }

    return new UserPaginateList(restResponseFactory)
        .paginateList(allRequestParams, query, "id", properties);
  }
  
  @RequestMapping(value="/identity/users", method = RequestMethod.POST, produces = "application/json")
  public UserResponse createUser(@RequestBody UserRequest userRequest, HttpServletRequest request, HttpServletResponse response) {
    if (userRequest.getId() == null) {
      throw new ActivitiIllegalArgumentException("Id cannot be null.");
    }

    // Check if a user with the given ID already exists so we return a CONFLICT
    if (identityService.createUserQuery().userId(userRequest.getId()).count() > 0) {
      throw new ActivitiConflictException("A user with id '" + userRequest.getId() + "' already exists.");
    }
    
    User created = identityService.newUser(userRequest.getId());
    created.setEmail(userRequest.getEmail());
    created.setFirstName(userRequest.getFirstName());
    created.setLastName(userRequest.getLastName());
    created.setPassword(userRequest.getPassword());
    identityService.saveUser(created);
    
    response.setStatus(HttpStatus.CREATED.value());
    
    return restResponseFactory.createUserResponse(created, true);
  }
  
}
