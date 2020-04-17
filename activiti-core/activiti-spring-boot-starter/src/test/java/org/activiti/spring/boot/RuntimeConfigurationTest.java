package org.activiti.spring.boot;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class RuntimeConfigurationTest {


    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private UserGroupManager userGroupManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SecurityUtil securityUtil;

    @Test
    public void validatingConfigurationForUser() {
        securityUtil.logInAs("user");

        UserDetails userDetails = userDetailsService.loadUserByUsername("user");
        assertThat(userDetails).isNotNull();

        assertThat(userDetails.getAuthorities()).hasSize(2);

        List<String> userRoles = userGroupManager.getUserRoles("user");
        assertThat(userRoles).isNotNull();
        assertThat(userRoles).hasSize(1);
        assertThat(userRoles.get(0)).isEqualTo("ACTIVITI_USER");
        List<String> userGroups = userGroupManager.getUserGroups("user");
        assertThat(userGroups).isNotNull();
        assertThat(userGroups).hasSize(1);
        assertThat(userGroups.get(0)).isEqualTo("activitiTeam");
    }

    @Test
    public void validatingConfigurationForAdmin() {
        securityUtil.logInAs("admin");

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
        assertThat(userDetails).isNotNull();

        assertThat(userDetails.getAuthorities()).hasSize(1);

        List<String> userRoles = userGroupManager.getUserRoles("admin");
        assertThat(userRoles).isNotNull();
        assertThat(userRoles).hasSize(1);
        assertThat(userRoles.get(0)).isEqualTo("ACTIVITI_ADMIN");
        List<String> userGroups = userGroupManager.getUserGroups("admin");
        assertThat(userGroups).isNotNull();
        assertThat(userGroups).hasSize(0);

    }

}
