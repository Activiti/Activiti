package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataAssociation;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * A validator for stuff that is shared accross all flow elements
 * 
 * @author jbarrez
 */
public class FlowElementValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		for (FlowElement flowElement : process.getFlowElements()) {
			
			if (flowElement instanceof Activity) {
				Activity activity = (Activity) flowElement;
				handleMultiInstanceLoopCharacteristics(process, activity, errors);
				handleDataAssociations(process, activity, errors);
			}
			
		}
		
	}
	
	protected void handleMultiInstanceLoopCharacteristics(Process process, Activity activity, List<ValidationError> errors) {
		MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = activity.getLoopCharacteristics();
		if (multiInstanceLoopCharacteristics != null) {

			if (StringUtils.isEmpty(multiInstanceLoopCharacteristics.getLoopCardinality())
	    		&& StringUtils.isEmpty(multiInstanceLoopCharacteristics.getInputDataItem())) {
	    	addError(errors, Problems.MULTI_INSTANCE_MISSING_COLLECTION, process, activity, 
	    			"Either loopCardinality or loopDataInputRef/activiti:collection must been set");
	    }

		}
	}

	protected void handleDataAssociations(Process process, Activity activity, List<ValidationError> errors) {
	  if (activity.getDataInputAssociations() != null) {
	  	for (DataAssociation dataAssociation : activity.getDataInputAssociations()) {
	  		if (StringUtils.isEmpty(dataAssociation.getTargetRef())) {
	  			 addError(errors, Problems.DATA_ASSOCIATION_MISSING_TARGETREF, process, activity, 
	  					 "Targetref is required on a data association");
	  	    }
	  	}
	  }
	  if (activity.getDataOutputAssociations() != null) {
	  	for (DataAssociation dataAssociation : activity.getDataOutputAssociations()) {
	  		if (StringUtils.isEmpty(dataAssociation.getTargetRef())) {
	  			 addError(errors, Problems.DATA_ASSOCIATION_MISSING_TARGETREF, process, activity, 
	  					 "Targetref is required on a data association");
	  	    }
	  	}
	  }
  }

}
