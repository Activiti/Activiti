package org.activiti.runtime.api.security;

import org.activiti.runtime.api.identity.ActivitiUser;

public interface SecurityManager {

    void authorize(ActivitiUser user);

    String getAuthenticatedUserId();

}
