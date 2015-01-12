package activiti;


import java.util.Collections;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Demonstrates the Activiti Actuator endpoints
 * available <a href="http://localhost:8080/activiti/processes/waiter">on localhost</a>
 * where, in this case, {@code waiter} is the name of the process definition.
 *
 */
@SpringBootApplication
public class Application {

    @Bean
    CommandLineRunner startProcess(final RuntimeService runtimeService, final TaskService taskService) {
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                for (int i = 0; i < 10; i++)
                    runtimeService.startProcessInstanceByKey("waiter", Collections.singletonMap("customerId", (Object) i ));

                for (int i=0; i<7; i++)
                    taskService.complete(taskService.createTaskQuery().list().get(0).getId());
            }
        };
    }

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }
}