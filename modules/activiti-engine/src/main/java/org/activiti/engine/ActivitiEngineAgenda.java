package org.activiti.engine.impl.runtime;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * This class extends {@link Agenda} with activiti specific operations
 */
public interface ActivitiAgenda extends Agenda {

    void planOperation(Runnable operation, ExecutionEntity executionEntity);

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
