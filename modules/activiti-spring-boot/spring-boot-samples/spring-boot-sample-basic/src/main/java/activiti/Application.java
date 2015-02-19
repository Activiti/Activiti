package activiti;

import org.activiti.engine.RuntimeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
<<<<<<< HEAD
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

     //  establish that it's detected the processes and that we can deploy one.
    @Bean
    CommandLineRunner basics(
            final PlatformTransactionManager platformTransactionManager,
            final RuntimeService runtimeService) {
=======
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

/**
 *
 */
@SpringBootApplication
public class Application {

    //  establish that it's detected the processes and that we can deploy one.
    @Bean
    CommandLineRunner basics(final RuntimeService runtimeService) {
>>>>>>> upstream/master
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                runtimeService.startProcessInstanceByKey("waiter", Collections.singletonMap("customerId", (Object) 243L));
            }
        };
    }

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

}
