package org.activiti.workflow.simple.util;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;

public class BpmnModelUtil {

	private static final String SIGNAL_PREFIX = "signal_";
	private static final String SIGNAL_THROWEVENT_PREFIX = "signal_throw_";
	private static final String SIGNAL_BOUNDARY_EVENT = "signal_boundary_";

	public static List<FlowElement> findSucessorFlowElementsFor(
			Process process, FlowElement sourceFlowElement) {
		List<FlowElement> successors = new ArrayList<FlowElement>();
		for (SequenceFlow sequenceFlow : process.findFlowElementsOfType(SequenceFlow.class)) {
			if (sequenceFlow.getSourceRef().equals(sourceFlowElement.getId())) {
				successors.add(process.getFlowElement(sequenceFlow.getTargetRef()));
			}
		}
		return successors;
	}

	public static SequenceFlow createSequenceFlow(WorkflowDefinitionConversion conversion, FlowNode source, FlowNode target) {
		return createSequenceFlow(conversion, source, target,(ActivitiListener[]) null);
	}

	public static SequenceFlow createSequenceFlow(WorkflowDefinitionConversion conversion, FlowNode source,
			FlowNode target, ActivitiListener... executionListeners) {
		SequenceFlow sequenceFlow = new SequenceFlow();
		sequenceFlow.setId(conversion.getUniqueNumberedId(ConversionConstants.DEFAULT_SEQUENCEFLOW_PREFIX));
		sequenceFlow.setSourceRef(source.getId());
		sequenceFlow.setTargetRef(target.getId());

		if (executionListeners != null && executionListeners.length > 0) {
			List<ActivitiListener> listeners = new ArrayList<ActivitiListener>();
			for (ActivitiListener listener : executionListeners) {
				listeners.add(listener);
			}
			sequenceFlow.setExecutionListeners(listeners);
		}

		return sequenceFlow;
	}

	public static Signal createSignal(WorkflowDefinitionConversion conversion, String signalName, boolean processInstanceScope) {
		Signal approvalFinishedSignal = new Signal();
		approvalFinishedSignal.setId(conversion.getUniqueNumberedId(SIGNAL_PREFIX));
		approvalFinishedSignal.setName(signalName);

		if (processInstanceScope) {
			approvalFinishedSignal.setScope("processInstance");
		}

		return approvalFinishedSignal;
	}

	public static ThrowEvent createSignalThrowEvent(
			WorkflowDefinitionConversion conversion,
			Signal approvalFinishedSignal) {
		SignalEventDefinition signalEventDefinition = new SignalEventDefinition();
		signalEventDefinition.setSignalRef(approvalFinishedSignal.getId());
		List<EventDefinition> signalEventDefinitions = new ArrayList<EventDefinition>();
		signalEventDefinitions.add(signalEventDefinition);

		ThrowEvent signalThrowEvent = new ThrowEvent();
		signalThrowEvent.setId(conversion.getUniqueNumberedId(SIGNAL_THROWEVENT_PREFIX));
		signalThrowEvent.setEventDefinitions(signalEventDefinitions);
		return signalThrowEvent;
	}

	public static BoundaryEvent createSignalBoundaryEvent(
			WorkflowDefinitionConversion conversion, Signal signal,
			Activity activity, boolean cancelActivity) {
		BoundaryEvent signalBoundaryEvent = new BoundaryEvent();
		signalBoundaryEvent.setId(conversion.getUniqueNumberedId(SIGNAL_BOUNDARY_EVENT));
		signalBoundaryEvent.setCancelActivity(cancelActivity);
		signalBoundaryEvent.setAttachedToRef(activity);

		SignalEventDefinition boundarySignalEventDefinition = new SignalEventDefinition();
		boundarySignalEventDefinition.setSignalRef(signal.getId());
		signalBoundaryEvent.addEventDefinition(boundarySignalEventDefinition);

		return signalBoundaryEvent;
	}

}
