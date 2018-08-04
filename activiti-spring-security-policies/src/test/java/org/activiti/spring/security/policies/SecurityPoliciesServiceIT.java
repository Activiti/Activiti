package org.activiti.spring.security.policies;

import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.security.SecurityManager;
import org.activiti.spring.security.policies.conf.SecurityPoliciesProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:propstest.properties")
@ContextConfiguration
public class SecurityPoliciesServiceIT {

    @Autowired
    private ProcessSecurityPoliciesManager processSecurityPoliciesManager;

    @Autowired
    private SecurityPoliciesProperties securityPoliciesProperties;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private UserGroupManager userGroupManager;

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

        assertThat(policies.get(2).getAccess()).isEqualTo(SecurityPolicyAccess.READ);

        assertThat(policies.get(2).getKeys()).hasSize(1);

        assertThat(policies.get(2).getKeys()).contains(securityPoliciesProperties.getWildcard());


    }


    @Test
    public void shouldBePoliciesDefined() throws Exception {
        assertThat(processSecurityPoliciesManager.arePoliciesDefined()).isTrue();
        assertThat(!securityPoliciesProperties.getPolicies().isEmpty()).isTrue();
        assertThat(securityPoliciesProperties.getPolicies()).hasSize(2);
    }

    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void shouldGetPoliciesForSalaboy() throws Exception {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isEqualTo("salaboy");

        List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
        assertThat(userGroups).hasSize(2);
        assertThat(userGroups).contains("developers", "activitiTeam");

        Map<String, Set<String>> keys = processSecurityPoliciesManager.getAllowedKeys(SecurityPolicyAccess.WRITE,
                SecurityPolicyAccess.READ);
        assertThat(keys.keySet()).hasSize(2);

        assertThat(keys.get("runtime-bundle")).hasSize(2);
        assertThat(keys.get("runtime-bundle")).contains("SampleProcess1", "SampleProcess2");
        assertThat(keys.get("application")).hasSize(0);
    }


    @Test
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void shouldNotGetKeysForWrite() throws Exception {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isEqualTo("garth");

        List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
        assertThat(userGroups).hasSize(1);
        assertThat(userGroups).contains("doctor");

        Map<String, Set<String>> keys = processSecurityPoliciesManager.getAllowedKeys(SecurityPolicyAccess.WRITE);
        assertThat(keys.keySet()).hasSize(2);
        assertThat(keys.get("application")).hasSize(0);
        assertThat(keys.get("runtime-bundle")).hasSize(0);
    }

    @Test
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void shouldGetKeysForRead() throws Exception {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isEqualTo("garth");

        List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
        assertThat(userGroups).hasSize(1);
        assertThat(userGroups).contains("doctor");

        Map<String, Set<String>> keys = processSecurityPoliciesManager.getAllowedKeys(SecurityPolicyAccess.READ);

        assertThat(keys.keySet()).hasSize(2);
        assertThat(keys.get("application")).hasSize(2);
        assertThat(keys.get("application")).contains("SampleProcess2", "SampleProcess3");
        assertThat(keys.get("runtime-bundle")).hasSize(0);

    }

    @Test
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void shouldNotGetAnyKey() throws Exception {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isEqualTo("garth");

        List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
        assertThat(userGroups).hasSize(1);
        assertThat(userGroups).contains("doctor");

        Map<String, Set<String>> keys = processSecurityPoliciesManager.getAllowedKeys();

        assertThat(keys.keySet()).hasSize(2);
        assertThat(keys.get("application")).hasSize(0);
        assertThat(keys.get("runtime-bundle")).hasSize(0);

    }


    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "myUserDetailsService")
    public void shouldGetAllTheKeysForAdmin() throws Exception {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isEqualTo("admin");


        Map<String, Set<String>> keys = processSecurityPoliciesManager.getAllowedKeys();

        assertThat(keys.keySet()).hasSize(2);
        assertThat(keys.get("application")).hasSize(2);
        assertThat(keys.get("runtime-bundle")).hasSize(2);


    }

//    @Test
//    public void shouldGetProcessDefsByUserAndMinPolicy() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("jEff",
//                null,
//                SecurityPolicyAccess.READ);
//
//        assertThat(keys.get(rb1)).hasSize(1);
//        assertThat(keys.get(rb1)).contains("SimpleProcess");
//
//        //write as min policy should work too for this case
//        keys = securityPoliciesService.getProcessDefinitionKeys("jEff",
//                null,
//                SecurityPolicyAccess.WRITE);
//
//        assertThat(keys.get(rb1)).contains("SimpleProcess");
//    }
//
//    @Test
//    public void shouldGetProcessDefsByGroupAndPolicies() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
//                Arrays.asList("finance"),
//                Arrays.asList(SecurityPolicyAccess.READ));
//
//        assertThat(keys.get(rb1)).hasSize(2);
//        assertThat(keys.get(rb1)).contains("SimpleProcess1",
//                "SimpleProcess2");
//    }
//
//    @Test
//    public void shouldGetProcessDefsByGroupsAndMinPolicy() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
//                Arrays.asList("finance",
//                        "nonexistent"),
//                SecurityPolicyAccess.READ);
//
//        assertThat(keys.get(rb1)).hasSize(2);
//        assertThat(keys.get(rb1)).contains("SimpleProcess1",
//                "SimpleProcess2");
//    }
//
//    @Test
//    public void shouldNotGetProcessDefsForGroupWithoutDefs() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
//                Arrays.asList("hrbitlikerealgroupbutnot",
//                        "nonexistent"),
//                SecurityPolicyAccess.READ);
//
//        assertThat(keys.get(rb1)).isNullOrEmpty();
//    }
//
//    @Test
//    public void shouldNotGetProcessDefsWithoutUserOrGroup() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
//                null,
//                Arrays.asList(SecurityPolicyAccess.WRITE));
//
//        assertThat(keys.get(rb1)).isNullOrEmpty();
//    }
//
//    @Test
//    public void shouldNotGetProcessDefsWithoutPolicyLevels() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
//                Arrays.asList("finance"),
//                new HashSet<>());
//
//        assertThat(keys.get(rb1)).isNullOrEmpty();
//    }
//
//    @Test
//    public void shouldNotGetProcessDefsWhenEntryMissingPolicyLevels() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("fredslinehasanerror",
//                null,
//                SecurityPolicyAccess.READ);
//        assertThat(keys.get(rb1)).isNullOrEmpty();
//    }
//
//    @Test
//    public void shouldNotGetProcessDefsWhenEntryMissingProcDefKeys() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("jimhasnothing",
//                null,
//                SecurityPolicyAccess.READ);
//        assertThat(keys.get(rb1)).isNullOrEmpty();
//    }
//
//    //cases from YAML
//    @Test
//    public void shouldGetProcessDefsByUserAndPoliciesYml() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("bOb",
//                null,
//                Arrays.asList(SecurityPolicyAccess.WRITE,
//                        SecurityPolicyAccess.READ));
//
//        assertThat(keys.get(rb1)).hasSize(1);
//        assertThat(keys.get(rb1)).contains("TestProcess");
//    }
//
//    @Test
//    public void shouldGetProcessDefsByGroupAndPoliciesYml() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
//                Arrays.asList("hr"),
//                Arrays.asList(SecurityPolicyAccess.READ));
//
//        assertThat(keys.get(rb1)).hasSize(2);
//        assertThat(keys.get(rb1)).contains("SimpleProcessYML1");
//        assertThat(keys.get(rb1)).contains("SimpleProcessYML2");
//    }
//
//    @Test
//    public void shouldGetWildcardByUserAndPoliciesIgnoringHyphens() throws Exception {
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("jim-bob",
//                null,
//                Arrays.asList(SecurityPolicyAccess.WRITE,
//                        SecurityPolicyAccess.READ));
//
//        assertThat(keys.get(rb1)).hasSize(1);
//        assertThat(keys.get(rb1)).contains("*");
//    }
//
//    @Test
//    @Ignore
//    //@TODO: check with Ryan
//    public void shouldGetWildcardByGroupsAndMinPolicy() throws Exception {
//
//        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
//                Arrays.asList("accounts",
//                        "nonexistent"),
//                SecurityPolicyAccess.READ);
//
//        assertThat(keys.get(rb1)).hasSize(1);
//        assertThat(keys.get(rb1)).contains(securityPoliciesService.getWildcard());
//    }
}
