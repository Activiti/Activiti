package org.activiti;

import java.util.Iterator;
import java.util.List;

import org.activiti.domain.Group;
import org.activiti.domain.User;
import org.activiti.service.GroupService;
import org.activiti.service.UserService;
import org.activiti.utils.KeycloakUtil;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MigrateUsersGroupsToKeycloackApplication {

    @Autowired
    private GroupService groupservice;

    @Autowired
    private UserService userService;

    @Autowired
    private KeycloakUtil keycloakUtil;

    public static void main(String[] args) {
        SpringApplication.run(MigrateUsersGroupsToKeycloackApplication.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return new CommandLineRunner() {

            public void run(String... strings) throws Exception {

                List<Group> groups = groupservice.loadAll();
                for (Group group : groups) {
                    keycloakUtil.createRole(group.getId(), group.getName());
                }

                List<User> users = userService.loadAll();
                for (User user : users) {
                    List<String> roles = userService.loadGroupsByUserId(user.getId());
                    keycloakUtil.createUserWithRoles(user.getId(),
                                                     user.getFirstName(),
                                                     user.getLastName(),
                                                     user.getPassword(),
                                                     roles);
                }

            }
        };
    }

}
