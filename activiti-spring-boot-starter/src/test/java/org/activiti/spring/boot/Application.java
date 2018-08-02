package org.activiti.spring.boot;

import org.activiti.runtime.api.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }


    //@TODO: add tests for
    //  - Complete task with variables
    //  - Add other users to test group and claim/release combinations
    //  - Add get/set variables tests
    //  - Add Impersonation methods to TaskAdminRuntime
}
