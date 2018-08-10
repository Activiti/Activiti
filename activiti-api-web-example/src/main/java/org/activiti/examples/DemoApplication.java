/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.examples;

import java.util.Map;

import org.activiti.runtime.api.connector.Connector;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);


    }


    @Override
    public void run(String... args) {
    }

    @Bean
    public MessageChannel fileChannel() {
        return new DirectChannel();
    }


    @Bean
    public Connector processTextConnector() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            String contentToProcess = (String) inBoundVariables.get("fileContent");
            // Logic Here to decide if content is approved or not
            if (contentToProcess.contains("activiti")) {
                integrationContext.addOutBoundVariable("approved",
                        true);
            } else {
                integrationContext.addOutBoundVariable("approved",
                        false);
            }
            return integrationContext;
        };
    }

    @Bean
    public Connector tagTextConnector() {
        return integrationContext -> {
            String contentToTag = (String) integrationContext.getInBoundVariables().get("fileContent");
            contentToTag += " :) ";
            integrationContext.addOutBoundVariable("fileContent",
                    contentToTag);
            System.out.println("Final Content: " + contentToTag);
            return integrationContext;
        };
    }

    @Bean
    public Connector discardTextConnector() {
        return integrationContext -> {
            String contentToDiscard = (String) integrationContext.getInBoundVariables().get("fileContent");
            contentToDiscard += " :( ";
            integrationContext.addOutBoundVariable("fileContent",
                    contentToDiscard);
            System.out.println("Final Content: " + contentToDiscard);
            return integrationContext;
        };
    }

}
