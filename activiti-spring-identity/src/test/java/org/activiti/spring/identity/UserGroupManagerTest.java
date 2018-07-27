package org.activiti.spring.identity;

import java.util.Arrays;
import java.util.List;

import org.activiti.runtime.api.identity.ActivitiUser;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserGroupManagerTest {

    @Autowired
    private UserGroupManager userGroupManager;

    @Test
    public void createTwoUserAndSwitchAuth() {

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
        List<String> salaboyGroups = userGroupManager.getUserGroups("salaboy");
        assertThat(salaboyGroups).hasSize(1);
        assertThat(salaboyGroups).contains("activitiTeam");
        List<String> salaboyRoles = userGroupManager.getUserRoles("salaboy");
        assertThat(salaboyRoles).hasSize(2);
        assertThat(salaboyRoles).contains("user", "admin");

        assertThat(salaboy.getGroupIds()).hasSize(1);
        assertThat(salaboy.getRoles()).hasSize(2);
        assertThat(((User) salaboy).getAuthorities()).hasSize(3);

        garth = userGroupManager.loadUser("garth");

        List<String> garthGroups = userGroupManager.getUserGroups("garth");
        assertThat(garthGroups).hasSize(1);
        assertThat(garthGroups).contains("doctor");
        List<String> garthRoles = userGroupManager.getUserRoles("garth");
        assertThat(garthRoles).hasSize(1);
        assertThat(garthRoles).contains("user");

        assertThat(garth.getGroupIds()).hasSize(1);
        assertThat(garth.getRoles()).hasSize(1);
        assertThat(((User) garth).getAuthorities()).hasSize(2);
    }
}
