package org.activiti.spring.identity;

import org.activiti.runtime.api.identity.UserGroupManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActivitiUserGroupManagerImpl implements UserGroupManager {

    @Autowired
    private UserDetailsService userDetailsService;


    @Override
    public List<String> getUserGroups(String username) {

        return userDetailsService.loadUserByUsername(username).getAuthorities().stream()
                .filter((GrantedAuthority a) -> a.getAuthority().startsWith("GROUP_"))
                .map((GrantedAuthority a) -> a.getAuthority().substring(6))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserRoles(String username) {
        return userDetailsService.loadUserByUsername(username).getAuthorities().stream()
                .filter((GrantedAuthority a) -> a.getAuthority().startsWith("ROLE_"))
                .map((GrantedAuthority a) -> a.getAuthority().substring(5))
                .collect(Collectors.toList());
    }
}
