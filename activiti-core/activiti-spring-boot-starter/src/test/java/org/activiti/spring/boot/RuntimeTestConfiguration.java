package org.activiti.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.task.runtime.events.TaskCandidateGroupAddedEvent;
import org.activiti.api.task.runtime.events.TaskCandidateGroupRemovedEvent;
import org.activiti.api.task.runtime.events.TaskCandidateUserAddedEvent;
import org.activiti.api.task.runtime.events.TaskCandidateUserRemovedEvent;
import org.activiti.api.task.runtime.events.TaskCompletedEvent;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.api.task.runtime.events.TaskUpdatedEvent;
import org.activiti.api.task.runtime.events.listener.TaskRuntimeEventListener;
import org.activiti.core.common.spring.identity.ExtendedInMemoryUserDetailsManager;
import org.activiti.spring.boot.process.ProcessBaseRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.tasks.TaskBaseRuntime;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@Import({ProcessCleanUpUtil.class,
         TaskCleanUpUtil.class,
         SecurityUtil.class,
         ProcessBaseRuntime.class,
         TaskBaseRuntime.class})
public class RuntimeTestConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeTestConfiguration.class);

    public static boolean processImageConnectorExecuted = false;

    public static boolean tagImageConnectorExecuted = false;

    public static boolean discardImageConnectorExecuted = false;

    public static Set<String> createdTasks = new HashSet<>();

    public static Set<String> updatedTasks = new HashSet<>();

    public static Set<String> completedProcesses = new HashSet<>();

    public static Set<String> completedTasks = new HashSet<>();

    public static Set<String> cancelledProcesses = new HashSet<>();

    public static Set<BPMNSequenceFlowTakenEvent> sequenceFlowTakenEvents = new HashSet<>();

    public static Set<VariableCreatedEvent> variableCreatedEventsFromProcessInstance = new HashSet<>();

    public static Set<TaskCandidateUserAddedEvent> taskCandidateUserAddedEvents = new HashSet<>();

    public static Set<TaskCandidateUserRemovedEvent> taskCandidateUserRemovedEvents = new HashSet<>();

    public static Set<TaskCandidateGroupAddedEvent> taskCandidateGroupAddedEvents = new HashSet<>();

    public static Set<TaskCandidateGroupRemovedEvent> taskCandidateGroupRemovedEvents = new HashSet<>();

    @Bean
    public UserDetailsService myUserDetailsService() {
        ExtendedInMemoryUserDetailsManager extendedInMemoryUserDetailsManager = new ExtendedInMemoryUserDetailsManager();

        List<GrantedAuthority> userAuthorities = new ArrayList<>();
        userAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        userAuthorities.add(new SimpleGrantedAuthority("GROUP_activitiTeam"));

        extendedInMemoryUserDetailsManager.createUser(new User("user",
                                                               "password",
                                                               userAuthorities));

        List<GrantedAuthority> johnAuthorities = new ArrayList<>();
        johnAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        johnAuthorities.add(new SimpleGrantedAuthority("GROUP_activitiTeam"));

        extendedInMemoryUserDetailsManager.createUser(new User("john",
                                                               "password",
                                                               johnAuthorities));

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
    public Connector testConnectorMultiInstanceExecution() {
        return integrationContext -> integrationContext;
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

    @Bean(name = "Process Image Connector.processImageActionName")
    public Connector processImageActionName() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            LOGGER.info("processImageActionName inbound variables keys: " + inBoundVariables.keySet());
            LOGGER.info("processImageActionName inbound variables values: " + inBoundVariables.values());

            boolean expectedValue = (Boolean) inBoundVariables.get("expectedKey");
            integrationContext.addOutBoundVariable("approved",
                                                   expectedValue);
            assertThat(((String) inBoundVariables.get("input_variable_name_1"))).isEqualTo("input-variable-name-1");
            return integrationContext;
        };
    }

    @Bean(name = "Tag Image Connector.tagImageActionName")
    public Connector tagImageActionName() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            LOGGER.info("tagImageActionName inbound variables keys: " + inBoundVariables.keySet());
            LOGGER.info("tagImageActionName inbound variables values: " + inBoundVariables.values());

            boolean expectedValue = (Boolean) inBoundVariables.get("expectedKey");
            integrationContext.addOutBoundVariable("approved",
                                                   expectedValue);
            assertThat(((String) inBoundVariables.get("input_variable_name_2"))).isEqualTo("input-variable-name-2");
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
    public TaskRuntimeEventListener<TaskCompletedEvent> taskCompletedListener() {
        return taskCompleted -> completedTasks.add(taskCompleted.getEntity().getId());
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCancelledEvent> processCancelledListener() {
        return processCancelled -> cancelledProcesses.add(processCancelled.getEntity().getId());
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
    public BPMNElementEventListener<BPMNSequenceFlowTakenEvent> sequenceFlowTakenEventListener() {
        return sequenceFlowTakenEvent -> sequenceFlowTakenEvents.add(sequenceFlowTakenEvent);
    }

    @Bean
    public VariableEventListener<VariableCreatedEvent> variableCreatedEventFromProcessInstanceListener() {
        return variableCreatedEvent -> {
            //we filter out the events from tasks
            if (variableCreatedEvent.getEntity().getTaskId() == null) {
                variableCreatedEventsFromProcessInstance.add(variableCreatedEvent);
            }
        };
    }

    @Bean
    public TaskRuntimeEventListener<TaskCandidateGroupAddedEvent> CandidateGroupAddedEvent() {
        return candidateGroupAddedEvent -> taskCandidateGroupAddedEvents.add(candidateGroupAddedEvent);
    }

    @Bean
    public TaskRuntimeEventListener<TaskCandidateGroupRemovedEvent> CandidateGroupRemovedEvent() {
        return candidateGroupRemovedEvent -> taskCandidateGroupRemovedEvents.add(candidateGroupRemovedEvent);
    }

    @Bean
    public TaskRuntimeEventListener<TaskCandidateUserAddedEvent> CandidateUserAddedListener() {
        return candidateUserAddedEvent -> taskCandidateUserAddedEvents.add(candidateUserAddedEvent);
    }

    @Bean
    public TaskRuntimeEventListener<TaskCandidateUserRemovedEvent> CandidateUserRemovedListener() {
        return candidateUserRemovedEvent -> taskCandidateUserRemovedEvents.add(candidateUserRemovedEvent);
    }

    @Bean(name = "Variable Mapping Connector.variableMappingActionName")
    public Connector variableMappingActionName() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();

            String variableOne = "input_variable_name_1";
            String variableTwo = "input_variable_name_2";
            String variableThree = "input_variable_name_3";
            String staticValue = "input_static_value";
            String integerConstant = "integer_constant";

            Integer currentAge = (Integer) inBoundVariables.get(variableTwo);
            Integer offSet = (Integer) inBoundVariables.get(variableThree);
            Integer integerConstantValue = (Integer) inBoundVariables.get(integerConstant);

            assertThat(inBoundVariables.entrySet())
                    .extracting(Map.Entry::getKey,
                                Map.Entry::getValue)
                    .containsOnly(
                            tuple(variableOne,
                                  "inName"),
                            tuple(variableTwo,
                                  20),
                            tuple(variableThree,
                                  5),
                            tuple(staticValue,
                                  "a static value"),
                            tuple(integerConstant,
                                  10));

            integrationContext.addOutBoundVariable("out_variable_name_1",
                                                   "outName");
            integrationContext.addOutBoundVariable("out_variable_name_2",
                                                   currentAge + offSet + integerConstantValue);
            integrationContext.addOutBoundVariable("out_unmapped_variable_matching_name",
                                                   "outTest");
            integrationContext.addOutBoundVariable("out_unmapped_variable_non_matching_name",
                                                   "outTest");
            return integrationContext;
        };
    }

    @Bean(name = "Variable Mapping Expression Connector.variableMappingExpressionActionName")
    public Connector variableMappingExpressionActionName() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();

            String variableOne = "input-variable-name-1";
            String variableTwo = "input-variable-name-2";
            String variableThree = "input-variable-name-3";
            String expressionVariable = "input-variable-expression";
            String expressionValue = "input-value-expression";
            String staticValue = "input-static-value";
            String integerConstant = "integer-constant";

            Integer currentAge = (Integer) inBoundVariables.get(variableTwo);
            Integer integerConstantValue = (Integer) inBoundVariables.get(integerConstant);

            String[] array = { "first", "John", "Doe", "last" };
            List<String> list = asList(array);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("age-in-months", 240L);
            dataMap.put("full-name", "John Doe");
            dataMap.put("demoString", "expressionResolved");
            dataMap.put("list", list);

            Map<String, Object> expectedResolvedJsonTemplate = new HashMap<>();
            expectedResolvedJsonTemplate.put("name", "John");
            expectedResolvedJsonTemplate.put("age", 20);
            expectedResolvedJsonTemplate.put("resident", true);

            assertThat(inBoundVariables.entrySet()).extracting(Map.Entry::getKey,
                                                               Map.Entry::getValue)
                    .containsOnly(tuple(variableOne, dataMap),
                                  tuple(variableTwo, 20),
                                  tuple(variableThree, "Hello John Doe, today is your 20th birthday! It means 7305.0 days of life"),
                                  tuple(expressionVariable, "John"),
                                  tuple(expressionValue, "John"),
                                  tuple(staticValue, "a static value"),
                                  tuple(integerConstant, 10),
                                  tuple("input-json-template", expectedResolvedJsonTemplate));

            integrationContext.addOutBoundVariable("out-variable-name-1", "outName");
            integrationContext.addOutBoundVariable("out-variable-name-2", currentAge + integerConstantValue);
            integrationContext.addOutBoundVariable("out-unmapped-variable-matching-name", "outTest");
            integrationContext.addOutBoundVariable("out-unmapped-variable-non-matching-name", "outTest");

            Map<String, Object> conferenceInfo = new HashMap<>();
            conferenceInfo.put("City", "London");
            conferenceInfo.put("numberOfAttendees", 5000);
            integrationContext.addOutBoundVariable("conferenceInfo", conferenceInfo);
            return integrationContext;
        };
    }

    @Bean(name = "OutputMappingExpVarConnector.outputMappingExpVarActionName")
    public Connector outputMappingVariableExpressionActionName() {
        return integrationContext -> {
            integrationContext.addOutBoundVariable("out-variable-name-1",
                                                   "${name}");
            return integrationContext;
        };
    }

    @Bean(name = "OutputMappingExpValueConnector.outputMappingExpValueActionName")
    public Connector outputMappingValueExpressionActionName() throws Exception {
        JsonNode value = new ObjectMapper().readTree("{\n"
            + "  \"city\": {\n"
            + "    \"name\": \"London\",\n"
            + "    \"place\": \"Tower of London\"\n"
            + "  }\n"
            + "}");
        return integrationContext -> {
            integrationContext.addOutBoundVariable("outVariable1Name",
                "value-set-in-connector");
            integrationContext.addOutBoundVariable("sightSeeing", value);
            return integrationContext;
        };
    }
}
