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
package org.activiti.app.service.api;

import java.util.Collection;

import org.activiti.engine.identity.User;
import org.springframework.security.core.GrantedAuthority;

/**
 * A cache of {@link User} objects.
 * 
 * @author Joram Barrez
 */
public interface UserCache {
  
  CachedUser getUser(String userId);

  CachedUser getUser(String userId, boolean throwExceptionOnNotFound, boolean throwExceptionOnInactive, boolean checkValidity);

  void putUser(String userId, CachedUser cachedUser);

  void invalidate(String userId);

  public static class CachedUser {

    private Collection<GrantedAuthority> grantedAuthorities;

    private User user;

    private long lastDatabaseCheck;

    public CachedUser(User user, Collection<GrantedAuthority> grantedAuthorities) {
      this.user = user;
      this.grantedAuthorities = grantedAuthorities;
      this.lastDatabaseCheck = System.currentTimeMillis();
    }

    public User getUser() {
      return user;
    }

    public Collection<GrantedAuthority> getGrantedAuthorities() {
      return grantedAuthorities;
    }

    public long getLastDatabaseCheck() {
      return lastDatabaseCheck;
    }

    public void setLastDatabaseCheck(long lastDatabaseCheck) {
      this.lastDatabaseCheck = lastDatabaseCheck;
    }

  }

}
