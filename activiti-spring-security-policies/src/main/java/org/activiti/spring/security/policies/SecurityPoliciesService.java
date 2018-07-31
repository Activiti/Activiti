package org.activiti.spring.security.policies;

import org.activiti.spring.security.policies.conf.SecurityPoliciesProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Component
public class SecurityPoliciesService {

    private SecurityPoliciesProperties securityPoliciesProperties;

    @Value("${activiti.security.policies.wildcard:*}")
    private String wildcard;

    @Autowired
    public SecurityPoliciesService(SecurityPoliciesProperties securityPoliciesProperties) {
        this.securityPoliciesProperties = securityPoliciesProperties;
    }

    public boolean policiesDefined() {
        return ((securityPoliciesProperties.getGroup() != null && !securityPoliciesProperties.getGroup().isEmpty()) || (securityPoliciesProperties.getUser() != null && !securityPoliciesProperties.getUser().isEmpty()));
    }


    public Map<String, Set<String>> getProcessDefinitionKeys(String userId, Collection<String> groups, Collection<SecurityPolicy> policyLevels) {


        Map<String, Set<String>> procDefKeysByRB = new HashMap<String, Set<String>>();

        if (groups != null) {
            for (String group : groups) {
                getProcDefKeysForUserOrGroup(policyLevels, procDefKeysByRB, group, securityPoliciesProperties.getGroup());
            }
        }

        getProcDefKeysForUserOrGroup(policyLevels, procDefKeysByRB, userId, securityPoliciesProperties.getUser());

        return procDefKeysByRB;

    }

    private void getProcDefKeysForUserOrGroup(Collection<SecurityPolicy> policyLevels, Map<String, Set<String>> procDefKeys, String userOrGroup, Map<String, String> policies) {

        if (userOrGroup == null || policies == null) {
            return;
        }

        // iterate through the properties either by user or group (already pre-filtered)

        for (String key : policies.keySet()) {

            if (keyMatchesUserOrGroup(userOrGroup, key)) {

                for (SecurityPolicy policyLevel : policyLevels) {

                    if (keyMatchesPolicyLevel(key, policyLevel)) {

                        addFilterKeysToMap(policies.get(key), key, procDefKeys);
                    }
                }
            }
        }
    }

    private boolean keyMatchesPolicyLevel(String key, SecurityPolicy policyLevel) {
        return policyLevel != null && key.toLowerCase().endsWith("policy." + policyLevel.name().toLowerCase()); //note . at beginning
    }

    private boolean keyMatchesUserOrGroup(String userOrGroup, String key) {
        return key != null && key.replace("-", "").toLowerCase().startsWith(userOrGroup.replace("-", "").toLowerCase() + "."); //note . at end
    }

    private void addFilterKeysToMap(String propertyValue, String key, Map<String, Set<String>> procDefKeys) {

        String appName = extractAppNameFromKey(key);

        if (propertyValue != null && propertyValue.contains(",")) {

            addToMap(new HashSet(Arrays.asList(propertyValue.split(","))), appName, procDefKeys);

        } else if (propertyValue != null && !propertyValue.isEmpty()) {

            addToMap(new HashSet(Arrays.asList(propertyValue)), appName, procDefKeys);
        }

    }

    private void addToMap(Set<String> collectionToAdd, String key, Map<String, Set<String>> map) {
        Set<String> entries = map.get(key);

        if (entries == null) {
            entries = collectionToAdd;
        } else {
            entries.addAll(collectionToAdd);
        }

        map.put(key, (Set) entries);
    }


    public String extractAppNameFromKey(String key) {
        //should be between first and second dot
        int firstDot = key.indexOf('.');
        int secondDot = key.indexOf('.', firstDot + 1);
        return key.substring(firstDot + 1, secondDot);
    }

    public Map<String, Set<String>> getProcessDefinitionKeys(String userId, Collection<String> groups, SecurityPolicy minPolicyLevel) {
        if (minPolicyLevel != null && minPolicyLevel.equals(SecurityPolicy.READ)) {
            return getProcessDefinitionKeys(userId, groups, Arrays.asList(SecurityPolicy.READ, SecurityPolicy.WRITE));
        }
        return getProcessDefinitionKeys(userId, groups, Arrays.asList(minPolicyLevel));
    }

    public String getWildcard() {
        return wildcard;
    }
}