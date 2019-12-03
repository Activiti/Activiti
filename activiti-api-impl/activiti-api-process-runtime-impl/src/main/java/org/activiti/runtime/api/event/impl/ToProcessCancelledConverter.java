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

import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.engine.delegate.event.ActivitiCancelledEvent;

import java.util.Optional;

public class ToProcessCancelledConverter implements EventConverter<ProcessCancelledEvent, ActivitiCancelledEvent> {

    @Override
    public Optional<ProcessCancelledEvent> from(ActivitiCancelledEvent internalEvent) {
        ProcessInstanceImpl processInstance = new ProcessInstanceImpl();
        processInstance.setId(internalEvent.getProcessInstanceId());
        processInstance.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        String cause = internalEvent.getCause() != null ? internalEvent.getCause().toString() : null;
        return Optional.of(new ProcessCancelledImpl(processInstance,
                cause));
    }
}
