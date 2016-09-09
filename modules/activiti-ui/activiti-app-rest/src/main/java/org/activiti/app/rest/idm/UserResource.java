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
package org.activiti.app.rest.idm;

import javax.servlet.http.HttpServletResponse;

import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.app.service.exception.NotPermittedException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing users.
 */
@RestController
public class UserResource {

  @Autowired
  protected IdentityService identityService;

  @RequestMapping(value = "/rest/users/{userId}", method = RequestMethod.GET, produces = "application/json")
  public UserRepresentation getUser(@PathVariable String userId, HttpServletResponse response) {
    User user = identityService.createUserQuery().userId(userId).singleResult();

    if (user == null) {
      throw new NotFoundException("User with id: " + userId + " does not exist or is inactive");
    }

    if (!user.getId().equals(SecurityUtils.getCurrentUserId())) {
      throw new NotPermittedException("Can only get user details for authenticated user");
    }

    return new UserRepresentation(user);
  }

}
