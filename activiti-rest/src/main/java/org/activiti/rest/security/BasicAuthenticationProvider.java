package org.activiti.rest.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.activiti.engine.UserGroupLookupProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class BasicAuthenticationProvider implements AuthenticationProvider {

  @Autowired(required = false)
  private UserGroupLookupProxy userGroupLookupProxy;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String name = authentication.getName();
    String password = authentication.getCredentials().toString();

    org.activiti.engine.impl.identity.Authentication.setAuthenticatedUserId(name);

    if(userGroupLookupProxy!=null) {
      List<String> groups = userGroupLookupProxy.getGroupsForCandidateUser(name);
      Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
      for(String group:groups){
        grantedAuthorities.add(new SimpleGrantedAuthority(group));
      }

      return new UsernamePasswordAuthenticationToken(name, password, grantedAuthorities);
    }

    //TODO: need to review - expected scenario is that a proxy to an identity provider is supplied

    return new UsernamePasswordAuthenticationToken(name, password, null);

  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }
}
