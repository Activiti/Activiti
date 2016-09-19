package org.activiti.engine.impl.agenda;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import static org.activiti.engine.impl.context.Context.getAgenda;
import static org.activiti.engine.impl.context.Context.getCommandContext;

/**
 * This class provides helper methods to schedule process operations
 */
public class ProcessAgendaHelper {

    public static void planContinueProcessOperation(ExecutionEntity execution) {
        getAgenda().planOperation(new ContinueProcessOperation(getCommandContext(), execution));
    }

    public static void planContinueProcessSynchronousOperation(ExecutionEntity execution) {
        getAgenda().planOperation(new ContinueProcessOperation(getCommandContext(), execution, true, false));
    }

    public static void planContinueProcessInCompensation(ExecutionEntity execution) {
        getAgenda().planOperation(new ContinueProcessOperation(getCommandContext(), execution, false, true));
    }

    public static void planContinueMultiInstanceOperation(ExecutionEntity execution) {
        getAgenda().planOperation(new ContinueMultiInstanceOperation(getCommandContext(), execution));
    }

    public static void planTakeOutgoingSequenceFlowsOperation(ExecutionEntity execution, boolean evaluateConditions) {
        getAgenda().planOperation(new TakeOutgoingSequenceFlowsOperation(getCommandContext(), execution, evaluateConditions));
    }

    public static void planEndExecutionOperation(ExecutionEntity execution) {
        getAgenda().planOperation(new EndExecutionOperation(getCommandContext(), execution));
    }

    public static void planTriggerExecutionOperation(ExecutionEntity execution) {
        getAgenda().planOperation(new TriggerExecutionOperation(getCommandContext(), execution));
    }

    public static void planDestroyScopeOperation(ExecutionEntity execution) {
        getAgenda().planOperation(new DestroyScopeOperation(getCommandContext(), execution));
    }

    public static void planExecuteInactiveBehaviorsOperation() {
        getAgenda().planOperation(new ExecuteInactiveBehaviorsOperation(getCommandContext()));
    }

}
