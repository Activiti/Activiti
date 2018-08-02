package org.activiti.spring.security.policies;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.runtime.api.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.runtime.api.model.payloads.GetProcessInstancesPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityPoliciesApplicationServiceImpl extends BaseSecurityPoliciesManagerImpl {


    @Autowired
    private SecurityPoliciesService securityPoliciesService;

    @Autowired
    private SecurityPoliciesProcessDefinitionRestrictionApplier processDefinitionRestrictionApplier;

    @Autowired
    private SecurityPoliciesProcessInstanceRestrictionApplier processInstanceRestrictionApplier;

    @Value("${spring.application.name:application}")
    private String applicationName;

    public GetProcessDefinitionsPayload restrictProcessDefQuery(SecurityPolicy securityPolicy) {
        return restrictQuery(processDefinitionRestrictionApplier, securityPolicy);
    }

    private Set<String> definitionKeysAllowedForApplicationPolicy(SecurityPolicy securityPolicy) {
        Map<String, Set<String>> restrictions = definitionKeysAllowedForPolicy(securityPolicy);
        Set<String> keys = new HashSet<>();

        for (String appName : restrictions.keySet()) {
            //only take policies for this app
            //or if we don't know our own appName (just being defensive) then include everything
            //ignore hyphens and case due to values getting set via env vars
            if (appName != null && appName.replace("-", "").equalsIgnoreCase(applicationName.replace("-", ""))) {
                keys.addAll(restrictions.get(appName));
            }
        }
        return keys;
    }


    public GetProcessInstancesPayload restrictProcessInstQuery(SecurityPolicy securityPolicy) {
        return restrictQuery(processInstanceRestrictionApplier, securityPolicy);
    }

    private <T> T restrictQuery(SecurityPoliciesRestrictionApplier<T> restrictionApplier, SecurityPolicy securityPolicy) {
        if (noSecurityPoliciesOrNoUser()) {
            return restrictionApplier.allowAll();
        }

        Set<String> keys = definitionKeysAllowedForApplicationPolicy(securityPolicy);

        if (keys != null && !keys.isEmpty()) {

            if (keys.contains(securityPoliciesService.getWildcard())) {
                return restrictionApplier.allowAll();
            }

            return restrictionApplier.restrictToKeys(keys);
        }

        //policies are in place but if we've got here then none for this user
        if ((keys == null || keys.isEmpty()) && securityPoliciesService.policiesDefined()) {
            return restrictionApplier.denyAll();
        }

        return restrictionApplier.allowAll();
    }

    public boolean canWrite(String processDefinitionKey) {
        return hasPermission(processDefinitionKey, SecurityPolicy.WRITE, applicationName)
                || hasPermission(processDefinitionKey, SecurityPolicy.WRITE, applicationName);
    }

    public boolean canRead(String processDefinitionKey) {
        return hasPermission(processDefinitionKey, SecurityPolicy.READ, applicationName)
                || hasPermission(processDefinitionKey, SecurityPolicy.WRITE, applicationName);
    }

    protected boolean anEntryInSetStartsKey(Set<String> keys, String processDefinitionKey) {
        for (String key : keys) {
            //override the base class with exact matching as startsWith is only preferable for audit where id might be used that would start with key
            if (processDefinitionKey.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

}
