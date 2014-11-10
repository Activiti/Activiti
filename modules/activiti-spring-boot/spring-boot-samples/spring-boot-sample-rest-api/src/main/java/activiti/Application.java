package activiti;


import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Demonstrates the REST API
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {
    @Bean
    CommandLineRunner seedUsersAndGroups(final IdentityService identityService) {
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