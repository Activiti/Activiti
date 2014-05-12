package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Interface;
import org.activiti.bpmn.model.Operation;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;

/**
 * @author jbarrez
 */
public class OperationValidator extends ValidatorImpl {
	
	@Override
	public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
		if (bpmnModel.getInterfaces() != null) {
			for (Interface bpmnInterface : bpmnModel.getInterfaces()) {
				if (bpmnInterface.getOperations() != null) {
					for (Operation operation : bpmnInterface.getOperations()) {
						if (bpmnModel.getMessages().contains(operation.getInMessageRef())) {
							addError(errors, Problems.OPERATION_INVALID_IN_MESSAGE_REFERENCE, null, operation, "Invalid inMessageRef for operation");
						}
					}
				}
			}
		}
	}

}
