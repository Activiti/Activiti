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

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.exception.BadRequestException;

/**
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class AbstractIdmProfileResource {

  private static final Logger logger = LoggerFactory.getLogger(AbstractIdmProfileResource.class);

  @Autowired
  private IdentityService identityService;

  @Autowired
  private UserCache userCache;

  public User getProfile() {
    CachedUser cachedUser = userCache.getUser(SecurityUtils.getCurrentUserId());
    return cachedUser.getUser();
  }

  public User updateUser(User userRepresentation) {

    User currentUser = SecurityUtils.getCurrentUserObject();

    // If user is not externally managed, we need the email address for login, so an empty email is not allowed
    if (StringUtils.isEmpty(userRepresentation.getEmail())) {
      throw new BadRequestException("Empty email is not allowed");
    }

    User user = identityService.createUserQuery().userId(currentUser.getId()).singleResult();
    user.setEmail(userRepresentation.getEmail());
    user.setFirstName(userRepresentation.getFirstName());
    user.setLastName(userRepresentation.getLastName());
    identityService.saveUser(user);
    return user;
  }

}
