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

import java.util.ArrayList;
import java.util.List;

import org.activiti.app.constant.GroupIds;
import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.idm.CreateUserRepresentation;
import org.activiti.app.model.idm.UpdateUsersRepresentation;
import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.ConflictingRequestException;
import org.activiti.app.service.exception.NotPermittedException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@RestController
public class IdmUsersResource {
  
  private static final int MAX_USER_SIZE = 100;
  
  @Autowired
  protected IdentityService identityService;
    
    @RequestMapping(value = "/rest/admin/users", method = RequestMethod.GET)
    public ResultListDataRepresentation getUsers(@RequestParam(required=false) String filter, 
            @RequestParam(required=false) String sort,
            @RequestParam(required=false) Integer start,
            @RequestParam(required=false) String groupId) {

      validateAdminRole();
      
      ResultListDataRepresentation result = new ResultListDataRepresentation();
      
      UserQuery userQuery = identityService.createUserQuery();
      if (StringUtils.isNotEmpty(filter)) {
        userQuery.userFullNameLike("%" + filter + "%");
      }
      
      if (StringUtils.isNotEmpty(sort)) {
        if ("idDesc".equals(sort)) {
          userQuery.orderByUserId().desc();
        } else if ("idAsc".equals(sort)) {
          userQuery.orderByUserId().asc();
        } else if ("emailAsc".equals(sort)) {
          userQuery.orderByUserEmail().asc();
        } else if ("emailDesc".equals(sort)) {
          userQuery.orderByUserEmail().desc();
        }
        
      }
      
      Integer startValue = start != null ? start.intValue() : 0;
      Integer size = MAX_USER_SIZE; // TODO: pass actual size
      List<User> users = userQuery.listPage(startValue, (size != null && size > 0) ? size : MAX_USER_SIZE);
      Long totalCount = userQuery.count();
      result.setTotal(Long.valueOf(totalCount.intValue()));
      result.setStart(startValue);
      result.setSize(users.size());
      result.setData(convertToUserRepresentations(users));
      
      return result;
    }
    
    protected List<UserRepresentation> convertToUserRepresentations(List<User> users) {
      List<UserRepresentation> result = new ArrayList<UserRepresentation>(users.size());
      for (User user : users) {
        result.add(new UserRepresentation(user));
      }
      return result;
    }

    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/rest/admin/users/{userId}", method = RequestMethod.PUT)
    public void updateUserDetails(@PathVariable String userId, @RequestBody UpdateUsersRepresentation updateUsersRepresentation) {
      User user = identityService.createUserQuery().userId(userId).singleResult();
      if (user != null) {
        user.setId(updateUsersRepresentation.getId());
        user.setFirstName(updateUsersRepresentation.getFirstName());
        user.setLastName(updateUsersRepresentation.getLastName());
        user.setEmail(updateUsersRepresentation.getEmail());
        identityService.saveUser(user);
      }
    }
    
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/rest/admin/users", method = RequestMethod.PUT)
    public void bulkUpdateUserDetails(@RequestBody UpdateUsersRepresentation updateUsersRepresentation) {
      validateAdminRole();
      
      // Password update
      if (updateUsersRepresentation.getPassword() != null) {
        for (String userId : updateUsersRepresentation.getUsers()) {
          User user = identityService.createUserQuery().userId(userId).singleResult();
          if (user != null) {
            user.setPassword(updateUsersRepresentation.getPassword());
            identityService.saveUser(user);
          }
        }
      }
    }
    
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/rest/admin/users/{userId}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable String userId) {
      validateAdminRole();
      
      List<Group> groups = identityService.createGroupQuery().groupMember(userId).list();
      if (groups != null && groups.size() > 0) {
        for (Group group : groups) {
          identityService.deleteMembership(userId, group.getId());
        }
      }
      identityService.deleteUser(userId);
    }
 
    
    @RequestMapping(value = "/rest/admin/users", method = RequestMethod.POST)
    public User createNewUser(@RequestBody CreateUserRepresentation userRepresentation) {
      validateAdminRole();
      
      if(StringUtils.isBlank(userRepresentation.getId()) ||
          StringUtils.isBlank(userRepresentation.getPassword()) || 
          StringUtils.isBlank(userRepresentation.getFirstName())) {
          throw new BadRequestException("Id, password and first name are required");
      }
      
      if (userRepresentation.getEmail() != null && identityService.createUserQuery().userEmail(userRepresentation.getEmail()).count() > 0) {
        throw new ConflictingRequestException("User already registered", "ACCOUNT.SIGNUP.ERROR.ALREADY-REGISTERED");
      } 
      
      User user = identityService.newUser(userRepresentation.getId() != null ? userRepresentation.getId() : userRepresentation.getEmail());
      user.setFirstName(userRepresentation.getFirstName());
      user.setLastName(userRepresentation.getLastName());
      user.setEmail(userRepresentation.getEmail());
      user.setPassword(userRepresentation.getPassword());
      identityService.saveUser(user);
      
      return user;
    }
    
    protected void validateAdminRole() {
      boolean isAdmin = identityService.createGroupQuery()
          .groupId(GroupIds.ROLE_ADMIN)
          .groupMember(SecurityUtils.getCurrentUserId())
          .count() > 0;
          if (!isAdmin) {
            throw new NotPermittedException();
          }
    }
    
}
