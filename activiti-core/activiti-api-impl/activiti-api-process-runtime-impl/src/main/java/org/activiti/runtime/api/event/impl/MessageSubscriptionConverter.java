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

package org.activiti.runtime.api.event.impl;

import java.util.Optional;

import org.activiti.api.runtime.model.impl.MessageSubscriptionImpl;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;

public class MessageSubscriptionConverter {

    public MessageSubscriptionImpl convertToMessageSubscription(MessageEventSubscriptionEntity messageEventSubscriptionEntity) {
        
        return Optional.of(messageEventSubscriptionEntity)
                       .map(entity -> MessageSubscriptionImpl.builder()
                                                             .withId(entity.getId())
                                                             .withEventName(entity.getEventName())
                                                             .withProcessDefinitionId(entity.getProcessDefinitionId())
                                                             .withConfiguration(entity.getConfiguration())
                                                             .withActivityId(entity.getActivityId())
                                                             .withBusinessKey(Optional.ofNullable(entity.getExecution())
                                                                                      .map(DelegateExecution::getProcessInstanceBusinessKey)
                                                                                      .orElse(null))
                                                             .withCreated(entity.getCreated())
                                                             .withExecutionId(entity.getExecutionId())
                                                             .withProcessInstanceId(entity.getProcessInstanceId())
                                                             .build())
                       .orElseThrow( () -> new IllegalArgumentException("messageEventSubscriptionEntity must not be null"));
    }
    
}
