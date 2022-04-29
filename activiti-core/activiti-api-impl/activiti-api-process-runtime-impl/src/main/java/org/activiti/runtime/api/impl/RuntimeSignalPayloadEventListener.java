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
package org.activiti.runtime.api.impl;

import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.engine.RuntimeService;
import org.activiti.runtime.api.signal.SignalPayloadEventListener;
import org.springframework.context.event.EventListener;

/**
 * Default implementation of SignalPayloadEventListener that delegates
 * Spring SignalPayload event into embedded RuntimeService.
 *
 */
public class RuntimeSignalPayloadEventListener implements SignalPayloadEventListener {

    private final RuntimeService runtimeService;

    public RuntimeSignalPayloadEventListener(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    @EventListener
    public void sendSignal(SignalPayload signalPayload) {
        runtimeService.signalEventReceived(signalPayload.getName(),
                                           signalPayload.getVariables());
    }

}
