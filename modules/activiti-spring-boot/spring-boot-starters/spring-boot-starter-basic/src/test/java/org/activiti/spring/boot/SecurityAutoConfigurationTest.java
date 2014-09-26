package org.activiti.spring.boot;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.Assert.assertEquals;

/**
 * @author Josh Long
 */
public class SecurityAutoConfigurationTest {
    private AnnotationConfigApplicationContext applicationContext;

    @After
    public void close() {
        this.applicationContext.close();
    }

    @Test
    public void userDetailsService() throws Throwable {

        this.applicationContext = new AnnotationConfigApplicationContext();
        this.applicationContext.register(SecurityConfiguration.class);
        this.applicationContext.refresh();
        UserDetailsService userDetailsService = this.applicationContext.getBean(UserDetailsService.class);
        Assert.assertNotNull("the userDetailsService should not be null", userDetailsService);
        assertEquals("there should only be 1 authority", userDetailsService.loadUserByUsername("jlong").getAuthorities().size(), 1);
        assertEquals("there should be 2 authorities", userDetailsService.loadUserByUsername("jbarrez").getAuthorities().size(), 2);
    }

    @Configuration
    @Import({DataSourceAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.DataSourceConfiguration.class,
            org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class,
            SecurityAutoConfiguration.class})
    public static class SecurityConfiguration {

        @Autowired
        private IdentityService identityService;

        protected User user(String userName, String f, String l) {
            User u = identityService.newUser(userName);
            u.setFirstName(f);
            u.setLastName(l);
            u.setPassword("password");
            identityService.saveUser(u);
            return u;
        }

        protected Group group(String groupName) {
            Group group = identityService.newGroup(groupName);
            group.setName(groupName);
            group.setType("security-role");
            identityService.saveGroup(group);
            return group;
        }

        @Bean
        InitializingBean init(
                final IdentityService identityService) {
            return new InitializingBean() {
                @Override
                public void afterPropertiesSet() throws Exception {

                    // install groups & users
                    Group userGroup = group("user");
                    Group adminGroup = group("admin");

                    User joram = user("jbarrez", "Joram", "Barrez");
                    identityService.createMembership(joram.getId(), userGroup.getId());
                    identityService.createMembership(joram.getId(), adminGroup.getId());

                    User josh = user("jlong", "Josh", "Long");
                    identityService.createMembership(josh.getId(), userGroup.getId());
                }
            };
        }

    }
}
