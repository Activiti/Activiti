/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.test.services;

import com.activiti.domain.User;
import com.activiti.repository.ServerConfigRepository;
import com.activiti.repository.UserRepository;
import com.activiti.service.UserService;
import com.activiti.service.activiti.ServerConfigService;

import org.activiti.test.ApplicationTestConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * A test that does some CRUD operations to verify database is allright.
 * Needs more love, obviously.
 *
 * @author Joram Barrez
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationTestConfiguration.class)
public class CrudTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerConfigService serverConfigService;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @After
    public void cleanup() {
        serverConfigRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testCreateUser() {
        userService.createAdminUser("jos", "jos", "Jos", "Jossen", "jos@alfresco.com");

        List<User> users = userRepository.findAll();
        int count = users.size();
        for (User user : users) {
            if (user.getLogin().equals("jos")) {
                Assert.assertEquals("Jos", user.getFirstName());
                Assert.assertEquals("Jossen", user.getLastName());
                Assert.assertEquals("jos@alfresco.com", user.getEmail());
            }
        }

        userService.deleteUser("jos");
        Assert.assertEquals(count - 1, userRepository.findAll().size());
    }
//
//    @Test
//    public void testCreateServerConfig() {
//
//        long count = serverConfigRepository.count();
//        ServerConfigRepresentation clusterConfig = serverConfigService.createDefaultServerConfig("testCluster", "testClusterUser", "password", true);
//        Assert.assertEquals(count + 1, serverConfigRepository.count());
//
//        // Server config gets created when creating a new clusterconfig
//        List<ServerConfigRepresentation> serverConfigs = serverConfigService.findByClusterConfigId(clusterConfig.getId());
//        Assert.assertEquals(1, serverConfigs.size());
//
//        ServerConfigRepresentation serverConfig = serverConfigs.get(0);
//        Assert.assertEquals("http://localhost", serverConfig.getServerAddress());
//        Assert.assertEquals(9999L, serverConfig.getServerPort().longValue());
//        Assert.assertEquals("Activiti app", serverConfig.getName());
//        Assert.assertEquals("activiti-app", serverConfig.getContextRoot());
//        Assert.assertEquals("admin@app.activiti.com", serverConfig.getUserName());
//        Assert.assertEquals("api", serverConfig.getRestRoot());
//        Assert.assertNotEquals("admin", serverConfig.getPassword()); // Password should be encrypted!
//
//    }

}
