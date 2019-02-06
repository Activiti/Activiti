package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.ProcessLevelValidator;

public class AsyncPropertyValidator extends ProcessLevelValidator {

	private boolean asyncExecutorActivate;

	public AsyncPropertyValidator(boolean asyncExecutorActivate) {
		this.asyncExecutorActivate = asyncExecutorActivate;
	}

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		if (!asyncExecutorActivate) {
			validateFlowElementsInContainer(process, errors);
		}
	}

	protected void validateFlowElementsInContainer(FlowElementsContainer container, List<ValidationError> errors) {
		for (FlowElement flowElement : container.getFlowElements()) {
			if (flowElement instanceof FlowElementsContainer) {
				FlowElementsContainer subProcess = (FlowElementsContainer) flowElement;
				validateFlowElementsInContainer(subProcess, errors);
			}

			if ((flowElement instanceof FlowNode) && ((FlowNode) flowElement).isAsynchronous()) {
				addWarning(errors, "", null, null, "");
			}
		}
	}
}
