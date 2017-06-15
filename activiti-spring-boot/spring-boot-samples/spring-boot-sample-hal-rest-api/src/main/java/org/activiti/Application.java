package org.activiti;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @Bean
    InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {

        return () -> {

            Group group = identityService.newGroup("user");
            group.setName("users");
            group.setType("security-role");
            identityService.saveGroup(group);

            createUser("admin", "admin", identityService);
            createUser("john", "pass", identityService);

        };
    }

    private void createUser(String username, String password, IdentityService identityService) {
        User admin = identityService.newUser(username);
        admin.setPassword(password);
        identityService.saveUser(admin);
    }

}
