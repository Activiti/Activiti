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

package org.activiti.runtime.api.event.internal;

import java.util.List;

import org.activiti.api.process.model.events.BPMNTimerScheduledEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.runtime.api.event.impl.ToTimerScheduledConverter;


public class TimerScheduledListenerDelegate implements ActivitiEventListener {

    private List<BPMNElementEventListener<BPMNTimerScheduledEvent>> processRuntimeEventListeners;

    private ToTimerScheduledConverter converter;

    public TimerScheduledListenerDelegate(List<BPMNElementEventListener<BPMNTimerScheduledEvent>> processRuntimeEventListeners,
                                          ToTimerScheduledConverter converter) {
        this.processRuntimeEventListeners = processRuntimeEventListeners;
        this.converter = converter;
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        if (event.getType().equals(ActivitiEventType.TIMER_SCHEDULED)) {  
            
            if (event instanceof ActivitiEntityEvent && TimerJobEntity.class.isAssignableFrom(((ActivitiEntityEvent) event).getEntity().getClass())) {
                converter.from((ActivitiEntityEvent) event)
                        .ifPresent(convertedEvent -> {
                            for (BPMNElementEventListener<BPMNTimerScheduledEvent> listener : processRuntimeEventListeners) {
                                listener.onEvent(convertedEvent);
                            }
                        });
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
