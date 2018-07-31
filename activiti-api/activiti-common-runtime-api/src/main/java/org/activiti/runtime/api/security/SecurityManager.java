package org.activiti.runtime.api.security;

import org.activiti.runtime.api.identity.ActivitiUser;

public interface SecurityManager {

    void authenticate(ActivitiUser user);

    String getAuthenticatedUserId();

}
