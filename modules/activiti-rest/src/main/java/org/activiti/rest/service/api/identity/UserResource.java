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
@Api(tags = { "Users" }, description = "Manage Users", authorizations = { @Authorization(value = "basicAuth") })
public class UserResource extends BaseUserResource {

  @ApiOperation(value = "Get a single user", tags = {"Users"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the user exists and is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested user does not exist.")
  })
  @RequestMapping(value = "/identity/users/{userId}", method = RequestMethod.GET, produces = "application/json")
  public UserResponse getUser(@ApiParam(name = "userId", value="The id of the user to get.") @PathVariable String userId, HttpServletRequest request) {
    return restResponseFactory.createUserResponse(getUserFromRequest(userId), false);
  }

  @ApiOperation(value = "Update a user", tags = {"Users"},
      notes="All request values are optional. "
          + "For example, you can only include the firstName attribute in the request body JSON-object, only updating the firstName of the user, leaving all other fields unaffected. "
          + "When an attribute is explicitly included and is set to null, the user-value will be updated to null. "
          + "Example: {\"firstName\" : null} will clear the firstName of the user).")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the user was updated."),
      @ApiResponse(code = 404, message = "Indicates the requested user was not found."),
      @ApiResponse(code = 409, message = "Indicates the requested user was updated simultaneously.")
  })
  @RequestMapping(value = "/identity/users/{userId}", method = RequestMethod.PUT, produces = "application/json")
  public UserResponse updateUser(@ApiParam(name = "userId") @PathVariable String userId, @RequestBody UserRequest userRequest, HttpServletRequest request) {
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

  @ApiOperation(value = "Delete a user", tags = {"Users"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the user was found and  has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested user was not found.")
  })
  @RequestMapping(value = "/identity/users/{userId}", method = RequestMethod.DELETE)
  public void deleteUser(@ApiParam(name = "userId", value="The id of the user to delete.") @PathVariable String userId, HttpServletResponse response) {
    User user = getUserFromRequest(userId);
    identityService.deleteUser(user.getId());
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}
