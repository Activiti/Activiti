package org.activiti.spring.security.policies;


import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.security.SecurityManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import static org.assertj.core.api.Assertions.assertThat;


public class BaseSecurityPoliciesApplicationServiceTest {

    @InjectMocks
    @Spy
    private SecurityPoliciesApplicationServiceImpl securityPoliciesApplicationService;

    @Mock
    private UserGroupManager userGroupManager;

    @Mock
    private SecurityManager securityManager;

    @Mock
    private SecurityPoliciesService securityPoliciesService;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldHavePermissionWhenNoPoliciesDefined(){
        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        assertThat(securityPoliciesApplicationService.canRead("key","rb1")).isTrue();

    }

    @Test
    public void shouldHavePermissionWhenDefIsInPolicy(){
        List<String> groups = Arrays.asList("hr");

        when(securityPoliciesService.policiesDefined()).thenReturn(true);

        when(securityManager.getAuthenticatedUserId()).thenReturn("bob");
        List<String> roles = Arrays.asList("user");
        when(userGroupManager.getUserRoles("bob")).thenReturn(roles);

        when(userGroupManager.getUserGroups("bob")).thenReturn(groups);
        Map<String,Set<String>> map = new HashMap<String,Set<String>>();
        map.put("rb1",new HashSet(Arrays.asList("key")));
        when(securityPoliciesService.getProcessDefinitionKeys("bob",groups, SecurityPolicy.WRITE)).thenReturn(map);
        when(securityPoliciesService.getProcessDefinitionKeys("bob",groups, SecurityPolicy.READ)).thenReturn(map);

        assertThat(securityPoliciesApplicationService.canRead("key","rb1")).isTrue();
        assertThat(securityPoliciesApplicationService.canWrite("key","rb1")).isTrue();
    }

    @Test
    public void shouldNotHavePermissionWhenDefIsNotInPolicy(){
        List<String> groups = Arrays.asList("hr");

        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(securityManager.getAuthenticatedUserId()).thenReturn("bob");
        List<String> roles = Arrays.asList("admin");
        when(userGroupManager.getUserRoles("admin")).thenReturn(roles);


        when(userGroupManager.getUserGroups("bob")).thenReturn(groups);
        Map<String,Set<String>> map = new HashMap<String,Set<String>>();
        map.put("rb1",new HashSet(Arrays.asList("key")));
        when(securityPoliciesService.getProcessDefinitionKeys("bob",groups, SecurityPolicy.WRITE)).thenReturn(map);
        when(securityPoliciesService.getProcessDefinitionKeys("bob",groups, SecurityPolicy.READ)).thenReturn(map);

        assertThat(securityPoliciesApplicationService.canRead("otherKey","rb1")).isFalse();
        assertThat(securityPoliciesApplicationService.canWrite("key","rb2")).isFalse();
    }


    @Test
    public void shouldHavePermissionWhenAdmin(){
        when(securityPoliciesService.policiesDefined()).thenReturn(true);
        when(securityManager.getAuthenticatedUserId()).thenReturn("admin");
        List<String> roles = Arrays.asList("admin");
        when(userGroupManager.getUserRoles("admin")).thenReturn(roles);

        assertThat(securityPoliciesApplicationService.canRead("key","rb1")).isTrue();
    }

    @Test
    public void shouldBeNoPoliciesOrNoUserWhenNoPolicies(){
        when(securityPoliciesService.policiesDefined()).thenReturn(false);
        assertThat(securityPoliciesApplicationService.noSecurityPoliciesOrNoUser()).isTrue();
    }

    @Test
    public void shouldBeNoPoliciesOrNoUserWhenNoUser(){
        when(securityManager.getAuthenticatedUserId()).thenReturn(null);
        assertThat(securityPoliciesApplicationService.noSecurityPoliciesOrNoUser()).isTrue();
    }
}
