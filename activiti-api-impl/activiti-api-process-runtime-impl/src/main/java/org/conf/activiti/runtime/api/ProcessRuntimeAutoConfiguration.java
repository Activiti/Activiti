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

package org.conf.activiti.runtime.api;

import java.util.Collections;
import java.util.List;

import org.activiti.api.process.model.events.BPMNActivityCancelledEvent;
import org.activiti.api.process.model.events.BPMNActivityCompletedEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;
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
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.runtime.api.conf.CommonRuntimeAutoConfiguration;
import org.activiti.runtime.api.conf.impl.ProcessRuntimeConfigurationImpl;
import org.activiti.runtime.api.event.impl.ToAPIProcessCreatedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPIProcessStartedEventConverter;
import org.activiti.runtime.api.event.impl.ToActivityCancelledConverter;
import org.activiti.runtime.api.event.impl.ToActivityCompletedConverter;
import org.activiti.runtime.api.event.impl.ToSignalReceivedConverter;
import org.activiti.runtime.api.event.impl.ToActivityStartedConverter;
import org.activiti.runtime.api.event.impl.ToProcessCancelledConverter;
import org.activiti.runtime.api.event.impl.ToProcessCompletedConverter;
import org.activiti.runtime.api.event.impl.ToProcessResumedConverter;
import org.activiti.runtime.api.event.impl.ToProcessSuspendedConverter;
import org.activiti.runtime.api.event.impl.ToProcessUpdatedConverter;
import org.activiti.runtime.api.event.impl.ToSequenceFlowTakenConverter;
import org.activiti.runtime.api.event.internal.ActivityCancelledListenerDelegate;
import org.activiti.runtime.api.event.internal.ActivityCompletedListenerDelegate;
import org.activiti.runtime.api.event.internal.SignalReceivedListenerDelegate;
import org.activiti.runtime.api.event.internal.ActivityStartedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessCancelledListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessCompletedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessCreatedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessResumedEventListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessStartedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessSuspendedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessUpdatedListenerDelegate;
import org.activiti.runtime.api.event.internal.SequenceFlowTakenListenerDelegate;
import org.activiti.runtime.api.impl.ProcessAdminRuntimeImpl;
import org.activiti.runtime.api.impl.ProcessRuntimeImpl;
import org.activiti.runtime.api.impl.RuntimeSignalPayloadEventListener;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.runtime.api.model.impl.ToActivityConverter;
import org.activiti.runtime.api.model.impl.ToSignalConverter;
import org.activiti.runtime.api.signal.SignalPayloadEventListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(CommonRuntimeAutoConfiguration.class)
public class ProcessRuntimeAutoConfiguration {
    
    /**
     * Creates default SignalPayloadEventListener bean if no existing bean found in ApplicationContext
     */
    @Bean
    @ConditionalOnMissingBean(SignalPayloadEventListener.class)
    public SignalPayloadEventListener signalPayloadEventListener(RuntimeService runtimeService) {
        return new RuntimeSignalPayloadEventListener(runtimeService);
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
                                         ApplicationEventPublisher eventPublisher) {
        return new ProcessRuntimeImpl(repositoryService,
                processDefinitionConverter,
                runtimeService,
                securityPoliciesManager,
                processInstanceConverter,
                variableInstanceConverter,
                processRuntimeConfiguration,
                eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessAdminRuntime processAdminRuntime(RepositoryService repositoryService,
                                                   APIProcessDefinitionConverter processDefinitionConverter,
                                                   RuntimeService runtimeService,
                                                   APIProcessInstanceConverter processInstanceConverter,
                                                   ApplicationEventPublisher eventPublisher) {
        return new ProcessAdminRuntimeImpl(repositoryService,
                processDefinitionConverter,
                runtimeService,
                processInstanceConverter,
                eventPublisher
        );
    }

    @Bean
    public APIProcessDefinitionConverter apiProcessDefinitionConverter(RepositoryService repositoryService) {
        return new APIProcessDefinitionConverter(repositoryService);
    }

    @Bean
    public APIProcessInstanceConverter apiProcessInstanceConverter() {
        return new APIProcessInstanceConverter();
    }


    @Bean
    public ProcessRuntimeConfiguration processRuntimeConfiguration(@Autowired(required = false) List<ProcessRuntimeEventListener<?>> processRuntimeEventListeners,
                                                                   @Autowired(required = false) List<VariableEventListener<?>> variableEventListeners) {
        return new ProcessRuntimeConfigurationImpl(getInitializedListeners(processRuntimeEventListeners),
                getInitializedListeners(variableEventListeners));
    }

    @Bean
    public ToAPIProcessStartedEventConverter apiProcessStartedEventConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToAPIProcessStartedEventConverter(processInstanceConverter);
    }

    @Bean
    public ToAPIProcessCreatedEventConverter apiProcessCreatedEventConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToAPIProcessCreatedEventConverter(processInstanceConverter);
    }

    @Bean
    public ToProcessUpdatedConverter processUpdatedConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToProcessUpdatedConverter(processInstanceConverter);
    }

    @Bean
    public ToProcessResumedConverter processResumedConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToProcessResumedConverter(processInstanceConverter);
    }

    @Bean
    public ToProcessSuspendedConverter processSuspendedConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToProcessSuspendedConverter(processInstanceConverter);
    }

    private <T> List<T> getInitializedListeners(List<T> eventListeners) {
        return eventListeners != null ? eventListeners : Collections.emptyList();
    }

    @Bean
    public InitializingBean registerProcessStartedEventListenerDelegate(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessStartedEvent>> listeners,
                                                                        ToAPIProcessStartedEventConverter processStartedEventConverter) {
        return () -> runtimeService.addEventListener(new ProcessStartedListenerDelegate(getInitializedListeners(listeners),
                        processStartedEventConverter),
                ActivitiEventType.PROCESS_STARTED);
    }

    @Bean
    public InitializingBean registerProcessCreatedEventListenerDelegate(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCreatedEvent>> eventListeners,
                                                                        ToAPIProcessCreatedEventConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessCreatedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_CREATED);
    }

    @Bean
    public InitializingBean registerProcessUpdatedEventListenerDelegate(RuntimeService runtimeService,
                                                                          @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessUpdatedEvent>> eventListeners,
                                                                          ToProcessUpdatedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessUpdatedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_UPDATED);
    }

    @Bean
    public InitializingBean registerProcessSuspendedEventListenerDelegate(RuntimeService runtimeService,
                                                                          @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessSuspendedEvent>> eventListeners,
                                                                          ToProcessSuspendedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessSuspendedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_SUSPENDED);
    }

    @Bean
    public InitializingBean registerProcessResumedEventListenerDelegate(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessResumedEvent>> eventListeners,
                                                                        ToProcessResumedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessResumedEventListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_ACTIVATED);
    }

    @Bean
    public ToProcessCompletedConverter processCompletedConverter(APIProcessInstanceConverter processInstanceConverter) {
        return new ToProcessCompletedConverter(processInstanceConverter);
    }

    @Bean
    public InitializingBean registerProcessCompletedListenerDelegate(RuntimeService runtimeService,
                                                                     @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCompletedEvent>> eventListeners,
                                                                     ToProcessCompletedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessCompletedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.PROCESS_COMPLETED);
    }

    @Bean
    public InitializingBean registerProcessCancelledListenerDelegate(RuntimeService runtimeService,
                                                                     @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCancelledEvent>> eventListeners) {
        return () -> runtimeService.addEventListener(new ProcessCancelledListenerDelegate(getInitializedListeners(eventListeners),
                        new ToProcessCancelledConverter()),
                ActivitiEventType.PROCESS_CANCELLED);
    }

    @Bean
    public ToActivityConverter activityConverter() {
        return new ToActivityConverter();
    }
    
    @Bean
    public ToSignalConverter signalConverter() {
        return new ToSignalConverter();
    }

    @Bean
    public InitializingBean registerActivityStartedListenerDelegate(RuntimeService runtimeService,
                                                                    @Autowired(required = false) List<BPMNElementEventListener<BPMNActivityStartedEvent>> eventListeners,
                                                                    ToActivityConverter activityConverter) {
        return () -> runtimeService.addEventListener(new ActivityStartedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToActivityStartedConverter(activityConverter)),
                ActivitiEventType.ACTIVITY_STARTED);
    }

    @Bean
    public InitializingBean registerActivityCompletedListenerDelegate(RuntimeService runtimeService,
                                                                      @Autowired(required = false) List<BPMNElementEventListener<BPMNActivityCompletedEvent>> eventListeners,
                                                                      ToActivityConverter activityConverter) {
        return () -> runtimeService.addEventListener(new ActivityCompletedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToActivityCompletedConverter(activityConverter)),
                ActivitiEventType.ACTIVITY_COMPLETED);
    }

    @Bean
    public InitializingBean registerActivityCancelledListenerDelegate(RuntimeService runtimeService,
                                                                      @Autowired(required = false) List<BPMNElementEventListener<BPMNActivityCancelledEvent>> eventListeners,
                                                                      ToActivityConverter activityConverter) {
        return () -> runtimeService.addEventListener(new ActivityCancelledListenerDelegate(getInitializedListeners(eventListeners),
                        new ToActivityCancelledConverter(activityConverter)),
                ActivitiEventType.ACTIVITY_CANCELLED);
    }
    
    @Bean
    public InitializingBean registerActivitySignaledListenerDelegate(RuntimeService runtimeService,
                                                                    @Autowired(required = false) List<BPMNElementEventListener<BPMNSignalReceivedEvent>> eventListeners,
                                                                    ToSignalConverter signalConverter) {
        return () -> runtimeService.addEventListener(new SignalReceivedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToSignalReceivedConverter(signalConverter)),
                ActivitiEventType.ACTIVITY_SIGNALED);
    }

    @Bean
    public InitializingBean registerSequenceFlowTakenListenerDelegate(RuntimeService runtimeService,
                                                                      @Autowired(required = false) List<BPMNElementEventListener<BPMNSequenceFlowTakenEvent>> eventListeners) {
        return () -> runtimeService.addEventListener(new SequenceFlowTakenListenerDelegate(getInitializedListeners(eventListeners),
                        new ToSequenceFlowTakenConverter()),
                ActivitiEventType.SEQUENCEFLOW_TAKEN);
    }
}
