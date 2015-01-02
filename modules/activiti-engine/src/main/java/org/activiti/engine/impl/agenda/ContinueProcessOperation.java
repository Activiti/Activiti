package org.activiti.engine.impl.agenda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.util.cache.ProcessDefinitionCacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class ContinueProcessOperation extends AbstractOperation {

	private static Logger logger = LoggerFactory.getLogger(ContinueProcessOperation.class);
	
	public ContinueProcessOperation(Agenda agenda, ActivityExecution execution) {
		super(agenda, execution);
	}

	@Override
	public void run() {

		FlowElement currentFlowElement = execution.getCurrentFlowElement();

		if (currentFlowElement == null) {
			currentFlowElement = findCurrentFlowElement(execution);
		} else {
			execution.setCurrentActivityId(currentFlowElement.getId());
		}

		if (currentFlowElement instanceof FlowNode) {
			continueThroughFlowNode((FlowNode) currentFlowElement);
		} else if (currentFlowElement instanceof SequenceFlow) {
			continueThroughSequenceFlow((SequenceFlow) currentFlowElement);
		} else {
			throw new RuntimeException("Programmatic error: no current flow element found or invalid type: " + currentFlowElement + ". Halting.");
		}

	}

	private void continueThroughFlowNode(FlowNode flowNode) {
		
		// See if flowNode is an async activity and schedule as a job if that evaluates to true
		if (flowNode instanceof Activity) {
			Activity activity = (Activity) flowNode;
			if (activity.isAsynchronous()) {
				scheduleJob(activity);
				return;
			}
		}

		// Synchronous execution

		// Execute any boundary events
		Collection<BoundaryEvent> boundaryEvents = findBoundaryEventsForFlowNode(execution.getProcessDefinitionId(), flowNode);
		if (boundaryEvents != null && boundaryEvents.size() > 0) {
			executeBoundaryEvents(boundaryEvents);
		}

		// Execute actual behavior
		ActivityBehavior activityBehavior = (ActivityBehavior) flowNode.getBehavior();
		logger.debug("Executing activityBehavior {} with execution {}", activityBehavior.getClass(), execution.getId());
		activityBehavior.execute(execution);
	}
	
	protected void continueThroughSequenceFlow(SequenceFlow sequenceFlow) {
		FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
		execution.setCurrentFlowElement(targetFlowElement);
		logger.debug("Sequence flow '{}' encountered. Continuing process by following it using execution {}", sequenceFlow.getId(), execution.getId());
		agenda.planContinueProcessOperation(execution);
	}

	protected void scheduleJob(Activity activity) {
		MessageEntity message = new MessageEntity();
		message.setExecutionId(execution.getId());
		message.setProcessInstanceId(execution.getProcessInstanceId());
		message.setProcessDefinitionId(execution.getProcessDefinitionId());
		message.setExclusive(!activity.isNotExclusive());
		message.setJobHandlerType(AsyncContinuationJobHandler.TYPE);

		// Inherit tenant id (if applicable)
		if (execution.getTenantId() != null) {
			message.setTenantId(execution.getTenantId());
		}

		Context.getCommandContext().getJobEntityManager().send(message);
	}

	protected void executeBoundaryEvents(Collection<BoundaryEvent> boundaryEvents) {
		
		throw new RuntimeException("Joram needs to implement boundary events");

//		// The parent execution becomes a scope, and a child execution iscreated
//		// for each of the boundary events
//		execution.setScope(true);
//
//		ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
//
//		for (BoundaryEvent boundaryEvent : boundaryEvents) {
//
//			ExecutionEntity childExecutionEntity = new ExecutionEntity((ExecutionEntity) execution); // TODO: cast shouldnt be// necessary
//			childExecutionEntity.setParentId(execution.getId());
//			childExecutionEntity.setCurrentFlowElement(boundaryEvent);
//			childExecutionEntity.setCurrentActivityId(boundaryEvent.getId());
//			executionEntityManager.insert(childExecutionEntity);
//
//			((ActivityBehavior) boundaryEvent.getBehaviour())
//			        .execute(childExecutionEntity);
//		}

	}
	
	protected Collection<BoundaryEvent> findBoundaryEventsForFlowNode(final String processDefinitionId, final FlowNode flowNode) {
		org.activiti.bpmn.model.Process process = getProcessDefinition(processDefinitionId);

		// This could be cached or could be done at parsing time
		List<BoundaryEvent> results = new ArrayList<BoundaryEvent>(1);
		Collection<BoundaryEvent> boundaryEvents = process.findFlowElementsOfType(BoundaryEvent.class, true);
		for (BoundaryEvent boundaryEvent : boundaryEvents) {
			if (boundaryEvent.getAttachedToRefId() != null
			        && boundaryEvent.getAttachedToRefId().equals(flowNode.getId())) {
				results.add(boundaryEvent);
			}
		}
		return results;
	}
	
	protected org.activiti.bpmn.model.Process getProcessDefinition(String processDefinitionId) {
		// TODO: must be extracted / cache should be accessed in another way
		return ProcessDefinitionCacheUtil.getCachedProcess(processDefinitionId);
	}

}
