/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.identity.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class BasicAuthenticationProvider implements AuthenticationProvider {


  @Autowired
  private UserDetailsService userDetailsService;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String name = authentication.getName();
    String password = authentication.getCredentials().toString();

    UserDetails userDetails = userDetailsService.loadUserByUsername(name);

    boolean authenticated = userDetails.getPassword().equals(password) && userDetails.isAccountNonExpired() && userDetails.isEnabled() && userDetails.isCredentialsNonExpired();

    if (authenticated) {
      org.activiti.engine.impl.identity.Authentication.setAuthenticatedUserId(name);
      return new UsernamePasswordAuthenticationToken(name, password, userDetails.getAuthorities());
    } else {
      throw new BadCredentialsException("Authentication failed for this username and password");
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }
}