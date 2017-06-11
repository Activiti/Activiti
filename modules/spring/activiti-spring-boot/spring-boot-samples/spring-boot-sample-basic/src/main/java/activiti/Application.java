package activiti;

import org.activiti.engine.RuntimeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
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
