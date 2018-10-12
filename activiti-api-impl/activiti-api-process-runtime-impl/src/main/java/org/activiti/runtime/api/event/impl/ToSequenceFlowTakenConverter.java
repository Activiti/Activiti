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

import org.activiti.api.process.model.events.SequenceFlowTakenEvent;
import org.activiti.api.runtime.event.impl.SequenceFlowTakenImpl;
import org.activiti.engine.delegate.event.ActivitiSequenceFlowTakenEvent;
import org.activiti.api.runtime.model.impl.SequenceFlowImpl;

public class ToSequenceFlowTakenConverter implements EventConverter<SequenceFlowTakenEvent, ActivitiSequenceFlowTakenEvent>{

    @Override
    public Optional<SequenceFlowTakenEvent> from(ActivitiSequenceFlowTakenEvent internalEvent) {
        SequenceFlowImpl sequenceFlow = new SequenceFlowImpl(internalEvent.getSourceActivityId(),
                                                             internalEvent.getTargetActivityId());
        sequenceFlow.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        sequenceFlow.setProcessInstanceId(internalEvent.getProcessInstanceId());
        sequenceFlow.setSourceActivityName(internalEvent.getSourceActivityName());
        sequenceFlow.setSourceActivityType(internalEvent.getSourceActivityType());
        sequenceFlow.setTargetActivityName(internalEvent.getTargetActivityName());
        sequenceFlow.setTargetActivityType(internalEvent.getTargetActivityType());
        return Optional.of(new SequenceFlowTakenImpl(sequenceFlow));
    }
}
