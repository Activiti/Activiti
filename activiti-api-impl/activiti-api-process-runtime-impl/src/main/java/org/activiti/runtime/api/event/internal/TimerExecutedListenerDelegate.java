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

import org.activiti.api.process.model.events.BPMNTimerExecutedEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.runtime.api.event.impl.ToTimerExecutedConverter;

import java.util.List;

public class TimerExecutedListenerDelegate implements ActivitiEventListener {

    private List<BPMNElementEventListener<BPMNTimerExecutedEvent>> processRuntimeEventListeners;

    private ToTimerExecutedConverter converter;

    public TimerExecutedListenerDelegate(List<BPMNElementEventListener<BPMNTimerExecutedEvent>> processRuntimeEventListeners,
                                         ToTimerExecutedConverter converter) {
        this.processRuntimeEventListeners = processRuntimeEventListeners;
        this.converter = converter;
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        converter.from(event)
                .ifPresent(convertedEvent -> {
                    for (BPMNElementEventListener<BPMNTimerExecutedEvent> listener : processRuntimeEventListeners) {
                        listener.onEvent(convertedEvent);
                    }
                });
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
