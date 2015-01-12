package activiti;


import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Demonstrates the <A href="http://localhost:8080/">REST API</A>
 */
@SpringBootApplication
public class Application {

    @Bean
    InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {

        return new InitializingBean() {
            @Override
            public void afterPropertiesSet() throws Exception {

                // install groups & users
                Group group = identityService.newGroup("user");
                group.setName("users");
                group.setType("security-role");
                identityService.saveGroup(group);

                User joram = identityService.newUser("jbarrez");
                joram.setFirstName("Joram");
                joram.setLastName("Barrez");
                joram.setPassword("password");
                identityService.saveUser(joram);

                User josh = identityService.newUser("jlong");
                josh.setFirstName("Josh");
                josh.setLastName("Long");
                josh.setPassword("password");
                identityService.saveUser(josh);

                identityService.createMembership("jbarrez", "user");
                identityService.createMembership("jlong", "user");
            }
        };
    }

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }
}