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

package org.activiti.runtime.api.conf;

import org.activiti.api.process.model.events.BPMNActivityCancelledEvent;
import org.activiti.api.process.model.events.BPMNActivityCompletedEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.process.model.events.BPMNErrorReceivedEvent;
import org.activiti.api.process.model.events.BPMNMessageReceivedEvent;
import org.activiti.api.process.model.events.BPMNMessageSentEvent;
import org.activiti.api.process.model.events.BPMNMessageWaitingEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;
import org.activiti.api.process.model.events.BPMNTimerCancelledEvent;
import org.activiti.api.process.model.events.BPMNTimerExecutedEvent;
import org.activiti.api.process.model.events.BPMNTimerFailedEvent;
import org.activiti.api.process.model.events.BPMNTimerFiredEvent;
import org.activiti.api.process.model.events.BPMNTimerRetriesDecrementedEvent;
import org.activiti.api.process.model.events.BPMNTimerScheduledEvent;
import org.activiti.api.process.model.events.MessageSubscriptionCancelledEvent;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.ProcessCreatedEvent;
import org.activiti.api.process.runtime.events.ProcessResumedEvent;
import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.process.runtime.events.ProcessSuspendedEvent;
import org.activiti.api.process.runtime.events.ProcessUpdatedEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.event.EventSubscriptionPayloadMappingProvider;
import org.activiti.runtime.api.conf.impl.ProcessRuntimeConfigurationImpl;
import org.activiti.runtime.api.event.impl.BPMNErrorConverter;
import org.activiti.runtime.api.event.impl.BPMNMessageConverter;
import org.activiti.runtime.api.event.impl.BPMNTimerConverter;
import org.activiti.runtime.api.event.impl.MessageSubscriptionConverter;
import org.activiti.runtime.api.event.impl.StartMessageSubscriptionConverter;
import org.activiti.runtime.api.event.impl.ToAPIProcessCreatedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPIProcessStartedEventConverter;
import org.activiti.runtime.api.event.impl.ToActivityCancelledConverter;
import org.activiti.runtime.api.event.impl.ToActivityCompletedConverter;
import org.activiti.runtime.api.event.impl.ToActivityStartedConverter;
import org.activiti.runtime.api.event.impl.ToErrorReceivedConverter;
import org.activiti.runtime.api.event.impl.ToMessageReceivedConverter;
import org.activiti.runtime.api.event.impl.ToMessageSentConverter;
import org.activiti.runtime.api.event.impl.ToMessageSubscriptionCancelledConverter;
import org.activiti.runtime.api.event.impl.ToMessageWaitingConverter;
import org.activiti.runtime.api.event.impl.ToProcessCancelledConverter;
import org.activiti.runtime.api.event.impl.ToProcessCompletedConverter;
import org.activiti.runtime.api.event.impl.ToProcessResumedConverter;
import org.activiti.runtime.api.event.impl.ToProcessSuspendedConverter;
import org.activiti.runtime.api.event.impl.ToProcessUpdatedConverter;
import org.activiti.runtime.api.event.impl.ToSequenceFlowTakenConverter;
import org.activiti.runtime.api.event.impl.ToSignalReceivedConverter;
import org.activiti.runtime.api.event.impl.ToTimerCancelledConverter;
import org.activiti.runtime.api.event.impl.ToTimerExecutedConverter;
import org.activiti.runtime.api.event.impl.ToTimerFailedConverter;
import org.activiti.runtime.api.event.impl.ToTimerFiredConverter;
import org.activiti.runtime.api.event.impl.ToTimerRetriesDecrementedConverter;
import org.activiti.runtime.api.event.impl.ToTimerScheduledConverter;
import org.activiti.runtime.api.event.internal.ActivityCancelledListenerDelegate;
import org.activiti.runtime.api.event.internal.ActivityCompletedListenerDelegate;
import org.activiti.runtime.api.event.internal.ActivityStartedListenerDelegate;
import org.activiti.runtime.api.event.internal.ErrorReceivedListenerDelegate;
import org.activiti.runtime.api.event.internal.MessageReceivedListenerDelegate;
import org.activiti.runtime.api.event.internal.MessageSentListenerDelegate;
import org.activiti.runtime.api.event.internal.MessageSubscriptionCancelledListenerDelegate;
import org.activiti.runtime.api.event.internal.MessageWaitingListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessCancelledListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessCompletedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessCreatedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessResumedEventListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessStartedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessSuspendedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessUpdatedListenerDelegate;
import org.activiti.runtime.api.event.internal.SequenceFlowTakenListenerDelegate;
import org.activiti.runtime.api.event.internal.SignalReceivedListenerDelegate;
import org.activiti.runtime.api.event.internal.TimerCancelledListenerDelegate;
import org.activiti.runtime.api.event.internal.TimerExecutedListenerDelegate;
import org.activiti.runtime.api.event.internal.TimerFailedListenerDelegate;
import org.activiti.runtime.api.event.internal.TimerFiredListenerDelegate;
import org.activiti.runtime.api.event.internal.TimerRetriesDecrementedListenerDelegate;
import org.activiti.runtime.api.event.internal.TimerScheduledListenerDelegate;
import org.activiti.runtime.api.impl.EventSubscriptionVariablesMappingProvider;
import org.activiti.runtime.api.impl.ExpressionResolver;
import org.activiti.runtime.api.impl.ExtensionsVariablesMappingProvider;
import org.activiti.runtime.api.impl.ProcessAdminRuntimeImpl;
import org.activiti.runtime.api.impl.ProcessRuntimeImpl;
import org.activiti.runtime.api.impl.ProcessVariablesPayloadValidator;
import org.activiti.runtime.api.impl.RuntimeReceiveMessagePayloadEventListener;
import org.activiti.runtime.api.impl.RuntimeSignalPayloadEventListener;
import org.activiti.runtime.api.impl.VariableNameValidator;
import org.activiti.runtime.api.message.ReceiveMessagePayloadEventListener;
import org.activiti.runtime.api.model.impl.APIDeploymentConverter;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.runtime.api.model.impl.ToActivityConverter;
import org.activiti.runtime.api.model.impl.ToSignalConverter;
import org.activiti.runtime.api.signal.SignalPayloadEventListener;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.ProcessVariablesInitiator;
import org.activiti.spring.process.variable.VariableParsingService;
import org.activiti.spring.process.variable.VariableValidationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

import static java.util.Collections.emptyList;

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
    public EventSubscriptionPayloadMappingProvider eventSubscriptionPayloadMappingProvider(
        ExtensionsVariablesMappingProvider variablesMappingProvider) {
        return new EventSubscriptionVariablesMappingProvider(variablesMappingProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessRuntime processRuntime(RepositoryService repositoryService,
                                         APIProcessDefinitionConverter processDefinitionConverter,
                                         RuntimeService runtimeService,
                                         TaskService taskService,
                                         ProcessSecurityPoliciesManager securityPoliciesManager,
                                         APIProcessInstanceConverter processInstanceConverter,
                                         APIVariableInstanceConverter variableInstanceConverter,
                                         APIDeploymentConverter apiDeploymentConverter,
                                         ProcessRuntimeConfiguration processRuntimeConfiguration,
                                         ApplicationEventPublisher eventPublisher,
                                         ProcessVariablesPayloadValidator processVariablesValidator,
                                         SecurityManager securityManager) {
        return new ProcessRuntimeImpl(repositoryService,
                processDefinitionConverter,
                runtimeService,
                taskService,
                securityPoliciesManager,
                processInstanceConverter,
                variableInstanceConverter,
                apiDeploymentConverter,
                processRuntimeConfiguration,
                eventPublisher,
                processVariablesValidator,
                securityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessAdminRuntime processAdminRuntime(RepositoryService repositoryService,
                                                   APIProcessDefinitionConverter processDefinitionConverter,
                                                   RuntimeService runtimeService,
                                                   APIProcessInstanceConverter processInstanceConverter,
                                                   ApplicationEventPublisher eventPublisher,
                                                   ProcessVariablesPayloadValidator processVariablesValidator,
                                                   APIVariableInstanceConverter variableInstanceConverter) {
        return new ProcessAdminRuntimeImpl(repositoryService,
                processDefinitionConverter,
                runtimeService,
                processInstanceConverter,
                variableInstanceConverter,
                eventPublisher,
                processVariablesValidator
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageSubscriptionConverter messageEventSubscriptionConverter() {
        return new MessageSubscriptionConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public StartMessageSubscriptionConverter startMessageEventSubscriptionConverter() {
        return new StartMessageSubscriptionConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public APIProcessDefinitionConverter apiProcessDefinitionConverter(RepositoryService repositoryService) {
        return new APIProcessDefinitionConverter(repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessVariablesInitiator processVariablesInitiator(ProcessExtensionService processExtensionService,
                                                               VariableParsingService variableParsingService,
                                                               VariableValidationService variableValidationService,
                                                               ExtensionsVariablesMappingProvider mappingProvider) {
        return new ProcessVariablesInitiator(processExtensionService,
                                             variableParsingService,
                                             variableValidationService,
                                             mappingProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessVariablesPayloadValidator processVariablesValidator(DateFormatterProvider dateFormatterProvider,
                                                                      ProcessExtensionService processExtensionService,
                                                                      VariableValidationService variableValidationService,
                                                                      VariableNameValidator variableNameValidator,
                                                                      ExpressionResolver expressionResolver) {
        return new ProcessVariablesPayloadValidator(dateFormatterProvider,
                processExtensionService,
                variableValidationService,
                variableNameValidator,
                expressionResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public APIProcessInstanceConverter apiProcessInstanceConverter() {
        return new APIProcessInstanceConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public APIDeploymentConverter apiDeploymentConverter(){
        return new APIDeploymentConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessRuntimeConfiguration processRuntimeConfiguration(@Autowired(required = false) @Lazy List<ProcessRuntimeEventListener<?>> processRuntimeEventListeners,
                                                                   @Autowired(required = false) @Lazy List<VariableEventListener<?>> variableEventListeners) {
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
        return eventListeners != null ? eventListeners : emptyList();
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
        APIProcessInstanceConverter processInstanceConverter,
        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCancelledEvent>> eventListeners) {

        return () -> runtimeService.addEventListener(
            new ProcessCancelledListenerDelegate(getInitializedListeners(eventListeners),
                new ToProcessCancelledConverter(processInstanceConverter)),
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

    @Bean
    @ConditionalOnMissingBean(name = "registerMessageSubscriptionCancelledListenerDelegate")
    public InitializingBean registerMessageSubscriptionCancelledListenerDelegate(RuntimeService runtimeService,
                                                                                 @Autowired(required = false) List<ProcessRuntimeEventListener<MessageSubscriptionCancelledEvent>> eventListeners,
                                                                                 MessageSubscriptionConverter converter) {
        return () -> runtimeService.addEventListener(new MessageSubscriptionCancelledListenerDelegate(getInitializedListeners(eventListeners),
                                                                                                      new ToMessageSubscriptionCancelledConverter(converter)),
                                                     ActivitiEventType.ENTITY_DELETED);
    }

}
