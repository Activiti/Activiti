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
package org.activiti.engine;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * This class extends {@link Agenda} with activiti specific operations
 */
@Internal
public interface ActivitiEngineAgenda extends Agenda {

    void planContinueProcessOperation(ExecutionEntity execution);

    void planContinueProcessSynchronousOperation(ExecutionEntity execution);

    void planContinueProcessInCompensation(ExecutionEntity execution);

    void planContinueMultiInstanceOperation(ExecutionEntity execution);

    void planTakeOutgoingSequenceFlowsOperation(ExecutionEntity execution, boolean evaluateConditions);

    void planEndExecutionOperation(ExecutionEntity execution);

    void planTriggerExecutionOperation(ExecutionEntity execution);

    void planDestroyScopeOperation(ExecutionEntity execution);

    void planExecuteInactiveBehaviorsOperation();
}
