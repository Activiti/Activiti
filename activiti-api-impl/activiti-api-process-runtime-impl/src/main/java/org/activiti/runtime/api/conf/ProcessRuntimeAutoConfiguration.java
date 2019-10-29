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

package org.activiti.runtime.api.conf;

import org.activiti.api.process.model.events.*;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.process.runtime.events.*;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.event.EventSubscriptionPayloadMappingProvider;
import org.activiti.runtime.api.conf.impl.ProcessRuntimeConfigurationImpl;
import org.activiti.runtime.api.event.impl.*;
import org.activiti.runtime.api.event.internal.*;
import org.activiti.runtime.api.impl.*;
import org.activiti.runtime.api.message.ReceiveMessagePayloadEventListener;
import org.activiti.runtime.api.model.impl.*;
import org.activiti.runtime.api.signal.SignalPayloadEventListener;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.variable.VariableValidationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
@AutoConfigureAfter(CommonRuntimeAutoConfiguration.class)
public class ProcessRuntimeAutoConfiguration {

    /**
     * Creates default SignalPayloadEventListener bean if no existing bean found in ApplicationContext.
     */
    @Bean
    @ConditionalOnMissingBean(SignalPayloadEventListener.class)
    public SignalPayloadEventListener signalPayloadEventListener(RuntimeService runtimeService) {
        return new RuntimeSignalPayloadEventListener(runtimeService);
    }

    /**
     * Creates default MessagePayloadEventListener bean if no existing bean found in ApplicationContext.
     */
    @Bean
    @ConditionalOnMissingBean(ReceiveMessagePayloadEventListener.class)
    public ReceiveMessagePayloadEventListener receiveMessagePayloadEventListener(RuntimeService runtimeService,
                                                                                 ManagementService managementService) {
        return new RuntimeReceiveMessagePayloadEventListener(runtimeService,
                managementService);
    }

    @Bean
    @ConditionalOnMissingBean(EventSubscriptionPayloadMappingProvider.class)
    public EventSubscriptionPayloadMappingProvider eventSubscriptionPayloadMappingProvider(VariablesMappingProvider variablesMappingProvider) {
        return new EventSubscriptionVariablesMappingProvider(variablesMappingProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessRuntime processRuntime(RepositoryService repositoryService,
                                         APIProcessDefinitionConverter processDefinitionConverter,
                                         RuntimeService runtimeService,
                                         ProcessSecurityPoliciesManager securityPoliciesManager,
                                         APIProcessInstanceConverter processInstanceConverter,
                                         APIVariableInstanceConverter variableInstanceConverter,
                                         ProcessRuntimeConfiguration processRuntimeConfiguration,
                                         ApplicationEventPublisher eventPublisher,
                                         ProcessVariablesPayloadValidator processVariablesValidator) {
        return new ProcessRuntimeImpl(repositoryService,
                processDefinitionConverter,
                runtimeService,
                securityPoliciesManager,
                processInstanceConverter,
                variableInstanceConverter,
                processRuntimeConfiguration,
                eventPublisher,
                processVariablesValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessAdminRuntime processAdminRuntime(RepositoryService repositoryService,
                                                   APIProcessDefinitionConverter processDefinitionConverter,
                                                   RuntimeService runtimeService,
                                                   APIProcessInstanceConverter processInstanceConverter,
                                                   ApplicationEventPublisher eventPublisher,
                                                   ProcessVariablesPayloadValidator processVariablesValidator) {
        return new ProcessAdminRuntimeImpl(repositoryService,
                processDefinitionConverter,
                runtimeService,
                processInstanceConverter,
                eventPublisher,
                processVariablesValidator
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public APIProcessDefinitionConverter apiProcessDefinitionConverter(RepositoryService repositoryService) {
        return new APIProcessDefinitionConverter(repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessVariablesPayloadValidator processVariablesValidator(DateFormatterProvider dateFormatterProvider,
                                                                      ProcessExtensionService processExtensionService,
                                                                      VariableValidationService variableValidationService,
                                                                      VariableNameValidator variableNameValidator) {
        return new ProcessVariablesPayloadValidator(dateFormatterProvider,
                processExtensionService,
                variableValidationService,
                variableNameValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public APIProcessInstanceConverter apiProcessInstanceConverter() {
        return new APIProcessInstanceConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessRuntimeConfiguration processRuntimeConfiguration(@Autowired(required = false) List<ProcessRuntimeEventListener<?>> processRuntimeEventListeners,
                                                                   @Autowired(required = false) List<VariableEventListener<?>> variableEventListeners) {
        return new ProcessRuntimeConfigurationImpl(getInitializedListeners(processRuntimeEventListeners),
                getInitializedListeners(variableEventListeners));
    }

    @Bean
    @ConditionalOnMissingBean
    public ToAPIProcessStartedEventConverter apiProcessStartedEventConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToAPIProcessStartedEventConverter(processInstanceConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ToAPIProcessCreatedEventConverter apiProcessCreatedEventConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToAPIProcessCreatedEventConverter(processInstanceConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ToProcessUpdatedConverter processUpdatedConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToProcessUpdatedConverter(processInstanceConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ToProcessResumedConverter processResumedConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToProcessResumedConverter(processInstanceConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ToProcessSuspendedConverter processSuspendedConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToProcessSuspendedConverter(processInstanceConverter);
    }

    private <T> List<T> getInitializedListeners(List<T> eventListeners) {
        return eventListeners != null ? eventListeners : Collections.emptyList();
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerProcessStartedEventListenerDelegate")
    public InitializingBean registerProcessStartedEventListenerDelegate(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessStartedEvent>> listeners,
                                                                        ToAPIProcessStartedEventConverter processStartedEventConverter) {
        return () -> runtimeService.addEventListener(new ProcessStartedListenerDelegate(getInitializedListeners(listeners),
                        processStartedEventConverter),
                ActivitiEventType.PROCESS_STARTED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerProcessCreatedEventListenerDelegate")
    public InitializingBean registerProcessCreatedEventListenerDelegate(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCreatedEvent>> eventListeners,
                                                                        ToAPIProcessCreatedEventConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessCreatedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_CREATED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerProcessUpdatedEventListenerDelegate")
    public InitializingBean registerProcessUpdatedEventListenerDelegate(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessUpdatedEvent>> eventListeners,
                                                                        ToProcessUpdatedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessUpdatedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_UPDATED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerProcessSuspendedEventListenerDelegate")
    public InitializingBean registerProcessSuspendedEventListenerDelegate(RuntimeService runtimeService,
                                                                          @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessSuspendedEvent>> eventListeners,
                                                                          ToProcessSuspendedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessSuspendedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_SUSPENDED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerProcessResumedEventListenerDelegate")
    public InitializingBean registerProcessResumedEventListenerDelegate(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessResumedEvent>> eventListeners,
                                                                        ToProcessResumedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessResumedEventListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_ACTIVATED);
    }

    @Bean
    @ConditionalOnMissingBean
    public ToProcessCompletedConverter processCompletedConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToProcessCompletedConverter(processInstanceConverter);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerProcessCompletedListenerDelegate")
    public InitializingBean registerProcessCompletedListenerDelegate(RuntimeService runtimeService,
                                                                     @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCompletedEvent>> eventListeners,
                                                                     ToProcessCompletedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessCompletedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.PROCESS_COMPLETED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerProcessCancelledListenerDelegate")
    public InitializingBean registerProcessCancelledListenerDelegate(RuntimeService runtimeService,
                                                                     @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCancelledEvent>> eventListeners) {
        return () -> runtimeService.addEventListener(new ProcessCancelledListenerDelegate(getInitializedListeners(eventListeners),
                        new ToProcessCancelledConverter()),
                ActivitiEventType.PROCESS_CANCELLED);
    }

    @Bean
    @ConditionalOnMissingBean
    public ToActivityConverter activityConverter() {
        return new ToActivityConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ToSignalConverter signalConverter() {
        return new ToSignalConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public BPMNTimerConverter bpmnTimerConveter() {
        return new BPMNTimerConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public BPMNMessageConverter bpmnMessageConveter() {
        return new BPMNMessageConverter();
    }

    @Bean
    public BPMNErrorConverter bpmnErrorConverter() {
        return new BPMNErrorConverter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerActivityStartedListenerDelegate")
    public InitializingBean registerActivityStartedListenerDelegate(RuntimeService runtimeService,
                                                                    @Autowired(required = false) List<BPMNElementEventListener<BPMNActivityStartedEvent>> eventListeners,
                                                                    ToActivityConverter activityConverter) {
        return () -> runtimeService.addEventListener(new ActivityStartedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToActivityStartedConverter(activityConverter)),
                ActivitiEventType.ACTIVITY_STARTED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerActivityCompletedListenerDelegate")
    public InitializingBean registerActivityCompletedListenerDelegate(RuntimeService runtimeService,
                                                                      @Autowired(required = false) List<BPMNElementEventListener<BPMNActivityCompletedEvent>> eventListeners,
                                                                      ToActivityConverter activityConverter) {
        return () -> runtimeService.addEventListener(new ActivityCompletedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToActivityCompletedConverter(activityConverter)),
                ActivitiEventType.ACTIVITY_COMPLETED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerActivityCancelledListenerDelegate")
    public InitializingBean registerActivityCancelledListenerDelegate(RuntimeService runtimeService,
                                                                      @Autowired(required = false) List<BPMNElementEventListener<BPMNActivityCancelledEvent>> eventListeners,
                                                                      ToActivityConverter activityConverter) {
        return () -> runtimeService.addEventListener(new ActivityCancelledListenerDelegate(getInitializedListeners(eventListeners),
                        new ToActivityCancelledConverter(activityConverter)),
                ActivitiEventType.ACTIVITY_CANCELLED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerActivitySignaledListenerDelegate")
    public InitializingBean registerActivitySignaledListenerDelegate(RuntimeService runtimeService,
                                                                     @Autowired(required = false) List<BPMNElementEventListener<BPMNSignalReceivedEvent>> eventListeners,
                                                                     ToSignalConverter signalConverter) {
        return () -> runtimeService.addEventListener(new SignalReceivedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToSignalReceivedConverter(signalConverter)),
                ActivitiEventType.ACTIVITY_SIGNALED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerTimerFiredListenerDelegate")
    public InitializingBean registerTimerFiredListenerDelegate(RuntimeService runtimeService,
                                                               @Autowired(required = false) List<BPMNElementEventListener<BPMNTimerFiredEvent>> eventListeners,
                                                               BPMNTimerConverter bpmnTimerConverter) {
        return () -> runtimeService.addEventListener(new TimerFiredListenerDelegate(getInitializedListeners(eventListeners),
                        new ToTimerFiredConverter(bpmnTimerConverter)),
                ActivitiEventType.TIMER_FIRED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerTimerScheduledListenerDelegate")
    public InitializingBean registerTimerScheduledListenerDelegate(RuntimeService runtimeService,
                                                                   @Autowired(required = false) List<BPMNElementEventListener<BPMNTimerScheduledEvent>> eventListeners,
                                                                   BPMNTimerConverter bpmnTimerConverter) {
        return () -> runtimeService.addEventListener(new TimerScheduledListenerDelegate(getInitializedListeners(eventListeners),
                        new ToTimerScheduledConverter(bpmnTimerConverter)),
                ActivitiEventType.TIMER_SCHEDULED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerTimerCancelledListenerDelegate")
    public InitializingBean registerTimerCancelledListenerDelegate(RuntimeService runtimeService,
                                                                   @Autowired(required = false) List<BPMNElementEventListener<BPMNTimerCancelledEvent>> eventListeners,
                                                                   BPMNTimerConverter bpmnTimerConverter) {
        return () -> runtimeService.addEventListener(new TimerCancelledListenerDelegate(getInitializedListeners(eventListeners),
                        new ToTimerCancelledConverter(bpmnTimerConverter)),
                ActivitiEventType.JOB_CANCELED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerTimerFailedListenerDelegate")
    public InitializingBean registerTimerFailedListenerDelegate(RuntimeService runtimeService,
                                                                @Autowired(required = false) List<BPMNElementEventListener<BPMNTimerFailedEvent>> eventListeners,
                                                                BPMNTimerConverter bpmnTimerConverter) {
        return () -> runtimeService.addEventListener(new TimerFailedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToTimerFailedConverter(bpmnTimerConverter)),
                ActivitiEventType.JOB_EXECUTION_FAILURE);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerTimerExecutedListenerDelegate")
    public InitializingBean registerTimerExecutedListenerDelegate(RuntimeService runtimeService,
                                                                  @Autowired(required = false) List<BPMNElementEventListener<BPMNTimerExecutedEvent>> eventListeners,
                                                                  BPMNTimerConverter bpmnTimerConverter) {
        return () -> runtimeService.addEventListener(new TimerExecutedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToTimerExecutedConverter(bpmnTimerConverter)),
                ActivitiEventType.JOB_EXECUTION_SUCCESS);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerTimerRetriesDecrementedListenerDelegate")
    public InitializingBean registerTimerRetriesDecrementedListenerDelegate(RuntimeService runtimeService,
                                                                            @Autowired(required = false) List<BPMNElementEventListener<BPMNTimerRetriesDecrementedEvent>> eventListeners,
                                                                            BPMNTimerConverter bpmnTimerConverter) {
        return () -> runtimeService.addEventListener(new TimerRetriesDecrementedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToTimerRetriesDecrementedConverter(bpmnTimerConverter)),
                ActivitiEventType.JOB_RETRIES_DECREMENTED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerMessageSentListenerDelegate")
    public InitializingBean registerMessageSentListenerDelegate(RuntimeService runtimeService,
                                                                @Autowired(required = false) List<BPMNElementEventListener<BPMNMessageSentEvent>> eventListeners,
                                                                BPMNMessageConverter bpmnMessageConverter) {
        return () -> runtimeService.addEventListener(new MessageSentListenerDelegate(getInitializedListeners(eventListeners),
                        new ToMessageSentConverter(bpmnMessageConverter)),
                ActivitiEventType.ACTIVITY_MESSAGE_SENT);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerMessageReceivedListenerDelegate")
    public InitializingBean registerMessageReceivedListenerDelegate(RuntimeService runtimeService,
                                                                    @Autowired(required = false) List<BPMNElementEventListener<BPMNMessageReceivedEvent>> eventListeners,
                                                                    BPMNMessageConverter bpmnMessageConverter) {
        return () -> runtimeService.addEventListener(new MessageReceivedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToMessageReceivedConverter(bpmnMessageConverter)),
                ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerMessageWaitingListenerDelegate")
    public InitializingBean registerMessageWaitingListenerDelegate(RuntimeService runtimeService,
                                                                   @Autowired(required = false) List<BPMNElementEventListener<BPMNMessageWaitingEvent>> eventListeners,
                                                                   BPMNMessageConverter bpmnMessageConverter) {
        return () -> runtimeService.addEventListener(new MessageWaitingListenerDelegate(getInitializedListeners(eventListeners),
                        new ToMessageWaitingConverter(bpmnMessageConverter)),
                ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerSequenceFlowTakenListenerDelegate")
    public InitializingBean registerSequenceFlowTakenListenerDelegate(RuntimeService runtimeService,
                                                                      @Autowired(required = false) List<BPMNElementEventListener<BPMNSequenceFlowTakenEvent>> eventListeners) {
        return () -> runtimeService.addEventListener(new SequenceFlowTakenListenerDelegate(getInitializedListeners(eventListeners),
                        new ToSequenceFlowTakenConverter()),
                ActivitiEventType.SEQUENCEFLOW_TAKEN);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registerErrorReceviedListenerDelegate")
    public InitializingBean registerErrorReceviedListenerDelegate(RuntimeService runtimeService,
                                                                  @Autowired(required = false) List<BPMNElementEventListener<BPMNErrorReceivedEvent>> eventListeners,
                                                                  BPMNErrorConverter bpmnErrorConverter) {
        return () -> runtimeService.addEventListener(new ErrorReceivedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToErrorReceivedConverter(bpmnErrorConverter)),
                ActivitiEventType.ACTIVITY_ERROR_RECEIVED);
    }
}
