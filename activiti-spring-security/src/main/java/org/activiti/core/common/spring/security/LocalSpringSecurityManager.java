package org.activiti.core.common.spring.security;

import org.activiti.api.runtime.shared.security.SecurityManager;
import org.springframework.security.core.context.SecurityContextHolder;

/*
 * This is a simple wrapper for Spring Security Context Holder
 */
public class LocalSpringSecurityManager implements SecurityManager {

    public String getAuthenticatedUserId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
