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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.rest.exception.ActivitiConflictException;
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
@Api(tags = { "Users" }, description = "Manage Users", authorizations = { @Authorization(value = "basicAuth") })
public class UserInfoCollectionResource extends BaseUserResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;

  @Autowired
  protected IdentityService identityService;

  @ApiOperation(value = "List a user’s info", tags = {"Users"}, nickname = "listUsersInfo")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the user was found and list of info (key and url) is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested user was not found.")
  })
  @RequestMapping(value = "/identity/users/{userId}/info", method = RequestMethod.GET, produces = "application/json")
  public List<UserInfoResponse> getUserInfo(@ApiParam(name="userId", value="The id of the user to get the info for.") @PathVariable String userId, HttpServletRequest request) {
    User user = getUserFromRequest(userId);

    return restResponseFactory.createUserInfoKeysResponse(identityService.getUserInfoKeys(user.getId()), user.getId());
  }

  @ApiOperation(value = "Create a new user’s info entry", tags = {"Users"}, nickname = "createUserInfo")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Indicates the user was found and the info has been created."),
      @ApiResponse(code = 400, message = "Indicates the key or value was missing from the request body. Status description contains additional information about the error."),
      @ApiResponse(code = 404, message = "Indicates the requested user was not found."),
      @ApiResponse(code = 409, message = "Indicates there is already an info-entry with the given key for the user, update the resource instance (PUT).")
  })
  @RequestMapping(value = "/identity/users/{userId}/info", method = RequestMethod.POST, produces = "application/json")
  public UserInfoResponse setUserInfo(@ApiParam(name="userId", value="The id of the user to create the info for.") @PathVariable String userId, @RequestBody UserInfoRequest userRequest, HttpServletRequest request, HttpServletResponse response) {

    User user = getUserFromRequest(userId);

    if (userRequest.getKey() == null) {
      throw new ActivitiIllegalArgumentException("The key cannot be null.");
    }
    if (userRequest.getValue() == null) {
      throw new ActivitiIllegalArgumentException("The value cannot be null.");
    }

    String existingValue = identityService.getUserInfo(user.getId(), userRequest.getKey());
    if (existingValue != null) {
      throw new ActivitiConflictException("User info with key '" + userRequest.getKey() + "' already exists for this user.");
    }

    identityService.setUserInfo(user.getId(), userRequest.getKey(), userRequest.getValue());

    response.setStatus(HttpStatus.CREATED.value());
    return restResponseFactory.createUserInfoResponse(userRequest.getKey(), userRequest.getValue(), user.getId());
  }
}
