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


package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of the Parallel Gateway/AND gateway as defined in the BPMN 2.0 specification.
 *
 * The Parallel Gateway can be used for splitting a path of execution into multiple paths of executions (AND-split/fork behavior), one for every outgoing sequence flow.
 *
 * The Parallel Gateway can also be used for merging or joining paths of execution (AND-join). In this case, on every incoming sequence flow an execution needs to arrive, before leaving the Parallel
 * Gateway (and potentially then doing the fork behavior in case of multiple outgoing sequence flow).
 *
 * Note that there is a slight difference to spec (p. 436): "The parallel gateway is activated if there is at least one Token on each incoming sequence flow." We only check the number of incoming
 * tokens to the number of sequenceflow. So if two tokens would arrive through the same sequence flow, our implementation would activate the gateway.
 *
 * Note that a Parallel Gateway having one incoming and multiple outgoing sequence flow, is the same as having multiple outgoing sequence flow on a given activity. However, a parallel gateway does NOT
 * check conditions on the outgoing sequence flow.
 *


 */
public class ParallelGatewayActivityBehavior extends GatewayActivityBehavior {

  private static final long serialVersionUID = 1840892471343975524L;

  private static Logger log = LoggerFactory.getLogger(ParallelGatewayActivityBehavior.class);

  public void execute(DelegateExecution execution) {

    // First off all, deactivate the execution
    execution.inactivate();

    // Join
    FlowElement flowElement = execution.getCurrentFlowElement();
    ParallelGateway parallelGateway = null;
    if (flowElement instanceof ParallelGateway) {
      parallelGateway = (ParallelGateway) flowElement;
    } else {
      throw new ActivitiException("Programmatic error: parallel gateway behaviour can only be applied" + " to a ParallelGateway instance, but got an instance of " + flowElement);
    }

    lockFirstParentScope(execution);

    DelegateExecution multiInstanceExecution = null;
    if (hasMultiInstanceParent(parallelGateway)) {
      multiInstanceExecution = findMultiInstanceParentExecution(execution);
    }

    ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
    Collection<ExecutionEntity> joinedExecutions = executionEntityManager.findInactiveExecutionsByActivityIdAndProcessInstanceId(execution.getCurrentActivityId(), execution.getProcessInstanceId());
    if (multiInstanceExecution != null) {
      joinedExecutions = cleanJoinedExecutions(joinedExecutions, multiInstanceExecution);
    }

    int nbrOfExecutionsToJoin = parallelGateway.getIncomingFlows().size();
    int nbrOfExecutionsCurrentlyJoined = joinedExecutions.size();

    // Fork

    // Is needed to set the endTime for all historic activity joins
    Context.getCommandContext().getHistoryManager().recordActivityEnd((ExecutionEntity) execution, null);

    if (nbrOfExecutionsCurrentlyJoined == nbrOfExecutionsToJoin) {

      // Fork
      if (log.isDebugEnabled()) {
        log.debug("parallel gateway '{}' activates: {} of {} joined", execution.getCurrentActivityId(), nbrOfExecutionsCurrentlyJoined, nbrOfExecutionsToJoin);
      }

      if (parallelGateway.getIncomingFlows().size() > 1) {

        // All (now inactive) children are deleted.
        for (ExecutionEntity joinedExecution : joinedExecutions) {

          // The current execution will be reused and not deleted
          if (!joinedExecution.getId().equals(execution.getId())) {
            executionEntityManager.deleteExecutionAndRelatedData(joinedExecution, null);
          }

        }
      }

      // TODO: potential optimization here: reuse more then 1 execution, only 1 currently
      Context.getAgenda().planTakeOutgoingSequenceFlowsOperation((ExecutionEntity) execution, false); // false -> ignoring conditions on parallel gw

    } else if (log.isDebugEnabled()) {
      log.debug("parallel gateway '{}' does not activate: {} of {} joined", execution.getCurrentActivityId(), nbrOfExecutionsCurrentlyJoined, nbrOfExecutionsToJoin);
    }

  }

  protected Collection<ExecutionEntity> cleanJoinedExecutions(Collection<ExecutionEntity> joinedExecutions, DelegateExecution multiInstanceExecution) {
    List<ExecutionEntity> cleanedExecutions = new ArrayList<ExecutionEntity>();
    for (ExecutionEntity executionEntity : joinedExecutions) {
      if (isChildOfMultiInstanceExecution(executionEntity, multiInstanceExecution)) {
        cleanedExecutions.add(executionEntity);
      }
    }
    return cleanedExecutions;
  }

  protected boolean isChildOfMultiInstanceExecution(DelegateExecution executionEntity, DelegateExecution multiInstanceExecution) {
    boolean isChild = false;
    DelegateExecution parentExecution = executionEntity.getParent();
    if (parentExecution != null) {
      if (parentExecution.getId().equals(multiInstanceExecution.getId())) {
        isChild = true;
      } else {
        boolean isNestedChild = isChildOfMultiInstanceExecution(parentExecution, multiInstanceExecution);
        if (isNestedChild) {
          isChild = true;
        }
      }
    }

    return isChild;
  }

  protected boolean hasMultiInstanceParent(FlowNode flowNode) {
    boolean hasMultiInstanceParent = false;
    if (flowNode.getSubProcess() != null) {
      if (flowNode.getSubProcess().getLoopCharacteristics() != null) {
        hasMultiInstanceParent = true;
      } else {
        boolean hasNestedMultiInstanceParent = hasMultiInstanceParent(flowNode.getSubProcess());
        if (hasNestedMultiInstanceParent) {
          hasMultiInstanceParent = true;
        }
      }
    }

    return hasMultiInstanceParent;
  }

  protected DelegateExecution findMultiInstanceParentExecution(DelegateExecution execution) {
    DelegateExecution multiInstanceExecution = null;
    DelegateExecution parentExecution = execution.getParent();
    if (parentExecution != null && parentExecution.getCurrentFlowElement() != null) {
      FlowElement flowElement = parentExecution.getCurrentFlowElement();
      if (flowElement instanceof Activity) {
        Activity activity = (Activity) flowElement;
        if (activity.getLoopCharacteristics() != null) {
          multiInstanceExecution = parentExecution;
        }
      }

      if (multiInstanceExecution == null) {
        DelegateExecution potentialMultiInstanceExecution = findMultiInstanceParentExecution(parentExecution);
        if (potentialMultiInstanceExecution != null) {
          multiInstanceExecution = potentialMultiInstanceExecution;
        }
      }
    }

    return multiInstanceExecution;
  }

}
