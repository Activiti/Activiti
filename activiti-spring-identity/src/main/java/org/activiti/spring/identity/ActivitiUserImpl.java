package org.activiti.spring.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.runtime.api.identity.ActivitiUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class ActivitiUserImpl extends User implements ActivitiUser {

    public ActivitiUserImpl(String username,
                            String password,
                            Collection<String> groups,
                            Collection<String> roles) {
        super(username,
              password,
              createGroupsAndRoles(groups,
                                   roles));
    }

    public ActivitiUserImpl(UserDetails userDetails) {
        super(userDetails.getUsername(),
              userDetails.getPassword(),
              userDetails.getAuthorities());
    }

    private static Collection<? extends GrantedAuthority> createGroupGrantedAuthorities(Collection<String> groupIds) {
        if(groupIds != null) {
            return groupIds.stream().map((String r) -> new SimpleGrantedAuthority("GROUP:" + r)).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    private static Collection<? extends GrantedAuthority> createRoleGrantedAuthorities(Collection<String> roles) {
        if(roles != null) {
            return roles.stream().map((String r) -> new SimpleGrantedAuthority("ROLE:" + r)).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    private static Collection<? extends GrantedAuthority> createGroupsAndRoles(Collection<String> groups,
                                                                               Collection<String> roles) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.addAll(createGroupGrantedAuthorities(groups));
        grantedAuthorities.addAll(createRoleGrantedAuthorities(roles));
        return grantedAuthorities;
    }

    public List<String> getGroupIds() {
        return getAuthorities().stream()
                .filter((GrantedAuthority a) -> a.getAuthority().startsWith("GROUP:"))
                .map((GrantedAuthority a) -> a.getAuthority().substring(6,
                                                                        a.getAuthority().length()))
                .collect(Collectors.toList());
    }

    public List<String> getRoles() {
        return getAuthorities().stream()
                .filter((GrantedAuthority a) -> a.getAuthority().startsWith("ROLE:"))
                .map((GrantedAuthority a) -> a.getAuthority().substring(5,
                                                                        a.getAuthority().length()))
                .collect(Collectors.toList());
    }
}
