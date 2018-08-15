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

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.runtime.api.event.ProcessCompleted;
import org.activiti.runtime.api.event.impl.ToProcessCompletedConverter;
import org.activiti.runtime.api.event.listener.ProcessRuntimeEventListener;

public class ProcessCompletedListenerDelegate implements ActivitiEventListener {

    private List<ProcessRuntimeEventListener<ProcessCompleted>> processRuntimeEventListeners;

    private ToProcessCompletedConverter processCompletedConverter;

    public ProcessCompletedListenerDelegate(List<ProcessRuntimeEventListener<ProcessCompleted>> processRuntimeEventListeners,
                                            ToProcessCompletedConverter processCompletedConverter) {
        this.processRuntimeEventListeners = processRuntimeEventListeners;
        this.processCompletedConverter = processCompletedConverter;
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        if (event instanceof ActivitiEntityEvent) {
            processCompletedConverter.from((ActivitiEntityEvent) event)
                    .ifPresent(convertedEvent -> {
                        for (ProcessRuntimeEventListener<ProcessCompleted> listener : processRuntimeEventListeners) {
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
