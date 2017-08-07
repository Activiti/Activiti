package org.activiti.tasks;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.activiti.engine.IdentityService;
import org.activiti.utils.KeycloakUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@DatabaseSetup("/META-INF/dbtest/identity-data.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
public class MigrateUsersAndGroupsServiceTest {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private KeycloakUtil keycloakUtil;

    @Autowired
    private MigrateUsersAndGroups migrateUsersAndGroups;

    @Before
    public void setUp() {
        assertNotNull(identityService);
        assertThat(identityService.createGroupQuery().list().size(), is(5));
        assertThat(identityService.createUserQuery().list().size(), is(5));

        //migrate users and groups
        migrateUsersAndGroups.migrate();
    }

    @Test
    public void duplicatedUse() {

        Response response = keycloakUtil.createUser("developer");
        assertThat(409,is(response.getStatus()));
        response.close();
        
        assertThat(7,is(keycloakUtil.getAllGroupsSize()));

    }

}
