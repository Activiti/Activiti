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
package org.activiti.runtime.api.event.internal;

import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.runtime.api.event.impl.ToActivityStartedConverter;

import java.util.List;

public class ActivityStartedListenerDelegate implements ActivitiEventListener {

    private List<BPMNElementEventListener<BPMNActivityStartedEvent>> processRuntimeEventListeners;

    private ToActivityStartedConverter converter;

    public ActivityStartedListenerDelegate(List<BPMNElementEventListener<BPMNActivityStartedEvent>> processRuntimeEventListeners,
                                           ToActivityStartedConverter converter) {
        this.processRuntimeEventListeners = processRuntimeEventListeners;
        this.converter = converter;
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        if (event instanceof ActivitiActivityEvent) {
            converter.from((ActivitiActivityEvent) event)
                    .ifPresent(convertedEvent -> {
                        for ( BPMNElementEventListener<BPMNActivityStartedEvent> listener : processRuntimeEventListeners ) {
                            listener.onEvent(convertedEvent);
                        }
                    });
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
