package org.activiti.cloud.services.security;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseSecurityPoliciesApplicationService implements SecurityPoliciesApplicationService {

    @Autowired
    protected UserGroupManager userGroupManager;

    @Autowired
    protected SecurityManager securityManager;

    @Autowired
    protected SecurityPoliciesService securityPoliciesService;

    protected boolean noSecurityPoliciesOrNoUser() {
        return !securityPoliciesService.policiesDefined() || securityManager.getAuthenticatedUserId() == null;
    }

    protected Map<String, Set<String>> definitionKeysAllowedForPolicy(SecurityPolicy securityPolicy) {
        List<String> groups = null;

        if (userGroupManager != null && securityManager.getAuthenticatedUserId()!= null) {
            groups = userGroupManager.getUserGroups(securityManager.getAuthenticatedUserId());
        }

        return securityPoliciesService.getProcessDefinitionKeys(securityManager.getAuthenticatedUserId(),
                groups,
                securityPolicy);
    }

    @Override
    public boolean canRead(String processDefinitionKey,
                           String appName) {
        return hasPermission(processDefinitionKey,
                SecurityPolicy.READ,
                appName);
    }


    @Override
    public boolean canWrite(String processDefinitionKey,
                            String appName){
        return hasPermission(processDefinitionKey, SecurityPolicy.WRITE,appName);
    }


    protected boolean hasPermission(String processDefinitionKey,
                                  SecurityPolicy securityPolicy,
                                  String appName) {

        if (!securityPoliciesService.policiesDefined() || userGroupManager == null || securityManager.getAuthenticatedUserId() == null) {
            return true;
        }

        if (securityManager != null && userGroupManager.getUserRoles(securityManager.getAuthenticatedUserId()).contains("admin")) {
            return true;
        }

        Set<String> keys = new HashSet<>();
        Map<String, Set<String>> policiesMap = definitionKeysAllowedForPolicy(securityPolicy);
        if(policiesMap.get(appName) !=null) {
            keys.addAll(policiesMap.get(appName));
        }
        //also factor for case sensitivity and hyphens (which are stripped when specified through env var)
        if(appName!=null && policiesMap.get(appName.replaceAll("-","").toLowerCase()) != null){
            keys.addAll(policiesMap.get(appName.replaceAll("-","").toLowerCase()));
        }

        return anEntryInSetStartsKey(keys,
                                     processDefinitionKey) || keys.contains(securityPoliciesService.getWildcard());
    }

    //startsWith logic supports the case of audit where only definition id might be available and it would start with the key
    //protected scope means we can override where exact matching more appropriate (consider keys ProcessWithVariables and ProcessWithVariables2)
    //even for audit would be better if we had a known separator which cant be part of key - this seems best we can do for now
    protected boolean anEntryInSetStartsKey(Set<String> keys, String processDefinitionKey){
        for(String key:keys){
            if(processDefinitionKey.startsWith(key)){
                return true;
            }
        }
        return false;
    }
}
