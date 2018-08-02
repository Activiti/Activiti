package org.activiti.spring.security.policies;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:propstest.properties")
public class SecurityPoliciesServiceIT {

    private final String rb1 = "runtime-bundle";

    @Autowired
    private SecurityPoliciesService securityPoliciesService;



    @Test
    public void shouldBePoliciesDefined() throws Exception {
        assertThat(securityPoliciesService.policiesDefined()).isTrue();
    }

    @Test
    public void shouldGetProcessDefsByUserAndPolicies() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("jEff",
                null,
                Arrays.asList(SecurityPolicy.WRITE,
                        SecurityPolicy.READ));

        assertThat(keys.get(rb1)).hasSize(1);
        assertThat(keys.get(rb1)).contains("SimpleProcess");
    }

    @Test
    public void shouldGetProcessDefsByUserAndMinPolicy() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("jEff",
                null,
                SecurityPolicy.READ);

        assertThat(keys.get(rb1)).hasSize(1);
        assertThat(keys.get(rb1)).contains("SimpleProcess");

        //write as min policy should work too for this case
        keys = securityPoliciesService.getProcessDefinitionKeys("jEff",
                null,
                SecurityPolicy.WRITE);

        assertThat(keys.get(rb1)).contains("SimpleProcess");
    }

    @Test
    public void shouldGetProcessDefsByGroupAndPolicies() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
                Arrays.asList("finance"),
                Arrays.asList(SecurityPolicy.READ));

        assertThat(keys.get(rb1)).hasSize(2);
        assertThat(keys.get(rb1)).contains("SimpleProcess1",
                "SimpleProcess2");
    }

    @Test
    public void shouldGetProcessDefsByGroupsAndMinPolicy() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
                Arrays.asList("finance",
                        "nonexistent"),
                SecurityPolicy.READ);

        assertThat(keys.get(rb1)).hasSize(2);
        assertThat(keys.get(rb1)).contains("SimpleProcess1",
                "SimpleProcess2");
    }

    @Test
    public void shouldNotGetProcessDefsForGroupWithoutDefs() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
                Arrays.asList("hrbitlikerealgroupbutnot",
                        "nonexistent"),
                SecurityPolicy.READ);

        assertThat(keys.get(rb1)).isNullOrEmpty();
    }

    @Test
    public void shouldNotGetProcessDefsWithoutUserOrGroup() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
                null,
                Arrays.asList(SecurityPolicy.WRITE));

        assertThat(keys.get(rb1)).isNullOrEmpty();
    }

    @Test
    public void shouldNotGetProcessDefsWithoutPolicyLevels() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
                Arrays.asList("finance"),
                new HashSet<>());

        assertThat(keys.get(rb1)).isNullOrEmpty();
    }

    @Test
    public void shouldNotGetProcessDefsWhenEntryMissingPolicyLevels() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("fredslinehasanerror",
                null,
                SecurityPolicy.READ);
        assertThat(keys.get(rb1)).isNullOrEmpty();
    }

    @Test
    public void shouldNotGetProcessDefsWhenEntryMissingProcDefKeys() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("jimhasnothing",
                null,
                SecurityPolicy.READ);
        assertThat(keys.get(rb1)).isNullOrEmpty();
    }

    //cases from YAML
    @Test
    public void shouldGetProcessDefsByUserAndPoliciesYml() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("bOb",
                null,
                Arrays.asList(SecurityPolicy.WRITE,
                        SecurityPolicy.READ));

        assertThat(keys.get(rb1)).hasSize(1);
        assertThat(keys.get(rb1)).contains("TestProcess");
    }

    @Test
    public void shouldGetProcessDefsByGroupAndPoliciesYml() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
                Arrays.asList("hr"),
                Arrays.asList(SecurityPolicy.READ));

        assertThat(keys.get(rb1)).hasSize(2);
        assertThat(keys.get(rb1)).contains("SimpleProcessYML1");
        assertThat(keys.get(rb1)).contains("SimpleProcessYML2");
    }

    @Test
    public void shouldGetWildcardByUserAndPoliciesIgnoringHyphens() throws Exception {
        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys("jim-bob",
                null,
                Arrays.asList(SecurityPolicy.WRITE,
                        SecurityPolicy.READ));

        assertThat(keys.get(rb1)).hasSize(1);
        assertThat(keys.get(rb1)).contains("*");
    }

    @Test
    @Ignore
    //@TODO: check with Ryan
    public void shouldGetWildcardByGroupsAndMinPolicy() throws Exception {

        Map<String, Set<String>> keys = securityPoliciesService.getProcessDefinitionKeys(null,
                Arrays.asList("accounts",
                        "nonexistent"),
                SecurityPolicy.READ);

        assertThat(keys.get(rb1)).hasSize(1);
        assertThat(keys.get(rb1)).contains(securityPoliciesService.getWildcard());
    }
}
