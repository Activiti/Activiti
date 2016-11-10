/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.agenda;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.runtime.ActivitiAgenda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

import static org.activiti.engine.impl.context.Context.getCommandContext;

/**
 * @author Joram Barrez
 * @author martin.grofcik
 */
public class DefaultActivitiAgenda implements ActivitiAgenda {

    private static final Logger logger = LoggerFactory.getLogger(DefaultActivitiAgenda.class);

    protected LinkedList<Runnable> operations = new LinkedList<Runnable>();

    @Override
    public boolean isEmpty() {
        return operations.isEmpty();
    }

    @Override
    public Runnable getNextOperation() {
        return operations.poll();
    }

    /**
     * Generic method to plan a {@link Runnable} execution.
     */
    @Override
    public void planOperation(Runnable operation) {
        planOperation(operation, null);
    }

    /**
     * Generic method to plan a {@link Runnable}.
     */
    @Override
    public void planOperation(Runnable operation, ExecutionEntity executionEntity) {
        operations.add(operation);
        if (executionEntity != null) {
            Context.getCommandContext().addInvolvedExecution(executionEntity);
        }
        logger.debug("Operation {} added to agenda", operation.getClass());
    }

    @Override
    public void planContinueProcessOperation(ExecutionEntity execution) {
        planOperation(new ContinueProcessOperation(getCommandContext(), execution));
    }

    @Override
    public void planContinueProcessSynchronousOperation(ExecutionEntity execution) {
        planOperation(new ContinueProcessOperation(getCommandContext(), execution, true, false));
    }

    @Override
    public void planContinueProcessInCompensation(ExecutionEntity execution) {
        planOperation(new ContinueProcessOperation(getCommandContext(), execution, false, true));
    }

    @Override
    public void planContinueMultiInstanceOperation(ExecutionEntity execution) {
        planOperation(new ContinueMultiInstanceOperation(getCommandContext(), execution));
    }

    @Override
    public void planTakeOutgoingSequenceFlowsOperation(ExecutionEntity execution, boolean evaluateConditions) {
        planOperation(new TakeOutgoingSequenceFlowsOperation(getCommandContext(), execution, evaluateConditions));
    }

    @Override
    public void planEndExecutionOperation(ExecutionEntity execution) {
        planOperation(new EndExecutionOperation(getCommandContext(), execution));
    }

    @Override
    public void planTriggerExecutionOperation(ExecutionEntity execution) {
        planOperation(new TriggerExecutionOperation(getCommandContext(), execution));
    }

    @Override
    public void planDestroyScopeOperation(ExecutionEntity execution) {
        planOperation(new DestroyScopeOperation(getCommandContext(), execution));
    }

    @Override
    public void planExecuteInactiveBehaviorsOperation() {
        planOperation(new ExecuteInactiveBehaviorsOperation(getCommandContext()));
    }
}
