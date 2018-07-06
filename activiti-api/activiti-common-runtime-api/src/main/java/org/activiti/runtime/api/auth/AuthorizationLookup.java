package org.activiti.runtime.api.auth;

import java.util.List;

public interface AuthorizationLookup {

    List<String> getRolesForUser(String userId);

    boolean isAdmin(String userId);
}
