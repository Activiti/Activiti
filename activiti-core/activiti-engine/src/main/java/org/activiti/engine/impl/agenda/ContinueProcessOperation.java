package org.activiti.engine.impl.agenda;

import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.logging.LogMDC;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation that takes the current {@link FlowElement} set on the {@link ExecutionEntity}
 * and executes the associated {@link ActivityBehavior}. In the case of async, schedules a {@link Job}.
 * <p>
 * Also makes sure the {@link ExecutionListener} instances are called.
 */
public class ContinueProcessOperation extends AbstractOperation {

    private static Logger logger = LoggerFactory.getLogger(ContinueProcessOperation.class);

    protected boolean forceSynchronousOperation;
    protected boolean inCompensation;

    public ContinueProcessOperation(CommandContext commandContext,
                                    ExecutionEntity execution,
                                    boolean forceSynchronousOperation,
                                    boolean inCompensation) {

        super(commandContext,
              execution);
        this.forceSynchronousOperation = forceSynchronousOperation;
        this.inCompensation = inCompensation;
    }

    public ContinueProcessOperation(CommandContext commandContext,
                                    ExecutionEntity execution) {
        this(commandContext,
             execution,
             false,
             false);
    }

    @Override
    public void run() {
        FlowElement currentFlowElement = getCurrentFlowElement(execution);
        if (currentFlowElement instanceof FlowNode) {
            continueThroughFlowNode((FlowNode) currentFlowElement);
        } else if (currentFlowElement instanceof SequenceFlow) {
            continueThroughSequenceFlow((SequenceFlow) currentFlowElement);
        } else {
            throw new ActivitiException("Programmatic error: no current flow element found or invalid type: " + currentFlowElement + ". Halting.");
        }
    }

    protected void executeProcessStartExecutionListeners() {
        Process process = ProcessDefinitionUtil.getProcess(execution.getProcessDefinitionId());
        executeExecutionListeners(process,
                                  execution.getParent(),
                                  ExecutionListener.EVENTNAME_START);
    }

    protected void continueThroughFlowNode(FlowNode flowNode) {

        // Check if it's the initial flow element. If so, we must fire the execution listeners for the process too
        if (flowNode.getIncomingFlows() != null
                && flowNode.getIncomingFlows().size() == 0
                && flowNode.getSubProcess() == null) {
            executeProcessStartExecutionListeners();
        }

        // For a subprocess, a new child execution is created that will visit the steps of the subprocess
        // The original execution that arrived here will wait until the subprocess is finished
        // and will then be used to continue the process instance.
        if (flowNode instanceof SubProcess) {
            createChildExecutionForSubProcess((SubProcess) flowNode);
        }

        if (flowNode instanceof Activity && ((Activity) flowNode).hasMultiInstanceLoopCharacteristics()) {
            // the multi instance execution will look at async
            executeMultiInstanceSynchronous(flowNode);
        } else if (forceSynchronousOperation || !flowNode.isAsynchronous()) {
            executeSynchronous(flowNode);
        } else {
            executeAsynchronous(flowNode);
        }
    }

    protected void createChildExecutionForSubProcess(SubProcess subProcess) {
        ExecutionEntity parentScopeExecution = findFirstParentScopeExecution(execution);

        // Create the sub process execution that can be used to set variables
        // We create a new execution and delete the incoming one to have a proper scope that
        // does not conflict anything with any existing scopes

        ExecutionEntity subProcessExecution = commandContext.getExecutionEntityManager().createChildExecution(parentScopeExecution);
        subProcessExecution.setCurrentFlowElement(subProcess);
        subProcessExecution.setScope(true);

        commandContext.getExecutionEntityManager().deleteExecutionAndRelatedData(execution, null);
        execution = subProcessExecution;
    }

    protected void executeSynchronous(FlowNode flowNode) {
        commandContext.getHistoryManager().recordActivityStart(execution);

        // Execution listener: event 'start'
        if (CollectionUtil.isNotEmpty(flowNode.getExecutionListeners())) {
            executeExecutionListeners(flowNode,
                                      ExecutionListener.EVENTNAME_START);
        }

        // Execute any boundary events, sub process boundary events will be executed from the activity behavior
        if (!inCompensation && flowNode instanceof Activity) { // Only activities can have boundary events
            List<BoundaryEvent> boundaryEvents = ((Activity) flowNode).getBoundaryEvents();
            if (CollectionUtil.isNotEmpty(boundaryEvents)) {
                executeBoundaryEvents(boundaryEvents,
                                      execution);
            }
        }

        // Execute actual behavior
        ActivityBehavior activityBehavior = (ActivityBehavior) flowNode.getBehavior();

        if (activityBehavior != null) {
            executeActivityBehavior(activityBehavior,
                                    flowNode);
        } else {
            logger.debug("No activityBehavior on activity '{}' with execution {}",
                         flowNode.getId(),
                         execution.getId());
            Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(execution,
                                                                       true);
        }
    }

    protected void executeAsynchronous(FlowNode flowNode) {
        JobEntity job = commandContext.getJobManager().createAsyncJob(execution,
                                                                      flowNode.isExclusive());
        commandContext.getJobManager().scheduleAsyncJob(job);
    }

    protected void executeMultiInstanceSynchronous(FlowNode flowNode) {

        // Execution listener: event 'start'
        if (CollectionUtil.isNotEmpty(flowNode.getExecutionListeners())) {
            executeExecutionListeners(flowNode,
                                      ExecutionListener.EVENTNAME_START);
        }

        // Execute any boundary events, sub process boundary events will be executed from the activity behavior
        if (!inCompensation && flowNode instanceof Activity) { // Only activities can have boundary events
            List<BoundaryEvent> boundaryEvents = ((Activity) flowNode).getBoundaryEvents();
            if (CollectionUtil.isNotEmpty(boundaryEvents)) {
                executeBoundaryEvents(boundaryEvents,
                                      execution);
            }
        }

        // Execute the multi instance behavior
        ActivityBehavior activityBehavior = (ActivityBehavior) flowNode.getBehavior();

        if (activityBehavior != null) {
            executeActivityBehavior(activityBehavior,
                                    flowNode);
        } else {
            throw new ActivitiException("Expected an activity behavior in flow node " + flowNode.getId());
        }
    }

    protected void executeActivityBehavior(ActivityBehavior activityBehavior,
                                           FlowNode flowNode) {
        logger.debug("Executing activityBehavior {} on activity '{}' with execution {}",
                     activityBehavior.getClass(),
                     flowNode.getId(),
                     execution.getId());

        if (Context.getProcessEngineConfiguration() != null &&
                Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled() &&
                !(activityBehavior instanceof MultiInstanceActivityBehavior)) {
            Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
                    ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_STARTED,
                                                             flowNode.getId(),
                                                             flowNode.getName(),
                                                             execution.getId(),
                                                             execution.getProcessInstanceId(),
                                                             execution.getProcessDefinitionId(),
                                                             flowNode));
        }

        try {
            activityBehavior.execute(execution);
        } catch (RuntimeException e) {
            if (LogMDC.isMDCEnabled()) {
                LogMDC.putMDCExecution(execution);
            }
            throw e;
        }
    }

    protected void continueThroughSequenceFlow(SequenceFlow sequenceFlow) {

        // Execution listener. Sequenceflow only 'take' makes sense ... but we've supported all three since the beginning
        if (CollectionUtil.isNotEmpty(sequenceFlow.getExecutionListeners())) {
            executeExecutionListeners(sequenceFlow,
                                      ExecutionListener.EVENTNAME_START);
            executeExecutionListeners(sequenceFlow,
                                      ExecutionListener.EVENTNAME_TAKE);
            executeExecutionListeners(sequenceFlow,
                                      ExecutionListener.EVENTNAME_END);
        }

        // Firing event that transition is being taken
        if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            FlowElement sourceFlowElement = sequenceFlow.getSourceFlowElement();
            FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
            Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
                    ActivitiEventBuilder.createSequenceFlowTakenEvent(
                            (ExecutionEntity) execution,
                            ActivitiEventType.SEQUENCEFLOW_TAKEN,
                            sequenceFlow.getId(),
                            sourceFlowElement != null ? sourceFlowElement.getId() : null,
                            sourceFlowElement != null ? (String) sourceFlowElement.getName() : null,
                            sourceFlowElement != null ? sourceFlowElement.getClass().getName() : null,
                            sourceFlowElement != null ? ((FlowNode) sourceFlowElement).getBehavior() : null,
                            targetFlowElement != null ? targetFlowElement.getId() : null,
                            targetFlowElement != null ? targetFlowElement.getName() : null,
                            targetFlowElement != null ? targetFlowElement.getClass().getName() : null,
                            targetFlowElement != null ? ((FlowNode) targetFlowElement).getBehavior() : null));
        }

        FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
        execution.setCurrentFlowElement(targetFlowElement);

        logger.debug("Sequence flow '{}' encountered. Continuing process by following it using execution {}",
                     sequenceFlow.getId(),
                     execution.getId());
        Context.getAgenda().planContinueProcessOperation(execution);
    }

    protected void executeBoundaryEvents(Collection<BoundaryEvent> boundaryEvents,
                                         ExecutionEntity execution) {

        // The parent execution becomes a scope, and a child execution is created for each of the boundary events
        for (BoundaryEvent boundaryEvent : boundaryEvents) {

            if (CollectionUtil.isEmpty(boundaryEvent.getEventDefinitions())
                    || (boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition)) {
                continue;
            }

            // A Child execution of the current execution is created to represent the boundary event being active
            ExecutionEntity childExecutionEntity = commandContext.getExecutionEntityManager().createChildExecution((ExecutionEntity) execution);
            childExecutionEntity.setParentId(execution.getId());
            childExecutionEntity.setCurrentFlowElement(boundaryEvent);
            childExecutionEntity.setScope(false);

            ActivityBehavior boundaryEventBehavior = ((ActivityBehavior) boundaryEvent.getBehavior());
            logger.debug("Executing boundary event activityBehavior {} with execution {}",
                         boundaryEventBehavior.getClass(),
                         childExecutionEntity.getId());
            boundaryEventBehavior.execute(childExecutionEntity);
        }
    }
}
