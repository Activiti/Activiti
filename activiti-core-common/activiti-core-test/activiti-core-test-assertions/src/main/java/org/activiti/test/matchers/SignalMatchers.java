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
package org.activiti.test.matchers;

import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.process.model.events.BPMNSignalEvent;
import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;

import static org.assertj.core.api.Assertions.assertThat;

public class SignalMatchers {

    private String signalName;

    private SignalMatchers(String signalName) {

        this.signalName = signalName;
    }

    public static SignalMatchers signal(String signalName) {
        return new SignalMatchers(signalName);
    }

    public OperationScopeMatcher hasBeenReceived() {
        return (operationScope, events) -> {
            List<BPMNSignalReceivedEvent> flowTakenEvents = events
                    .stream()
                    .filter(event -> BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED.equals(event.getEventType()))
                    .map(BPMNSignalReceivedEvent.class::cast)
                    .filter(event -> event.getEntity().getProcessInstanceId().equals(operationScope.getProcessInstanceId()))
                    .collect(Collectors.toList());
            assertThat(flowTakenEvents)
                    .extracting(event -> event.getEntity().getSignalPayload().getName())
                    .as("Unable to find event " + BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED)
                    .contains(signalName);
        };
    }
}
