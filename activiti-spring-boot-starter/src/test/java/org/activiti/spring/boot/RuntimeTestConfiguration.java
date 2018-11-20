package org.activiti.spring.boot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.api.process.model.events.SequenceFlowTakenEvent;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.api.task.runtime.events.TaskUpdatedEvent;
import org.activiti.api.task.runtime.events.listener.TaskRuntimeEventListener;
import org.activiti.core.common.spring.identity.ExtendedInMemoryUserDetailsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.*;

@Configuration
public class RuntimeTestConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeTestConfiguration.class);

    public static boolean processImageConnectorExecuted = false;

    public static boolean tagImageConnectorExecuted = false;

    public static boolean discardImageConnectorExecuted = false;

    public static Set<String> createdTasks = new HashSet<>();

    public static Set<String> updatedTasks = new HashSet<>();

    public static Set<String> completedProcesses = new HashSet<>();

    public static Set<SequenceFlowTakenEvent> sequenceFlowTakenEvents = new HashSet<>();

    @Bean
    public UserDetailsService myUserDetailsService() {
        ExtendedInMemoryUserDetailsManager extendedInMemoryUserDetailsManager = new ExtendedInMemoryUserDetailsManager();

        List<GrantedAuthority> salaboyAuthorities = new ArrayList<>();
        salaboyAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        salaboyAuthorities.add(new SimpleGrantedAuthority("GROUP_activitiTeam"));

        extendedInMemoryUserDetailsManager.createUser(new User("salaboy",
                                                               "password",
                                                               salaboyAuthorities));

        List<GrantedAuthority> adminAuthorities = new ArrayList<>();
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_ADMIN"));

        extendedInMemoryUserDetailsManager.createUser(new User("admin",
                                                               "password",
                                                               adminAuthorities));

        List<GrantedAuthority> garthAuthorities = new ArrayList<>();
        garthAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        garthAuthorities.add(new SimpleGrantedAuthority("GROUP_doctor"));

        extendedInMemoryUserDetailsManager.createUser(new User("garth",
                                                               "password",
                                                               garthAuthorities));

        //dean has role but no group
        List<GrantedAuthority> deanAuthorities = new ArrayList<>();
        deanAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        extendedInMemoryUserDetailsManager.createUser(new User("dean",
                "password",
                deanAuthorities));

        return extendedInMemoryUserDetailsManager;
    }

    @Bean
    public Connector processImageConnector() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            LOGGER.info("My inbound variables keys: " + inBoundVariables.keySet());
            LOGGER.info("My inbound variables values: " + inBoundVariables.values());
            boolean expectedValue = (Boolean) inBoundVariables.get("expectedKey");

            integrationContext.addOutBoundVariable("approved",
                                                   expectedValue);
            processImageConnectorExecuted = true;
            return integrationContext;
        };
    }

    @Bean
    public Connector processImageActionName() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            LOGGER.info("processImageActionName inbound variables keys: " + inBoundVariables.keySet());
            LOGGER.info("processImageActionName inbound variables values: " + inBoundVariables.values());

            boolean expectedValue = (Boolean) inBoundVariables.get("expectedKey");
            integrationContext.addOutBoundVariable("approved",
                                                   expectedValue);
            assertThat(((String) inBoundVariables.get("input-variable-name-1"))).isEqualTo("input-variable-name-1");
            return integrationContext;
        };
    }

    @Bean
    public Connector tagImageActionName() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            LOGGER.info("tagImageActionName inbound variables keys: " + inBoundVariables.keySet());
            LOGGER.info("tagImageActionName inbound variables values: " + inBoundVariables.values());

            boolean expectedValue = (Boolean) inBoundVariables.get("expectedKey");
            integrationContext.addOutBoundVariable("approved",
                                                   expectedValue);
            assertThat(((String) inBoundVariables.get("input-variable-name-2"))).isEqualTo("input-variable-name-2");
            return integrationContext;
        };
    }

    @Bean
    public Connector tagImageConnector() {
        return integrationContext -> {
            tagImageConnectorExecuted = true;
            return integrationContext;
        };
    }

    @Bean
    public Connector discardImageConnector() {
        return integrationContext -> {
            discardImageConnectorExecuted = true;
            return integrationContext;
        };
    }

    @Bean
    public TaskRuntimeEventListener<TaskCreatedEvent> taskCreatedListener() {
        return taskCreated -> createdTasks.add(taskCreated.getEntity().getId());
    }

    @Bean
    public TaskRuntimeEventListener<TaskUpdatedEvent> taskUpdatedListener() {
        return taskUpdated -> updatedTasks.add(taskUpdated.getEntity().getId());
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCompletedEvent> processCompletedListener() {
        return processCompleted -> completedProcesses.add(processCompleted.getEntity().getId());
    }

    @Bean
    public ProcessRuntimeEventListener<SequenceFlowTakenEvent> sequenceFlowTakenEventListener() {
        return sequenceFlowTakenEvent -> sequenceFlowTakenEvents.add(sequenceFlowTakenEvent);
    }
}
