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
package org.activiti.app.service.idm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.activiti.app.service.api.UserCache;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * Cache containing User objects to prevent too much DB-traffic (users exist seperatly from the Activiti tables, they need to be fetched afterward one by one to join with those entities).
 * 
 * TODO: This could probably be made more efficient with bulk getting. The Google cache impl allows this: override loadAll and use getAll() to fetch multiple entities.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@Service
public class UserCacheImpl implements UserCache {

  private final Logger logger = LoggerFactory.getLogger(UserCacheImpl.class);

  @Autowired
  protected Environment environment;

  @Autowired
  protected IdentityService identityService;

  protected LoadingCache<String, CachedUser> userCache;

  @PostConstruct
  protected void initCache() {
    Long userCacheMaxSize = environment.getProperty("cache.users.max.size", Long.class);
    Long userCacheMaxAge = environment.getProperty("cache.users.max.age", Long.class);

    userCache = CacheBuilder.newBuilder().maximumSize(userCacheMaxSize != null ? userCacheMaxSize : 2048)
        .expireAfterAccess(userCacheMaxAge != null ? userCacheMaxAge : (24 * 60 * 60), TimeUnit.SECONDS).recordStats().build(new CacheLoader<String, CachedUser>() {

          public CachedUser load(final String userId) throws Exception {
            User userFromDatabase = identityService.createUserQuery().userId(userId).singleResult();
            if (userFromDatabase == null) {
              throw new UsernameNotFoundException("User " + userId + " was not found in the database");
            }
            
            Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
            
            return new CachedUser(userFromDatabase, grantedAuthorities);
          }

        });
  }

  public void putUser(String userId, CachedUser cachedUser) {
    userCache.put(userId, cachedUser);
  }

  public CachedUser getUser(String userId) {
    return getUser(userId, false, false, true); // always check validity by default
  }

  public CachedUser getUser(String userId, boolean throwExceptionOnNotFound, boolean throwExceptionOnInactive, boolean checkValidity) {
    try {
      // The cache is a LoadingCache and will fetch the value itself
      CachedUser cachedUser = userCache.get(userId);
      return cachedUser;

    } catch (ExecutionException e) {
      return null;
    } catch (UncheckedExecutionException uee) {

      // Some magic with the exceptions is needed:
      // the exceptions like UserNameNotFound and Locked cannot
      // bubble up, since Spring security will react on them otherwise
      if (uee.getCause() instanceof RuntimeException) {
        RuntimeException runtimeException = (RuntimeException) uee.getCause();

        if (runtimeException instanceof UsernameNotFoundException) {
          if (throwExceptionOnNotFound) {
            throw runtimeException;
          } else {
            return null;
          }
        }

        if (runtimeException instanceof LockedException) {
          if (throwExceptionOnNotFound) {
            throw runtimeException;
          } else {
            return null;
          }
        }

      }
      throw uee;
    }
  }

  @Override
  public void invalidate(String userId) {
    userCache.invalidate(userId);
  }
}
