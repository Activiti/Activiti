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

import static java.util.Collections.emptyList;

import java.util.List;

import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.api.task.runtime.conf.TaskRuntimeConfiguration;
import org.activiti.api.task.runtime.events.TaskActivatedEvent;
import org.activiti.api.task.runtime.events.TaskAssignedEvent;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.api.task.runtime.events.TaskCandidateGroupAddedEvent;
import org.activiti.api.task.runtime.events.TaskCandidateGroupRemovedEvent;
import org.activiti.api.task.runtime.events.TaskCandidateUserAddedEvent;
import org.activiti.api.task.runtime.events.TaskCandidateUserRemovedEvent;
import org.activiti.api.task.runtime.events.TaskCompletedEvent;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.api.task.runtime.events.TaskSuspendedEvent;
import org.activiti.api.task.runtime.events.TaskUpdatedEvent;
import org.activiti.api.task.runtime.events.listener.TaskRuntimeEventListener;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.runtime.api.conf.impl.TaskRuntimeConfigurationImpl;
import org.activiti.runtime.api.event.impl.ToAPITaskAssignedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPITaskCandidateGroupAddedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPITaskCandidateUserAddedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPITaskCreatedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPITaskUpdatedEventConverter;
import org.activiti.runtime.api.event.impl.ToTaskActivatedConverter;
import org.activiti.runtime.api.event.impl.ToTaskCancelledConverter;
import org.activiti.runtime.api.event.impl.ToTaskCandidateGroupRemovedConverter;
import org.activiti.runtime.api.event.impl.ToTaskCandidateUserRemovedConverter;
import org.activiti.runtime.api.event.impl.ToTaskCompletedConverter;
import org.activiti.runtime.api.event.impl.ToTaskSuspendedConverter;
import org.activiti.runtime.api.event.internal.TaskActivatedListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskAssignedListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskCancelledListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskCandidateGroupAddedListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskCandidateGroupRemovedListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskCandidateUserAddedListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskCandidateUserRemovedListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskCompletedListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskCreatedListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskSuspendedListenerDelegate;
import org.activiti.runtime.api.event.internal.TaskUpdatedListenerDelegate;
import org.activiti.runtime.api.impl.TaskAdminRuntimeImpl;
import org.activiti.runtime.api.impl.TaskRuntimeHelper;
import org.activiti.runtime.api.impl.TaskRuntimeImpl;
import org.activiti.runtime.api.impl.TaskVariablesPayloadValidator;
import org.activiti.runtime.api.impl.VariableNameValidator;
import org.activiti.runtime.api.model.impl.APITaskCandidateGroupConverter;
import org.activiti.runtime.api.model.impl.APITaskCandidateUserConverter;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@AutoConfigureAfter(CommonRuntimeAutoConfiguration.class)
public class TaskRuntimeAutoConfiguration {

    @Bean
    public TaskRuntime taskRuntime(TaskService taskService,
                                   SecurityManager securityManager,
                                   APITaskConverter taskConverter,
                                   APIVariableInstanceConverter variableInstanceConverter,
                                   TaskRuntimeConfiguration configuration,
                                   TaskRuntimeHelper taskRuntimeHelper
                                   ) {
        return new TaskRuntimeImpl(taskService,
                                   securityManager,
                                   taskConverter,
                                   variableInstanceConverter,
                                   configuration,
                                   taskRuntimeHelper);
    }

    @Bean
    public TaskAdminRuntime taskAdminRuntime(TaskService taskService,
                                             APITaskConverter taskConverter,
                                             APIVariableInstanceConverter variableInstanceConverter,
                                             TaskRuntimeHelper taskRuntimeHelper,
                                             SecurityManager securityManager) {
        return new TaskAdminRuntimeImpl(taskService,
                                        taskConverter,
                                        variableInstanceConverter,
                                        taskRuntimeHelper,
                                        securityManager

        );
    }

    @Bean
    public TaskRuntimeHelper taskRuntimeHelper(TaskService taskService,
                                               APITaskConverter taskConverter,
                                               SecurityManager securityManager,
                                               TaskVariablesPayloadValidator taskVariablesValidator) {
        return new TaskRuntimeHelper(
                             taskService,
                             taskConverter,
                             securityManager,
                             taskVariablesValidator
        );
    }

    @Bean
    public APITaskConverter apiTaskConverter(TaskService taskService) {
        return new APITaskConverter(taskService);
    }

    @Bean
    public TaskVariablesPayloadValidator taskVariablesValidator(DateFormatterProvider dateFormatterProvider,
                                                                VariableNameValidator variableNameValidator) {
        return new TaskVariablesPayloadValidator(
                             dateFormatterProvider,
                             variableNameValidator
        );
    }

    @Bean
    public TaskRuntimeConfiguration taskRuntimeConfiguration(@Autowired(required = false) @Lazy List<TaskRuntimeEventListener<?>> taskRuntimeEventListeners,
                                                             @Autowired(required = false) @Lazy List<VariableEventListener<?>> variableEventListeners) {
        return new TaskRuntimeConfigurationImpl(getInitializedTaskRuntimeEventListeners(taskRuntimeEventListeners),
                                                getInitializedTaskRuntimeEventListeners(variableEventListeners));
    }

    @Bean
    public InitializingBean registerTaskCreatedEventListener(RuntimeService runtimeService,
                                                             @Autowired(required = false) List<TaskRuntimeEventListener<TaskCreatedEvent>> listeners,
                                                             ToAPITaskCreatedEventConverter taskCreatedEventConverter) {
        return () -> runtimeService.addEventListener(new TaskCreatedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                     taskCreatedEventConverter),
                                                     ActivitiEventType.TASK_CREATED);
    }

    @Bean
    public InitializingBean registerTaskUpdatedEventListener(RuntimeService runtimeService,
                                                             @Autowired(required = false) List<TaskRuntimeEventListener<TaskUpdatedEvent>> listeners,
                                                             ToAPITaskUpdatedEventConverter taskCreatedEventConverter) {
        return () -> runtimeService.addEventListener(new TaskUpdatedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                     taskCreatedEventConverter),
                                                     ActivitiEventType.ENTITY_UPDATED);
    }

    private <T> List<T> getInitializedTaskRuntimeEventListeners(List<T> taskRuntimeEventListeners) {
        return taskRuntimeEventListeners != null ? taskRuntimeEventListeners : emptyList();
    }

    @Bean
    public InitializingBean registerTaskAssignedEventListener(RuntimeService runtimeService,
                                                              @Autowired(required = false) List<TaskRuntimeEventListener<TaskAssignedEvent>> listeners,
                                                              ToAPITaskAssignedEventConverter taskAssignedEventConverter) {
        return () -> runtimeService.addEventListener(new TaskAssignedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                      taskAssignedEventConverter),
                                                     ActivitiEventType.TASK_ASSIGNED);
    }

    @Bean
    public InitializingBean registerTaskCompletedEventListener(RuntimeService runtimeService,
                                                               @Autowired(required = false) List<TaskRuntimeEventListener<TaskCompletedEvent>> listeners,
                                                               APITaskConverter taskConverter, SecurityManager securityManager) {
        return () -> runtimeService.addEventListener(new TaskCompletedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                       new ToTaskCompletedConverter(taskConverter, securityManager)),
                                                     ActivitiEventType.TASK_COMPLETED);
    }

    @Bean
    public InitializingBean registerTaskCancelledEventListener(RuntimeService runtimeService,
                                                               @Autowired(required = false) List<TaskRuntimeEventListener<TaskCancelledEvent>> taskRuntimeEventListeners,
                                                               APITaskConverter taskConverter) {
        return () -> runtimeService.addEventListener(new TaskCancelledListenerDelegate(getInitializedTaskRuntimeEventListeners(taskRuntimeEventListeners),
                                                                                       new ToTaskCancelledConverter(taskConverter
                                                                                       )),
                                                     ActivitiEventType.ENTITY_DELETED);
    }

    @Bean
    public InitializingBean registerTaskSuspendedListener(RuntimeService runtimeService,
                                                          @Autowired(required = false) List<TaskRuntimeEventListener<TaskSuspendedEvent>> listeners,
                                                          APITaskConverter taskConverter) {
        return () -> runtimeService.addEventListener(new TaskSuspendedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                       new ToTaskSuspendedConverter(taskConverter)),
                                                     ActivitiEventType.ENTITY_SUSPENDED);
    }

    @Bean
    public InitializingBean registerTaskActivatedListener(RuntimeService runtimeService,
                                                          @Autowired(required = false) List<TaskRuntimeEventListener<TaskActivatedEvent>> listeners,
                                                          APITaskConverter taskConverter) {
        return () -> runtimeService.addEventListener(new TaskActivatedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                       new ToTaskActivatedConverter(taskConverter)),
                                                     ActivitiEventType.ENTITY_ACTIVATED);
    }

    @Bean
    public ToAPITaskCreatedEventConverter apiTaskCreatedEventConverter(APITaskConverter taskConverter) {
        return new ToAPITaskCreatedEventConverter(taskConverter);
    }

    @Bean
    public ToAPITaskUpdatedEventConverter apiTaskUpdatedEventConverter(APITaskConverter taskConverter) {
        return new ToAPITaskUpdatedEventConverter(taskConverter);
    }

    @Bean
    public ToAPITaskAssignedEventConverter apiTaskAssignedEventConverter(APITaskConverter taskConverter) {
        return new ToAPITaskAssignedEventConverter(taskConverter);
    }

    @Bean
    public APITaskCandidateUserConverter apiTaskCandidateUserConverter() {
        return new APITaskCandidateUserConverter();
    }

    @Bean
    public ToAPITaskCandidateUserAddedEventConverter toAPITaskCandidateUserAddedEventConverter(APITaskCandidateUserConverter taskCandidateUserConverter) {
        return new ToAPITaskCandidateUserAddedEventConverter(taskCandidateUserConverter);
    }

    @Bean
    public InitializingBean registerTaskCandidateUserAddedEventListener(RuntimeService runtimeService,
                                                                        @Autowired(required = false) List<TaskRuntimeEventListener<TaskCandidateUserAddedEvent>> listeners,
                                                                        ToAPITaskCandidateUserAddedEventConverter taskCandidateUserAddedEventConverter) {
        return () -> runtimeService.addEventListener(new TaskCandidateUserAddedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                                taskCandidateUserAddedEventConverter),
                                                     ActivitiEventType.ENTITY_CREATED);
    }

    @Bean
    public InitializingBean registerTaskCandidateUserRemovedEventListener(RuntimeService runtimeService,
                                                                          @Autowired(required = false) List<TaskRuntimeEventListener<TaskCandidateUserRemovedEvent>> listeners,
                                                                          APITaskCandidateUserConverter taskCandidateUserConverter) {
        return () -> runtimeService.addEventListener(new TaskCandidateUserRemovedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                                  new ToTaskCandidateUserRemovedConverter(taskCandidateUserConverter)),
                                                     ActivitiEventType.ENTITY_DELETED);
    }

    @Bean
    public APITaskCandidateGroupConverter apiTaskCandidateGroupConverter() {
        return new APITaskCandidateGroupConverter();
    }

    @Bean
    public ToAPITaskCandidateGroupAddedEventConverter toAPITaskCandidateGroupAddedEventConverter(APITaskCandidateGroupConverter taskCandidateGroupConverter) {
        return new ToAPITaskCandidateGroupAddedEventConverter(taskCandidateGroupConverter);
    }

    @Bean
    public InitializingBean registerTaskCandidateGroupAddedEventListener(RuntimeService runtimeService,
                                                                         @Autowired(required = false) List<TaskRuntimeEventListener<TaskCandidateGroupAddedEvent>> listeners,
                                                                         ToAPITaskCandidateGroupAddedEventConverter taskCandidateGroupAddedEventConverter) {
        return () -> runtimeService.addEventListener(new TaskCandidateGroupAddedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                                 taskCandidateGroupAddedEventConverter),
                                                     ActivitiEventType.ENTITY_CREATED);
    }

    @Bean
    public InitializingBean registerTaskCandidateGroupRemovedEventListener(RuntimeService runtimeService,
                                                                           @Autowired(required = false) List<TaskRuntimeEventListener<TaskCandidateGroupRemovedEvent>> listeners,
                                                                           APITaskCandidateGroupConverter taskCandidateGroupConverter) {
        return () -> runtimeService.addEventListener(new TaskCandidateGroupRemovedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                                   new ToTaskCandidateGroupRemovedConverter(taskCandidateGroupConverter)),
                                                     ActivitiEventType.ENTITY_DELETED);
    }

}
