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

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.core.common.spring.security.policies.conf.SecurityPoliciesProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class SecurityPoliciesServiceIT {

    @Autowired
    private ProcessSecurityPoliciesManager processSecurityPoliciesManager;

    @Autowired
    private SecurityPoliciesProperties securityPoliciesProperties;

    @Autowired
    private SecurityManager securityManager;

    @Test
    public void basicParsingTest() {
        List<SecurityPolicy> policies =
                securityPoliciesProperties.getPolicies();

        assertThat(policies).isNotNull();
        assertThat(policies).hasSize(3);

        assertThat(policies.get(0).getName()).isEqualTo("My Policy");

        assertThat(policies.get(0).getServiceName()).isEqualTo("runtime-bundle");

        assertThat(policies.get(0).getUsers()).hasSize(3);

        assertThat(policies.get(0).getGroups()).hasSize(2);

        assertThat(policies.get(0).getAccess()).isEqualTo(SecurityPolicyAccess.WRITE);

        assertThat(policies.get(0).getKeys()).hasSize(2);

        assertThat(policies.get(0).getKeys()).contains("SampleProcess1", "SampleProcess2");

        assertThat(policies.get(1).getName()).isEqualTo("Other Policy");

        assertThat(policies.get(1).getServiceName()).isEqualTo("application");

        assertThat(policies.get(1).getUsers()).hasSize(1);

        assertThat(policies.get(1).getGroups()).hasSize(2);

        assertThat(policies.get(1).getAccess()).isEqualTo(SecurityPolicyAccess.READ);

        assertThat(policies.get(1).getKeys()).hasSize(2);

        assertThat(policies.get(1).getKeys()).contains("SampleProcess2", "SampleProcess3");


        assertThat(policies.get(2).getName()).isEqualTo("Policy with Wildcard");

        assertThat(policies.get(2).getServiceName()).isEqualTo("default");

        assertThat(policies.get(2).getUsers()).hasSize(1);

        assertThat(policies.get(2).getGroups()).hasSize(1);

        assertThat(policies.get(2).getAccess()).isEqualTo(SecurityPolicyAccess.WRITE);

        assertThat(policies.get(2).getKeys()).hasSize(1);

        assertThat(policies.get(2).getKeys()).contains(securityPoliciesProperties.getWildcard());


    }


    @Test
    public void shouldBePoliciesDefined() {
        assertThat(processSecurityPoliciesManager.arePoliciesDefined()).isTrue();
        assertThat(!securityPoliciesProperties.getPolicies().isEmpty()).isTrue();
        assertThat(securityPoliciesProperties.getPolicies()).hasSize(3);
    }

    @Test
    @WithUserDetails(value = "bob", userDetailsServiceBeanName = "myUserDetailsService")
    public void shouldGetPoliciesForUser() {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isEqualTo("bob");

        List<String> userGroups = securityManager.getAuthenticatedUserGroups();
        assertThat(userGroups).hasSize(2);
        assertThat(userGroups).contains("developers", "activitiTeam");

        Map<String, Set<String>> keys = processSecurityPoliciesManager.getAllowedKeys(SecurityPolicyAccess.WRITE,
                SecurityPolicyAccess.READ);
        assertThat(keys.keySet()).hasSize(3);

        assertThat(keys.get("runtime-bundle")).hasSize(2);
        assertThat(keys.get("runtime-bundle")).contains("SampleProcess1", "SampleProcess2");
        assertThat(keys.get("application")).hasSize(0);
        assertThat(keys.get("default")).hasSize(1);
        assertThat(keys.get("default")).contains(securityPoliciesProperties.getWildcard());
    }


    @Test
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void shouldNotGetKeysForWrite() {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isEqualTo("garth");

        List<String> userGroups = securityManager.getAuthenticatedUserGroups();
        assertThat(userGroups).hasSize(1);
        assertThat(userGroups).contains("doctor");

        Map<String, Set<String>> keys = processSecurityPoliciesManager.getAllowedKeys(SecurityPolicyAccess.WRITE);
        assertThat(keys.keySet()).hasSize(3);
        assertThat(keys.get("application")).hasSize(0);
        assertThat(keys.get("runtime-bundle")).hasSize(0);
        assertThat(keys.get("default")).hasSize(0);
    }

    @Test
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void shouldGetKeysForRead() {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isEqualTo("garth");

        List<String> userGroups = securityManager.getAuthenticatedUserGroups();
        assertThat(userGroups).hasSize(1);
        assertThat(userGroups).contains("doctor");

        Map<String, Set<String>> keys = processSecurityPoliciesManager.getAllowedKeys(SecurityPolicyAccess.READ);

        assertThat(keys.keySet()).hasSize(3);
        assertThat(keys.get("application")).hasSize(2);
        assertThat(keys.get("application")).contains("SampleProcess2", "SampleProcess3");
        assertThat(keys.get("runtime-bundle")).hasSize(0);
        assertThat(keys.get("default")).hasSize(0);

    }

    @Test
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void shouldNotGetAnyKey() {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isEqualTo("garth");

        List<String> userGroups = securityManager.getAuthenticatedUserGroups();
        assertThat(userGroups).hasSize(1);
        assertThat(userGroups).contains("doctor");

        Map<String, Set<String>> keys = processSecurityPoliciesManager.getAllowedKeys();

        assertThat(keys.keySet()).hasSize(3);
        assertThat(keys.get("application")).hasSize(0);
        assertThat(keys.get("runtime-bundle")).hasSize(0);
        assertThat(keys.get("default")).hasSize(0);

    }

}
