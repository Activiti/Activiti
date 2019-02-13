package org.activiti.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoApplication implements CommandLineRunner {

    private Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    private List<ProcessCompletedEvent> processCompletedEvents = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class,
                              args);
    }

    @Override
    public void run(String... args) {
        securityUtil.logInAs("reviewer");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                                                                                                      10));
        logger.info("> Available Process definitions: " + processDefinitionPage.getTotalItems());
        for (ProcessDefinition pd : processDefinitionPage.getContent()) {
            logger.info("\t > Process definition: " + pd);
        }

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                                                                       .start()
                                                                       .withProcessDefinitionKey("RankMovieId")
                                                                       .withName("myProcess")
                                                                       .withVariable("movieToRank",
                                                                                     "Lord of the rings")
                                                                       .build());
        logger.info(">>> Created Process Instance: " + processInstance);

        logger.info(">>> Process variables:");
        List<VariableInstance> variables = processRuntime.variables(
                ProcessPayloadBuilder
                        .variables()
                        .withProcessInstance(processInstance)
                        .build());
        variables.forEach(variableInstance -> logger.info("\t> " + variableInstance.getName() + " -> " + variableInstance.getValue()));

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         20));
        tasks.getContent().forEach(task -> {
            logger.info(">>> Performing task -> " + task);
            taskRuntime.complete(TaskPayloadBuilder
                                         .complete()
                                         .withTaskId(task.getId())
                                         .build());
        });

        logger.info(">>> Completed process Instances: ");
        processCompletedEvents.forEach(processCompletedEvent -> logger.info("\t> Process instance : " + processCompletedEvent.getEntity()));
    }

    @Bean("MoviesId.getMovieDescId")
    public Connector getMovieDesc() {
        return getConnector();
    }

    @Bean("connector-8abc839468e609ca0168e61babb60005.getMovieDescId")
    public Connector getMovieDescUUIDs() {
        return getConnector();
    }

    private Connector getConnector() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            logger.info(">>inbound: " + inBoundVariables);
            integrationContext.addOutBoundVariable("movieDescription",
                                                   "The Lord of the Rings is an epic high fantasy novel written by English author and scholar J. R. R. Tolkien");
            return integrationContext;
        };
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCompletedEvent> processCompletedEventListener() {
        return processCompletedEvent -> processCompletedEvents.add(processCompletedEvent);
    }
}
