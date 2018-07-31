package org.activiti.spring.security;

import java.util.Arrays;

import org.activiti.runtime.api.identity.ActivitiUser;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.security.SecurityManager;
import org.activiti.spring.identity.ActivitiUserImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdentityAndSecurityManagersTest {

    @Autowired
    private UserGroupManager userGroupManager;

    @Autowired
    private SecurityManager securityManager;

    @Test
    public void createTwoUserAndSwitchAuth() {

        String noUserAuthenticated = securityManager.getAuthenticatedUserId();
        assertThat(noUserAuthenticated).isEmpty();

        ActivitiUser salaboy = userGroupManager.create("salaboy",
                                                       "password",
                                                       Arrays.asList("activitiTeam"),
                                                       Arrays.asList("user",
                                                                     "admin"));

        assertThat(salaboy.getGroupIds()).hasSize(1);
        assertThat(salaboy.getRoles()).hasSize(2);
        assertThat(((User) salaboy).getAuthorities()).hasSize(3);

        ActivitiUser garth = userGroupManager.create("Garth",
                                                     "darkplace",
                                                     Arrays.asList("doctor"),
                                                     Arrays.asList("user"));

        assertThat(garth.getGroupIds()).hasSize(1);
        assertThat(garth.getRoles()).hasSize(1);
        assertThat(((User) garth).getAuthorities()).hasSize(2);

        salaboy = userGroupManager.loadUser("salaboy");

        assertThat(salaboy.getGroupIds()).hasSize(1);
        assertThat(salaboy.getRoles()).hasSize(2);
        assertThat(((User) salaboy).getAuthorities()).hasSize(3);

        garth = userGroupManager.loadUser("garth");

        assertThat(garth.getGroupIds()).hasSize(1);
        assertThat(garth.getRoles()).hasSize(1);
        assertThat(((User) garth).getAuthorities()).hasSize(2);

        securityManager.authenticate(salaboy);

        String authenticatedUserId = securityManager.getAuthenticatedUserId();

        assertThat(authenticatedUserId).isEqualTo(salaboy.getUsername());

        securityManager.authenticate(garth);

        authenticatedUserId = securityManager.getAuthenticatedUserId();

        assertThat(authenticatedUserId).isEqualTo(garth.getUsername());

        Exception exception = null;
        try {
            securityManager.authenticate(new ActivitiUserImpl("sanchez", "password", null, null));
        }catch(Exception ex){
            exception = ex;
        }
        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).contains("Invalid user: User Doesn't exist!");


    }
}
