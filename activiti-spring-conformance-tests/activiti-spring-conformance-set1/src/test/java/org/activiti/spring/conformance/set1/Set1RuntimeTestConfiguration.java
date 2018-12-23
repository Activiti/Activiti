package org.activiti.spring.conformance.set1;

import java.util.ArrayList;
import java.util.List;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.model.shared.event.VariableDeletedEvent;
import org.activiti.api.model.shared.event.VariableUpdatedEvent;
import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.model.events.BPMNActivityCancelledEvent;
import org.activiti.api.process.model.events.BPMNActivityCompletedEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.ProcessCreatedEvent;
import org.activiti.api.process.runtime.events.ProcessResumedEvent;
import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.process.runtime.events.ProcessSuspendedEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.task.runtime.events.TaskAssignedEvent;
import org.activiti.api.task.runtime.events.TaskCompletedEvent;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.api.task.runtime.events.TaskSuspendedEvent;
import org.activiti.api.task.runtime.events.TaskUpdatedEvent;
import org.activiti.api.task.runtime.events.listener.TaskEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class Set1RuntimeTestConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Set1RuntimeTestConfiguration.class);

    public static List<RuntimeEvent> collectedEvents = new ArrayList<>();

    private static boolean connector1Executed = false;

    private static boolean connector2Executed = false;

    private static IntegrationContext resultIntegrationContext = null;
    

    @Bean
    public BPMNElementEventListener<BPMNActivityStartedEvent> bpmnActivityStartedListener() {
        return bpmnActivityStartedEvent -> collectedEvents.add(bpmnActivityStartedEvent);
    }

    @Bean
    public BPMNElementEventListener<BPMNActivityCompletedEvent> bpmnActivityCompletedListener() {
        return bpmnActivityCompletedEvent -> collectedEvents.add(bpmnActivityCompletedEvent);
    }

    @Bean
    public BPMNElementEventListener<BPMNActivityCancelledEvent> bpmnActivityCancelledListener() {
        return bpmnActivityCancelledEvent -> collectedEvents.add(bpmnActivityCancelledEvent);
    }


    @Bean
    public BPMNElementEventListener<BPMNSequenceFlowTakenEvent> bpmnSequenceFlowTakenListener() {
        return bpmnSequenceFlowTakenEvent -> collectedEvents.add(bpmnSequenceFlowTakenEvent);
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCreatedEvent> processCreatedListener() {
        return processCreatedEvent -> collectedEvents.add(processCreatedEvent);
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessStartedEvent> processStartedListener() {
        return processStartedEvent -> collectedEvents.add(processStartedEvent);
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCompletedEvent> processCompletedListener() {
        return processCompletedEvent -> collectedEvents.add(processCompletedEvent);
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessResumedEvent> processResumedListener() {
        return processResumedEvent -> collectedEvents.add(processResumedEvent);
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessSuspendedEvent> processSuspendedListener() {
        return processSuspendedEvent -> collectedEvents.add(processSuspendedEvent);
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCancelledEvent> processCancelledListener() {
        return processCancelledEvent -> collectedEvents.add(processCancelledEvent);
    }

    @Bean
    public VariableEventListener<VariableCreatedEvent> variableCreatedEventListener() {
        return variableCreatedEvent -> collectedEvents.add(variableCreatedEvent);
    }

    @Bean
    @Primary
    public VariableEventListener<VariableDeletedEvent> variableDeletedEventListener() {
        return variableDeletedEvent -> collectedEvents.add(variableDeletedEvent);
    }

    @Bean
    public VariableEventListener<VariableUpdatedEvent> variableUpdatedEventListener() {
        return variableUpdatedEvent -> collectedEvents.add(variableUpdatedEvent);
    }

    @Bean
    public TaskEventListener<TaskCreatedEvent> taskCreatedEventListener() {
        return taskCreatedEvent -> collectedEvents.add(taskCreatedEvent);
    }

    @Bean
    public TaskEventListener<TaskUpdatedEvent> taskUpdatedEventListener() {
        return taskUpdatedEvent -> collectedEvents.add(taskUpdatedEvent);
    }

    @Bean
    public TaskEventListener<TaskCompletedEvent> taskCompletedEventListener() {
        return taskCompletedEvent -> collectedEvents.add(taskCompletedEvent);
    }

    @Bean
    public TaskEventListener<TaskSuspendedEvent> taskSuspendedEventListener() {
        return taskSuspendedEvent -> collectedEvents.add(taskSuspendedEvent);
    }

    @Bean
    public TaskEventListener<TaskAssignedEvent> taskAssignedEventListener() {
        return taskAssignedEvent -> collectedEvents.add(taskAssignedEvent);
    }


    @Bean(name = "service-implementation")
    public Connector serviceImplementation() {
        return integrationContext -> {
            connector1Executed = true;
            
            resultIntegrationContext = integrationContext;
            
            return integrationContext;
        };
    }

    @Bean(name = "service-implementation-modify-data")
    public Connector serviceImplementationModifyData() {
        return integrationContext -> {
            connector2Executed = true;
            integrationContext.getOutBoundVariables().put("var1", integrationContext.getInBoundVariables().get("var1") + "-modified");
            return integrationContext;
        };
    }

    
    public static IntegrationContext getResultIntegrationContext() {
        return resultIntegrationContext;
    }

    
    public static void reset() {
        collectedEvents.clear();
        resultIntegrationContext = null;
        connector1Executed = false;
        connector2Executed = false;
    }

    
    public static boolean isConnector1Executed() {
        return connector1Executed;
    }

    
    public static boolean isConnector2Executed() {
        return connector2Executed;
    }

}
