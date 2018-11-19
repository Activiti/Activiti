package org.activiti.spring.conformance.set2;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.model.shared.event.VariableDeletedEvent;
import org.activiti.api.model.shared.event.VariableUpdatedEvent;
import org.activiti.api.process.model.events.BPMNActivityCancelledEvent;
import org.activiti.api.process.model.events.BPMNActivityCompletedEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.process.runtime.events.*;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.task.runtime.events.*;
import org.activiti.api.task.runtime.events.listener.TaskEventListener;
import org.activiti.core.common.spring.identity.ExtendedInMemoryUserDetailsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RuntimeTestConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeTestConfiguration.class);

    public static List<RuntimeEvent> collectedEvents = new ArrayList<>();


    @Bean
    public UserDetailsService myUserDetailsService() {
        ExtendedInMemoryUserDetailsManager extendedInMemoryUserDetailsManager = new ExtendedInMemoryUserDetailsManager();

        List<GrantedAuthority> user1 = new ArrayList<>();
        user1.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        user1.add(new SimpleGrantedAuthority("GROUP_group1"));

        extendedInMemoryUserDetailsManager.createUser(new User("user1",
                "password",
                user1));

        List<GrantedAuthority> user2 = new ArrayList<>();
        user2.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        user2.add(new SimpleGrantedAuthority("GROUP_group2"));

        extendedInMemoryUserDetailsManager.createUser(new User("user2",
                "password",
                user2));

        List<GrantedAuthority> user3 = new ArrayList<>();
        user3.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        user3.add(new SimpleGrantedAuthority("GROUP_group1"));
        user3.add(new SimpleGrantedAuthority("GROUP_group2"));

        extendedInMemoryUserDetailsManager.createUser(new User("user3",
                "password",
                user3));

        List<GrantedAuthority> user4 = new ArrayList<>();
        user4.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));

        extendedInMemoryUserDetailsManager.createUser(new User("user4",
                "password",
                user4));


        List<GrantedAuthority> adminAuthorities = new ArrayList<>();
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_ADMIN"));

        extendedInMemoryUserDetailsManager.createUser(new User("admin",
                "password",
                adminAuthorities));


        return extendedInMemoryUserDetailsManager;
    }


    @Bean
    @Primary
    public BPMNElementEventListener<BPMNActivityStartedEvent> bpmnActivityStartedListener() {
        return bpmnActivityStartedEvent -> collectedEvents.add(bpmnActivityStartedEvent);
    }

    @Bean
    @Primary
    public BPMNElementEventListener<BPMNActivityCompletedEvent> bpmnActivityCompletedListener() {
        return bpmnActivityCompletedEvent -> collectedEvents.add(bpmnActivityCompletedEvent);
    }

    @Bean
    @Primary
    public BPMNElementEventListener<BPMNActivityCancelledEvent> bpmnActivityCancelledListener() {
        return bpmnActivityCancelledEvent -> collectedEvents.add(bpmnActivityCancelledEvent);
    }


    @Bean
    @Primary
    public BPMNElementEventListener<BPMNSequenceFlowTakenEvent> bpmnSequenceFlowTakenListener() {
        return bpmnSequenceFlowTakenEvent -> collectedEvents.add(bpmnSequenceFlowTakenEvent);
    }

    @Bean
    @Primary
    public ProcessRuntimeEventListener<ProcessCreatedEvent> processCreatedListener() {
        return processCreatedEvent -> collectedEvents.add(processCreatedEvent);
    }

    @Bean
    @Primary
    public ProcessRuntimeEventListener<ProcessStartedEvent> processStartedListener() {
        return processStartedEvent -> collectedEvents.add(processStartedEvent);
    }

    @Bean
    @Primary
    public ProcessRuntimeEventListener<ProcessCompletedEvent> processCompletedListener() {
        return processCompletedEvent -> collectedEvents.add(processCompletedEvent);
    }

    @Bean
    @Primary
    public ProcessRuntimeEventListener<ProcessResumedEvent> processResumedListener() {
        return processResumedEvent -> collectedEvents.add(processResumedEvent);
    }

    @Bean
    @Primary
    public ProcessRuntimeEventListener<ProcessSuspendedEvent> processSuspendedListener() {
        return processSuspendedEvent -> collectedEvents.add(processSuspendedEvent);
    }

    @Bean
    @Primary
    public ProcessRuntimeEventListener<ProcessCancelledEvent> processCancelledListener() {
        return processCancelledEvent -> collectedEvents.add(processCancelledEvent);
    }

    @Bean
    @Primary
    public VariableEventListener<VariableCreatedEvent> variableCreatedEventListener() {
        return variableCreatedEvent -> collectedEvents.add(variableCreatedEvent);
    }

    @Bean
    @Primary
    public VariableEventListener<VariableDeletedEvent> variableDeletedEventListener() {
        return variableDeletedEvent -> collectedEvents.add(variableDeletedEvent);
    }

    @Bean
    @Primary
    public VariableEventListener<VariableUpdatedEvent> variableUpdatedEventListener() {
        return variableUpdatedEvent -> collectedEvents.add(variableUpdatedEvent);
    }

    @Bean
    @Primary
    public TaskEventListener<TaskCreatedEvent> taskCreatedEventListener() {
        return taskCreatedEvent -> collectedEvents.add(taskCreatedEvent);
    }

    @Bean
    @Primary
    public TaskEventListener<TaskUpdatedEvent> taskUpdatedEventListener() {
        return taskUpdatedEvent -> collectedEvents.add(taskUpdatedEvent);
    }

    @Bean
    @Primary
    public TaskEventListener<TaskCompletedEvent> taskCompletedEventListener() {
        return taskCompletedEvent -> collectedEvents.add(taskCompletedEvent);
    }

    @Bean
    @Primary
    public TaskEventListener<TaskSuspendedEvent> taskSuspendedEventListener() {
        return taskSuspendedEvent -> collectedEvents.add(taskSuspendedEvent);
    }

    @Bean
    @Primary
    public TaskEventListener<TaskAssignedEvent> taskAssignedEventListener() {
        return taskAssignedEvent -> collectedEvents.add(taskAssignedEvent);
    }

}
