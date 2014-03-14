package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;

/**
 * @author jbarrez
 */
public class DiagramInterchangeInfoValidator extends ValidatorImpl {

	
	@Override
	public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
		if (bpmnModel.getLocationMap().size() > 0) {

			// Location map
			for (String bpmnReference : bpmnModel.getLocationMap().keySet()) {
				if (bpmnModel.getFlowElement(bpmnReference) == null) {
					// ACT-1625: don't warn when artifacts are referenced from DI
					if (bpmnModel.getArtifact(bpmnReference) == null) {
						addWarning(errors, Problems.DI_INVALID_REFERENCE, null, bpmnModel.getFlowElement(bpmnReference),
								"Invalid reference in diagram interchange definition: could not find " + bpmnReference);
					}
				} else if (!(bpmnModel.getFlowElement(bpmnReference) instanceof FlowNode)) {
					addWarning(errors, Problems.DI_DOES_NOT_REFERENCE_FLOWNODE, null, bpmnModel.getFlowElement(bpmnReference),
							"Invalid reference in diagram interchange definition: " + bpmnReference + " does not reference a flow node");
				}
			}
			
		}
			
		if (bpmnModel.getFlowLocationMap().size() > 0) {
			// flowlocation map
			for (String bpmnReference : bpmnModel.getFlowLocationMap().keySet()) {
				if (bpmnModel.getFlowElement(bpmnReference) == null) {
					// ACT-1625: don't warn when artifacts are referenced from DI
					if (bpmnModel.getArtifact(bpmnReference) == null) {
						addWarning(errors, Problems.DI_INVALID_REFERENCE, null, bpmnModel.getFlowElement(bpmnReference),
								"Invalid reference in diagram interchange definition: could not find " + bpmnReference);
					}
				} else if (!(bpmnModel.getFlowElement(bpmnReference) instanceof SequenceFlow)) {
					if (bpmnModel.getFlowLocationMap().get(bpmnReference).size() > 0) {
						addWarning(errors, Problems.DI_DOES_NOT_REFERENCE_SEQ_FLOW, null, bpmnModel.getFlowElement(bpmnReference),
								"Invalid reference in diagram interchange definition: " + bpmnReference + " does not reference a sequence flow");
					} else {
						addWarning(errors, Problems.DI_DOES_NOT_REFERENCE_SEQ_FLOW, null, bpmnModel.getFlowElement(bpmnReference),
								"Invalid reference in diagram interchange definition: " + bpmnReference + " does not reference a sequence flow");
					}
				}
			}
		}
	}

}
