/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.core.common.spring.security.policies;

import static java.util.Arrays.asList;

import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.core.common.spring.security.policies.conf.SecurityPoliciesProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseSecurityPoliciesManagerImpl implements SecurityPoliciesManager {

    protected SecurityManager securityManager;

    protected SecurityPoliciesProperties securityPoliciesProperties;

    public BaseSecurityPoliciesManagerImpl(SecurityManager securityManager,
                                           SecurityPoliciesProperties securityPoliciesProperties) {
        this.securityManager = securityManager;
        this.securityPoliciesProperties = securityPoliciesProperties;
    }

    public boolean arePoliciesDefined() {
        return !securityPoliciesProperties.getPolicies().isEmpty();
    }

    @Override
    public Map<String, Set<String>> getAllowedKeys(SecurityPolicyAccess... securityPoliciesAccess) {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        List<SecurityPolicy> policies = securityPoliciesProperties.getPolicies();
        Map<String, Set<String>> definitionKeysAllowedByPolicy = new HashMap<>();

        List<String> groups = null;

        if (authenticatedUserId != null) {
            groups = securityManager.getAuthenticatedUserGroups();
        }
        for (SecurityPolicy ssp : policies) {
            definitionKeysAllowedByPolicy.computeIfAbsent(ssp.getServiceName(),
                                                          k -> new HashSet<>());

            // I need to check that the user is listed in the user lists or that at least one of the user groups is in the group list
            if (isUserInPolicy(ssp, authenticatedUserId) || isGroupInPolicy(ssp, groups)) {

                // Here if securityPolicyAccess is READ, it should also include WRITES, if it is NONE nothing, and if it is WRITE only WRITE
                List<SecurityPolicyAccess> securityPolicyAccesses = asList(securityPoliciesAccess);
                if (securityPolicyAccesses.contains(SecurityPolicyAccess.WRITE)) {
                    if (ssp.getAccess().equals(SecurityPolicyAccess.WRITE)) {
                        definitionKeysAllowedByPolicy.get(ssp.getServiceName()).addAll(ssp.getKeys());
                    }

                } else if (securityPolicyAccesses.contains(SecurityPolicyAccess.READ)) {
                    if (ssp.getAccess().equals(SecurityPolicyAccess.READ) || ssp.getAccess().equals(SecurityPolicyAccess.WRITE)) {
                        definitionKeysAllowedByPolicy.get(ssp.getServiceName()).addAll(ssp.getKeys());
                    }
                }


            }
        }
        return definitionKeysAllowedByPolicy;
    }


    private boolean isUserInPolicy(SecurityPolicy ssp, String userId) {
        return (ssp.getUsers() != null && !ssp.getUsers().isEmpty() && ssp.getUsers().contains(userId));
    }

    private boolean isGroupInPolicy(SecurityPolicy ssp, List<String> groups) {
        if (ssp.getGroups() != null && groups != null) {
            for (String g : ssp.getGroups()) {
                if (groups.contains(g)) {
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    public boolean canRead(String processDefinitionKey,
                           String appName) {
        return hasPermission(processDefinitionKey,
                SecurityPolicyAccess.READ,
                appName);
    }


    @Override
    public boolean canWrite(String processDefinitionKey,
                            String appName) {
        return hasPermission(processDefinitionKey, SecurityPolicyAccess.WRITE, appName);
    }


    public boolean hasPermission(String processDefinitionKey,
                                 SecurityPolicyAccess securityPolicyAccess,
                                 String appName) {

        // No security policies defined, allowed to see everything
        if (securityPoliciesProperties.getPolicies().isEmpty()) {
            return true;
        }

        // If you are an admin you can see everything , @TODO: make it more flexible
        if (securityManager.getAuthenticatedUserRoles().contains("ACTIVITI_ADMIN")) {
            return true;
        }

        Set<String> keys = new HashSet<>();
        Map<String, Set<String>> policiesMap = getAllowedKeys(securityPolicyAccess);
        if (policiesMap.get(appName) != null) {
            keys.addAll(policiesMap.get(appName));
        }
        //also factor for case sensitivity and hyphens (which are stripped when specified through env var)
        if (appName != null && policiesMap.get(appName.replaceAll("-", "").toLowerCase()) != null) {
            keys.addAll(policiesMap.get(appName.replaceAll("-", "").toLowerCase()));
        }

        return anEntryInSetStartsKey(keys,
                processDefinitionKey) || keys.contains(securityPoliciesProperties.getWildcard());
    }

    //startsWith logic supports the case of audit where only definition id might be available and it would start with the key
    //protected scope means we can override where exact matching more appropriate (consider keys ProcessWithVariables and ProcessWithVariables2)
    //even for audit would be better if we had a known separator which cant be part of key - this seems best we can do for now
    protected boolean anEntryInSetStartsKey(Set<String> keys, String processDefinitionKey) {
        for (String key : keys) {
            if (processDefinitionKey.startsWith(key)) {
                return true;
            }
        }
        return false;
    }

    protected SecurityPoliciesProperties getSecurityPoliciesProperties() {
        return securityPoliciesProperties;
    }
}
