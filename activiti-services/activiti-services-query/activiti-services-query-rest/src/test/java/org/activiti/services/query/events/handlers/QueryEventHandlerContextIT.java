/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.query.events.handlers;

import static org.activiti.services.query.events.handlers.QueryEventHandlerContextIT.MOCK_DEPENDENCIES_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.app.repository.VariableRepository;
import org.activiti.services.query.events.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = QueryEventHandlerContextIT.QueryEventHandlerContextConfig.class)
@ActiveProfiles(MOCK_DEPENDENCIES_PROFILE)
@DataJpaTest // Needed to auto configure JPA Entity Manager
public class QueryEventHandlerContextIT {

    public static final String MOCK_DEPENDENCIES_PROFILE = "mockDependencies";

    @Autowired
    private QueryEventHandlerContext context;

    @Configuration
    @Profile(MOCK_DEPENDENCIES_PROFILE)
    @ComponentScan(basePackages = {"org.activiti.services.query.events.handlers",  "org.activiti.services.query.app.repository"})
    public static class QueryEventHandlerContextConfig {

        // the purpose of this test is to verify that handlers for every supported event is inject
        // so we can mock transitive component dependencies
        @Primary
        @Bean
        public ProcessInstanceRepository getProcessInstanceRepository() {
            return mock(ProcessInstanceRepository.class);
        }

        @Primary
        @Bean
        public TaskRepository getTaskRepository() {
            return mock(TaskRepository.class);
        }

        @Primary
        @Bean
        public VariableRepository getVariableRepository() {
            return mock(VariableRepository.class);
        }

    }

    @Test
    public void shouldHaveHandlersForAllSupportedEvents() throws Exception {
        //given

        //when
        Map<Class<? extends ProcessEngineEvent>, QueryEventHandler> handlers = context.getHandlers();

        //then
        assertThat(handlers).containsOnlyKeys(
                ProcessStartedEvent.class,
                ProcessCompletedEvent.class,
                TaskCreatedEvent.class,
                TaskAssignedEvent.class,
                TaskCompletedEvent.class,
                VariableCreatedEvent.class,
                VariableUpdatedEvent.class,
                VariableDeletedEvent.class
        );
        assertThat(handlers.get(ProcessStartedEvent.class)).isInstanceOf(ProcessStartedHandler.class);
        assertThat(handlers.get(ProcessCompletedEvent.class)).isInstanceOf(ProcessCompletedEventHandler.class);
        assertThat(handlers.get(TaskCreatedEvent.class)).isInstanceOf(TaskCreatedEventHandler.class);
        assertThat(handlers.get(TaskAssignedEvent.class)).isInstanceOf(TaskAssignedEventHandler.class);
        assertThat(handlers.get(TaskCompletedEvent.class)).isInstanceOf(TaskCompletedEventHandler.class);
        assertThat(handlers.get(VariableCreatedEvent.class)).isInstanceOf(VariableCreatedEventHandler.class);
        assertThat(handlers.get(VariableUpdatedEvent.class)).isInstanceOf(VariableUpdatedEventHandler.class);
        assertThat(handlers.get(VariableDeletedEvent.class)).isInstanceOf(VariableDeletedEventHandler.class);
    }
}