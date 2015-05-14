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

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jbarrez
 */
public class ExclusiveGatewayValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<ExclusiveGateway> gateways = process.findFlowElementsOfType(ExclusiveGateway.class);
		for (ExclusiveGateway gateway : gateways) {
			validateExclusiveGateway(process, gateway, errors);
		}
	}
	
  public void validateExclusiveGateway(Process process, ExclusiveGateway exclusiveGateway, List<ValidationError> errors) {
    if (exclusiveGateway.getOutgoingFlows().isEmpty()) {
    	addError(errors, Problems.EXCLUSIVE_GATEWAY_NO_OUTGOING_SEQ_FLOW, process, exclusiveGateway, 
    			"Exclusive gateway has no outgoing sequence flow");
    } else if (exclusiveGateway.getOutgoingFlows().size() == 1) {
    	SequenceFlow sequenceFlow = exclusiveGateway.getOutgoingFlows().get(0);
    	 if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
       	addError(errors, Problems.EXCLUSIVE_GATEWAY_CONDITION_NOT_ALLOWED_ON_SINGLE_SEQ_FLOW, process, exclusiveGateway, 
       			"Exclusive gateway has only one outgoing sequence flow. This is not allowed to have a condition.");
      }
    } else {
    	String defaultSequenceFlow = exclusiveGateway.getDefaultFlow();

      List<SequenceFlow> flowsWithoutCondition = new ArrayList<SequenceFlow>();
      for (SequenceFlow flow : exclusiveGateway.getOutgoingFlows()) {
      	String condition = flow.getConditionExpression();
      	boolean isDefaultFlow = flow.getId() != null && flow.getId().equals(defaultSequenceFlow);
      	boolean hasConditon = StringUtils.isNotEmpty(condition); 
      	
        if (!hasConditon && !isDefaultFlow) {
          flowsWithoutCondition.add(flow);
        }
        if (hasConditon && isDefaultFlow) {
        	addError(errors, Problems.EXCLUSIVE_GATEWAY_CONDITION_ON_DEFAULT_SEQ_FLOW, process, exclusiveGateway, 
        			"Default sequenceflow has a condition, which is not allowed");
        }
      }
      
      if (!flowsWithoutCondition.isEmpty()) {
      	addWarning(errors, Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS, process, exclusiveGateway,
    				"Exclusive gateway has at least one outgoing sequence flow without a condition (which isn't the default one)");
    	}
      
    }
  }

	
	

}
