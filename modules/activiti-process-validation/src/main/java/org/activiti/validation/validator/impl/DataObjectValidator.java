package org.activiti.validation.validator.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jbarrez
 */
public class DataObjectValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		
		// Gather data objects
		List<ValuedDataObject> allDataObjects = new ArrayList<ValuedDataObject>();
		allDataObjects.addAll(process.getDataObjects());
		List<SubProcess> subProcesses = process.findFlowElementsOfType(SubProcess.class, true);
		for (SubProcess subProcess : subProcesses) {
			allDataObjects.addAll(subProcess.getDataObjects());
		}
		
		// Validate
		for (ValuedDataObject dataObject : allDataObjects) {
			if (StringUtils.isEmpty(dataObject.getName())) {
				addError(errors, Problems.DATA_OBJECT_MISSING_NAME, process, dataObject, "Name is mandatory for a data object");
			}
		}
	
	}

}
