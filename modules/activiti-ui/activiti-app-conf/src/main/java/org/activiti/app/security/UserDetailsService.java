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
package org.activiti.app.security;

import java.util.ArrayList;
import java.util.Collection;

import org.activiti.app.security.ActivitiAppUser;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.api.UserCache.CachedUser;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is called AFTER successful authentication, to populate the user object with additional details The default (no ldap) way of authentication is a bit hidden in Spring Security magic. But
 * basically, the user object is fetched from the db and the hashed password is compared with the hash of the provided password (using the Spring {@link StandardPasswordEncoder}).
 */
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService, CustomUserDetailService {

  @Autowired
  private UserCache userCache;

  @Autowired
  private IdentityService identityService;

  @Autowired
  private Environment env;

  private long userValidityPeriod;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(final String login) {

    // This method is only called during the login.
    // All subsequent calls use the method with the long userId as parameter.
    // (Hence why the cache is NOT used here, but it is used in the loadByUserId)

    String actualLogin = login;
    User userFromDatabase = null;

    userFromDatabase = identityService.createUserQuery().userId(actualLogin).singleResult();

    // Verify user
    if (userFromDatabase == null) {
      throw new UsernameNotFoundException("User " + actualLogin + " was not found in the database");
    }

    // Add capabilities to user object
    Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

    // add default authority
    grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));

    // check if user is in super user group
    String superUserGroupName = env.getRequiredProperty("admin.group");
    for (Group group : identityService.createGroupQuery().groupMember(userFromDatabase.getId()).list()) {
      if (StringUtils.equals(superUserGroupName, group.getName())) {
        grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
      }
    }
    
    // Adding it manually to cache
    userCache.putUser(userFromDatabase.getId(), new CachedUser(userFromDatabase, grantedAuthorities));

    // Set authentication globally for Activiti
    Authentication.setAuthenticatedUserId(String.valueOf(userFromDatabase.getId()));

    return new ActivitiAppUser(userFromDatabase, actualLogin, grantedAuthorities);
  }

  @Transactional
  public UserDetails loadByUserId(final String userId) {

    CachedUser cachedUser = userCache.getUser(userId, true, true, false); // Do not check for validity. This would lead to A LOT of db requests! For login, there is a validity period (see below)
    if (cachedUser == null) {
      throw new UsernameNotFoundException("User " + userId + " was not found in the database");
    }

    long lastDatabaseCheck = cachedUser.getLastDatabaseCheck();
    long currentTime = System.currentTimeMillis(); // No need to create a Date object. The Date constructor simply calls this method too!

    if (userValidityPeriod <= 0L || (currentTime - lastDatabaseCheck >= userValidityPeriod)) {

      userCache.invalidate(userId);
      cachedUser = userCache.getUser(userId, true, true, false); // Fetching it again will refresh data

      cachedUser.setLastDatabaseCheck(currentTime);
    }

    // The Spring security docs clearly state a new instance must be returned on every invocation
    User user = cachedUser.getUser();
    String actualUserId = user.getEmail();

    // Set authentication globally for Activiti
    Authentication.setAuthenticatedUserId(String.valueOf(user.getId()));

    return new ActivitiAppUser(cachedUser.getUser(), actualUserId, cachedUser.getGrantedAuthorities());
  }

  public void setUserValidityPeriod(long userValidityPeriod) {
    this.userValidityPeriod = userValidityPeriod;
  }
}
