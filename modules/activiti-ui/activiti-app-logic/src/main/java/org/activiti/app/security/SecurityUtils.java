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

import org.activiti.app.constant.GroupIds;
import org.activiti.app.service.exception.NotPermittedException;
import org.activiti.engine.identity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

  private static User assumeUser;

  private SecurityUtils() {
  }

  /**
   * Get the login of the current user.
   */
  public static String getCurrentUserId() {
    User user = getCurrentUserObject();
    if (user != null) {
      return user.getId();
    }
    return null;
  }

  /**
   * @return the {@link User} object associated with the current logged in user.
   */
  public static User getCurrentUserObject() {
    if (assumeUser != null) {
      return assumeUser;
    }

    User user = null;
    ActivitiAppUser appUser = getCurrentActivitiAppUser();
    if (appUser != null) {
      user = appUser.getUserObject();
    }
    return user;
  }

  public static ActivitiAppUser getCurrentActivitiAppUser() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    ActivitiAppUser user = null;
    if (securityContext != null && securityContext.getAuthentication() != null) {
      Object principal = securityContext.getAuthentication().getPrincipal();
      if (principal != null && principal instanceof ActivitiAppUser) {
        user = (ActivitiAppUser) principal;
      }
    }
    return user;
  }

  public static boolean currentUserHasCapability(String capability) {
    ActivitiAppUser user = getCurrentActivitiAppUser();
    for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
      if (capability.equals(grantedAuthority.getAuthority())) {
        return true;
      }
    }
    return false;
  }

  public static void assumeUser(User user) {
    assumeUser = user;
  }

  public static void clearAssumeUser() {
    assumeUser = null;
  }
  
}
