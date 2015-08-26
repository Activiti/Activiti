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
package com.activiti.service.api;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.activiti.domain.idm.User;

/**
 * A cache of {@link User} objects.
 * 
 * @author Joram Barrez
 */
public interface UserCache {

	CachedUser getUser(Long userId);
	
	CachedUser getUser(Long userId, boolean throwExceptionOnNotFound, boolean throwExceptionOnInactive, boolean checkValidity);

	CachedUser getUser(String userId);
	
	void putUser(Long userId, CachedUser cachedUser);
	
	void invalidate(Long userId);

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
