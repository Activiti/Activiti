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
package com.activiti.security;


import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.api.UserService;
import org.activiti.engine.impl.identity.Authentication;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class is called AFTER successful authentication, to populate the user object with additional details
 * The default (no ldap) way of authentication is a bit hidden in Spring Security magic. But basically,
 * the user object is fetched from the db and the hashed password is compared with the hash of the provided
 * password (using the Spring {@link StandardPasswordEncoder}). 
 */
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService, CustomUserDetailService {

	@Inject
	private UserCache userCache;

	@Inject
	private UserService userService;

	@Inject
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

		actualLogin = login.toLowerCase();
		userFromDatabase = userService.findUserByEmailFetchGroups(actualLogin);

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
		for (Group group : userFromDatabase.getGroups()) {
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
	public UserDetails loadByUserId(final Long userId) {

		CachedUser cachedUser = userCache.getUser(userId, true, true, false); // Do not check for validity. This would lead to A LOT of db requests! For login, there is a validity period (see below)
		if (cachedUser == null) {
			throw new UsernameNotFoundException("User " + userId + " was not found in the database");
		}
		
		long lastDatabaseCheck = cachedUser.getLastDatabaseCheck();
		long currentTime = System.currentTimeMillis(); // No need to create a Date object. The Date constructor simply calls this method too!
		
		if (userValidityPeriod <= 0L || (currentTime - lastDatabaseCheck >= userValidityPeriod)) {
			
			// We need to verify if the user cache is still valid (ie no changes - lastUpdate would have been updated)
			// This means an extra query for the get, but a huge save in db traffic the subsequent calls
			Long count = userService.getUserCountByUserIdAndLastUpdateDate(cachedUser.getUser().getId(), 
					cachedUser.getUser().getLastUpdate());
			if (count == 0L) { // No such user was found -> cache version is invalid
				userCache.invalidate(userId);
				cachedUser = userCache.getUser(userId, true, true, false); // Fetching it again will refresh data
			}
			
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
