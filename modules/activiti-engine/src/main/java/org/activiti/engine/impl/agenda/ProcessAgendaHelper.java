package org.activiti.engine.impl.agenda;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.runtime.Agenda;

/**
 * This class provides helper methods to schedule process operations
 */
public class ProcessAgendaHelper {

    public static void planContinueProcessOperation(Agenda agenda, CommandContext commandContext, ExecutionEntity execution) {
        agenda.planOperation(new ContinueProcessOperation(commandContext, execution), execution);
    }

    public static void planContinueProcessSynchronousOperation(Agenda agenda, CommandContext commandContext, ExecutionEntity execution) {
        agenda.planOperation(new ContinueProcessOperation(commandContext, execution, true, false), execution);
    }

    public static void planContinueProcessInCompensation(Agenda agenda, CommandContext commandContext, ExecutionEntity execution) {
        agenda.planOperation(new ContinueProcessOperation(commandContext, execution, false, true), execution);
    }

    public static void planContinueMultiInstanceOperation(Agenda agenda, CommandContext commandContext, ExecutionEntity execution) {
        agenda.planOperation(new ContinueMultiInstanceOperation(commandContext, execution), execution);
    }

    public static void planTakeOutgoingSequenceFlowsOperation(Agenda agenda, CommandContext commandContext, ExecutionEntity execution, boolean evaluateConditions) {
        agenda.planOperation(new TakeOutgoingSequenceFlowsOperation(commandContext, execution, evaluateConditions), execution);
    }

    public static void planEndExecutionOperation(Agenda agenda, CommandContext commandContext, ExecutionEntity execution) {
        agenda.planOperation(new EndExecutionOperation(commandContext, execution), execution);
    }

    public static void planTriggerExecutionOperation(Agenda agenda, CommandContext commandContext, ExecutionEntity execution) {
        agenda.planOperation(new TriggerExecutionOperation(commandContext, execution), execution);
    }

    public static void planDestroyScopeOperation(Agenda agenda, CommandContext commandContext, ExecutionEntity execution) {
        agenda.planOperation(new DestroyScopeOperation(commandContext, execution), execution);
    }

    public static void planExecuteInactiveBehaviorsOperation(Agenda agenda, CommandContext commandContext) {
        agenda.planOperation(new ExecuteInactiveBehaviorsOperation(commandContext));
    }

}
