package org.activiti.core.common.spring.security.policies;

import org.activiti.api.process.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.api.process.model.payloads.GetProcessInstancesPayload;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.core.common.spring.security.policies.conf.SecurityPoliciesProperties;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessSecurityPoliciesManagerImpl extends BaseSecurityPoliciesManagerImpl implements ProcessSecurityPoliciesManager{

    private final SecurityPoliciesRestrictionApplier<GetProcessDefinitionsPayload> processDefinitionRestrictionApplier;

    private final SecurityPoliciesRestrictionApplier<GetProcessInstancesPayload> processInstanceRestrictionApplier;

    @Value("${spring.application.name:application}")
    private String applicationName;

    public ProcessSecurityPoliciesManagerImpl(SecurityManager securityManager,
                                              SecurityPoliciesProperties securityPoliciesProperties,
                                              SecurityPoliciesRestrictionApplier<GetProcessDefinitionsPayload> processDefinitionRestrictionApplier,
                                              SecurityPoliciesRestrictionApplier<GetProcessInstancesPayload> processInstanceRestrictionApplier) {
        super(securityManager, securityPoliciesProperties);
        this.processDefinitionRestrictionApplier = processDefinitionRestrictionApplier;
        this.processInstanceRestrictionApplier = processInstanceRestrictionApplier;
    }

    public GetProcessDefinitionsPayload restrictProcessDefQuery(SecurityPolicyAccess securityPolicyAccess) {
        return restrictQuery(processDefinitionRestrictionApplier, securityPolicyAccess);
    }

    private Set<String> definitionKeysAllowedForApplicationPolicy(SecurityPolicyAccess securityPolicyAccess) {
        Map<String, Set<String>> restrictions = getAllowedKeys(securityPolicyAccess);
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


    public GetProcessInstancesPayload restrictProcessInstQuery(SecurityPolicyAccess securityPolicyAccess) {
        return restrictQuery(processInstanceRestrictionApplier, securityPolicyAccess);
    }

    private <T> T restrictQuery(SecurityPoliciesRestrictionApplier<T> restrictionApplier, SecurityPolicyAccess securityPolicyAccess) {
        if (!arePoliciesDefined()) {
            return restrictionApplier.allowAll();
        }

        Set<String> keys = definitionKeysAllowedForApplicationPolicy(securityPolicyAccess);

        if (keys != null && !keys.isEmpty()) {

            if (keys.contains(getSecurityPoliciesProperties().getWildcard())) {
                return restrictionApplier.allowAll();
            }

            return restrictionApplier.restrictToKeys(keys);
        }

        //policies are in place but if we've got here then none for this user
        if (!getSecurityPoliciesProperties().getPolicies().isEmpty()) {
            return restrictionApplier.denyAll();
        }

        return restrictionApplier.allowAll();
    }

    public boolean canWrite(String processDefinitionKey) {
        return hasPermission(processDefinitionKey, SecurityPolicyAccess.WRITE, applicationName);
    }

    public boolean canRead(String processDefinitionKey) {
        return hasPermission(processDefinitionKey, SecurityPolicyAccess.READ, applicationName)
                || hasPermission(processDefinitionKey, SecurityPolicyAccess.WRITE, applicationName);
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
