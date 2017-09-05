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

package org.activiti.services.audit.config;

import org.activiti.services.audit.events.ActivityCompletedEventEntity;
import org.activiti.services.audit.events.ActivityStartedEventEntity;
import org.activiti.services.audit.events.ProcessCompletedEventEntity;
import org.activiti.services.audit.events.ProcessStartedEventEntity;
import org.activiti.services.audit.events.SequenceFlowTakenEventEntity;
import org.activiti.services.audit.events.TaskAssignedEventEntity;
import org.activiti.services.audit.events.TaskCompletedEventEntity;
import org.activiti.services.audit.events.TaskCreatedEventEntity;
import org.activiti.services.audit.events.VariableCreatedEventEntity;
import org.activiti.services.audit.events.VariableDeletedEventEntity;
import org.activiti.services.audit.events.VariableUpdatedEventEntity;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
public class RepositoryConfig extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(ActivityCompletedEventEntity.class);
        config.exposeIdsFor(ActivityStartedEventEntity.class);
        config.exposeIdsFor(ProcessCompletedEventEntity.class);
        config.exposeIdsFor(ProcessStartedEventEntity.class);
        config.exposeIdsFor(SequenceFlowTakenEventEntity.class);
        config.exposeIdsFor(TaskAssignedEventEntity.class);
        config.exposeIdsFor(TaskCompletedEventEntity.class);
        config.exposeIdsFor(TaskCreatedEventEntity.class);
        config.exposeIdsFor(VariableCreatedEventEntity.class);
        config.exposeIdsFor(VariableDeletedEventEntity.class);
        config.exposeIdsFor(VariableUpdatedEventEntity.class);
    }
}