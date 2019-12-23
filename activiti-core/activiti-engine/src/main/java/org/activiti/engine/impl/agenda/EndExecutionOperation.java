package org.activiti.engine.impl.agenda;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Transaction;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This operations ends an execution and follows the typical BPMN rules to continue the process (if possible).
 * <p>
 * This operations is typically not scheduled from an {@link ActivityBehavior}, but rather from
 * another operation. This happens when the conditions are so that the process can't continue via the regular
 * ways and an execution cleanup needs to happen, potentially opening up new ways of continuing the process instance.
 */
public class EndExecutionOperation extends AbstractOperation {

    private static final Logger logger = LoggerFactory.getLogger(EndExecutionOperation.class);

    public EndExecutionOperation(CommandContext commandContext,
                                 ExecutionEntity execution) {
        super(commandContext,
              execution);
    }

    @Override
    public void run() {
        if (execution.isProcessInstanceType()) {
            handleProcessInstanceExecution(execution);
        } else {
            handleRegularExecution();
        }
    }

    protected void handleProcessInstanceExecution(ExecutionEntity processInstanceExecution) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

        String processInstanceId = processInstanceExecution.getId(); // No parent execution == process instance id
        logger.debug("No parent execution found. Verifying if process instance {} can be stopped.",
                     processInstanceId);

        ExecutionEntity superExecution = processInstanceExecution.getSuperExecution();
        SubProcessActivityBehavior subProcessActivityBehavior = null;

        // copy variables before destroying the ended sub process instance (call activity)
        if (superExecution != null) {
            FlowNode superExecutionElement = (FlowNode) superExecution.getCurrentFlowElement();
            subProcessActivityBehavior = (SubProcessActivityBehavior) superExecutionElement.getBehavior();
            try {
                subProcessActivityBehavior.completing(superExecution,
                                                      processInstanceExecution);
            } catch (RuntimeException e) {
                logger.error("Error while completing sub process of execution {}",
                             processInstanceExecution,
                             e);
                throw e;
            } catch (Exception e) {
                logger.error("Error while completing sub process of execution {}",
                             processInstanceExecution,
                             e);
                throw new ActivitiException("Error while completing sub process of execution " + processInstanceExecution,
                                            e);
            }
        }

        int activeExecutions = getNumberOfActiveChildExecutionsForProcessInstance(executionEntityManager,
                                                                                  processInstanceId);
        if (activeExecutions == 0) {
            logger.debug("No active executions found. Ending process instance {} ",
                         processInstanceId);

            // note the use of execution here vs processinstance execution for getting the flowelement
            executionEntityManager.deleteProcessInstanceExecutionEntity(processInstanceId,
                                                                        execution.getCurrentFlowElement() != null ? execution.getCurrentFlowElement().getId() : null,
                                                                        null,
                                                                        false,
                                                                        false);
        } else {
            logger.debug("Active executions found. Process instance {} will not be ended.",
                         processInstanceId);
        }

        Process process = ProcessDefinitionUtil.getProcess(processInstanceExecution.getProcessDefinitionId());

        // Execute execution listeners for process end.
        if (CollectionUtil.isNotEmpty(process.getExecutionListeners())) {
            executeExecutionListeners(process,
                                      processInstanceExecution,
                                      ExecutionListener.EVENTNAME_END);
        }

        // and trigger execution afterwards if doing a call activity
        if (superExecution != null) {
            superExecution.setSubProcessInstance(null);
            try {
                subProcessActivityBehavior.completed(superExecution);
            } catch (RuntimeException e) {
                logger.error("Error while completing sub process of execution {}",
                             processInstanceExecution,
                             e);
                throw e;
            } catch (Exception e) {
                logger.error("Error while completing sub process of execution {}",
                             processInstanceExecution,
                             e);
                throw new ActivitiException("Error while completing sub process of execution " + processInstanceExecution,
                                            e);
            }
        }
    }

    protected void handleRegularExecution() {

        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

        // There will be a parent execution (or else we would be in the process instance handling method)
        ExecutionEntity parentExecution = executionEntityManager.findById(execution.getParentId());

        // If the execution is a scope, all the child executions must be deleted first.
        if (execution.isScope()) {
            executionEntityManager.deleteChildExecutions(execution, null);
        }

        // Delete current execution
        logger.debug("Ending execution {}",
                     execution.getId());
        executionEntityManager.deleteExecutionAndRelatedData(execution, null);

        logger.debug("Parent execution found. Continuing process using execution {}",
                     parentExecution.getId());

        // When ending an execution in a multi instance subprocess , special care is needed
        if (isEndEventInMultiInstanceSubprocess(execution)) {
            handleMultiInstanceSubProcess(executionEntityManager,
                                          parentExecution);
            return;
        }

        SubProcess subProcess = execution.getCurrentFlowElement().getSubProcess();

        // If there are no more active child executions, the process can be continued
        // If not (eg an embedded subprocess still has active elements, we cannot continue)
        if (getNumberOfActiveChildExecutionsForExecution(executionEntityManager,
                                                         parentExecution.getId()) == 0
                || isAllEventScopeExecutions(executionEntityManager,
                                             parentExecution)) {

            ExecutionEntity executionToContinue = null;

            if (subProcess != null) {

                // In case of ending a subprocess: go up in the scopes and continue via the parent scope
                // unless its a compensation, then we don't need to do anything and can just end it

                if (subProcess.isForCompensation()) {
                    Context.getAgenda().planEndExecutionOperation(parentExecution);
                } else {
                    executionToContinue = handleSubProcessEnd(executionEntityManager,
                                                              parentExecution,
                                                              subProcess);
                }
            } else {

                // In the 'regular' case (not being in a subprocess), we use the parent execution to
                // continue process instance execution

                executionToContinue = handleRegularExecutionEnd(executionEntityManager,
                                                                parentExecution);
            }

            if (executionToContinue != null) {
                // only continue with outgoing sequence flows if the execution is
                // not the process instance root execution (otherwise the process instance is finished)
                if (executionToContinue.isProcessInstanceType()) {
                    handleProcessInstanceExecution(executionToContinue);
                } else {
                    Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionToContinue,
                                                                               true);
                }
            }
        }
    }

    protected ExecutionEntity handleSubProcessEnd(ExecutionEntityManager executionEntityManager,
                                                  ExecutionEntity parentExecution,
                                                  SubProcess subProcess) {

        ExecutionEntity executionToContinue = null;
        // create a new execution to take the outgoing sequence flows
        executionToContinue = executionEntityManager.createChildExecution(parentExecution.getParent());
        executionToContinue.setCurrentFlowElement(subProcess);

        boolean hasCompensation = false;
        if (subProcess instanceof Transaction) {
            hasCompensation = true;
        } else {
            for (FlowElement subElement : subProcess.getFlowElements()) {
                if (subElement instanceof Activity) {
                    Activity subActivity = (Activity) subElement;
                    if (CollectionUtil.isNotEmpty(subActivity.getBoundaryEvents())) {
                        for (BoundaryEvent boundaryEvent : subActivity.getBoundaryEvents()) {
                            if (CollectionUtil.isNotEmpty(boundaryEvent.getEventDefinitions()) &&
                                    boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
                                hasCompensation = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // All executions will be cleaned up afterwards. However, for compensation we need
        // a copy of these executions so we can use them later on when the compensation is thrown.
        // The following method does exactly that, and moves the executions beneath the process instance.
        if (hasCompensation) {
            ScopeUtil.createCopyOfSubProcessExecutionForCompensation(parentExecution);
        }

        executionEntityManager.deleteChildExecutions(parentExecution, null);
        executionEntityManager.deleteExecutionAndRelatedData(parentExecution, null);

        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
                ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_COMPLETED,
                                                         subProcess.getId(),
                                                         subProcess.getName(),
                                                         parentExecution.getId(),
                                                         parentExecution.getProcessInstanceId(),
                                                         parentExecution.getProcessDefinitionId(),
                                                         subProcess));
        return executionToContinue;
    }

    protected ExecutionEntity handleRegularExecutionEnd(ExecutionEntityManager executionEntityManager,
                                                        ExecutionEntity parentExecution) {
        ExecutionEntity executionToContinue = null;

        if (!parentExecution.isProcessInstanceType()
                && !(parentExecution.getCurrentFlowElement() instanceof SubProcess)) {
            parentExecution.setCurrentFlowElement(execution.getCurrentFlowElement());
        }

        if (execution.getCurrentFlowElement() instanceof SubProcess) {
            SubProcess currentSubProcess = (SubProcess) execution.getCurrentFlowElement();
            if (currentSubProcess.getOutgoingFlows().size() > 0) {
                // create a new execution to take the outgoing sequence flows
                executionToContinue = executionEntityManager.createChildExecution(parentExecution);
                executionToContinue.setCurrentFlowElement(execution.getCurrentFlowElement());
            } else {
                if (!parentExecution.getId().equals(parentExecution.getProcessInstanceId())) {
                    // create a new execution to take the outgoing sequence flows
                    executionToContinue = executionEntityManager.createChildExecution(parentExecution.getParent());
                    executionToContinue.setCurrentFlowElement(parentExecution.getCurrentFlowElement());

                    executionEntityManager.deleteChildExecutions(parentExecution, null);
                    executionEntityManager.deleteExecutionAndRelatedData(parentExecution,                         null);
                } else {
                    executionToContinue = parentExecution;
                }
            }
        } else {
            executionToContinue = parentExecution;
        }
        return executionToContinue;
    }

    protected void handleMultiInstanceSubProcess(ExecutionEntityManager executionEntityManager,
                                                 ExecutionEntity parentExecution) {
        List<ExecutionEntity> activeChildExecutions = getActiveChildExecutionsForExecution(executionEntityManager,
                                                                                           parentExecution.getId());
        boolean containsOtherChildExecutions = false;
        for (ExecutionEntity activeExecution : activeChildExecutions) {
            if (!activeExecution.getId().equals(execution.getId())) {
                containsOtherChildExecutions = true;
            }
        }

        if (!containsOtherChildExecutions) {

            // Destroy the current scope (subprocess) and leave via the subprocess

            ScopeUtil.createCopyOfSubProcessExecutionForCompensation(parentExecution);
            Context.getAgenda().planDestroyScopeOperation(parentExecution);

            SubProcess subProcess = execution.getCurrentFlowElement().getSubProcess();
            MultiInstanceActivityBehavior multiInstanceBehavior = (MultiInstanceActivityBehavior) subProcess.getBehavior();
            parentExecution.setCurrentFlowElement(subProcess);
            multiInstanceBehavior.leave(parentExecution);
        }
    }

    protected boolean isEndEventInMultiInstanceSubprocess(ExecutionEntity executionEntity) {
        if (executionEntity.getCurrentFlowElement() instanceof EndEvent) {
            SubProcess subProcess = ((EndEvent) execution.getCurrentFlowElement()).getSubProcess();
            return !executionEntity.getParent().isProcessInstanceType()
                    && subProcess != null
                    && subProcess.getLoopCharacteristics() != null
                    && subProcess.getBehavior() instanceof MultiInstanceActivityBehavior;
        }
        return false;
    }

    protected int getNumberOfActiveChildExecutionsForProcessInstance(ExecutionEntityManager executionEntityManager,
                                                                     String processInstanceId) {
        Collection<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByProcessInstanceId(processInstanceId);
        int activeExecutions = 0;
        for (ExecutionEntity execution : executions) {
            if (execution.isActive() && !processInstanceId.equals(execution.getId())) {
                activeExecutions++;
            }
        }
        return activeExecutions;
    }

    protected int getNumberOfActiveChildExecutionsForExecution(ExecutionEntityManager executionEntityManager,
                                                               String executionId) {
        List<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByParentExecutionId(executionId);
        int activeExecutions = 0;

        // Filter out the boundary events
        for (ExecutionEntity activeExecution : executions) {
            if (!(activeExecution.getCurrentFlowElement() instanceof BoundaryEvent)) {
                activeExecutions++;
            }
        }

        return activeExecutions;
    }

    protected List<ExecutionEntity> getActiveChildExecutionsForExecution(ExecutionEntityManager executionEntityManager,
                                                                         String executionId) {
        List<ExecutionEntity> activeChildExecutions = new ArrayList<ExecutionEntity>();
        List<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByParentExecutionId(executionId);

        for (ExecutionEntity activeExecution : executions) {
            if (!(activeExecution.getCurrentFlowElement() instanceof BoundaryEvent)) {
                activeChildExecutions.add(activeExecution);
            }
        }

        return activeChildExecutions;
    }

    protected boolean isAllEventScopeExecutions(ExecutionEntityManager executionEntityManager,
                                                ExecutionEntity parentExecution) {
        boolean allEventScopeExecutions = true;
        List<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByParentExecutionId(parentExecution.getId());
        for (ExecutionEntity childExecution : executions) {
            if (childExecution.isEventScope() && childExecution.getExecutions().size() == 0) {
                executionEntityManager.deleteExecutionAndRelatedData(childExecution, null);
            } else {
                allEventScopeExecutions = false;
                break;
            }
        }
        return allEventScopeExecutions;
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
