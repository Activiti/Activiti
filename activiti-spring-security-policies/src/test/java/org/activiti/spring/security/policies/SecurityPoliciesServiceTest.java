package org.activiti.spring.security.policies;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.activiti.spring.security.policies.conf.SecurityPoliciesProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SecurityPoliciesServiceTest {

    @InjectMocks
    private SecurityPoliciesService securityPoliciesService;

    @Mock
    private SecurityPoliciesProperties securityPoliciesProperties;

    private final String rb1 = "rb1";
    private final String rb2 = "rb2";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        HashMap<String, String> group = new HashMap<>();
        group.put("finance." + rb1 + ".policy.read",
                  "SimpleProcess1,SimpleProcess2");
        group.put("hr." + rb1 + ".policy.read",
                  "SimpleProcessYML1,SimpleProcessYML2");

        HashMap<String, String> user = new HashMap<>();
        user.put("jeff." + rb1 + ".policy.write",
                 "SimpleProcess");
        user.put("jeff." + rb2 + ".policy.write",
                 "SimpleProcess");
        user.put("fredslinehasanerror." + rb1 + ".policy.",
                 "SimpleProcess");
        user.put("jimhasnothing." + rb1 + ".policy.read",
                 "");
        user.put("bob." + rb1 + ".policy.read",
                 "TestProcess");

        when(securityPoliciesProperties.getGroup()).thenReturn(group);
        when(securityPoliciesProperties.getUser()).thenReturn(user);
    }

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
        assertThat(keys.get(rb2)).hasSize(1);
        assertThat(keys.get(rb2)).contains("SimpleProcess");
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
}
