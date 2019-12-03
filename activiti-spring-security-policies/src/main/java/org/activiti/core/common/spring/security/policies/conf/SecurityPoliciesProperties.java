package org.activiti.core.common.spring.security.policies.conf;

import org.activiti.core.common.spring.security.policies.SecurityPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("activiti.security")
public class SecurityPoliciesProperties {

    private List<SecurityPolicy> policies = new ArrayList<>();

    private String wildcard = "*";


    public List<SecurityPolicy> getPolicies() {
        return policies;
    }

    public String getWildcard() {
        return wildcard;
    }

}