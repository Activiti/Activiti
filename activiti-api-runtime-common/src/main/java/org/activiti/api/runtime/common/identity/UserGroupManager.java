package org.activiti.api.runtime.common.identity;

import java.util.List;

public interface UserGroupManager {

    List<String> getUserGroups(String username);

    List<String> getUserRoles(String username);

}
