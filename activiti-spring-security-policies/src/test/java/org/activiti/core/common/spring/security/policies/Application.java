package org.activiti.core.common.spring.security.policies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.activiti.core.common.spring.identity", "org.activiti.core.common.spring.security"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}