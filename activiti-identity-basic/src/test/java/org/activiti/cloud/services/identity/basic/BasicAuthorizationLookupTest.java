package org.activiti.cloud.services.identity.basic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BasicAuthorizationLookupTest {

    @InjectMocks
    private BasicAuthorizationLookup userRoleLookupProxy;

    @Mock
    private BasicIdentityLookup identityLookup;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userRoleLookupProxy.setAdminRoleName("admin");
    }

    @Test
    public void testGetRoles() {

        List<String> roles = new ArrayList<>();
        roles.add("role");

        when(identityLookup.getGroupsForCandidateUser("test"))
                .thenReturn(roles);

        assertThat(userRoleLookupProxy.getRolesForUser("test")).contains("role");
        assertThat(userRoleLookupProxy.isAdmin("test")).isFalse();
    }

    @Test
    public void testAdminRole() {

        List<String> roles = new ArrayList<>();
        roles.add("admin");

        when(identityLookup.getGroupsForCandidateUser("admin"))
                .thenReturn(roles);

        assertThat(userRoleLookupProxy.isAdmin("admin")).isTrue();
    }
}
