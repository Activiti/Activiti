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

import org.activiti.api.process.model.payloads.TimerPayload;
import org.activiti.api.runtime.model.impl.BPMNTimerImpl;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.persistence.entity.AbstractJobEntity;

public class BPMNTimerConverter {

    public TimerPayload convertToTimerPayload(AbstractJobEntity jobEntity) {
        TimerPayload timerPayload = new TimerPayload();

        timerPayload.setDuedate(jobEntity.getDuedate());
        timerPayload.setEndDate(jobEntity.getEndDate());
        timerPayload.setRetries(jobEntity.getRetries());
        timerPayload.setMaxIterations(jobEntity.getMaxIterations());
        timerPayload.setRepeat(jobEntity.getRepeat());
        timerPayload.setExceptionMessage(jobEntity.getExceptionMessage());

        return timerPayload;
    }

    public BPMNTimerImpl convertToBPMNTimer(ActivitiEntityEvent internalEvent) {
        AbstractJobEntity jobEntity = (AbstractJobEntity) internalEvent.getEntity();

        BPMNTimerImpl timer = new BPMNTimerImpl(TimerEventHandler.getActivityIdFromConfiguration(jobEntity.getJobHandlerConfiguration()));
        timer.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        timer.setProcessInstanceId(internalEvent.getProcessInstanceId());
        timer.setTimerPayload(convertToTimerPayload(jobEntity));

        return timer;
    }

    public boolean isTimerRelatedEvent(ActivitiEvent event) {
        return event instanceof ActivitiEntityEvent &&
                AbstractJobEntity.class.isAssignableFrom(((ActivitiEntityEvent) event).getEntity().getClass()) &&
                ((AbstractJobEntity) ((ActivitiEntityEvent) event).getEntity()).getJobType().equals("timer");
    }
}
