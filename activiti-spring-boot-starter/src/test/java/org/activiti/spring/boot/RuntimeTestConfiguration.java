package org.activiti.spring.boot;

import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.api.task.runtime.events.listener.TaskRuntimeEventListener;
import org.activiti.model.connector.VariableDefinition;
import org.activiti.spring.identity.ExtendedInMemoryUserDetailsManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Configuration
public class RuntimeTestConfiguration {

    public static boolean processImageConnectorExecuted = false;

    public static boolean tagImageConnectorExecuted = false;

    public static boolean discardImageConnectorExecuted = false;

    public static Set<String> createdTasks = new HashSet<>();

    public static Set<String> completedProcesses = new HashSet<>();


    @Bean
    public UserDetailsService myUserDetailsService() {
        ExtendedInMemoryUserDetailsManager extendedInMemoryUserDetailsManager = new ExtendedInMemoryUserDetailsManager();

        List<GrantedAuthority> salaboyAuthorities = new ArrayList<>();
        salaboyAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        salaboyAuthorities.add(new SimpleGrantedAuthority("GROUP_activitiTeam"));

        extendedInMemoryUserDetailsManager.createUser(new User("salaboy", "password", salaboyAuthorities));

        List<GrantedAuthority> adminAuthorities = new ArrayList<>();
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_ADMIN"));

        extendedInMemoryUserDetailsManager.createUser(new User("admin", "password", adminAuthorities));

        List<GrantedAuthority> garthAuthorities = new ArrayList<>();
        garthAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        garthAuthorities.add(new SimpleGrantedAuthority("GROUP_doctor"));

        extendedInMemoryUserDetailsManager.createUser(new User("garth", "password", garthAuthorities));

        return extendedInMemoryUserDetailsManager;
    }

    @Bean
    public Connector processImageConnector() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            System.out.println("My inbound variables keys: " + inBoundVariables.keySet());
            System.out.println("My inbound variables values: " + inBoundVariables.values());
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
            System.out.println("processImageActionName inbound variables keys: " + inBoundVariables.keySet());
            System.out.println("processImageActionName inbound variables values: " + inBoundVariables.values());

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
            System.out.println("tagImageActionName inbound variables keys: " + inBoundVariables.keySet());
            System.out.println("tagImageActionName inbound variables values: " + inBoundVariables.values());

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
    public TaskRuntimeEventListener<TaskCreatedEvent> taskCreatedListener () {
        return taskCreated -> createdTasks.add(taskCreated.getEntity().getId());
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCompletedEvent> processCompletedListener () {
        return processCompleted -> completedProcesses.add(processCompleted.getEntity().getId());
    }
}
