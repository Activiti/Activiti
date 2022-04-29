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
package org.activiti.spring.boot;

import java.util.ArrayList;
import java.util.List;

import org.activiti.api.process.model.events.BPMNMessageEvent;
import org.activiti.api.process.model.events.BPMNMessageReceivedEvent;
import org.activiti.api.process.model.events.BPMNMessageSentEvent;
import org.activiti.api.process.model.events.BPMNMessageWaitingEvent;
import org.activiti.api.process.model.events.MessageSubscriptionCancelledEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageTestConfiguration {

    public static List<BPMNMessageEvent> messageEvents = new ArrayList<BPMNMessageEvent>();
    public static List<MessageSubscriptionCancelledEvent> messageSubscriptionCancelledEvents = new ArrayList<MessageSubscriptionCancelledEvent>();

    @Bean
    public BPMNElementEventListener<BPMNMessageSentEvent> messageSentEventListener() {
        return bpmnMessageSentEvent -> messageEvents.add(bpmnMessageSentEvent);
    }

    @Bean
    public BPMNElementEventListener<BPMNMessageReceivedEvent> messageReceivedEventListener() {
        return bpmnMessageReceivedEvent -> messageEvents.add(bpmnMessageReceivedEvent);
    }

    @Bean
    public BPMNElementEventListener<BPMNMessageWaitingEvent> messageWaitingEventListener() {
        return bpmnMessageWaitingEvent -> messageEvents.add(bpmnMessageWaitingEvent);
    }

    @Bean
    public ProcessRuntimeEventListener<MessageSubscriptionCancelledEvent> messageSubscriptionCancelledEventListener() {
        return messageSubscriptionCancelledEvent -> messageSubscriptionCancelledEvents.add(messageSubscriptionCancelledEvent);
    }

}
