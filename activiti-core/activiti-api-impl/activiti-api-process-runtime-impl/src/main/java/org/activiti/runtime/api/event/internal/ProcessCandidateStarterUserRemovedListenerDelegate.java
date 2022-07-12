/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.api.process.runtime.events.ProcessCandidateStarterUserRemovedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.runtime.api.event.impl.ToAPIProcessCandidateStarterUserRemovedEventConverter;

import java.util.List;

public class ProcessCandidateStarterUserRemovedListenerDelegate implements ActivitiEventListener {

    private List<ProcessRuntimeEventListener<ProcessCandidateStarterUserRemovedEvent>> listeners;

    private ToAPIProcessCandidateStarterUserRemovedEventConverter processCandidateStarterUserRemovedEventConverter;

    public ProcessCandidateStarterUserRemovedListenerDelegate(List<ProcessRuntimeEventListener<ProcessCandidateStarterUserRemovedEvent>> listeners,
                                                              ToAPIProcessCandidateStarterUserRemovedEventConverter processCandidateStarterUserRemovedEventConverter) {
        this.listeners = listeners;
        this.processCandidateStarterUserRemovedEventConverter = processCandidateStarterUserRemovedEventConverter;
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        if (event instanceof ActivitiEntityEvent) {
            processCandidateStarterUserRemovedEventConverter.from((ActivitiEntityEvent) event)
                    .ifPresent(convertedEvent -> {
                        for (ProcessRuntimeEventListener<ProcessCandidateStarterUserRemovedEvent> listener : listeners ) {
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
