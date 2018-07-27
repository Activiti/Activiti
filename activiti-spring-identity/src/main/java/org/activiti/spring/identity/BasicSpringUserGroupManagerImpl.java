package org.activiti.spring.identity;

import java.util.List;

import org.activiti.runtime.api.identity.UserGroupManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

@Component
public class BasicSpringUserGroupManagerImpl implements UserGroupManager {

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Override
    public ActivitiUserImpl create(String username,
                                   String password,
                                   List<String> groups,
                                   List<String> roles) {

        ActivitiUserImpl activitiUser = new ActivitiUserImpl(username,
                                                             password,
                                                             groups,
                                                             roles);
        userDetailsManager.createUser(activitiUser);
        return activitiUser;
    }

    public void delete(String username) {
        userDetailsManager.deleteUser(username);
    }

    public boolean exists(String s) {
        return userDetailsManager.userExists(s);
    }

    public ActivitiUserImpl loadUser(String username) {
        return new ActivitiUserImpl(userDetailsManager.loadUserByUsername(username));
    }

    @Override
    public List<String> getUserGroups(String username) {
        return new ActivitiUserImpl(userDetailsManager.loadUserByUsername(username)).getGroupIds();
    }

    @Override
    public List<String> getUserRoles(String username) {
        return new ActivitiUserImpl(userDetailsManager.loadUserByUsername(username)).getRoles();
    }
}
