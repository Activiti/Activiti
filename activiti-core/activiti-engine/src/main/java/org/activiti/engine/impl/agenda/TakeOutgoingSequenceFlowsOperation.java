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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.AdhocSubProcess;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.impl.util.condition.ConditionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation that leaves the {@link FlowElement} where the {@link ExecutionEntity} is currently at
 * and leaves it following the sequence flow.
 */
public class TakeOutgoingSequenceFlowsOperation extends AbstractOperation {

    private static final Logger logger = LoggerFactory.getLogger(TakeOutgoingSequenceFlowsOperation.class);

    protected boolean evaluateConditions;

    public TakeOutgoingSequenceFlowsOperation(CommandContext commandContext,
                                              ExecutionEntity executionEntity,
                                              boolean evaluateConditions) {
        super(commandContext,
              executionEntity);
        this.evaluateConditions = evaluateConditions;
    }

    @Override
    public void run() {
        FlowElement currentFlowElement = getCurrentFlowElement(execution);

        // Compensation check
        if ((currentFlowElement instanceof Activity)
                && (((Activity) currentFlowElement)).isForCompensation()) {

      /*
       * If the current flow element is part of a compensation, we don't always
       * want to follow the regular rules of leaving an activity.
       * More specifically, if there are no outgoing sequenceflow, we simply must stop
       * the execution there and don't go up in the scopes as we usually do
       * to find the outgoing sequenceflow
       */

            cleanupCompensation();
            return;
        }

        // When leaving the current activity, we need to delete any related execution (eg active boundary events)
        cleanupExecutions(currentFlowElement);

        if (currentFlowElement instanceof FlowNode) {
            handleFlowNode((FlowNode) currentFlowElement);
        } else if (currentFlowElement instanceof SequenceFlow) {
            handleSequenceFlow();
        }
    }

    protected void handleFlowNode(FlowNode flowNode) {
        handleActivityEnd(flowNode);
        if (flowNode.getParentContainer() != null
                && flowNode.getParentContainer() instanceof AdhocSubProcess) {
            handleAdhocSubProcess(flowNode);
        } else {
            leaveFlowNode(flowNode);
        }
    }

    protected void handleActivityEnd(FlowNode flowNode) {
        // a process instance execution can never leave a flow node, but it can pass here whilst cleaning up
        // hence the check for NOT being a process instance
        if (!execution.isProcessInstanceType()) {

            if (CollectionUtil.isNotEmpty(flowNode.getExecutionListeners())) {
                executeExecutionListeners(flowNode,
                                          ExecutionListener.EVENTNAME_END);
            }

            commandContext.getHistoryManager().recordActivityEnd(execution,
                                                                 null);

            if (!(execution.getCurrentFlowElement() instanceof SubProcess) && !(flowNode.getBehavior() instanceof MultiInstanceActivityBehavior)) {
                Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
                        ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_COMPLETED,
                                                                 flowNode.getId(),
                                                                 flowNode.getName(),
                                                                 execution.getId(),
                                                                 execution.getProcessInstanceId(),
                                                                 execution.getProcessDefinitionId(),
                                                                 flowNode));
            }
        }
    }

    protected void leaveFlowNode(FlowNode flowNode) {

        logger.debug("Leaving flow node {} with id '{}' by following it's {} outgoing sequenceflow",
                     flowNode.getClass(),
                     flowNode.getId(),
                     flowNode.getOutgoingFlows().size());

        // Get default sequence flow (if set)
        String defaultSequenceFlowId = null;
        if (flowNode instanceof Activity) {
            defaultSequenceFlowId = ((Activity) flowNode).getDefaultFlow();
        } else if (flowNode instanceof Gateway) {
            defaultSequenceFlowId = ((Gateway) flowNode).getDefaultFlow();
        }

        // Determine which sequence flows can be used for leaving
        List<SequenceFlow> outgoingSequenceFlows = new ArrayList<SequenceFlow>();
        for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {

            String skipExpressionString = sequenceFlow.getSkipExpression();
            if (!SkipExpressionUtil.isSkipExpressionEnabled(execution,
                                                            skipExpressionString)) {

                if (!evaluateConditions
                        || (evaluateConditions && ConditionUtil.hasTrueCondition(sequenceFlow,
                                                                                 execution) && (defaultSequenceFlowId == null || !defaultSequenceFlowId.equals(sequenceFlow.getId())))) {
                    outgoingSequenceFlows.add(sequenceFlow);
                }
            } else if (flowNode.getOutgoingFlows().size() == 1 || SkipExpressionUtil.shouldSkipFlowElement(commandContext,
                                                                                                           execution,
                                                                                                           skipExpressionString)) {
                // The 'skip' for a sequence flow means that we skip the condition, not the sequence flow.
                outgoingSequenceFlows.add(sequenceFlow);
            }
        }

        // Check if there is a default sequence flow
        if (outgoingSequenceFlows.size() == 0 && evaluateConditions) { // The elements that set this to false also have no support for default sequence flow
            if (defaultSequenceFlowId != null) {
                for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
                    if (defaultSequenceFlowId.equals(sequenceFlow.getId())) {
                        outgoingSequenceFlows.add(sequenceFlow);
                        break;
                    }
                }
            }
        }

        // No outgoing found. Ending the execution
        if (outgoingSequenceFlows.size() == 0) {
            if (flowNode.getOutgoingFlows() == null || flowNode.getOutgoingFlows().size() == 0) {
                logger.debug("No outgoing sequence flow found for flow node '{}'.",
                             flowNode.getId());
                Context.getAgenda().planEndExecutionOperation(execution);
            } else {
                throw new ActivitiException("No outgoing sequence flow of element '" + flowNode.getId() + "' could be selected for continuing the process");
            }
        } else {

            // Leave, and reuse the incoming sequence flow, make executions for all the others (if applicable)

            ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
            List<ExecutionEntity> outgoingExecutions = new ArrayList<ExecutionEntity>(flowNode.getOutgoingFlows().size());

            SequenceFlow sequenceFlow = outgoingSequenceFlows.get(0);

            // Reuse existing one
            execution.setCurrentFlowElement(sequenceFlow);
            execution.setActive(true);
            outgoingExecutions.add((ExecutionEntity) execution);

            // Executions for all the other one
            if (outgoingSequenceFlows.size() > 1) {
                for (int i = 1; i < outgoingSequenceFlows.size(); i++) {

                    ExecutionEntity parent = execution.getParentId() != null ? execution.getParent() : execution;
                    ExecutionEntity outgoingExecutionEntity = commandContext.getExecutionEntityManager().createChildExecution(parent);

                    SequenceFlow outgoingSequenceFlow = outgoingSequenceFlows.get(i);
                    outgoingExecutionEntity.setCurrentFlowElement(outgoingSequenceFlow);

                    executionEntityManager.insert(outgoingExecutionEntity);
                    outgoingExecutions.add(outgoingExecutionEntity);
                }
            }

            // Leave (only done when all executions have been made, since some queries depend on this)
            for (ExecutionEntity outgoingExecution : outgoingExecutions) {
                Context.getAgenda().planContinueProcessOperation(outgoingExecution);
            }
        }
    }

    protected void handleAdhocSubProcess(FlowNode flowNode) {
        boolean completeAdhocSubProcess = false;
        AdhocSubProcess adhocSubProcess = (AdhocSubProcess) flowNode.getParentContainer();
        if (adhocSubProcess.getCompletionCondition() != null) {
            Expression expression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(adhocSubProcess.getCompletionCondition());
            Condition condition = new UelExpressionCondition(expression);
            if (condition.evaluate(adhocSubProcess.getId(),
                                   execution)) {
                completeAdhocSubProcess = true;
            }
        }

        if (flowNode.getOutgoingFlows().size() > 0) {
            leaveFlowNode(flowNode);
        } else {
            commandContext.getExecutionEntityManager().deleteExecutionAndRelatedData(execution, null);
        }

        if (completeAdhocSubProcess) {
            boolean endAdhocSubProcess = true;
            if (!adhocSubProcess.isCancelRemainingInstances()) {
                List<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager().findChildExecutionsByParentExecutionId(execution.getParentId());
                for (ExecutionEntity executionEntity : childExecutions) {
                    if (!executionEntity.getId().equals(execution.getId())) {
                        endAdhocSubProcess = false;
                        break;
                    }
                }
            }

            if (endAdhocSubProcess) {
                Context.getAgenda().planEndExecutionOperation(execution.getParent());
            }
        }
    }

    protected void handleSequenceFlow() {
        commandContext.getHistoryManager().recordActivityEnd(execution,
                                                             null);
        Context.getAgenda().planContinueProcessOperation(execution);
    }

    protected void cleanupCompensation() {

        // The compensation is at the end here. Simply stop the execution.

        commandContext.getHistoryManager().recordActivityEnd(execution,
                                                             null);
        commandContext.getExecutionEntityManager().deleteExecutionAndRelatedData(execution, null);

        ExecutionEntity parentExecutionEntity = execution.getParent();
        if (parentExecutionEntity.isScope() && !parentExecutionEntity.isProcessInstanceType()) {

            if (allChildExecutionsEnded(parentExecutionEntity,
                                        null)) {

                // Go up the hierarchy to check if the next scope is ended too.
                // This could happen if only the compensation activity is still active, but the
                // main process is already finished.

                ExecutionEntity executionEntityToEnd = parentExecutionEntity;
                ExecutionEntity scopeExecutionEntity = findNextParentScopeExecutionWithAllEndedChildExecutions(parentExecutionEntity,
                                                                                                               parentExecutionEntity);
                while (scopeExecutionEntity != null) {
                    executionEntityToEnd = scopeExecutionEntity;
                    scopeExecutionEntity = findNextParentScopeExecutionWithAllEndedChildExecutions(scopeExecutionEntity,
                                                                                                   parentExecutionEntity);
                }

                if (executionEntityToEnd.isProcessInstanceType()) {
                    Context.getAgenda().planEndExecutionOperation(executionEntityToEnd);
                } else {
                    Context.getAgenda().planDestroyScopeOperation(executionEntityToEnd);
                }
            }
        }
    }

    protected void cleanupExecutions(FlowElement currentFlowElement) {
        if (execution.getParentId() != null && execution.isScope()) {

            // If the execution is a scope (and not a process instance), the scope must first be
            // destroyed before we can continue and follow the sequence flow

            Context.getAgenda().planDestroyScopeOperation(execution);
        } else if (currentFlowElement instanceof Activity) {

            // If the current activity is an activity, we need to remove any currently active boundary events

            Activity activity = (Activity) currentFlowElement;
            if (CollectionUtil.isNotEmpty(activity.getBoundaryEvents())) {

                // Cancel events are not removed
                List<String> notToDeleteEvents = new ArrayList<String>();
                for (BoundaryEvent event : activity.getBoundaryEvents()) {
                    if (CollectionUtil.isNotEmpty(event.getEventDefinitions()) &&
                            event.getEventDefinitions().get(0) instanceof CancelEventDefinition) {
                        notToDeleteEvents.add(event.getId());
                    }
                }

                // Delete all child executions
                Collection<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager().findChildExecutionsByParentExecutionId(execution.getId());
                for (ExecutionEntity childExecution : childExecutions) {
                    if (childExecution.getCurrentFlowElement() == null || !notToDeleteEvents.contains(childExecution.getCurrentFlowElement().getId())) {
                        commandContext.getExecutionEntityManager().deleteExecutionAndRelatedData(childExecution, null);
                    }
                }
            }
        }
    }

    // Compensation helper methods

    /**
     * @param executionEntityToIgnore The execution entity which we can ignore to be ended,
     * as it's the execution currently being handled in this operation.
     */
    protected ExecutionEntity findNextParentScopeExecutionWithAllEndedChildExecutions(ExecutionEntity executionEntity,
                                                                                      ExecutionEntity executionEntityToIgnore) {
        if (executionEntity.getParentId() != null) {
            ExecutionEntity scopeExecutionEntity = executionEntity.getParent();

            // Find next scope
            while (!scopeExecutionEntity.isScope() || !scopeExecutionEntity.isProcessInstanceType()) {
                scopeExecutionEntity = scopeExecutionEntity.getParent();
            }

            // Return when all child executions for it are ended
            if (allChildExecutionsEnded(scopeExecutionEntity,
                                        executionEntityToIgnore)) {
                return scopeExecutionEntity;
            }
        }
        return null;
    }

    protected boolean allChildExecutionsEnded(ExecutionEntity parentExecutionEntity,
                                              ExecutionEntity executionEntityToIgnore) {
        for (ExecutionEntity childExecutionEntity : parentExecutionEntity.getExecutions()) {
            if (executionEntityToIgnore == null || !executionEntityToIgnore.getId().equals(childExecutionEntity.getId())) {
                if (!childExecutionEntity.isEnded()) {
                    return false;
                }
                if (childExecutionEntity.getExecutions() != null && childExecutionEntity.getExecutions().size() > 0) {
                    if (!allChildExecutionsEnded(childExecutionEntity,
                                                 executionEntityToIgnore)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
