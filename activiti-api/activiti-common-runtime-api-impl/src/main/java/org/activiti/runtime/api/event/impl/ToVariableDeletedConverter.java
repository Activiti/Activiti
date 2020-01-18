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

import org.activiti.engine.delegate.event.ActivitiVariableEvent;
import org.activiti.runtime.api.event.VariableDeleted;
import org.activiti.runtime.api.model.impl.VariableInstanceImpl;

public class ToVariableDeletedConverter implements EventConverter<VariableDeleted, ActivitiVariableEvent> {

    @Override
    public Optional<VariableDeleted> from(ActivitiVariableEvent internalEvent) {
        VariableInstanceImpl<Object> variableInstance = new VariableInstanceImpl<>(internalEvent.getVariableName(),
                                                                                   internalEvent.getVariableType().getTypeName(),
                                                                                   internalEvent.getVariableValue(),
                                                                                   internalEvent.getProcessInstanceId());
        variableInstance.setTaskId(internalEvent.getTaskId());
        return Optional.of(new VariableDeletedEventImpl(variableInstance));
    }
}
