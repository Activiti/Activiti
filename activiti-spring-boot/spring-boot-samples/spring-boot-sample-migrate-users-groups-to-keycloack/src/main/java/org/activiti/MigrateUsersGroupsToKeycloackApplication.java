package org.activiti;

import org.activiti.tasks.MigrateUsersAndGroups;
import org.activiti.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class MigrateUsersGroupsToKeycloackApplication {

    @Autowired
    private MigrateUsersAndGroups migrateUsersAndGroups;

    public static void main(String[] args) {
        SpringApplication.run(MigrateUsersGroupsToKeycloackApplication.class, args).close();
    }

    @Bean
    @Profile("!test")
    CommandLineRunner init(final KeycloakUtil keycloakUtil) {
        return new CommandLineRunner() {

            public void run(String... strings) throws Exception {

                migrateUsersAndGroups.migrate();

            }

        };
    }

}
