/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.examples;

import static java.util.Collections.singletonList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class DemoApplication implements CommandLineRunner {

    private Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    private final ProcessRuntime processRuntime;

    private final TaskRuntime taskRuntime;

    private final SecurityUtil securityUtil;

    private final ObjectMapper objectMapper;

    public DemoApplication(ProcessRuntime processRuntime,
                           TaskRuntime taskRuntime,
                           SecurityUtil securityUtil,
                           ObjectMapper objectMapper) {
        this.processRuntime = processRuntime;
        this.taskRuntime = taskRuntime;
        this.securityUtil = securityUtil;
        this.objectMapper = objectMapper;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

    }

    @Override
    public void run(String... args) {
        securityUtil.logInAs("system");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 10));
        logger.info("> Available Process definitions: " + processDefinitionPage.getTotalItems());
        for (ProcessDefinition pd : processDefinitionPage.getContent()) {
            logger.info("\t > Process definition: " + pd);
        }

    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void processText() {

        securityUtil.logInAs("system");

        LinkedHashMap content = pickRandomString();
        //here content represents a json document with a body, approved flag and tags as attributes
        //these attributes could be handled as plain variables as the variables are in activiti-api-basic-process-example
        //or the document could be mapped directly to a bean as in activiti-api-basic-full-example-bean

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

        logger.info("> Starting process to process content: " + content + " at " + formatter.format(new Date()));

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("categorizeHumanProcess")
                .withName("Processing Content: " + content)
                .withVariable("content", objectMapper.convertValue(content, JsonNode.class))
                .build());
        logger.info(">>> Created Process Instance: " + processInstance);


    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void checkAndWorkOnTasksWhenAvailable() {
        securityUtil.logInAs("bob");

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));
        if (tasks.getTotalItems() > 0) {
            for (Task t : tasks.getContent()) {

                logger.info("> Claiming task: " + t.getId());
                taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(t.getId()).build());

                List<VariableInstance> variables = taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(t.getId()).build());
                VariableInstance variableInstance = variables.get(0);
                if (variableInstance.getName().equals("content")) {
                    LinkedHashMap contentToProcess = objectMapper.convertValue(variableInstance.getValue(), LinkedHashMap.class);
                    logger.info("> Content received inside the task to approve: " + contentToProcess);

                    if (contentToProcess.get("body").toString().contains("activiti")) {
                        logger.info("> User Approving content");
                        contentToProcess.put("approved",true);
                    } else {
                        logger.info("> User Discarding content");
                        contentToProcess.put("approved",false);
                    }
                    taskRuntime.complete(TaskPayloadBuilder.complete()
                            .withTaskId(t.getId()).withVariable("content", contentToProcess).build());
                }


            }

        } else {
            logger.info("> There are no task for me to work on.");
        }

    }


    @Bean
    public Connector tagTextConnector() {
        return integrationContext -> {
            LinkedHashMap contentToTag = (LinkedHashMap) integrationContext.getInBoundVariables().get("content");
            contentToTag.put("tags", singletonList(" :) "));
            integrationContext.addOutBoundVariable("content", contentToTag);
            logger.info("Final Content: " + contentToTag);
            return integrationContext;
        };
    }

    @Bean
    public Connector discardTextConnector() {
        return integrationContext -> {
            LinkedHashMap contentToDiscard = (LinkedHashMap) integrationContext.getInBoundVariables().get("content");
            contentToDiscard.put("tags", singletonList(" :( "));
            integrationContext.addOutBoundVariable("content", contentToDiscard);
            logger.info("Final Content: " + contentToDiscard);
            return integrationContext;
        };
    }


    private LinkedHashMap pickRandomString() {
        String[] texts = {"hello from london", "Hi there from activiti!", "all good news over here.", "I've tweeted about activiti today.",
                "other boring projects.", "activiti cloud - Cloud Native Java BPM"};
        LinkedHashMap<Object,Object> content = new LinkedHashMap<>();
        content.put("body",texts[new Random().nextInt(texts.length)]);
        return content;
    }

}
