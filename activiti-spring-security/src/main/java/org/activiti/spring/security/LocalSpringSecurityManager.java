package org.activiti.spring.security;

import java.util.Collection;

import org.activiti.runtime.api.identity.ActivitiUser;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

/*
 * The use of this class/bean is discouraged besides for examples
 * This class is allowing you to test with different users in the Java context, without requiring to set the Web Security context
 * As soon as you add controllers to your Spring Boot application, you should rely on the default security context
 */
@Component
//@TODO: this should be conditional so it doesn't interfer with Spring Security if it is not needed
public class LocalSpringSecurityManager implements SecurityManager {

    @Autowired
    private UserGroupManager userGroupManager;

    public void authorize(ActivitiUser user) {
        if (userGroupManager.exists(user.getUsername())) {
            SecurityContextHolder.setContext(new SecurityContextImpl(new Authentication() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return ((User) user).getAuthorities();
                }

                @Override
                public Object getCredentials() {
                    return null;
                }

                @Override
                public Object getDetails() {
                    return null;
                }

                @Override
                public Object getPrincipal() {
                    return null;
                }

                @Override
                public boolean isAuthenticated() {
                    return true;
                }

                @Override
                public void setAuthenticated(boolean b) throws IllegalArgumentException {

                }

                @Override
                public String getName() {
                    return user.getUsername();
                }
            }));
        } else {
            throw new IllegalStateException("Invalid user: User Doesn't exist!");
        }
    }

    public String getAuthenticatedUserId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
