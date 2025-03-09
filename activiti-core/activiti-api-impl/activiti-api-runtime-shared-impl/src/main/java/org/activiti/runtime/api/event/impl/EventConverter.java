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
package org.activiti.runtime.api.event.impl;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.runtime.model.impl.VariableInstanceImpl;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;

import java.util.Optional;

public interface EventConverter<ApiEventTypeT extends RuntimeEvent<?, ?>, InternalApiTypeT extends ActivitiEvent> {

    Optional<ApiEventTypeT> from(InternalApiTypeT internalEvent);

    default VariableInstanceImpl<Object> createVariableInstance(ActivitiVariableEvent internalEvent,
                                                                boolean isEphemeral) {
        return new VariableInstanceImpl<>(internalEvent.getVariableName(),
                                         internalEvent.getVariableType().getTypeName(),
                                         isEphemeral ? null : internalEvent.getVariableValue(),
                                         internalEvent.getProcessInstanceId(),
                                         internalEvent.getTaskId());
    }
}
