package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Message;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jbarrez
 */
public class MessageValidator extends ValidatorImpl {

	@Override
	public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
		if (bpmnModel.getMessages() != null && bpmnModel.getMessages().size() > 0) {
			for (Message message : bpmnModel.getMessages()) {
				
				// Item ref
				if (StringUtils.isNotEmpty(message.getItemRef())) {
	        if (!bpmnModel.getItemDefinitions().containsKey(message.getItemRef())) {
	        	addError(errors, Problems.MESSAGE_INVALID_ITEM_REF, null, message, "Item reference is invalid: not found");
	        } 
	      }
				
			}
		}
	}

}
