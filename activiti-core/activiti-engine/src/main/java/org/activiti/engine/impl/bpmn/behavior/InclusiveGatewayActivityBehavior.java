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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.Collection;
import java.util.Iterator;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.InactiveActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.ExecutionGraphUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Inclusive Gateway/OR gateway/inclusive data-based gateway as defined in the BPMN specification.
 *



 */
public class InclusiveGatewayActivityBehavior
    extends GatewayActivityBehavior
    implements InactiveActivityBehavior {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(
        InclusiveGatewayActivityBehavior.class.getName()
    );

    @Override
    public void execute(DelegateExecution execution) {
        // The join in the inclusive gateway works as follows:
        // When an execution enters it, it is inactivated.
        // All the inactivated executions stay in the inclusive gateway
        // until ALL executions that CAN reach the inclusive gateway have reached it.
        //
        // This check is repeated on execution changes until the inactivated
        // executions leave the gateway.

        execution.inactivate();
        executeInclusiveGatewayLogic((ExecutionEntity) execution);
    }

    @Override
    public void executeInactive(ExecutionEntity executionEntity) {
        executeInclusiveGatewayLogic(executionEntity);
    }

    protected void executeInclusiveGatewayLogic(ExecutionEntity execution) {
        CommandContext commandContext = Context.getCommandContext();
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

        lockFirstParentScope(execution);

        Collection<ExecutionEntity> allExecutions = executionEntityManager.findChildExecutionsByProcessInstanceId(
            execution.getProcessInstanceId()
        );
        Iterator<ExecutionEntity> executionIterator = allExecutions.iterator();
        boolean oneExecutionCanReachGateway = false;
        while (!oneExecutionCanReachGateway && executionIterator.hasNext()) {
            ExecutionEntity executionEntity = executionIterator.next();
            if (
                !executionEntity
                    .getActivityId()
                    .equals(execution.getCurrentActivityId())
            ) {
                boolean canReachGateway = ExecutionGraphUtil.isReachable(
                    execution.getProcessDefinitionId(),
                    executionEntity.getActivityId(),
                    execution.getCurrentActivityId()
                );
                if (canReachGateway) {
                    oneExecutionCanReachGateway = true;
                }
            } else if (
                executionEntity
                    .getActivityId()
                    .equals(execution.getCurrentActivityId()) &&
                executionEntity.isActive()
            ) {
                // Special case: the execution has reached the inc gw, but the operation hasn't been executed yet for that execution
                oneExecutionCanReachGateway = true;
            }
        }

        // If no execution can reach the gateway, the gateway activates and executes fork behavior
        if (!oneExecutionCanReachGateway) {
            logger.debug(
                "Inclusive gateway cannot be reached by any execution and is activated"
            );

            // Kill all executions here (except the incoming)
            Collection<ExecutionEntity> executionsInGateway = executionEntityManager.findInactiveExecutionsByActivityIdAndProcessInstanceId(
                execution.getCurrentActivityId(),
                execution.getProcessInstanceId()
            );
            for (ExecutionEntity executionEntityInGateway : executionsInGateway) {
                if (
                    !executionEntityInGateway.getId().equals(execution.getId())
                ) {
                    commandContext
                        .getHistoryManager()
                        .recordActivityEnd(executionEntityInGateway, null);
                    executionEntityManager.deleteExecutionAndRelatedData(
                        executionEntityInGateway,
                        null
                    );
                }
            }

            // Leave
            commandContext
                .getAgenda()
                .planTakeOutgoingSequenceFlowsOperation(execution, true);
        }
    }
}
