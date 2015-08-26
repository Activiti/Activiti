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

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A {@link UserDetails} implementation that exposes the {@link com.activiti.domain.idm.User.User} object
 * the logged in user represents.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class ActivitiAppUser extends User {

    private static final long serialVersionUID = 1L;
    
    protected com.activiti.domain.idm.User userObject;
    
    /**
     * The userId needs to be passed explicitly. It can be the email, but also the external id when eg LDAP is being used. 
     */
    public ActivitiAppUser(com.activiti.domain.idm.User user, String userId, Collection<? extends GrantedAuthority> authorities) {
        super(userId, user.getPassword() != null ? user.getPassword() : "", authorities); // Passwords needs to be non-null. Even if it's not there (eg LDAP auth)
        this.userObject = user;
    }
    
    public com.activiti.domain.idm.User getUserObject() {
        return userObject;
    }
}
