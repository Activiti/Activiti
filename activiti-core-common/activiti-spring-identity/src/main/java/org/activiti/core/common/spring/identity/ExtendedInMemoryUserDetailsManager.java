package org.activiti.core.common.spring.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

public class ExtendedInMemoryUserDetailsManager extends InMemoryUserDetailsManager {

    private List<String> users = new ArrayList<>();
    private List<String> groups = new ArrayList<>();

    @Override
    public void createUser(UserDetails user) {
        super.createUser(user);
        users.add(user.getUsername());
        groups = user.getAuthorities().stream()
                .filter(x -> (x.getAuthority().contains("GROUP")))
                .map(x -> (x.getAuthority()))
                .collect(Collectors.toList());
    }

    public List<String> getUsers() {
        return users;
    }

    public List<String> getGroups() {
        return groups;
    }
}
