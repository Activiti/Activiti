package org.activiti.api.runtime.shared.identity;

import java.util.List;

public interface UserGroupManager {

    List<String> getUserGroups(String username);

    List<String> getUserRoles(String username);

    List<String> getGroups();

    List<String> getUsers();
}
