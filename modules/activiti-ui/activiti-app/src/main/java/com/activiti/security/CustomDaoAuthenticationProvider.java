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

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;


/**
 * @author jbarrez
 */
public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {
	
	protected void additionalAuthenticationChecks(org.springframework.security.core.userdetails.UserDetails userDetails, 
			org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication) throws org.springframework.security.core.AuthenticationException {
		
		// Overriding this method to catch empty/null passwords. This happens when users are synced with LDAP sync:
		// they will have an external id, but no password (password is checked against ldap).
		//
		// The default DaoAuthenticationProvider will choke on an empty password (an arrayIndexOutOfBoundsException 
		// somewhere deep in the bowels of password encryption), hence this override
		if (StringUtils.isEmpty(userDetails.getPassword())) {
			  throw new BadCredentialsException(messages.getMessage(
	                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
		}
		
		super.additionalAuthenticationChecks(userDetails, authentication);
		
	};

}
