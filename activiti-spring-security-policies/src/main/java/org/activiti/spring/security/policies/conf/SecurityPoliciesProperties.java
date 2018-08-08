package org.activiti.spring.security.policies.conf;

import org.activiti.spring.security.policies.SecurityPolicy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("activiti.security")
@Component
public class SecurityPoliciesProperties implements InitializingBean {

    private List<SecurityPolicy> policies = new ArrayList<>();

    private String wildcard = "*";


    public List<SecurityPolicy> getPolicies() {
        return policies;
    }

    public String getWildcard() {
        return wildcard;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // do nothing
    }
}