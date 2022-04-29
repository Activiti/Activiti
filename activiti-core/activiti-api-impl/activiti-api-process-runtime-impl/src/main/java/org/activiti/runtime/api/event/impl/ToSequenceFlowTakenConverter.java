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
package org.activiti.runtime.api.event.impl;

import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.runtime.event.impl.BPMNSequenceFlowTakenImpl;
import org.activiti.api.runtime.model.impl.BPMNSequenceFlowImpl;
import org.activiti.engine.delegate.event.ActivitiSequenceFlowTakenEvent;

import java.util.Optional;

public class ToSequenceFlowTakenConverter implements EventConverter<BPMNSequenceFlowTakenEvent, ActivitiSequenceFlowTakenEvent> {

    @Override
    public Optional<BPMNSequenceFlowTakenEvent> from(ActivitiSequenceFlowTakenEvent internalEvent) {
        BPMNSequenceFlowImpl sequenceFlow = new BPMNSequenceFlowImpl(internalEvent.getId(),
                internalEvent.getSourceActivityId(),
                internalEvent.getTargetActivityId());

        sequenceFlow.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        sequenceFlow.setProcessInstanceId(internalEvent.getProcessInstanceId());
        sequenceFlow.setSourceActivityName(internalEvent.getSourceActivityName());
        sequenceFlow.setSourceActivityType(internalEvent.getSourceActivityType());
        sequenceFlow.setTargetActivityName(internalEvent.getTargetActivityName());
        sequenceFlow.setTargetActivityType(internalEvent.getTargetActivityType());

        BPMNSequenceFlowTakenImpl sequenceFlowTaken = new BPMNSequenceFlowTakenImpl(sequenceFlow);
        sequenceFlowTaken.setProcessInstanceId(internalEvent.getProcessInstanceId());
        sequenceFlowTaken.setProcessDefinitionId(internalEvent.getProcessDefinitionId());

        return Optional.of(sequenceFlowTaken);
    }
}
