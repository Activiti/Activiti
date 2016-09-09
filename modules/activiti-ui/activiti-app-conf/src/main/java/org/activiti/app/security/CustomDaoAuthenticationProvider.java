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
			  throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
		}
		
		super.additionalAuthenticationChecks(userDetails, authentication);
		
	};

}
