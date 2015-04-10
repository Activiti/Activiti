package org.activiti.engine.impl.agenda;

import java.util.Collection;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class EndExecutionOperation extends AbstractOperation {

    private static final Logger logger = LoggerFactory.getLogger(EndExecutionOperation.class);

    public EndExecutionOperation(Agenda agenda, ActivityExecution execution) {
        super(agenda, execution);
    }

    @Override
    public void run() {

        CommandContext commandContext = Context.getCommandContext();
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        ExecutionEntity executionEntity = (ExecutionEntity) execution; // TODO:
                                                                       // don't
                                                                       // like
                                                                       // cast
                                                                       // here
                                                                       // ...

        // Find parent execution. If not found, it's the process instance and
        // other logic needs to happen
        ExecutionEntity parentExecution = null;
        if (executionEntity.getParentId() != null) {
            parentExecution = executionEntityManager.get(executionEntity.getParentId());
        }

        if (parentExecution != null) {

            // If the execution is a scope, and it is ended, all the child
            // executions must be deleted first.
            if (executionEntity.isScope()) {
                deleteChildExecutions(commandContext, executionEntity);
            }

            // Delete current execution
            logger.debug("Ending execution {}", execution.getId());
            deleteExecution(commandContext, executionEntity);

            logger.debug("Parent execution found. Continuing process using execution {}", parentExecution.getId());
            SubProcess subProcess = null;
            if (executionEntity.getCurrentFlowElement() instanceof EndEvent) {
                EndEvent endEvent = (EndEvent) executionEntity.getCurrentFlowElement();
                subProcess = endEvent.getSubProcess();

                if (parentExecution.getParentId() != null) {
                    deleteExecution(commandContext, parentExecution);
                    parentExecution = executionEntityManager.get(parentExecution.getParentId());
                }
            }

            if (subProcess != null) {
                parentExecution.setCurrentFlowElement(subProcess);
            } else {
                parentExecution.setCurrentFlowElement(executionEntity.getCurrentFlowElement());
            }
            agenda.planTakeOutgoingSequenceFlowsOperation(parentExecution, true);

        } else {

            String processInstanceId = executionEntity.getId(); // No parent
                                                                // execution ==
                                                                // process
                                                                // instance id
            logger.debug("No parent execution found. Verifying if process instance {} can be stopped.", processInstanceId);

            // TODO: optimisation can be made by keeping the nr of active
            // executions directly on the process instance in db

            // TODO: verify how many executions are still active in the process
            // instance, and stop the process instance otherwise
            Collection<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByProcessInstanceId(processInstanceId);
            int activeExecutions = 0;
            for (ExecutionEntity execution : executions) {
                if (execution.isActive()) {
                    activeExecutions++;
                }
            }

            if (activeExecutions == 0) {
                logger.debug("No active executions found. Ending process instance {} ", processInstanceId);
                deleteProcessInstanceExecutionEntity(commandContext, executionEntityManager, processInstanceId);
            } else {
                logger.debug("Active executions found. Process instance {} will not be ended.", processInstanceId);
            }
        }
    }

}
