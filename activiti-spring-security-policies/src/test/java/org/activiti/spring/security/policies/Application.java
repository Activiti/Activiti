package org.activiti.spring.security.policies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@SpringBootApplication
@ComponentScan(basePackages = {"org.activiti.spring.identity", "org.activiti.spring.security"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}