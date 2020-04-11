package org.activiti.core.common.spring.identity;

import java.util.List;

import org.activiti.core.common.spring.identity.ExtendedInMemoryUserDetailsManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ExtendedInMemoryUserDetailsManagerIT {

    @Autowired
    private ExtendedInMemoryUserDetailsManager extendedInMemoryUserDetailsManager;

    @Test
    public void checkGroupAuthorities() {
        List<String> groups = extendedInMemoryUserDetailsManager.getGroups();
        assertThat(groups).isNotNull();
        groups.stream().forEach(x -> assertThat(x).contains("GROUP"));
    }

    @Test
    public void checkUsers() {
        List<String> users = extendedInMemoryUserDetailsManager.getUsers();
        assertThat(users).isNotNull();
        assertThat(users.size() > 1).isTrue();
        String adminUser = users.stream().filter(x -> x.equals("admin")).findFirst().get();
        assertThat(adminUser).isNotNull();
    }
}
