package org.activiti.cloud.services.identity.basic;

import java.util.List;

import org.activiti.runtime.api.auth.AuthorizationLookup;
import org.activiti.runtime.api.identity.IdentityLookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BasicAuthorizationLookup implements AuthorizationLookup {

    @Value("${admin-role-name:admin}")
    private String adminRoleName;

    private IdentityLookup identityLookup;

    @Autowired
    public BasicAuthorizationLookup(IdentityLookup identityLookup) {
        this.identityLookup = identityLookup;
    }

    @Override
    public List<String> getRolesForUser(String s) {
        // NOT RECOMMENDED TO MIX GROUPS AND ROLES IN GENERAL - THIS IS JUST A LIMITATION OF THIS EXAMPLE
        return identityLookup.getGroupsForCandidateUser(s);
    }

    @Override
    public boolean isAdmin(String userId) {
        List<String> roles = getRolesForUser(userId);
        return (roles != null && roles.contains(adminRoleName));
    }

    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }
}
