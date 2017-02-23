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
@Api(tags = { "Users" }, description = "Manage Users", authorizations = { @Authorization(value = "basicAuth") })
public class UserInfoResource extends BaseUserResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;

  @Autowired
  protected IdentityService identityService;

  @ApiOperation(value = "Get a user’s info", tags = {"Users"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the user was found and the user has info for the given key."),
      @ApiResponse(code = 404, message = "Indicates the requested user was not found or the user doesn’t have info for the given key. Status description contains additional information about the error.")
  })
  @RequestMapping(value = "/identity/users/{userId}/info/{key}", method = RequestMethod.GET, produces = "application/json")
  public UserInfoResponse getUserInfo(@ApiParam(name = "userId", value="The id of the user to get the info for.") @PathVariable("userId") String userId,@ApiParam(name = "key", value="The key of the user info to get.") @PathVariable("key") String key, HttpServletRequest request) {
    User user = getUserFromRequest(userId);

    String existingValue = identityService.getUserInfo(user.getId(), key);
    if (existingValue == null) {
      throw new ActivitiObjectNotFoundException("User info with key '" + key + "' does not exists for user '" + user.getId() + "'.", null);
    }

    return restResponseFactory.createUserInfoResponse(key, existingValue, user.getId());
  }

  @ApiOperation(value = "Update a user’s info", tags = {"Users"},  nickname = "updateUserInfo")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the user was found and the info has been updated."),
      @ApiResponse(code = 400, message = "Indicates the value was missing from the request body."),
      @ApiResponse(code = 404, message = "Indicates the requested user was not found or the user doesn’t have info for the given key. Status description contains additional information about the error.")
  })
  @RequestMapping(value = "/identity/users/{userId}/info/{key}", method = RequestMethod.PUT, produces = "application/json")
  public UserInfoResponse setUserInfo(@ApiParam(name = "userId", value="The id of the user to update the info for.") @PathVariable("userId") String userId,@ApiParam(name = "key", value="The key of the user info to update.") @PathVariable("key") String key, @RequestBody UserInfoRequest userRequest, HttpServletRequest request) {

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

  @ApiOperation(value = "Delete a user’s info", tags = {"Users"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the user was found and the info for the given key has been deleted. Response body is left empty intentionally."),
      @ApiResponse(code = 404, message = "Indicates the requested user was not found or the user doesn’t have info for the given key. Status description contains additional information about the error.")
  })
  @RequestMapping(value = "/identity/users/{userId}/info/{key}", method = RequestMethod.DELETE)
  public void deleteUserInfo(@ApiParam(name = "userId", value="The id of the user to delete the info for.") @PathVariable("userId") String userId,@ApiParam(name = "key", value="The key of the user info to delete.") @PathVariable("key") String key, HttpServletResponse response) {
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
