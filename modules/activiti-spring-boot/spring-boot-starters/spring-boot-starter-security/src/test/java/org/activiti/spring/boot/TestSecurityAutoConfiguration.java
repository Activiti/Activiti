package org.activiti.spring.boot;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @author Josh Long
 */
public class TestSecurityAutoConfiguration {

    private ConfigurableApplicationContext applicationContext;

    @Configuration
    @EnableAutoConfiguration
    public static class SecurityConfiguration {

        @Bean
        public CommandLineRunner seedUsersAndGroups(final RuntimeService runtimeService, final IdentityService identityService) {
            return new CommandLineRunner() {
                @Override
                public void run(String... strings) throws Exception {


                    // install groups & users
                    Group group = identityService.newGroup("user");
                    group.setName("users");
                    group.setType("security-role");
                    identityService.saveGroup(group);

                    User joram = identityService.newUser("jbarrez");
                    joram.setFirstName("Joram");
                    joram.setLastName("Barrez");
                    joram.setPassword("joram");
                    identityService.saveUser(joram);

                    User josh = identityService.newUser("jlong");
                    josh.setFirstName("Josh");
                    josh.setLastName("Long");
                    josh.setPassword("josh");
                    identityService.saveUser(josh);

                    identityService.createMembership("jbarrez", "user");
                    identityService.createMembership("jlong", "user");

                }
            };
        }

        @Bean
        public TaskExecutor taskExecutor() {
            return new SimpleAsyncTaskExecutor();
        }

        @Bean
        DataSource dataSource() {
            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setUsername("sa");
            basicDataSource.setUrl("jdbc:h2:mem:activiti");
            basicDataSource.setDefaultAutoCommit(false);
            basicDataSource.setDriverClassName(org.h2.Driver.class.getName());
            basicDataSource.setPassword("");
            return basicDataSource;
        }

        @Bean
        PlatformTransactionManager dataSourceTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }


    @After
    public void close() {
        this.applicationContext.close();
    }

    @Before
    public void setUp() {
        this.applicationContext = SpringApplication.run(SecurityConfiguration.class);
    }

    @Test
    public void testSecurityIntegration() throws Exception {
        UserDetailsService userDetailsService = this.applicationContext.getBean(UserDetailsService.class);
        Assert.assertNotNull("the userDetailsService manager can't be null", userDetailsService);
        UserDetails userDetails = userDetailsService.loadUserByUsername("jlong");
        Assert.assertNotNull("userDetails for 'jlong' should not be null", userDetails);
    }
}
