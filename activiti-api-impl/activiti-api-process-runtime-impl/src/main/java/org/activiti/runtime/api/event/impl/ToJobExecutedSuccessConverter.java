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

import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNTimerFiredEvent;
import org.activiti.api.process.model.payloads.TimerPayload;
import org.activiti.api.runtime.event.impl.BPMNTimerFiredEventImpl;
import org.activiti.api.runtime.model.impl.BPMNTimerImpl;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.impl.persistence.entity.JobEntityImpl;

public class ToJobExecutedSuccessConverter implements EventConverter<BPMNTimerFiredEvent, ActivitiEntityEvent> {

    public ToJobExecutedSuccessConverter() {
    }

    @Override
    public Optional<BPMNTimerFiredEvent> from(ActivitiEntityEvent internalEvent) {
        
        JobEntityImpl jobEntity = (JobEntityImpl)internalEvent.getEntity();
        
        BPMNTimerImpl timer = new BPMNTimerImpl(jobEntity.getId());
        timer.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        timer.setProcessInstanceId(internalEvent.getProcessInstanceId());

        TimerPayload timerPayload = ProcessPayloadBuilder.timer()
                                    .withDueDate(jobEntity.getDuedate())
                                    .withEndDate(jobEntity.getEndDate())
                                    .withExecutionId(jobEntity.getExecutionId())
                                    .withProcessInstanceId(jobEntity.getProcessInstanceId())
                                    .withProcessDefinitionId(jobEntity.getProcessDefinitionId())
                                    .withIsExclusive(jobEntity.isExclusive())
                                    .withRetries(jobEntity.getRetries())
                                    .withMaxIterations(jobEntity.getMaxIterations())
                                    .withRepeat(jobEntity.getRepeat())
                                    .withJobHandlerType(jobEntity.getJobHandlerType())
                                    .withJobHandlerConfiguration(jobEntity.getJobHandlerConfiguration())
                                    .withExceptionMessage(jobEntity.getExceptionMessage())
                                    .withTenantId(jobEntity.getTenantId())
                                    .withJobType(jobEntity.getJobType())
                                    .build();

        timer.setTimerPayload(timerPayload);
        
        BPMNTimerFiredEventImpl event = new BPMNTimerFiredEventImpl(timer);
     	event.setProcessInstanceId(internalEvent.getProcessInstanceId());
        event.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        return Optional.of(event);
    }
    
}
