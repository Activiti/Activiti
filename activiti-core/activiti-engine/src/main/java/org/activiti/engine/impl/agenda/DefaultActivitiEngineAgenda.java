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

package org.activiti.engine.impl.agenda;

import java.util.LinkedList;

import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**


 */
public class DefaultActivitiEngineAgenda implements ActivitiEngineAgenda {

    private static final Logger logger = LoggerFactory.getLogger(DefaultActivitiEngineAgenda.class);

    protected LinkedList<Runnable> operations = new LinkedList<Runnable>();
    protected CommandContext commandContext;

    public DefaultActivitiEngineAgenda(CommandContext commandContext) {
      this.commandContext = commandContext;
    }

    @Override
    public boolean isEmpty() {
        return operations.isEmpty();
    }

    @Override
    public Runnable getNextOperation() {
        return operations.poll();
    }

    /**
     * Generic method to plan a {@link Runnable}.
     */
    @Override
    public void planOperation(Runnable operation) {
        operations.add(operation);

        if (operation instanceof AbstractOperation) {
            ExecutionEntity execution = ((AbstractOperation) operation).getExecution();
            if (execution != null) {
                commandContext.addInvolvedExecution(execution);
            }
        }

        logger.debug("Operation {} added to agenda", operation.getClass());
    }

    @Override
    public void planContinueProcessOperation(ExecutionEntity execution) {
        planOperation(new ContinueProcessOperation(commandContext, execution));
    }

    @Override
    public void planContinueProcessSynchronousOperation(ExecutionEntity execution) {
        planOperation(new ContinueProcessOperation(commandContext, execution, true, false));
    }

    @Override
    public void planContinueProcessInCompensation(ExecutionEntity execution) {
        planOperation(new ContinueProcessOperation(commandContext, execution, false, true));
    }

    @Override
    public void planContinueMultiInstanceOperation(ExecutionEntity execution) {
        planOperation(new ContinueMultiInstanceOperation(commandContext, execution));
    }

    @Override
    public void planTakeOutgoingSequenceFlowsOperation(ExecutionEntity execution, boolean evaluateConditions) {
        planOperation(new TakeOutgoingSequenceFlowsOperation(commandContext, execution, evaluateConditions));
    }

    @Override
    public void planEndExecutionOperation(ExecutionEntity execution) {
        planOperation(new EndExecutionOperation(commandContext, execution));
    }

    @Override
    public void planTriggerExecutionOperation(ExecutionEntity execution) {
        planOperation(new TriggerExecutionOperation(commandContext, execution));
    }

    @Override
    public void planDestroyScopeOperation(ExecutionEntity execution) {
        planOperation(new DestroyScopeOperation(commandContext, execution));
    }

    @Override
    public void planExecuteInactiveBehaviorsOperation() {
        planOperation(new ExecuteInactiveBehaviorsOperation(commandContext));
    }
}
