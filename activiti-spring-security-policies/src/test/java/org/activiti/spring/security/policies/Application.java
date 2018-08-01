package org.activiti.spring.security.policies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"org.activiti.spring.identity"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}