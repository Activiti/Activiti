package org.activiti.cloud.services.identity.basic;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BasicIdentityLookupTest {

    @InjectMocks
    private BasicIdentityLookup identityLookup;

    @Mock
    private UserDetailsService userDetailsService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetGroups() {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("testrole"));
        User user = new User("test",
                             "pass",
                             authorities);
        when(userDetailsService.loadUserByUsername("test"))
                .thenReturn(user);

        assertThat(identityLookup.getGroupsForCandidateUser("test")).contains("testrole");
    }
}
