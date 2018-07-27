package org.activiti.runtime.api.identity;

import java.util.List;

public interface UserGroupManager {

    ActivitiUser create(String userName,
                            String password,
                            List<String> groups,
                            List<String> roles);

    void delete(String username);

    boolean exists(String s);

    ActivitiUser loadUser(String username);

    List<String> getUserGroups(String username);

    List<String> getUserRoles(String username);

}
