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

import java.util.Collections;
import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.runtime.api.TaskAdminRuntime;
import org.activiti.runtime.api.impl.TaskAdminRuntimeImpl;
import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.conf.impl.TaskRuntimeConfigurationImpl;
import org.activiti.runtime.api.event.TaskActivated;
import org.activiti.runtime.api.event.TaskAssigned;
import org.activiti.runtime.api.event.TaskCancelled;
import org.activiti.runtime.api.event.TaskCandidateGroupAdded;
import org.activiti.runtime.api.event.TaskCandidateGroupRemoved;
import org.activiti.runtime.api.event.TaskCandidateUserAdded;
import org.activiti.runtime.api.event.TaskCandidateUserRemoved;
import org.activiti.runtime.api.event.TaskCompleted;
import org.activiti.runtime.api.event.TaskCreated;
import org.activiti.runtime.api.event.TaskSuspended;
import org.activiti.runtime.api.event.VariableEventListener;
import org.activiti.runtime.api.event.impl.ToAPITaskAssignedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPITaskCandidateGroupAddedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPITaskCandidateUserAddedEventConverter;
import org.activiti.runtime.api.event.impl.ToAPITaskCreatedEventConverter;
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
import org.activiti.runtime.api.event.listener.TaskRuntimeEventListener;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.security.SecurityManager;
import org.activiti.runtime.api.impl.TaskRuntimeImpl;
import org.activiti.runtime.api.model.impl.APITaskCandidateGroupConverter;
import org.activiti.runtime.api.model.impl.APITaskCandidateUserConverter;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRuntimeAutoConfiguration {

    @Bean
    public TaskRuntime taskRuntime(TaskService taskService,
                                   UserGroupManager userGroupManager,
                                   SecurityManager securityManager,
                                   APITaskConverter taskConverter,
                                   APIVariableInstanceConverter variableInstanceConverter,
                                   TaskRuntimeConfiguration configuration) {
        return new TaskRuntimeImpl(taskService,
                                   userGroupManager,
                                   securityManager,
                                   taskConverter,
                                   variableInstanceConverter,
                                   configuration);
    }

    @Bean
    public TaskAdminRuntime taskAdminRuntime(TaskService taskService,
                                        UserGroupManager userGroupManager,
                                        SecurityManager securityManager,
                                        APITaskConverter taskConverter,
                                        APIVariableInstanceConverter variableInstanceConverter,
                                        TaskRuntimeConfiguration configuration) {
        return new TaskAdminRuntimeImpl(taskService,
                                        userGroupManager,
                                        securityManager,
                                        taskConverter,
                                        variableInstanceConverter,
                                        configuration);
    }

    @Bean
    public APITaskConverter apiTaskConverter(TaskService taskService,
                                             APIVariableInstanceConverter variableInstanceConverter) {
        return new APITaskConverter(taskService,
                                    variableInstanceConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public APIVariableInstanceConverter apiVariableInstanceConverter(APIVariableInstanceConverter variableInstanceConverter) {
        return new APIVariableInstanceConverter();
    }


    @Bean
    public TaskRuntimeConfiguration taskRuntimeConfiguration(@Autowired(required = false) List<TaskRuntimeEventListener<?>> taskRuntimeEventListeners,
                                                             @Autowired(required = false) List<VariableEventListener<?>> variableEventListeners) {
        return new TaskRuntimeConfigurationImpl(getInitializedTaskRuntimeEventListeners(taskRuntimeEventListeners),
                                                getInitializedTaskRuntimeEventListeners(variableEventListeners));
    }

    @Bean
    public InitializingBean registerTaskCreatedEventListener(RuntimeService runtimeService,
                                                             @Autowired(required = false) List<TaskRuntimeEventListener<TaskCreated>> listeners,
                                                             ToAPITaskCreatedEventConverter taskCreatedEventConverter) {
        return () -> runtimeService.addEventListener(new TaskCreatedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                     taskCreatedEventConverter),
                                                     ActivitiEventType.TASK_CREATED);
    }

    private <T> List<T> getInitializedTaskRuntimeEventListeners(List<T> taskRuntimeEventListeners) {
        return taskRuntimeEventListeners != null ? taskRuntimeEventListeners : Collections.emptyList();
    }

    @Bean
    public InitializingBean registerTaskAssignedEventListener(RuntimeService runtimeService,
                                                              @Autowired(required = false) List<TaskRuntimeEventListener<TaskAssigned>> listeners,
                                                              ToAPITaskAssignedEventConverter taskAssignedEventConverter) {
        return () -> runtimeService.addEventListener(new TaskAssignedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                      taskAssignedEventConverter),
                                                     ActivitiEventType.TASK_ASSIGNED);
    }

    @Bean
    public InitializingBean registerTaskCompletedEventListener(RuntimeService runtimeService,
                                                               @Autowired(required = false) List<TaskRuntimeEventListener<TaskCompleted>> listeners,
                                                               APITaskConverter taskConverter) {
        return () -> runtimeService.addEventListener(new TaskCompletedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                       new ToTaskCompletedConverter(taskConverter)),
                                                     ActivitiEventType.TASK_COMPLETED);
    }

    @Bean
    public InitializingBean registerTaskCancelledEventListener(RuntimeService runtimeService,
                                                               @Autowired(required = false) List<TaskRuntimeEventListener<TaskCancelled>> taskRuntimeEventListeners,
                                                               TaskService taskService,
                                                               APITaskConverter taskConverter) {
        return () -> runtimeService.addEventListener(new TaskCancelledListenerDelegate(getInitializedTaskRuntimeEventListeners(taskRuntimeEventListeners),
                                                                                       new ToTaskCancelledConverter(taskConverter,
                                                                                                                    taskService)),
                                                     ActivitiEventType.ACTIVITY_CANCELLED);
    }

    @Bean
    public InitializingBean registerTaskSuspendedListener(RuntimeService runtimeService,
                                                          @Autowired(required = false) List<TaskRuntimeEventListener<TaskSuspended>> listeners,
                                                          APITaskConverter taskConverter) {
        return () -> runtimeService.addEventListener(new TaskSuspendedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                       new ToTaskSuspendedConverter(taskConverter)),
                                                     ActivitiEventType.ENTITY_SUSPENDED);
    }

    @Bean
    public InitializingBean registerTaskActivatedListener(RuntimeService runtimeService,
                                                          @Autowired(required = false) List<TaskRuntimeEventListener<TaskActivated>> listeners,
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
                                                                        @Autowired(required = false) List<TaskRuntimeEventListener<TaskCandidateUserAdded>> listeners,
                                                                        ToAPITaskCandidateUserAddedEventConverter taskCandidateUserAddedEventConverter) {
        return () -> runtimeService.addEventListener(new TaskCandidateUserAddedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                                taskCandidateUserAddedEventConverter),
                                                     ActivitiEventType.ENTITY_CREATED);
    }

    @Bean
    public InitializingBean registerTaskCandidateUserRemovedEventListener(RuntimeService runtimeService,
                                                                          @Autowired(required = false) List<TaskRuntimeEventListener<TaskCandidateUserRemoved>> listeners,
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
                                                                         @Autowired(required = false) List<TaskRuntimeEventListener<TaskCandidateGroupAdded>> listeners,
                                                                         ToAPITaskCandidateGroupAddedEventConverter taskCandidateGroupAddedEventConverter) {
        return () -> runtimeService.addEventListener(new TaskCandidateGroupAddedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                                 taskCandidateGroupAddedEventConverter),
                                                     ActivitiEventType.ENTITY_CREATED);
    }

    @Bean
    public InitializingBean registerTaskCandidateGroupRemovedEventListener(RuntimeService runtimeService,
                                                                           @Autowired(required = false) List<TaskRuntimeEventListener<TaskCandidateGroupRemoved>> listeners,
                                                                           APITaskCandidateGroupConverter taskCandidateGroupConverter) {
        return () -> runtimeService.addEventListener(new TaskCandidateGroupRemovedListenerDelegate(getInitializedTaskRuntimeEventListeners(listeners),
                                                                                                   new ToTaskCandidateGroupRemovedConverter(taskCandidateGroupConverter)),
                                                     ActivitiEventType.ENTITY_DELETED);
    }
}
