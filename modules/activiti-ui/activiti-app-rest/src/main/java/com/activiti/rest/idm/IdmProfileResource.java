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
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.idm.ChangePasswordRepresentation;
import com.activiti.model.idm.GroupRepresentation;
import com.activiti.model.idm.UserRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.NotFoundException;

/**
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping(value = "/rest/admin")
public class IdmProfileResource {
  
  @Autowired
  protected IdentityService identityService;

  @RequestMapping(value = "/profile", method = RequestMethod.GET, produces = "application/json")
  public UserRepresentation getProfile() {
    User user = SecurityUtils.getCurrentActivitiAppUser().getUserObject();
    
    UserRepresentation userRepresentation = new UserRepresentation(user);
    
    List<Group> groups = identityService.createGroupQuery().groupMember(user.getId()).list();
    for (Group group : groups) {
      userRepresentation.getGroups().add(new GroupRepresentation(group));
    }
    
    return userRepresentation;
  }

  @RequestMapping(value = "/profile", method = RequestMethod.POST, produces = "application/json")
  public UserRepresentation updateProfile(@RequestBody UserRepresentation userRepresentation) {
    User currentUser = SecurityUtils.getCurrentUserObject();

    // If user is not externally managed, we need the email address for login, so an empty email is not allowed
    if (StringUtils.isEmpty(userRepresentation.getEmail())) {
      throw new BadRequestException("Empty email is not allowed");
    }
    
    User user = identityService.createUserQuery().userId(currentUser.getId()).singleResult();
    user.setFirstName(userRepresentation.getFirstName());
    user.setLastName(userRepresentation.getLastName());
    user.setEmail(userRepresentation.getEmail());
    identityService.saveUser(user);
    return new UserRepresentation(user);
  }
  
  @RequestMapping(value = "/profile-password", method = RequestMethod.POST, produces = "application/json")
  public void changePassword(@RequestBody ChangePasswordRepresentation changePasswordRepresentation) {
    User user = identityService.createUserQuery().userId(SecurityUtils.getCurrentUserId()).singleResult();
    if (!user.getPassword().equals(changePasswordRepresentation.getOriginalPassword())) {
      throw new NotFoundException();
    }
    user.setPassword(changePasswordRepresentation.getNewPassword());
    identityService.saveUser(user);
  }
  
}
