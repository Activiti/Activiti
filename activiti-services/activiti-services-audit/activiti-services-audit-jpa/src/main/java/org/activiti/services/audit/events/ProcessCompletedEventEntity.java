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

package org.activiti.services.audit.events;

import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.audit.converter.ProcessInstanceJpaJsonConverter;

@Entity
@DiscriminatorValue(value = "ProcessCompletedEvent")
public class ProcessCompletedEventEntity extends ProcessEngineEventEntity {

    @Convert(converter = ProcessInstanceJpaJsonConverter.class)
    private ProcessInstance processInstance;

    public ProcessCompletedEventEntity() {
    }

    public ProcessCompletedEventEntity(Long id,
                                       Long timestamp,
                                       String eventType,
                                       String executionId,
                                       String processDefinitionId,
                                       String processInstanceId,
                                       ProcessInstance processInstance) {
        super(id,
              timestamp,
              eventType,
              executionId,
              processDefinitionId,
              processInstanceId);
        this.processInstance = processInstance;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }
}
