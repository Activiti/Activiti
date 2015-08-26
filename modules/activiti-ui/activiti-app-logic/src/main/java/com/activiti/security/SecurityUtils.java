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

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.activiti.domain.idm.User;

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
    public static Long getCurrentUserId() {
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
