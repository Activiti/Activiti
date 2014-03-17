package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jbarrez
 */
public class SequenceflowValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<SequenceFlow> sequenceFlows = process.findFlowElementsOfType(SequenceFlow.class);
		for (SequenceFlow sequenceFlow : sequenceFlows) {
			
			String sourceRef = sequenceFlow.getSourceRef();
			String targetRef = sequenceFlow.getTargetRef();
			
			if (StringUtils.isEmpty(sourceRef)) {
				addError(errors, Problems.SEQ_FLOW_INVALID_SRC, process, sequenceFlow, "Invalid source for sequenceflow");
			}
			if (StringUtils.isEmpty(targetRef)) {
				addError(errors, Problems.SEQ_FLOW_INVALID_TARGET, process, sequenceFlow, "Invalid target for sequenceflow");
			}
			
		  // Implicit check: sequence flow cannot cross (sub) process boundaries, hence we check the parent and not the process
			// (could be subprocess for example)
			FlowElement source = process.getFlowElementRecursive(sourceRef);
			FlowElement target = process.getFlowElementRecursive(targetRef);
			
			
			// Src and target validation
			if (source == null) {
				addError(errors, Problems.SEQ_FLOW_INVALID_SRC, process, sequenceFlow, "Invalid source for sequenceflow");
			}
			if (target == null) {
				addError(errors, Problems.SEQ_FLOW_INVALID_TARGET, process, sequenceFlow, "Invalid target for sequenceflow");
			}
			
		}
	}

}
