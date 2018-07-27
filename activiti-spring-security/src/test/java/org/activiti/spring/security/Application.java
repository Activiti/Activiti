package org.activiti.spring.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"org.activiti.spring.security", "org.activiti.spring.identity"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
