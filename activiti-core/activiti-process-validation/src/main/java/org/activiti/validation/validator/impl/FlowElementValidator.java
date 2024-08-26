/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.validation.validator.impl;

import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataAssociation;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * A validator for stuff that is shared across all flow elements
 *


 */
public class FlowElementValidator extends ProcessLevelValidator {

	protected static final int ID_MAX_LENGTH = 255;
    private void handleValidations(Process process, Activity activity, List<ValidationError> errors){
        handleConstraints(process, activity, errors);
        handleMultiInstanceLoopCharacteristics(process, activity, errors);
        handleDataAssociations(process, activity, errors);
    }

	@Override
    protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
        for (FlowElement flowElement : process.getFlowElements()) {
            handleFlowElement(process, flowElement, errors);
        }
    }

    private void handleFlowElement(Process process, FlowElement flowElement, List<ValidationError> errors) {
        if (flowElement instanceof Activity) {
            Activity activity = (Activity) flowElement;
            if (activity instanceof SubProcess) {
                SubProcess subProcess = (SubProcess) activity;
                for (FlowElement subElement : subProcess.getFlowElements()) {
                    handleFlowElement(process, subElement, errors);
                }
            } else {
                handleValidations(process, activity, errors);
            }
        }
    }

	protected void handleConstraints(Process process, Activity activity, List<ValidationError> errors) {
		if (activity.getId() != null && activity.getId().length() > ID_MAX_LENGTH) {
			Map<String, String> params = new HashMap<>();
			params.put("maxLength", String.valueOf(ID_MAX_LENGTH));
			addError(errors, Problems.FLOW_ELEMENT_ID_TOO_LONG, process, activity, params);
		}
	}

	protected void handleMultiInstanceLoopCharacteristics(Process process, Activity activity, List<ValidationError> errors) {
		MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = activity.getLoopCharacteristics();
		if (multiInstanceLoopCharacteristics != null) {

			if (StringUtils.isEmpty(multiInstanceLoopCharacteristics.getLoopCardinality())
	    		&& StringUtils.isEmpty(multiInstanceLoopCharacteristics.getInputDataItem())) {

			  addError(errors, Problems.MULTI_INSTANCE_MISSING_COLLECTION, process, activity);
	    }

		}
	}

	protected void handleDataAssociations(Process process, Activity activity, List<ValidationError> errors) {
	  if (activity.getDataInputAssociations() != null) {
	  	for (DataAssociation dataAssociation : activity.getDataInputAssociations()) {
	  		if (StringUtils.isEmpty(dataAssociation.getTargetRef())) {
	  			 addError(errors, Problems.DATA_ASSOCIATION_MISSING_TARGETREF, process, activity);
	  	    }
	  	}
	  }
	  if (activity.getDataOutputAssociations() != null) {
	  	for (DataAssociation dataAssociation : activity.getDataOutputAssociations()) {
	  		if (StringUtils.isEmpty(dataAssociation.getTargetRef())) {
	  			 addError(errors, Problems.DATA_ASSOCIATION_MISSING_TARGETREF, process, activity);
	  	    }
	  	}
	  }
  }

}
