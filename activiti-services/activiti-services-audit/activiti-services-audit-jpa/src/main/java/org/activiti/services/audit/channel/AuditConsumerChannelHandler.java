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

package org.activiti.services.audit.channel;

import org.activiti.services.audit.EventsRepository;
import org.activiti.services.audit.events.ProcessEngineEventEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(AuditConsumerChannels.class)
public class AuditConsumerChannelHandler {

    private final EventsRepository eventsRepository;

    @Autowired
    public AuditConsumerChannelHandler(EventsRepository eventsRepository) {
        this.eventsRepository = eventsRepository;
    }

    @StreamListener(AuditConsumerChannels.AUDIT_CONSUMER)
    public synchronized void receive(ProcessEngineEventEntity[] events) {
        for (ProcessEngineEventEntity event : events) {
            eventsRepository.save(event);
        }
    }
}