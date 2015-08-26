/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.service.idm;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cache containing User objects to prevent too much DB-traffic (users exist seperatly from the
 * Activiti tables, they need to be fetched afterward one by one to join with those entities). 
 *
 * TODO: This could probably be made more efficient with bulk getting.
 * The Google cache impl allows this: override loadAll and use getAll() to fetch multiple entities.
 *
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@Service
public class UserCacheImpl implements UserCache {

	private final Logger logger = LoggerFactory.getLogger(UserCacheImpl.class);

	@Inject
	private Environment environment;

	@Inject
	private UserService userService;

	@Inject
	private Environment env;

	private LoadingCache<Long, CachedUser> userCache;

	@PostConstruct
	protected void initCache() {
		Long userCacheMaxSize = environment.getProperty("cache.users.max.size", Long.class);
		Long userCacheMaxAge = environment.getProperty("cache.users.max.age", Long.class);
		userCache = CacheBuilder.newBuilder()
				.maximumSize(userCacheMaxSize != null ? userCacheMaxSize : 2048)
				.expireAfterAccess(userCacheMaxAge != null ? userCacheMaxAge : (24 * 60 * 60) , TimeUnit.SECONDS)
				.recordStats()
				.build(new CacheLoader<Long, CachedUser>() {

					public CachedUser load(final Long userId) throws Exception {
						User userFromDatabase = userService.getUser(userId, true);
						if (userFromDatabase == null) {
							throw new UsernameNotFoundException("User " + userId + " was not found in the database");
						}

						Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

						// add default authority
						grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

						// check if user is in super user group
						String superUserGroupName = env.getRequiredProperty("admin.group");
						for (Group group : userFromDatabase.getGroups()) {
							if (StringUtils.equals(superUserGroupName, group.getName())) {
								grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
							}
						}
						return new CachedUser(userFromDatabase, grantedAuthorities);
					}


				});
	}

	public void putUser(Long userId, CachedUser cachedUser) {
		userCache.put(userId, cachedUser);
	}

	public CachedUser getUser(Long userId) {
		return getUser(userId, false, false, true); // always check validity by default
	}

	public CachedUser getUser(Long userId, boolean throwExceptionOnNotFound, boolean throwExceptionOnInactive, boolean checkValidity) {
		try {
			// The cache is a LoadingCache and will fetch the value itself
			CachedUser cachedUser = userCache.get(userId);

			if (checkValidity) {
				if (cachedUser != null && cachedUser.getUser() != null) {
					Long count = userService.getUserCountByUserIdAndLastUpdateDate(cachedUser.getUser().getId(), cachedUser.getUser().getLastUpdate());
					if (count == 0L) { // No such user was found -> cache version is invalid
						userCache.invalidate(userId);
						cachedUser = userCache.get(userId);
					}
				}
			}

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


	/**
	 * @return the user for the given id. Returns null if no user exists with the given id
	 * or if the id was not a valid user-id in the first place.
	 */
	public CachedUser getUser(String userId) {
		try {
			return getUser(Long.parseLong(userId));
		} catch(NumberFormatException nfe) {
			// Ignore exception
			return null;
		}
	}


	@Override
	public void invalidate(Long userId) {
		userCache.invalidate(userId);
	}

}