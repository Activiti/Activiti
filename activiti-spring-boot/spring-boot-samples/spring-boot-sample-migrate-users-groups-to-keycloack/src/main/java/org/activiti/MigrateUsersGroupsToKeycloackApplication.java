package org.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MigrateUsersGroupsToKeycloackApplication {

    @Autowired
    KeycloakUtil keycloakUtil;

    public static void main(String[] args) {
        SpringApplication.run(MigrateUsersGroupsToKeycloackApplication.class, args);
    }

    @Bean
    CommandLineRunner init(final KeycloakUtil keycloakUtil) {
        return new CommandLineRunner() {

            public void run(String... strings) throws Exception {
                keycloakUtil.starMigration();

            }

        };
    }

}
