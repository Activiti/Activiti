package org.activiti.rest.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class BasicAuthenticationProvider implements AuthenticationProvider {
  
  @Autowired
  private IdentityService identityService;
  
  @Override
  public Authentication authenticate(Authentication authentication) 
    throws AuthenticationException {
      String name = authentication.getName();
      String password = authentication.getCredentials().toString();

      boolean authenticated = identityService.checkPassword(name, password);
      if (authenticated) {
        List<Group> groups = identityService.createGroupQuery().groupMember(name).list();
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        for (Group group : groups) {
            grantedAuthorities.add(new SimpleGrantedAuthority(group.getId()));
        }
        identityService.setAuthenticatedUserId(name);
        return new UsernamePasswordAuthenticationToken(name, password, grantedAuthorities);
      } else {
        throw new BadCredentialsException("Authentication failed for this username and password");
      }
  }

  @Override
  public boolean supports(Class<?> authentication) {
      return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }
}
