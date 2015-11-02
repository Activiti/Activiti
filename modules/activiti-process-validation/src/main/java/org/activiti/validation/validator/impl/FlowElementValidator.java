/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * A validator for stuff that is shared across all flow elements
 * 
 * @author jbarrez
 * @author Erik Winlof
 */
public class FlowElementValidator extends ProcessLevelValidator {

	protected static final int ID_MAX_LENGTH = 255;

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		for (FlowElement flowElement : process.getFlowElements()) {
			
			if (flowElement instanceof Activity) {
				Activity activity = (Activity) flowElement;
				handleConstraints(process, activity, errors);
				handleMultiInstanceLoopCharacteristics(process, activity, errors);
				handleDataAssociations(process, activity, errors);
			}
			
		}
		
	}

	protected void handleConstraints(Process process, Activity activity, List<ValidationError> errors) {
		if (activity.getId() != null && activity.getId().length() > ID_MAX_LENGTH) {
			addError(errors, Problems.FLOW_ELEMENT_ID_TOO_LONG, process, activity,
					"The id of a flow element must not contain more than " + ID_MAX_LENGTH + " characters");
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
