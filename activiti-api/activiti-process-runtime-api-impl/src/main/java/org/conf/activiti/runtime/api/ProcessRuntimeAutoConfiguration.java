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

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.runtime.api.ProcessAdminRuntime;
import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.conf.impl.ProcessRuntimeConfigurationImpl;
import org.activiti.runtime.api.event.BPMNActivityCancelled;
import org.activiti.runtime.api.event.BPMNActivityCompleted;
import org.activiti.runtime.api.event.BPMNActivityStarted;
import org.activiti.runtime.api.event.ProcessCancelled;
import org.activiti.runtime.api.event.ProcessCompleted;
import org.activiti.runtime.api.event.ProcessCreated;
import org.activiti.runtime.api.event.ProcessResumed;
import org.activiti.runtime.api.event.ProcessStarted;
import org.activiti.runtime.api.event.ProcessSuspended;
import org.activiti.runtime.api.event.SequenceFlowTaken;
import org.activiti.runtime.api.event.VariableEventListener;
import org.activiti.runtime.api.event.impl.ToAPIProcessCreatedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPIProcessStartedEventConverter;
import org.activiti.runtime.api.event.impl.ToActivityCancelledConverter;
import org.activiti.runtime.api.event.impl.ToActivityCompletedConverter;
import org.activiti.runtime.api.event.impl.ToActivityStartedConverter;
import org.activiti.runtime.api.event.impl.ToProcessCancelledConverter;
import org.activiti.runtime.api.event.impl.ToProcessCompletedConverter;
import org.activiti.runtime.api.event.impl.ToProcessResumedConverter;
import org.activiti.runtime.api.event.impl.ToProcessSuspendedConverter;
import org.activiti.runtime.api.event.impl.ToSequenceFlowTakenConverter;
import org.activiti.runtime.api.event.internal.ActivityCancelledListenerDelegate;
import org.activiti.runtime.api.event.internal.ActivityCompletedListenerDelegate;
import org.activiti.runtime.api.event.internal.ActivityStartedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessCancelledListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessCompletedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessCreatedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessResumedEventListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessStartedListenerDelegate;
import org.activiti.runtime.api.event.internal.ProcessSuspendedListenerDelegate;
import org.activiti.runtime.api.event.internal.SequenceFlowTakenListenerDelegate;
import org.activiti.runtime.api.event.listener.ProcessRuntimeEventListener;
import org.activiti.runtime.api.impl.ProcessAdminRuntimeImpl;
import org.activiti.runtime.api.impl.ProcessRuntimeImpl;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.runtime.api.model.impl.ToActivityConverter;
import org.activiti.spring.security.policies.ProcessSecurityPoliciesManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessRuntimeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProcessRuntime processRuntime(RepositoryService repositoryService,
                                         APIProcessDefinitionConverter processDefinitionConverter,
                                         RuntimeService runtimeService,
                                         ProcessSecurityPoliciesManager securityPoliciesManager,
                                         APIProcessInstanceConverter processInstanceConverter,
                                         APIVariableInstanceConverter variableInstanceConverter,
                                         ProcessRuntimeConfiguration processRuntimeConfiguration) {
        return new ProcessRuntimeImpl(repositoryService,
                processDefinitionConverter,
                runtimeService,
                                      securityPoliciesManager,
                processInstanceConverter,
                variableInstanceConverter,
                processRuntimeConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessAdminRuntime processAdminRuntime(RepositoryService repositoryService,
                                                   APIProcessDefinitionConverter processDefinitionConverter,
                                                   RuntimeService runtimeService,
                                                   APIProcessInstanceConverter processInstanceConverter) {
        return new ProcessAdminRuntimeImpl(repositoryService,
                processDefinitionConverter,
                runtimeService,
                processInstanceConverter
        );
    }

    @Bean
    public APIProcessDefinitionConverter apiProcessDefinitionConverter() {
        return new APIProcessDefinitionConverter();
    }

    @Bean
    public APIProcessInstanceConverter apiProcessInstanceConverter() {
        return new APIProcessInstanceConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public APIVariableInstanceConverter apiVariableInstanceConverter() {
        return new APIVariableInstanceConverter();
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
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessStarted>> listeners,
                                                                        ToAPIProcessStartedEventConverter processStartedEventConverter) {
        return () -> runtimeService.addEventListener(new ProcessStartedListenerDelegate(getInitializedListeners(listeners),
                        processStartedEventConverter),
                ActivitiEventType.PROCESS_STARTED);
    }

    @Bean
    public InitializingBean registerProcessCreatedEventListenerDelegate(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCreated>> eventListeners,
                                                                        ToAPIProcessCreatedEventConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessCreatedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_CREATED);
    }

    @Bean
    public InitializingBean registerProcessSuspendedEventListenerDelegate(RuntimeService runtimeService,
                                                                          @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessSuspended>> eventListeners,
                                                                          ToProcessSuspendedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessSuspendedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.ENTITY_SUSPENDED);
    }

    @Bean
    public InitializingBean registerProcessResumedEventListenerDelegate(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessResumed>> eventListeners,
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
                                                                     @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCompleted>> eventListeners,
                                                                     ToProcessCompletedConverter converter) {
        return () -> runtimeService.addEventListener(new ProcessCompletedListenerDelegate(getInitializedListeners(eventListeners),
                        converter),
                ActivitiEventType.PROCESS_COMPLETED);
    }

    @Bean
    public InitializingBean registerProcessCancelledListenerDelegate(RuntimeService runtimeService,
                                                                     @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessCancelled>> eventListeners) {
        return () -> runtimeService.addEventListener(new ProcessCancelledListenerDelegate(getInitializedListeners(eventListeners),
                        new ToProcessCancelledConverter()),
                ActivitiEventType.PROCESS_CANCELLED);
    }

    @Bean
    public ToActivityConverter activityConverter() {
        return new ToActivityConverter();
    }

    @Bean
    public InitializingBean registerActivityStartedListenerDelegate(RuntimeService runtimeService,
                                                                    @Autowired(required = false) List<ProcessRuntimeEventListener<BPMNActivityStarted>> eventListeners,
                                                                    ToActivityConverter activityConverter) {
        return () -> runtimeService.addEventListener(new ActivityStartedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToActivityStartedConverter(activityConverter)),
                ActivitiEventType.ACTIVITY_STARTED);
    }

    @Bean
    public InitializingBean registerActivityCompletedListenerDelegate(RuntimeService runtimeService,
                                                                      @Autowired(required = false) List<ProcessRuntimeEventListener<BPMNActivityCompleted>> eventListeners,
                                                                      ToActivityConverter activityConverter) {
        return () -> runtimeService.addEventListener(new ActivityCompletedListenerDelegate(getInitializedListeners(eventListeners),
                        new ToActivityCompletedConverter(activityConverter)),
                ActivitiEventType.ACTIVITY_COMPLETED);
    }

    @Bean
    public InitializingBean registerActivityCancelledListenerDelegate(RuntimeService runtimeService,
                                                                      @Autowired(required = false) List<ProcessRuntimeEventListener<BPMNActivityCancelled>> eventListeners,
                                                                      ToActivityConverter activityConverter) {
        return () -> runtimeService.addEventListener(new ActivityCancelledListenerDelegate(getInitializedListeners(eventListeners),
                        new ToActivityCancelledConverter(activityConverter)),
                ActivitiEventType.ACTIVITY_CANCELLED);
    }

    @Bean
    public InitializingBean registerSequenceFlowTakenListenerDelegate(RuntimeService runtimeService,
                                                                      @Autowired(required = false) List<ProcessRuntimeEventListener<SequenceFlowTaken>> eventListeners) {
        return () -> runtimeService.addEventListener(new SequenceFlowTakenListenerDelegate(getInitializedListeners(eventListeners),
                        new ToSequenceFlowTakenConverter()),
                ActivitiEventType.SEQUENCEFLOW_TAKEN);
    }
}
