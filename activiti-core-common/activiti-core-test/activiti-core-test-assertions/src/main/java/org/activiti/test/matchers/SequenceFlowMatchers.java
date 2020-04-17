/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.test.matchers;

import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.SequenceFlowEvent;

import static org.assertj.core.api.Assertions.assertThat;

public class SequenceFlowMatchers {

    private String definitionKey;

    private SequenceFlowMatchers(String definitionKey) {

        this.definitionKey = definitionKey;
    }

    public static SequenceFlowMatchers sequenceFlow(String definitionKey) {
        return new SequenceFlowMatchers(definitionKey);
    }

    public OperationScopeMatcher hasBeenTaken() {
        return (operationScope, events) -> {
            List<BPMNSequenceFlowTakenEvent> flowTakenEvents = events
                    .stream()
                    .filter(event -> SequenceFlowEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN.equals(event.getEventType()))
                    .map(BPMNSequenceFlowTakenEvent.class::cast)
                    .collect(Collectors.toList());
            assertThat(flowTakenEvents)
                    .filteredOn(event -> event.getEntity().getProcessInstanceId().equals(operationScope.getProcessInstanceId()))
                    .extracting(event -> event.getEntity().getElementId())
                    .contains(definitionKey);
        };
    }

}
